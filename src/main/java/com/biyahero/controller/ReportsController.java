package com.biyahero.controller;

import com.biyahero.model.TripReport;
import com.biyahero.service.ReportService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import com.biyahero.service.FileService;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportsController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private TableView<TripReport>           reportsTable;
    @FXML private TableColumn<TripReport, String> colTripId;
    @FXML private TableColumn<TripReport, String> colDriver;
    @FXML private TableColumn<TripReport, String> colRoute;
    @FXML private TableColumn<TripReport, String> colDate;
    @FXML private TableColumn<TripReport, String> colOccupancy;
    @FXML private TableColumn<TripReport, String> colRevenue;

    @FXML private Label totalRevenueLabel;
    @FXML private Label totalTripsLabel;
    @FXML private Label avgOccupancyLabel;
    @FXML private Label tableRangeLabel;

    @FXML private ComboBox<String> sortComboBox;
    @FXML private TextField        tripIdSearchField;

    private final ReportService reportService = new ReportService();
    private final FileService fileService = new FileService();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    public void initialize() {
        setupColumns();
        setupSort();
        // Auto-filter when dates change
        startDatePicker.valueProperty().addListener((o, a, b) -> handleFilter());
        endDatePicker  .valueProperty().addListener((o, a, b) -> handleFilter());
        loadAll();
    }

    // ── columns ──────────────────────────────────────────────────────────────

    private void setupColumns() {
        colTripId   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedTripId()));
        colDriver   .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDriverName()));
        colRoute    .setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRouteName()));
        colOccupancy.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOccupancyRate()));
        colDate.setCellValueFactory(c -> {
            var dt = c.getValue().getDepartureTime();
            return new SimpleStringProperty(dt != null ? fmt.format(dt) : "N/A");
        });
        colRevenue.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("₱%.2f", c.getValue().getTotalRevenue())));
    }

    // ── sort ─────────────────────────────────────────────────────────────────

    private void setupSort() {
        sortComboBox.getItems().setAll("Trip ID", "Departure Date", "Revenue (High→Low)");
        sortComboBox.setValue("Trip ID");
        sortComboBox.setOnAction(e -> sortCurrentList());
    }

    private void sortCurrentList() {
        List<TripReport> list = new ArrayList<>(reportsTable.getItems());
        switch (sortComboBox.getValue()) {
            case "Departure Date" ->
                    list.sort((a, b) -> {
                        if (a.getDepartureTime() == null) return 1;
                        if (b.getDepartureTime() == null) return -1;
                        return a.getDepartureTime().compareTo(b.getDepartureTime());
                    });
            case "Revenue (High→Low)" ->
                    list.sort((a, b) -> Double.compare(b.getTotalRevenue(), a.getTotalRevenue()));
            default ->
                    list.sort((a, b) -> Integer.compare(a.getTripId(), b.getTripId()));
        }
        reportsTable.setItems(FXCollections.observableArrayList(list));
    }

    // ── data loading ─────────────────────────────────────────────────────────

    private void loadAll() {
        try {
            display(reportService.getAllReports(), "All completed trips");
        } catch (Exception e) {
            System.err.println("Error loading reports: " + e.getMessage());
        }
    }

    @FXML
    private void handleFilter() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end   = endDatePicker.getValue();
        try {
            if (start != null && end != null) {
                display(reportService.getReportsByDateRange(start, end),
                        "Trips from " + fmt.format(start) + " to " + fmt.format(end));
            } else {
                loadAll();
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Invalid Date Range", ex.getMessage());
        }
    }

    private void display(List<TripReport> reports, String rangeText) {
        reportsTable.setItems(FXCollections.observableArrayList(reports));
        if (tableRangeLabel != null) tableRangeLabel.setText(rangeText);
        updateSummary(reports);
        sortCurrentList(); // respect current sort
    }

    private void updateSummary(List<TripReport> reports) {
        totalTripsLabel.setText(String.valueOf(reports.size()));

        double revenue = reports.stream().mapToDouble(TripReport::getTotalRevenue).sum();
        totalRevenueLabel.setText(String.format("₱%.2f", revenue));

        double avg = reports.stream()
                .filter(r -> r.getTotalCapacity() > 0)
                .mapToDouble(r -> (double) r.getBookedSeats() / r.getTotalCapacity() * 100)
                .average().orElse(0);
        avgOccupancyLabel.setText(reports.isEmpty() ? "N/A" : String.format("%.1f%%", avg));
    }

    // ── export stubs ─────────────────────────────────────────────────────────

    @FXML
    private void exportToCSV() {
        File file = getSaveTarget("CSV File", "*.csv", "BiyaHero_Report.csv");
        if (file != null) {
            try {
                // Note: Her method name is exportTripReportsToCSV
                fileService.exportTripReportsToCSV(reportsTable.getItems(), file.getAbsolutePath());
                showSuccess("Report exported to CSV successfully!");
            } catch (IOException e) {
                showError("CSV Export failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void exportToPDF() {
        File file = getSaveTarget("PDF Document", "*.pdf", "BiyaHero_Report.pdf");
        if (file != null) {
            try {
                // Note: Her method name is exportTripReportsToPDF
                // We pass tableRangeLabel.getText() to fill her 'rangeLabel' parameter
                fileService.exportTripReportsToPDF(
                        reportsTable.getItems(),
                        file.getAbsolutePath(),
                        tableRangeLabel.getText()
                );
                showSuccess("PDF Document generated successfully!");
            } catch (IOException e) {
                showError("PDF Export failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void exportToJSON() {
        File file = getSaveTarget("JSON File", "*.json", "BiyaHero_Report.json");
        if (file != null) {
            try {
                // Note: Her method name is exportTripReportsToJSON
                fileService.exportTripReportsToJSON(reportsTable.getItems(), file.getAbsolutePath());
                showSuccess("Report exported to JSON successfully!");
            } catch (IOException e) {
                showError("JSON Export failed!");
            }
        }
    }

    @FXML
    private void exportToSQL() {
        File file = getSaveTarget("SQL Backup", "*.sql", "BiyaHero_Backup.sql");
        if (file != null) {
            try {
                // Note: Her method name is exportTripReportsToSQL
                fileService.exportTripReportsToSQL(reportsTable.getItems(), file.getAbsolutePath());
                showSuccess("SQL Backup generated!");
            } catch (IOException e) {
                showError("SQL Export failed!");
            }
        }
    }

    private File getSaveTarget(String desc, String ext, String defaultName) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Data");
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, ext));
        return fc.showSaveDialog(reportsTable.getScene().getWindow());
    }

    @FXML
    private void handleGenerateManifest() {
        String input = tripIdSearchField.getText().trim();
        if (input.isEmpty()) {
            showError("Please enter a Trip ID to generate a manifest.");
            return;
        }

        try {
            // 1. Search for the specific report in your current table data
            TripReport targetReport = reportsTable.getItems().stream()
                    .filter(r -> r.getFormattedTripId().equalsIgnoreCase(input) ||
                            String.valueOf(r.getTripId()).equals(input))
                    .findFirst()
                    .orElse(null);

            if (targetReport == null) {
                showError("Trip ID " + input + " not found in the current list.");
                return;
            }

            // 2. Open FileChooser for the specific manifest
            File file = getSaveTarget("PDF Manifest", "*.pdf", "Manifest_" + targetReport.getFormattedTripId() + ".pdf");

            if (file != null) {
                // 3. Reuse the PDF logic from FileService
                // Since a manifest is just a report for ONE trip, we pass a List with one item
                fileService.exportTripReportsToPDF(
                        List.of(targetReport),
                        file.getAbsolutePath(),
                        "PASSENGER MANIFEST - " + targetReport.getFormattedTripId()
                );

                showSuccess("Manifest generated for " + targetReport.getFormattedTripId());
            }
        } catch (Exception e) {
            showError("Failed to generate manifest: " + e.getMessage());
        }
    }
    @FXML private void handleTripReportPopup() { System.out.println("Opening manifest popup…"); }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Action Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }


}