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

    @FXML
    private void handleSave() {
        String name = nameField.getText();
        String license = licenseField.getText();
        String contact = contactField.getText();
        String status = statusComboBox.getValue();

        try {
            if (existingDriver == null) {
                // Service requires 4 args: license, name, contact, status
                driverService.addDriver(license, name, contact, status);
            } else {
                // Update basic info
                driverService.updateDriver(existingDriver.getDriverId(), license, name, contact);
                // Update status separately (since updateDriver doesn't take status)
                driverService.updateDriverStatus(existingDriver.getDriverId(), status);
            }
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
}