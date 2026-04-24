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
    @FXML private ComboBox<String> statusComboBox; // Add this to your add-driver-dialog.fxml!
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

    // Inside your AddDriverController (NOT the Service or DAO)
    @FXML
    private void handleSave() {
        try {
            // 1. Unpack the data from your TextFields into separate Strings
            String name = nameField.getText();
            String license = licenseField.getText();
            String contact = contactField.getText();

            // 2. Get the status from the ComboBox or force it to "Available"
            // This is the CRITICAL 4th argument that the error is complaining about
            String status = (statusComboBox != null && statusComboBox.getValue() != null)
                    ? statusComboBox.getValue()
                    : "Available";

            // 3. Pass the 4 STRINGS to the DAO/Service instead of the Driver object
            // This satisfies the "Expected 4 arguments" requirement
            driverService.addDriver(name, license, contact, status);

            // 4. Close the window using the method you already have
            closeWindow();

        } catch (Exception e) {
            System.err.println("Error adding driver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
}