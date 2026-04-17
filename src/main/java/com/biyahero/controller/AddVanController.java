package com.biyahero.controller;

import com.biyahero.model.Van;
import com.biyahero.service.VanService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddVanController {
    @FXML private TextField plateField, modelField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label titleLabel;

    private final VanService vanService = new VanService();
    private Van existingVan = null;

    @FXML
    public void initialize() {
        statusComboBox.getItems().addAll("Available", "Maintenance", "On Trip");
        statusComboBox.setValue("Available");
    }

    public void setExistingData(Van van) {
        this.existingVan = van;
        plateField.setText(van.getPlateNumber());
        modelField.setText(van.getModel());
        statusComboBox.setValue(van.getVanStatus());
        titleLabel.setText("Edit Van Details");
    }

    @FXML
    private void handleSave() {
        if (existingVan == null) {
            vanService.addVan(plateField.getText(), modelField.getText(), 15, statusComboBox.getValue());
        } else {
            vanService.updateVan(existingVan.getVanId(), plateField.getText(), modelField.getText(), 15, statusComboBox.getValue());
        }
        ((Stage) plateField.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) plateField.getScene().getWindow();
        stage.close();
    }
}