package com.biyahero.controller;

import com.biyahero.model.*;
import com.biyahero.service.TripService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddTripController {
    @FXML private ComboBox<Route> routeComboBox;
    @FXML private ComboBox<Van> vanComboBox;
    @FXML private ComboBox<Driver> driverComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;

    private TripService tripService = new TripService(); // Or inject via constructor

    @FXML
    public void initialize() {
        // 1. Load the data from your partner's service [cite: 20, 21]
        vanComboBox.getItems().setAll(tripService.getAvailableVans());
        driverComboBox.getItems().setAll(tripService.getAvailableDrivers());

        // 2. Set the Van Converter
        vanComboBox.setConverter(new StringConverter<Van>() {
            @Override
            public String toString(Van van) {
                // Show "Plate - Model" in the dropdown [cite: 9]
                return (van == null) ? "" : van.getPlateNumber() + " - " + van.getModel();
            }

            @Override
            public Van fromString(String string) { return null; }
        });

        // 3. Set the Driver Converter
        driverComboBox.setConverter(new StringConverter<Driver>() {
            @Override
            public String toString(Driver driver) {
                // Show only the Full Name in the dropdown [cite: 10]
                return (driver == null) ? "" : driver.getName();
            }

            @Override
            public Driver fromString(String string) { return null; }
        });

        // 4. Set the Route converter

        routeComboBox.setConverter(new StringConverter<Route>() {
            @Override
            public String toString(Route r) {
                // Match your DB: showing the route name and the fare
                return (r == null) ? "" : r.getRouteName() + " (₱" + r.getBaseFare() + ")";
            }
            @Override
            public Route fromString(String s) { return null; }
        });
    }

    @FXML
    private void handleSave() {
        try {
            // Combine DatePicker and TextField into LocalDateTime
            LocalTime time = LocalTime.parse(timeField.getText());
            LocalDateTime departure = LocalDateTime.of(datePicker.getValue(), time);

            Trip newTrip = new Trip();
            newTrip.setRouteId(routeComboBox.getValue().getRouteId());
            newTrip.setVanId(vanComboBox.getValue().getVanId());
            newTrip.setDriverId(driverComboBox.getValue().getDriverId());
            newTrip.setDepartureTime(departure);
            newTrip.setTripStatus("Scheduled"); // Initial lifecycle state

            // tripService.createTrip(newTrip);
            closeWindow();
        } catch (Exception e) {
            // Show alert for parsing errors
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { /* Logic to close stage */ }
}