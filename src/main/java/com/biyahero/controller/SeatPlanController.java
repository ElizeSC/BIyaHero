package com.biyahero.controller;

import com.biyahero.model.Stop;
import com.biyahero.model.Trip;
import com.biyahero.service.BookingService;
import com.biyahero.service.RouteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class SeatPlanController {

    @FXML private GridPane seatGrid;

    // NEW: Dropdowns to handle route segments
    @FXML private ComboBox<Stop> cbPickupStop;
    @FXML private ComboBox<Stop> cbDropoffStop;

    private final BookingService bookingService = new BookingService();
    private final RouteService routeService = new RouteService();
    private Trip currentTrip;
    private List<Stop> routeStops;

    @FXML
    public void initialize() {
        setupComboBox(cbPickupStop);
        setupComboBox(cbDropoffStop);

        // DYNAMIC FILTERING: When Pick-up changes, update Drop-off options!
        cbPickupStop.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && routeStops != null) {
                int selectedIndex = routeStops.indexOf(newVal);

                // Get only the stops that come AFTER the selected pick-up stop
                if (selectedIndex >= 0 && selectedIndex < routeStops.size() - 1) {
                    List<Stop> validDropoffs = routeStops.subList(selectedIndex + 1, routeStops.size());
                    cbDropoffStop.getItems().setAll(validDropoffs);

                    // NOTE: Removed the automatic drop-off selection!
                    // It will stay blank until the Dispatcher picks one.
                } else {
                    cbDropoffStop.getItems().clear();
                }
                renderSeats();
            }
        });

        // When Drop-off changes, re-render the colors
        cbDropoffStop.setOnAction(e -> renderSeats());
    }

    public void setTripData(Trip trip) {
        this.currentTrip = trip;

        // Load the stops in order (Point A -> B -> C -> D)
        this.routeStops = routeService.getStopsForRoute(trip.getRouteId());
        cbPickupStop.getItems().setAll(routeStops);

        if (!routeStops.isEmpty()) {
            cbPickupStop.setValue(routeStops.get(0));

            // THE UNEDITABLE LOGIC:
            // If the van is still at the terminal, lock the Pick-up to Point A!
            if ("Scheduled".equals(trip.getTripStatus())) {
                //cbPickupStop.setDisable(true); // Makes it grayed out and uneditable!
            } else {
                cbPickupStop.setDisable(false); // Leaves it editable for roadside Walk-ins
            }
        }

        renderSeats();
    }

    private void renderSeats() {
        seatGrid.getChildren().clear();

        Stop pickup = cbPickupStop.getValue();
        Stop dropoff = cbDropoffStop.getValue();

        // Disable grid if they haven't picked valid stops yet
        if (pickup == null || dropoff == null || pickup.equals(dropoff)) {
            seatGrid.setDisable(true);
            return;
        }
        seatGrid.setDisable(false);

        // 🔥 THE MAGIC FIX IS HERE! 🔥
        // We now pass the pickup and dropoff IDs to trigger the Overlap Formula!
        List<Integer> occupied = bookingService.getOccupiedSeats(
                currentTrip.getTripId(),
                pickup.getStopId(),
                dropoff.getStopId()
        );

        // 1. Create the Driver Block (Top Left)
        Button driverBlock = new Button("Driver");
        driverBlock.setDisable(true);
        driverBlock.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-opacity: 1; -fx-background-radius: 8;");
        driverBlock.setPrefSize(50, 50);
        seatGrid.add(driverBlock, 0, 0);

        // 2. Create the Door Block
        Button doorBlock = new Button("Door");
        doorBlock.setDisable(true);
        doorBlock.setStyle("-fx-background-color: transparent; -fx-border-color: #94A3B8; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 8; -fx-text-fill: #94A3B8; -fx-font-weight: bold; -fx-opacity: 1;");
        doorBlock.setPrefSize(50, 50);
        seatGrid.add(doorBlock, 2, 1);

        // 3. Generate the 15 Passenger Seats
        int seatNum = 1;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 3; col++) {
                if ((row == 0 && col == 0) || (row == 1 && col == 2)) continue;
                if (seatNum > 15) break;

                Button btn = new Button(String.valueOf(seatNum));
                btn.setPrefSize(50, 50);
                final int seat = seatNum;

                if (occupied.contains(seat)) {
                    btn.setStyle("-fx-background-color: #F1C40F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
                    btn.setOnAction(e -> showSeatDetails(seat));
                } else {
                    btn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
                    btn.setOnAction(e -> openBookingForm(seat));
                }
                seatGrid.add(btn, col, row);
                seatNum++;
            }
        }
    }

    /**
     * Helper to format ComboBoxes so they display Stop names properly
     */
    private void setupComboBox(ComboBox<Stop> cb) {
        cb.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(Stop item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getStopName());
            }
        });
        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Stop item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getStopName());
            }
        });
    }

    /**
     * Routes to the appropriate form based on trip status:
     * En Route  → simplified walk-in dialog (no personal info needed)
     * Scheduled → full booking form
     */
    private void openBookingForm(int seat) {

        Stop pickup = cbPickupStop.getValue();
        Stop dropoff = cbDropoffStop.getValue();

        if ("En Route".equals(currentTrip.getTripStatus())) {
            openWalkInForm(seat, pickup, dropoff);
        } else {
            openFullBookingForm(seat, pickup, dropoff);
        }
    }

    private void openWalkInForm(int seat, Stop pickup, Stop dropoff) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/biyahero/view/walk-in-dialog.fxml"));
            Parent root = loader.load();

            // Pass the stops to the Walk-In Controller
            WalkInBookingController ctrl = loader.getController();
            ctrl.setData(currentTrip, seat, pickup, dropoff);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Quick Boarding — Seat " + seat);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            renderSeats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFullBookingForm(int seat, Stop pickup, Stop dropoff) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/biyahero/view/add-booking-dialog.fxml"));
            Parent root = loader.load();

            // Pass the stops to the Full Booking Controller
            AddBookingController ctrl = loader.getController();
            ctrl.setTripData(currentTrip);
            ctrl.setSeatNumber(seat);
            ctrl.setRouteSegments(cbPickupStop.getValue(), cbDropoffStop.getValue());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("New Booking — Seat " + seat);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            renderSeats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) seatGrid.getScene().getWindow()).close();
    }

    private void showSeatDetails(int seat) {
        // 1. Get all bookings for this trip
        List<com.biyahero.model.Booking> allBookings = bookingService.getBookingsByTrip(currentTrip.getTripId());

        // 2. Filter for only active bookings on THIS specific seat
        List<com.biyahero.model.Booking> seatBookings = allBookings.stream()
                .filter(b -> b.getSeatNumber() == seat)
                .filter(b -> !"Cancelled".equals(b.getBookingStatus()) &&
                        !"Completed".equals(b.getBookingStatus()) &&
                        !"Vacated".equals(b.getBookingStatus()))
                .collect(java.util.stream.Collectors.toList());

        // 3. Build a nice text summary
        StringBuilder details = new StringBuilder();
        details.append("Active Bookings for Seat ").append(seat).append(":\n\n");

        for (com.biyahero.model.Booking b : seatBookings) {
            try {
                // Grab the passenger info
                var passenger = bookingService.getPassengerByBooking(b.getBookingId());
                String pName = (passenger != null) ? passenger.getName() : "Walk-in";

                // Grab the route names
                var pickup = routeService.getStopById(b.getPickupStopId());
                var dropoff = routeService.getStopById(b.getDropoffStopId());
                String pickupName = (pickup != null) ? pickup.getStopName() : "Stop " + b.getPickupStopId();
                String dropoffName = (dropoff != null) ? dropoff.getStopName() : "Stop " + b.getDropoffStopId();

                // Format the text
                details.append("👤 Passenger: ").append(pName).append("\n");
                details.append("📍 Route: ").append(pickupName).append(" ➔ ").append(dropoffName).append("\n");
                details.append("💵 Fare: ₱").append(b.getFarePaid()).append("\n");
                details.append("------------------------------------\n");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (seatBookings.isEmpty()) {
            details.append("No active booking details found (seat might be blocked manually).");
        }

        // 4. Show the Information Alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Seat " + seat + " Details");
        alert.setHeaderText("Itinerary for Seat " + seat);
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
}