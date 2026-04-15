package com.biyahero.controller;

import com.biyahero.model.Van;
import com.biyahero.model.Driver;
import com.biyahero.service.VanService;
import com.biyahero.service.DriverService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class VanDriverController {

    // --- FXML UI Components ---
    @FXML private TableView<Van> vanTable;
    @FXML private TableColumn<Van, String> colVanId, colPlate, colModel, colStatus;
    @FXML private TableColumn<Van, Integer> colCapacity;

    @FXML private TableView<Driver> driverTable;
    @FXML private TableColumn<Driver, String> colDriverId, colDriverName, colLicense, colContact;

    @FXML private TextField searchField;
    @FXML private Button btnVans, btnDrivers;

    // --- Services ---
    private final VanService vanService = new VanService();
    private final DriverService driverService = new DriverService();

    // State tracker to know which list to search
    private boolean isVanView = true;

    @FXML
    public void initialize() {
        setupVanTable();
        setupDriverTable();
        loadInitialData();
        setupSearch();
    }

    private void setupVanTable() {
        colVanId.setCellValueFactory(new PropertyValueFactory<>("formattedId"));
        colPlate.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("vanStatus"));
    }

    private void setupDriverTable() {
        colDriverId.setCellValueFactory(new PropertyValueFactory<>("formattedId"));
        colDriverName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLicense.setCellValueFactory(new PropertyValueFactory<>("licenseNo"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
    }

    private void loadInitialData() {
        try {
            vanTable.setItems(FXCollections.observableArrayList(vanService.getAllVans()));
            driverTable.setItems(FXCollections.observableArrayList(driverService.getAllDrivers()));
        } catch (Exception e) {
            System.err.println("Error loading initial data: " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isVanView) {
                vanTable.setItems(FXCollections.observableArrayList(vanService.searchVans(newVal)));
            } else {
                driverTable.setItems(FXCollections.observableArrayList(driverService.searchDriver(newVal)));
            }
        });
    }

    @FXML
    private void showVanTable() {
        isVanView = true;
        toggleTables(true);

        // Update Button UI
        updateButtonStyle(btnVans, true);
        updateButtonStyle(btnDrivers, false);

        searchField.setPromptText("Search Van...");
    }

    @FXML
    private void showDriverTable() {
        isVanView = false;
        toggleTables(false);

        // Update Button UI
        updateButtonStyle(btnDrivers, true);
        updateButtonStyle(btnVans, false);

        searchField.setPromptText("Search Driver...");
    }

    // Helper method to keep things clean
    private void toggleTables(boolean showVans) {
        vanTable.setVisible(showVans);
        vanTable.setManaged(showVans);
        driverTable.setVisible(!showVans);
        driverTable.setManaged(!showVans);
    }

    // This helper manages the CSS classes dynamically
    private void updateButtonStyle(Button button, boolean active) {
        if (active) {
            if (!button.getStyleClass().contains("primary-button")) {
                button.getStyleClass().add("primary-button");
            }
            button.setStyle(""); // Clear any inline "transparent" styles
        } else {
            button.getStyleClass().remove("primary-button");
            // Apply the "inactive" look manually if it's not in your CSS
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #2f3640; -fx-cursor: hand;");
        }
    }
}