package com.biyahero.controller;

import com.biyahero.model.TripReport;
import com.biyahero.service.ReportService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML private void exportToCSV()  { System.out.println("CSV Export"); }
    @FXML private void exportToJSON() { System.out.println("JSON Export"); }
    @FXML private void exportToSQL()  { System.out.println("SQL Export"); }

    @FXML
    private void handleGenerateManifest() {
        String input = tripIdSearchField.getText().trim();
        if (input.isEmpty()) { showAlert("Error", "Please enter a Trip ID."); return; }
        try {
            int id = Integer.parseInt(input);
            System.out.println("Manifest for trip " + id);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Trip ID must be a number.");
        }
    }

    @FXML private void handleTripReportPopup() { System.out.println("Opening manifest popup…"); }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}