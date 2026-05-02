package com.biyahero.controller;

import com.biyahero.model.RouteStop;
import com.biyahero.model.Stop;
import com.biyahero.model.Trip;
import com.biyahero.service.BookingService;
import com.biyahero.service.FareService;
import com.biyahero.service.RouteService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class WalkInBookingController {

    @FXML private Label            seatNumberLabel;
    @FXML private ComboBox<String> pickupStopCombo;
    @FXML private ComboBox<String> dropoffStopCombo;
    @FXML private TextField        fareField;
    @FXML private Label            warningLabel;

    private final BookingService bookingService = new BookingService();
    private final RouteService   routeService   = new RouteService();
    private final FareService    fareService    = new FareService();

    private Trip            selectedTrip;
    private int             seatNumber;
    private List<RouteStop> routeStops;
    private double          currentRouteBaseFare = 0.00;
    private double currentPerStopRate = 15.00;

    public void setData(Trip trip, int seat, Stop pickup, Stop dropoff) {
        this.selectedTrip = trip;
        this.seatNumber   = seat;
        seatNumberLabel.setText(String.valueOf(seat));

        try {
            // 🔥 Fetch the base fare dynamically for the calculator
            var route = routeService.getRouteById(trip.getRouteId());
            if (route != null) {
                this.currentRouteBaseFare = route.getBaseFare();
            }

            // 1. Populate the dropdowns with text strings
            routeStops = routeService.getRouteStops(trip.getRouteId());
            List<String> names = routeStops.stream()
                    .map(rs -> safeStopName(rs.getStopId()))
                    .collect(Collectors.toList());

            pickupStopCombo.setItems(FXCollections.observableArrayList(names));
            dropoffStopCombo.setItems(FXCollections.observableArrayList(names));

            // 2. Lock in the Pick-up stop passed from SeatPlanController
            if (pickup != null) {
                pickupStopCombo.setValue(pickup.getStopName());
                pickupStopCombo.setDisable(true);
                pickupStopCombo.setStyle("-fx-opacity: 1; -fx-background-color: #e2e8f0;");
            } else {
                int defaultPickup = indexOfStop(trip.getCurrentStopId());
                pickupStopCombo.getSelectionModel().select(Math.max(defaultPickup, 0));
            }

            // 3. Lock in the Drop-off stop passed from SeatPlanController
            if (dropoff != null) {
                dropoffStopCombo.setValue(dropoff.getStopName());
                dropoffStopCombo.setDisable(true);
                dropoffStopCombo.setStyle("-fx-opacity: 1; -fx-background-color: #e2e8f0;");
            } else {
                dropoffStopCombo.getSelectionModel().selectLast();
            }

            // 🔥 Instantly calculate the fare once data is loaded!
            updateDisplayedFare();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 🔥 NEW: Calculates and displays the fare for Walk-ins
     */
    @FXML
    private void updateDisplayedFare() {
        try {
            int pickupIdx = pickupStopCombo.getSelectionModel().getSelectedIndex();
            int dropoffIdx = dropoffStopCombo.getSelectionModel().getSelectedIndex();

            if (pickupIdx < 0 || dropoffIdx < 0) {
                fareField.setText("");
                return;
            }

            // Prevent negative distance calculations if they pick a bad dropoff
            if (pickupIdx >= dropoffIdx) {
                fareField.setText("0.00");
                return;
            }

            // Simple index math since lists match perfectly
            int stops = dropoffIdx - pickupIdx;
            double finalPrice = fareService.calculateFare(currentRouteBaseFare, currentPerStopRate, stops, "Regular");

            // Format to 2 decimal places (without the ₱ sign so parseDouble() doesn't crash on save)
            fareField.setText(String.format("%.2f", finalPrice));

        } catch (Exception e) {
            fareField.setText("");
        }
    }

    @FXML
    private void handleSave() {
        hideWarning();

        int pickupIdx  = pickupStopCombo.getSelectionModel().getSelectedIndex();
        int dropoffIdx = dropoffStopCombo.getSelectionModel().getSelectedIndex();

        if (pickupIdx < 0 || dropoffIdx < 0) {
            showWarning("Please select both pickup and dropoff stops.");
            return;
        }
        if (pickupIdx >= dropoffIdx) {
            showWarning("Dropoff stop must come after the pickup stop.");
            return;
        }
        if (fareField.getText().trim().isEmpty()) {
            showWarning("Please enter the fare amount.");
            return;
        }

        double fare;
        try {
            fare = Double.parseDouble(fareField.getText().trim());
            if (fare < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showWarning("Fare must be a valid positive number.");
            return;
        }

        try {
            int pickupId  = routeStops.get(pickupIdx).getStopId();
            int dropoffId = routeStops.get(dropoffIdx).getStopId();

            bookingService.createWalkInBooking(
                    selectedTrip.getTripId(), seatNumber, pickupId, dropoffId, fare);

            closeWindow();

        } catch (Exception e) {
            showWarning("Booking failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() { closeWindow(); }

    private int indexOfStop(Integer stopId) {
        if (stopId == null) return -1;
        for (int i = 0; i < routeStops.size(); i++) {
            if (routeStops.get(i).getStopId() == stopId) return i;
        }
        return -1;
    }

    private String safeStopName(int stopId) {
        try {
            Stop s = routeService.getStopById(stopId);
            return s != null ? s.getStopName() : "Stop #" + stopId;
        } catch (Exception e) {
            return "Stop #" + stopId;
        }
    }

    private void showWarning(String msg) {
        warningLabel.setText(msg);
        warningLabel.setVisible(true);
        warningLabel.setManaged(true);
    }

    private void hideWarning() {
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);
    }

    private void closeWindow() {
        ((Stage) fareField.getScene().getWindow()).close();
    }
}