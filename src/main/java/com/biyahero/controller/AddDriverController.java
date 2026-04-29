package com.biyahero.controller;

import com.biyahero.model.Driver;
import com.biyahero.service.DriverService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddDriverController {
    @FXML private TextField nameField, licenseField, contactField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label titleLabel;

    private final DriverService driverService = new DriverService();
    private Driver existingDriver = null;

    @FXML
    public void initialize() {
        statusComboBox.getItems().addAll("Available", "On Trip", "Off Duty");
        statusComboBox.setValue("Available");
    }

    public void setExistingData(Driver d) {
        this.existingDriver = d;
        titleLabel.setText("Edit Driver Details");
        nameField.setText(d.getName());
        licenseField.setText(d.getLicenseNo());
        contactField.setText(d.getContactNumber());
        statusComboBox.setValue(d.getDriverStatus());
    }

    @FXML
    private void handleSave() {
        try {
            String name    = nameField.getText().trim();
            String license = licenseField.getText().trim();
            String contact = contactField.getText().trim();
            String status  = (statusComboBox != null && statusComboBox.getValue() != null)
                    ? statusComboBox.getValue()
                    : "Available";

            if (existingDriver == null) {
                // DriverService.addDriver(licenseNo, name, contactNumber, driverStatus)
                driverService.addDriver(license, name, contact, status);
            } else {
                driverService.updateDriver(existingDriver.getDriverId(),
                        license.isEmpty() ? null : license,
                        name.isEmpty()    ? null : name,
                        contact.isEmpty() ? null : contact);
                driverService.updateDriverStatus(existingDriver.getDriverId(), status);
            }

            closeWindow();
        } catch (Exception e) {
            System.err.println("Error saving driver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
}