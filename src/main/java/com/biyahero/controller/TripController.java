package com.biyahero.controller;

import com.biyahero.model.Trip;
import com.biyahero.service.TripService;
import com.biyahero.service.RouteService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class TripController {

    // 1. FXML UI Components (Must match fx:id in your FXML exactly)
    @FXML private TableView<Trip> tripTable;
    @FXML private TableColumn<Trip, String> colTripId;
    @FXML private TableColumn<Trip, Integer> colRoute;
    @FXML private TableColumn<Trip, LocalDateTime> colDeparture;
    @FXML private TableColumn<Trip, LocalDateTime> colArrival;
    @FXML private TableColumn<Trip, String> colStatus;
    @FXML private TableColumn<Trip, Void> colActions;
    @FXML private TextField searchField;

    // 2. Services (Partner's classes)
    private final TripService tripService = new TripService();
    private final RouteService routeService = new RouteService();

    @FXML
    public void initialize() {
        setupTableColumns();
        refreshTable();
        setupSearchListener();
    }

    /**
     * Links the TableColumns to the Trip model properties
     */
    private void setupTableColumns() {
        colTripId.setCellValueFactory(new PropertyValueFactory<>("formattedId"));
        colRoute.setCellValueFactory(new PropertyValueFactory<>("routeId"));
        colDeparture.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalDt"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("tripStatus"));

        // Setup the Action Buttons (Depart, Arrive, Cancel)
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button startBtn = new Button("Depart");
            private final Button completeBtn = new Button("Arrive");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox box = new HBox(8, startBtn, completeBtn, cancelBtn);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                startBtn.setOnAction(e -> handleStart(getTableView().getItems().get(getIndex())));
                completeBtn.setOnAction(e -> handleComplete(getTableView().getItems().get(getIndex())));
                cancelBtn.setOnAction(e -> handleCancel(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Trip trip = getTableView().getItems().get(getIndex());
                    // Button visibility logic based on Status
                    startBtn.setDisable(!"Scheduled".equals(trip.getTripStatus()));
                    completeBtn.setDisable(!"En Route".equals(trip.getTripStatus()));
                    cancelBtn.setDisable("Completed".equals(trip.getTripStatus()) || "Cancelled".equals(trip.getTripStatus()));
                    setGraphic(box);
                }
            }
        });
    }

    /**
     * Fetches fresh data from the Database
     */
    @FXML
    public void refreshTable() {
        List<Trip> allTrips = tripService.getAllTrips();
        tripTable.setItems(FXCollections.observableArrayList(allTrips));
    }

    /**
     * Handles the "Schedule Trip" button click (Matches FXML onAction)
     */
    @FXML
    private void handleAddTrip() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/biyahero/view/add-trip-dialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Schedule New Trip");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshTable(); // Auto-refresh after closing dialog
        } catch (IOException e) {
            showError("UI Error", "Could not open the scheduling dialog.");
            e.printStackTrace();
        }
    }

    private void handleStart(Trip t) {
        try {
            tripService.startTrip(t.getTripId());
            refreshTable();
        } catch (Exception e) {
            showError("Dispatch Error", e.getMessage());
        }
    }

    private void handleComplete(Trip t) {
        try {
            // Find the last stop for this route to trigger completion in partner's logic
            var stops = routeService.getStopsForRoute(t.getRouteId());
            if (!stops.isEmpty()) {
                int finalStopId = stops.get(stops.size() - 1).getStopId();
                tripService.updateCurrentStop(t.getTripId(), finalStopId);
                refreshTable();
            }
        } catch (Exception e) {
            showError("Arrival Error", e.getMessage());
        }
    }

    private void handleCancel(Trip t) {
        try {
            tripService.cancelTrip(t.getTripId());
            refreshTable();
        } catch (Exception e) {
            showError("Cancellation Error", e.getMessage());
        }
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                refreshTable();
            } else {
                // Use partner's search method
                tripTable.setItems(FXCollections.observableArrayList(tripService.searchScheduledTrips(newVal)));
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}