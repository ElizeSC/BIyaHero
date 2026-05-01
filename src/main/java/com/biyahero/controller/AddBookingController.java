package com.biyahero.controller;

import com.biyahero.model.RouteStop;
import com.biyahero.model.Stop;
import com.biyahero.model.Trip;
import com.biyahero.service.BookingService;
import com.biyahero.service.RouteService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class AddBookingController {

    @FXML private TextField passengerNameField;
    @FXML private TextField contactField;
    @FXML private TextField addressField;
    @FXML private TextField seatNumberField;
    @FXML private ComboBox<String> pickupStopCombo;
    @FXML private ComboBox<String> dropoffStopCombo;
    @FXML private TextField fareField;

    private final BookingService bookingService = new BookingService();
    private final RouteService routeService = new RouteService();
    private Trip selectedTrip;
    private List<RouteStop> routeStops;

    public void setTripData(Trip trip) {
        this.selectedTrip = trip;
        System.out.println("DEBUG: Opening booking for Trip ID: " + trip.getTripId() + " (Route: " + trip.getRouteId() + ")");

        try {
            // 1. Get the RouteStops
            List<RouteStop> routeStopLinks = routeService.getRouteStops(trip.getRouteId());

            if (routeStopLinks == null || routeStopLinks.isEmpty()) {
                System.out.println("DEBUG ERROR: No RouteStops found for Route ID " + trip.getRouteId());
                return;
            }

            // 2. Fetch the actual names as Strings
            List<String> stopNames = routeStopLinks.stream()
                    .map(rs -> {
                        var stop = routeService.getStopById(rs.getStopId());
                        return stop != null ? stop.getStopName() : "Unknown ID: " + rs.getStopId();
                    })
                    .collect(Collectors.toList());

            // 3. Set the items in the dropdowns
            pickupStopCombo.setItems(FXCollections.observableArrayList(stopNames));
            dropoffStopCombo.setItems(FXCollections.observableArrayList(stopNames));

            this.routeStops = routeStopLinks;

        } catch (Exception e) {
            System.err.println("DEBUG EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setSeatNumber(int seatNumber) {
        if (seatNumberField != null) {
            seatNumberField.setText(String.valueOf(seatNumber));
            seatNumberField.setEditable(false);
        }
    }

    // THE FIX IS HERE! Extract the Stop Name and add the styling lock!
    public void setRouteSegments(Stop pickup, Stop dropoff) {
        if (pickup != null) {
            pickupStopCombo.setValue(pickup.getStopName()); // Correct extraction!
            pickupStopCombo.setDisable(true);
            pickupStopCombo.setStyle("-fx-opacity: 1; -fx-background-color: #e2e8f0;");
        }

        if (dropoff != null) {
            dropoffStopCombo.setValue(dropoff.getStopName()); // Correct extraction!
            dropoffStopCombo.setDisable(true);
            dropoffStopCombo.setStyle("-fx-opacity: 1; -fx-background-color: #e2e8f0;");
        }
    }

    @FXML
    private void handleSaveBooking() {
        String name = passengerNameField.getText();

        try {
            String pName = pickupStopCombo.getValue();
            String dName = dropoffStopCombo.getValue();

            if (pName == null || dName == null) {
                showError("Please select both pickup and drop-off points.");
                return;
            }

            // Map string names back to IDs using the routeService
            int pickupId = routeStops.stream()
                    .map(rs -> routeService.getStopById(rs.getStopId()))
                    .filter(s -> s != null && s.getStopName().equals(pName))
                    .findFirst()
                    .get().getStopId();

            int dropoffId = routeStops.stream()
                    .map(rs -> routeService.getStopById(rs.getStopId()))
                    .filter(s -> s != null && s.getStopName().equals(dName))
                    .findFirst()
                    .get().getStopId();

            // Call Service
            bookingService.createBooking(
                    selectedTrip.getTripId(),
                    Integer.parseInt(seatNumberField.getText()),
                    pickupId,
                    dropoffId,
                    Double.parseDouble(fareField.getText()),
                    name,
                    contactField.getText(),
                    addressField.getText()
            );

            // Success!
            showInfo("Booking Successful", "Passenger " + name + " has been booked.");
            closeWindow();

        } catch (Exception e) {
            showError("Booking Failed: " + e.getMessage());
        }
    }

    @FXML private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) passengerNameField.getScene().getWindow();
        stage.close();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Booking Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}