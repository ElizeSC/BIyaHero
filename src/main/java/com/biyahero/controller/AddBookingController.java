package com.biyahero.controller;

import com.biyahero.model.Stop;
import com.biyahero.model.Trip;
import com.biyahero.service.BookingService;
import com.biyahero.service.FareService;
import com.biyahero.service.RouteService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class AddBookingController {

    // 1. FXML Variables (Exactly matching your add-booking-dialog.fxml)
    @FXML private TextField passengerNameField;
    @FXML private TextField contactField;
    @FXML private TextField addressField;
    @FXML private TextField seatNumberField;
    @FXML private TextField fareField;
    @FXML private ComboBox<Stop> pickupStopCombo;
    @FXML private ComboBox<Stop> dropoffStopCombo;

    // 2. Services
    private final FareService fareService = new FareService();
    private final RouteService routeService = new RouteService();
    private final BookingService bookingService = new BookingService();

    private Trip currentTrip;
    private double currentRouteBaseFare = 0.00;
    private double currentPerStopRate = 15.00;

    @FXML
    public void initialize() {
        // Make sure the ComboBoxes display the stop names, not memory addresses!
        setupComboBox(pickupStopCombo);
        setupComboBox(dropoffStopCombo);
    }

    /**
     * Called by SeatPlanController to pass the trip data.
     * We dynamically fetch the Base Fare here so you don't have to pass it manually!
     */
    public void setTripData(Trip trip) {
        this.currentTrip = trip;

        // Fetch the base fare dynamically using the RouteService
        var route = routeService.getRouteById(trip.getRouteId());
        if (route != null) {
            this.currentRouteBaseFare = route.getBaseFare();
            this.currentPerStopRate = route.getPerStopFare();
        } else {
            // If the route is missing from the database, use default fallback values so the app doesn't crash!
            System.err.println("WARNING: No route found for Trip ID: " + trip.getTripId());
            this.currentRouteBaseFare = 0.00;
            this.currentPerStopRate = 15.00;
        }

        // Populate the dropdowns with all stops for this route
        List<Stop> stops = routeService.getStopsForRoute(trip.getRouteId());
        pickupStopCombo.getItems().setAll(stops);
        dropoffStopCombo.getItems().setAll(stops);
    }

    /**
     * Called by SeatPlanController to lock in the clicked seat number.
     */
    public void setSeatNumber(int seat) {
        seatNumberField.setText(String.valueOf(seat));
        seatNumberField.setEditable(false); // Lock it so they can't change it
    }

    /**
     * Called by SeatPlanController to pre-fill the selected route segments.
     */
    public void setRouteSegments(Stop pickup, Stop dropoff) {
        pickupStopCombo.setValue(pickup);
        dropoffStopCombo.setValue(dropoff);

        // Instantly calculate the fare once the segments are set!
        updateDisplayedFare();
    }

    /**
     * Calculates and displays the fare. Linked to the ComboBox onAction in FXML.
     */
    @FXML
    private void updateDisplayedFare() {
        try {
            Stop pickup = pickupStopCombo.getValue();
            Stop dropoff = dropoffStopCombo.getValue();

            if (pickup == null || dropoff == null) {
                fareField.setText("");
                return;
            }

            int stops = calculateStopsBetween(pickup, dropoff);
            double finalPrice = fareService.calculateFare(currentRouteBaseFare, currentPerStopRate, stops, "Regular");

            fareField.setText(String.format("%.2f", finalPrice));

        } catch (Exception e) {
            fareField.setText("");
        }
    }

    /**
     * Helper to calculate the distance between stops.
     */
    private int calculateStopsBetween(Stop pickup, Stop dropoff) {
        if (pickup == null || dropoff == null) return 0;

        int pickupIndex = -1;
        int dropoffIndex = -1;

        // 🔥 THE FIX: Search by the actual Stop ID instead of memory addresses!
        List<Stop> allStops = pickupStopCombo.getItems();
        for (int i = 0; i < allStops.size(); i++) {
            if (allStops.get(i).getStopId() == pickup.getStopId()) {
                pickupIndex = i;
            }
            if (allStops.get(i).getStopId() == dropoff.getStopId()) {
                dropoffIndex = i;
            }
        }

        // If for some reason we still can't find them, return 0
        if (pickupIndex == -1 || dropoffIndex == -1) return 0;

        // Return the positive difference between the two positions
        return Math.abs(dropoffIndex - pickupIndex);
    }

    /**
     * Helper to format Stop objects in the ComboBox.
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

    @FXML
    private void handleSaveBooking() {
        String name    = passengerNameField.getText().trim();
        String contact = contactField.getText().trim();
        String address = addressField.getText().trim();
        Stop pickup    = pickupStopCombo.getValue();
        Stop dropoff   = dropoffStopCombo.getValue();

        if (name.isEmpty() || pickup == null || dropoff == null || fareField.getText().isEmpty()) {
            showError("Please fill in all required fields.");
            return;
        }

        try {
            int    seat = Integer.parseInt(seatNumberField.getText().trim());
            double fare = Double.parseDouble(fareField.getText().trim());

            bookingService.createBooking(
                currentTrip.getTripId(), seat,
                pickup.getStopId(), dropoff.getStopId(),
                fare, name,
                contact.isEmpty() ? null : contact,
                address.isEmpty() ? null : address
            );
            handleCancel();

        } catch (NumberFormatException e) {
            showError("Invalid seat or fare value.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Booking Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) fareField.getScene().getWindow();
        stage.close();
    }
}