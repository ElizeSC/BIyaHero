package com.biyahero.controller;

import com.biyahero.model.Driver;
import com.biyahero.service.DriverService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddDriverController {
    @FXML private TextField nameField, licenseField, contactField;
    private final DriverService driverService = new DriverService();
    private Driver existingDriver = null;

    public void setExistingData(Driver d) {
        this.existingDriver = d;
        nameField.setText(d.getName());
        licenseField.setText(d.getLicenseNo());
        contactField.setText(d.getContactNumber());
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText();
        String license = licenseField.getText();
        String contact = contactField.getText();

        try {
            if (existingDriver == null) {
                // FIXED: Now passing 4 arguments to match Service
                driverService.addDriver(license, name, contact, "Available");
            } else {
                // FIXED: This matches your Service's 4-argument update method
                driverService.updateDriver(
                        existingDriver.getDriverId(),
                        license,
                        name,
                        contact
                );
            }
            ((Stage) nameField.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}