package com.biyahero.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ReportsController {

    // These IDs MUST match the fx:id in your FXML exactly
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TableView<?> reportsTable; // Use <?> for now to keep it simple
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalTripsLabel;
    @FXML private Label avgOccupancyLabel;

    @FXML private ComboBox<String> exportTypeCombo;

    @FXML private ComboBox<String> sortComboBox;

    // These Methods MUST match the onAction in your FXML
    @FXML
    private void handleFilter() {
        System.out.println("Filter clicked!");
    }

    @FXML
    private void exportToCSV() {
        System.out.println("CSV Export clicked!");
    }

    @FXML
    private void exportToJSON() {
        System.out.println("JSON Export clicked!");
    }

    @FXML
    private void exportToSQL() {
        System.out.println("SQL Export clicked!");
    }

    @FXML private TextField tripIdSearchField;

    @FXML
    private void handleGenerateManifest() {
        String input = tripIdSearchField.getText().trim();

        if (input.isEmpty()) {
            showAlert("Error", "Please enter a Trip ID.");
            return;
        }

        try {
            int tripId = Integer.parseInt(input);
            System.out.println("Fetching manifest for Trip ID: " + tripId);

            // Logic will eventually look like this:
            // List<Passenger> list = bookingService.getPassengersByTripId(tripId);
            // manifestService.exportToPDF(list);

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Trip ID must be a number.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleTripReportPopup() {
        // This is where you'd launch your custom pop-up layout
        System.out.println("Opening Trip ID Manifest Selector...");
    }
}