package com.biyahero.controller;

import com.biyahero.model.Booking;
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

    // Change this to RouteStop to match what your service returns
    private List<RouteStop> routeStops;

    public void setTripData(Trip trip) {
        this.selectedTrip = trip;
        System.out.println("DEBUG: Opening booking for Trip ID: " + trip.getTripId() + " (Route: " + trip.getRouteId() + ")");

        try {
            // 1. Get the RouteStops
            List<com.biyahero.model.RouteStop> routeStopLinks = routeService.getRouteStops(trip.getRouteId());

            if (routeStopLinks == null || routeStopLinks.isEmpty()) {
                System.out.println("DEBUG ERROR: No RouteStops found for Route ID " + trip.getRouteId());
                return;
            }

            System.out.println("DEBUG: Found " + routeStopLinks.size() + " route stops.");

            // 2. Fetch the actual names
            List<String> stopNames = routeStopLinks.stream()
                    .map(rs -> {
                        var stop = routeService.getStopById(rs.getStopId());
                        if (stop == null) {
                            System.out.println("DEBUG WARNING: Stop not found for ID " + rs.getStopId());
                            return "Unknown ID: " + rs.getStopId();
                        }
                        return stop.getStopName();
                    })
                    .collect(Collectors.toList());

            // 3. Set the items
            pickupStopCombo.setItems(FXCollections.observableArrayList(stopNames));
            dropoffStopCombo.setItems(FXCollections.observableArrayList(stopNames));

            this.routeStops = routeStopLinks;

            System.out.println("DEBUG: ComboBoxes populated with: " + stopNames);

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

    @FXML
    private void handleSaveBooking() {
        String name = passengerNameField.getText();

        try {
            // 3. Get the selected stop names
            String pName = pickupStopCombo.getValue();
            String dName = dropoffStopCombo.getValue();

            if (pName == null || dName == null) {
                showError("Please select both pickup and drop-off points.");
                return;
            }

            // 4. Map names back to IDs using the routeService
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

            // 5. Call Service (If DB fails, DAO must throw exception to trigger catch)
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

            // 6. Success!
            showInfo("Booking Successful", "Passenger " + name + " has been booked.");
            closeWindow();

        } catch (Exception e) {
            // This catches NumberFormatErrors or the RuntimeException from your DAO
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