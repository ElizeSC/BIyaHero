package com.biyahero.controller;

import com.biyahero.model.RouteStop;
import com.biyahero.model.Stop;
import com.biyahero.service.RouteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class RouteBuilderController {
    @FXML private TextField txtRouteName, txtBaseFare;
    @FXML private ListView<Stop> lvAvailableStops, lvRouteSequence;
    @FXML private TextField perStopFareField;

    private final RouteService routeService = new RouteService();
    private final ObservableList<Stop> availableStops = FXCollections.observableArrayList();
    private final ObservableList<Stop> selectedSequence = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Load initial stops from DB
        availableStops.addAll(routeService.getAllStops());
        lvAvailableStops.setItems(availableStops);
        lvRouteSequence.setItems(selectedSequence);

        // Custom Cell Factory to show stop names nicely
        lvAvailableStops.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(Stop item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getStopName());
            }
        });
        lvRouteSequence.setCellFactory(lvAvailableStops.getCellFactory());
    }

    @FXML
    private void handleAddStop() {
        Stop selected = lvAvailableStops.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedSequence.add(selected);
            // We keep it in available in case a stop is visited twice (like a loop)
        }
    }

    @FXML
    private void handleRemoveStop() {
        int index = lvRouteSequence.getSelectionModel().getSelectedIndex();
        if (index >= 0) selectedSequence.remove(index);
    }

    @FXML
    private void handleSave() {
        try {
            String name = txtRouteName.getText();
            double fare = Double.parseDouble(txtBaseFare.getText());
            double perStopFare = 15.00;

            if (!perStopFareField.getText().isEmpty()) {
                perStopFare = Double.parseDouble(perStopFareField.getText());
            }

            // Map the UI sequence to RouteStop models
            List<RouteStop> orderedStops = new ArrayList<>();
            for (int i = 0; i < selectedSequence.size(); i++) {
                RouteStop rs = new RouteStop();
                rs.setStopId(selectedSequence.get(i).getStopId());
                rs.setStopOrder(i + 1); // 1-based ordering
                rs.setDistanceFromPrev(0.0); // Default or prompt user for this
                orderedStops.add(rs);
            }

            // Save via the transactional service
            routeService.createRoute(name, fare, perStopFare, orderedStops);
            closeWindow();
        } catch (Exception e) {
            // Show error dialog logic here
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    @FXML private void handleMoveUp() { moveItem(-1); }
    @FXML private void handleMoveDown() { moveItem(1); }

    private void moveItem(int direction) {
        int index = lvRouteSequence.getSelectionModel().getSelectedIndex();
        if (index < 0 || (index + direction < 0) || (index + direction >= selectedSequence.size())) return;
        Stop item = selectedSequence.remove(index);
        selectedSequence.add(index + direction, item);
        lvRouteSequence.getSelectionModel().select(index + direction);
    }

    @FXML
    private void handleAddNewStop() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Stop");
        dialog.setHeaderText("Create a new location");
        dialog.setContentText("Enter Stop Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Stop newStop = new Stop();
                newStop.setStopName(name.trim());

                // Save and get the stop with the actual ID from DB
                Stop savedStop = routeService.saveNewStop(newStop);

                // Add the SAVED stop to the list
                availableStops.add(savedStop);
            }
        });
    }

    @FXML
    private void handleImportStopsCSV() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Import File");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("All Supported Files", "*.csv", "*.json"),
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new javafx.stage.FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        java.io.File file = fileChooser.showOpenDialog(lvAvailableStops.getScene().getWindow());

        if (file != null) {
            com.biyahero.service.DataImportService importService = new com.biyahero.service.DataImportService();
            com.biyahero.service.DataImportService.ImportResult result = importService.importData(file.getAbsolutePath(), "STOPS");

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    result.success ? javafx.scene.control.Alert.AlertType.INFORMATION : javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Data Import");
            alert.setHeaderText("Importing STOPS");
            alert.setContentText(result.message);
            alert.showAndWait();

            if (result.success) {
                availableStops.clear();
                availableStops.addAll(routeService.getAllStops());
            }
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) txtRouteName.getScene().getWindow()).close(); }
}