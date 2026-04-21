package com.biyahero.controller;

import com.biyahero.model.Route;
import com.biyahero.model.Van;
import com.biyahero.model.Driver;
import com.biyahero.service.RouteService;
import com.biyahero.service.TripService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class AddTripController {

    // FXML IDs matching your dialog FXML
    @FXML private ComboBox<Route> routeComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private ComboBox<Van> vanComboBox;
    @FXML private ComboBox<Driver> driverComboBox;

    private final RouteService routeService = new RouteService();
    private final TripService tripService = new TripService();

    @FXML
    public void initialize() {
        // Populate data from Services
        routeComboBox.setItems(FXCollections.observableArrayList(routeService.getAllRoutes()));
        vanComboBox.setItems(FXCollections.observableArrayList(tripService.getAvailableVans()));
        driverComboBox.setItems(FXCollections.observableArrayList(tripService.getAvailableDrivers()));

        setupComboBoxConverters();
    }

    private void setupComboBoxConverters() {
        routeComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Route r) { return r == null ? "" : r.getRouteName(); }
            @Override public Route fromString(String s) { return null; }
        });

        vanComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Van v) { return v == null ? "" : v.getPlateNumber() + " (" + v.getModel() + ")"; }
            @Override public Van fromString(String s) { return null; }
        });

        // Updated to use getName() as per your Driver model
        driverComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Driver d) {
                return d == null ? "" : d.getName();
            }
            @Override public Driver fromString(String s) { return null; }
        });
    }

    @FXML
    private void handleSave() {
        try {
            // Validate selections
            if (routeComboBox.getValue() == null || datePicker.getValue() == null ||
                    vanComboBox.getValue() == null || driverComboBox.getValue() == null) {
                throw new IllegalArgumentException("All fields are required.");
            }

            // Parse Date and Time
            LocalTime time;
            try {
                time = LocalTime.parse(timeField.getText());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid time format. Please use HH:mm (e.g., 14:30).");
            }

            LocalDateTime departureTime = LocalDateTime.of(datePicker.getValue(), time);

            // Execute the creation logic from TripService
            tripService.createTrip(
                    routeComboBox.getValue().getRouteId(),
                    vanComboBox.getValue().getVanId(),
                    driverComboBox.getValue().getDriverId(),
                    departureTime
            );

            closeDialog();
        } catch (Exception e) {
            showError("Scheduling Error", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) routeComboBox.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}