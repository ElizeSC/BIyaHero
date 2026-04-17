package com.biyahero.controller;

import com.biyahero.model.Driver;
import com.biyahero.model.Van;
import com.biyahero.service.TripService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddTripController {

    @FXML private ComboBox<Integer> routeComboBox; // Simplified to ID for now
    @FXML private ComboBox<Van> vanComboBox;
    @FXML private ComboBox<Driver> driverComboBox;
    @FXML private DatePicker departureDatePicker;
    @FXML private TextField hourField;
    @FXML private TextField minuteField;

    private final TripService tripService = new TripService();

    @FXML
    public void initialize() {
        // Populate Routes (Assuming route IDs 1-5 exist for testing)
        routeComboBox.getItems().addAll(1, 2, 3, 4, 5);

        // Populate Available Vans & Drivers using TripService
        vanComboBox.setItems(javafx.collections.FXCollections.observableArrayList(tripService.getAvailableVans()));
        driverComboBox.setItems(javafx.collections.FXCollections.observableArrayList(tripService.getAvailableDrivers()));

        // Set Converters so the ComboBox displays the Name/Plate instead of the Object Address
        setupComboBoxDisplays();
    }

    private void setupComboBoxDisplays() {
        vanComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Van v) { return v == null ? "" : v.getPlateNumber() + " (" + v.getModel() + ")"; }
            @Override public Van fromString(String s) { return null; }
        });

        driverComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Driver d) { return d == null ? "" : d.getName(); }
            @Override public Driver fromString(String s) { return null; }
        });
    }

    @FXML
    private void handleSave() {
        try {
            int routeId = routeComboBox.getValue();
            Van selectedVan = vanComboBox.getValue();
            Driver selectedDriver = driverComboBox.getValue();

            // Construct LocalDateTime from DatePicker and TextFields
            LocalDateTime departureTime = LocalDateTime.of(
                    departureDatePicker.getValue(),
                    LocalTime.of(Integer.parseInt(hourField.getText()), Integer.parseInt(minuteField.getText()))
            );

            // Call the service method your partner wrote
            tripService.createTrip(routeId, selectedVan.getVanId(), selectedDriver.getDriverId(), departureTime);

            closeWindow();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) vanComboBox.getScene().getWindow();
        stage.close();
    }
}