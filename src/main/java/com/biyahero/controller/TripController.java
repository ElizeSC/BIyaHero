package com.biyahero.controller;

import com.biyahero.model.Trip;
import com.biyahero.model.Route;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TripController {

    @FXML private TableView<Trip> tripTable;
    @FXML private TableColumn<Trip, String> colTripId;
    @FXML private TableColumn<Trip, String> colRoute;
    @FXML private TableColumn<Trip, LocalDateTime> colDeparture;
    @FXML private TableColumn<Trip, LocalDateTime> colArrival;
    @FXML private TableColumn<Trip, String> colStatus;
    @FXML private TableColumn<Trip, Void> colActions;
    @FXML private TextField searchField;

    private final TripService tripService = new TripService();
    private final RouteService routeService = new RouteService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

    @FXML
    public void initialize() {
        setupTableColumns();
        refreshTable();
        setupSearch();
    }
    private void setupTableColumns() {
        // 1. MUST match Trip{tripId=...}
        colTripId.setCellValueFactory(new PropertyValueFactory<>("tripId"));

        // 2. MUST match Trip{tripStatus=...}
        colStatus.setCellValueFactory(new PropertyValueFactory<>("tripStatus"));

        // 3. MUST match Trip{departureTime=...}
        colDeparture.setCellValueFactory(new PropertyValueFactory<>("departureTime"));

        // 4. Custom Route Column (using routeId from the log)
        colRoute.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Trip trip = (Trip) getTableRow().getItem();
                    // This pulls the name based on the routeId we saw in your log
                    var route = routeService.getRouteById(trip.getRouteId());
                    setText(route != null ? route.getRouteName() : "Route " + trip.getRouteId());
                }
            }
        });

        // 5. Arrival Column (Check if your Trip model has getArrivalDt or getArrivalTime)
        // If it's missing from your Trip{...} toString, this might still be blank
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalDt"));

        setupActionButtons();
    }

    private void setupActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button startBtn = new Button("Depart");
            private final Button completeBtn = new Button("Arrive");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox box = new HBox(8, startBtn, completeBtn, cancelBtn);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                startBtn.getStyleClass().add("action-button-start");
                completeBtn.getStyleClass().add("action-button-complete");
                cancelBtn.getStyleClass().add("action-button-delete");

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
                    startBtn.setDisable(!"Scheduled".equals(trip.getTripStatus()));
                    completeBtn.setDisable(!"En Route".equals(trip.getTripStatus()));
                    cancelBtn.setDisable("Completed".equals(trip.getTripStatus()) || "Cancelled".equals(trip.getTripStatus()));
                    setGraphic(box);
                }
            }
        });
    }

    @FXML
    public void refreshTable() {
        List<Trip> trips = tripService.getAllTrips();
        tripTable.setItems(FXCollections.observableArrayList(trips));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                refreshTable();
            } else {
                // FIXED: Changed searchTrip to searchScheduledTrips
                tripTable.setItems(FXCollections.observableArrayList(tripService.searchScheduledTrips(newVal)));
            }
        });
    }

    // FXML calls this from your button: onAction="#handleAddTrip"
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
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleStart(Trip t) {
        tripService.startTrip(t.getTripId());
        refreshTable();
    }

    private void handleComplete(Trip t) {
        // Find the last stop for the route to trigger the completion status
        var stops = routeService.getRouteStops(t.getRouteId());
        if (!stops.isEmpty()) {
            int lastStopId = stops.get(stops.size() - 1).getStopId();
            tripService.updateCurrentStop(t.getTripId(), lastStopId);
            refreshTable();
        }
    }

    private void handleCancel(Trip t) {
        tripService.cancelTrip(t.getTripId());
        refreshTable();
    }

    private TableCell<Trip, LocalDateTime> createDateCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : formatter.format(item));
            }
        };
    }
}