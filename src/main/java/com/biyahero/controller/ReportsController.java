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
}