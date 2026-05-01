package com.biyahero.controller;

import com.biyahero.model.RouteStop;
import com.biyahero.model.Stop;
import com.biyahero.model.Trip;
import com.biyahero.service.BookingService;
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

    private Trip            selectedTrip;
    private int             seatNumber;
    private List<RouteStop> routeStops;

    public void setData(Trip trip, int seat, Stop pickup, Stop dropoff) {
        this.selectedTrip = trip;
        this.seatNumber   = seat;
        seatNumberLabel.setText(String.valueOf(seat));

        try {
            // 1. Populate the dropdowns with text strings FIRST
            routeStops = routeService.getRouteStops(trip.getRouteId());
            List<String> names = routeStops.stream()
                    .map(rs -> safeStopName(rs.getStopId()))
                    .collect(Collectors.toList());

            pickupStopCombo.setItems(FXCollections.observableArrayList(names));
            dropoffStopCombo.setItems(FXCollections.observableArrayList(names));

            // 2. Lock in the Pick-up stop passed from SeatPlanController
            if (pickup != null) {
                pickupStopCombo.setValue(pickup.getStopName()); // Extract the String name!
                pickupStopCombo.setDisable(true);
                pickupStopCombo.setStyle("-fx-opacity: 1; -fx-background-color: #e2e8f0;");
            } else {
                // Fallback default if nothing was passed
                int defaultPickup = indexOfStop(trip.getCurrentStopId());
                pickupStopCombo.getSelectionModel().select(Math.max(defaultPickup, 0));
            }

            // 3. Lock in the Drop-off stop passed from SeatPlanController
            if (dropoff != null) {
                dropoffStopCombo.setValue(dropoff.getStopName()); // Extract the String name!
                dropoffStopCombo.setDisable(true);
                dropoffStopCombo.setStyle("-fx-opacity: 1; -fx-background-color: #e2e8f0;");
            } else {
                // Fallback default
                dropoffStopCombo.getSelectionModel().selectLast();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        hideWarning();

        // ── validation ──────────────────────────────────────────────────────
        int pickupIdx  = pickupStopCombo .getSelectionModel().getSelectedIndex();
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

        // ── save ─────────────────────────────────────────────────────────────
        try {
            int pickupId  = routeStops.get(pickupIdx) .getStopId();
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

    // ── helpers ──────────────────────────────────────────────────────────────

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