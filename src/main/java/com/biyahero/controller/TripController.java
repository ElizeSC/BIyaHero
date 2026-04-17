package com.biyahero.controller;

import com.biyahero.model.Trip;
import com.biyahero.service.TripService;
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

public class TripController {

    @FXML private TableView<Trip> tripTable;
    @FXML private TableColumn<Trip, String> colTripId, colRoute, colStatus;
    @FXML private TableColumn<Trip, LocalDateTime> colDeparture;
    @FXML private TableColumn<Trip, Void> colActions;
    @FXML private TextField searchField;

    private final TripService tripService = new TripService();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();
    }

// Inside TripController.java

    private void setupTable() {
        colTripId.setCellValueFactory(new PropertyValueFactory<>("formattedId"));
        colRoute.setCellValueFactory(new PropertyValueFactory<>("routeId"));
        colDeparture.setCellValueFactory(new PropertyValueFactory<>("departureTime"));

        // ADD THIS: New Arrival Column if you added it to FXML
        // colArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalDt"));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("tripStatus"));

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
                    // Logic based on the partner's new status names
                    startBtn.setDisable(!"Scheduled".equals(trip.getTripStatus()));
                    completeBtn.setDisable(!"En Route".equals(trip.getTripStatus()));
                    setGraphic(box);
                }
            }
        });
    }

    private void loadData() {
        tripTable.setItems(FXCollections.observableArrayList(tripService.getAllTrips()));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, newVal) -> {
            tripTable.setItems(FXCollections.observableArrayList(tripService.searchTrip(newVal)));
        });
    }

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
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleStart(Trip t) {
        tripService.startTrip(t.getTripId());
        loadData();
    }

    private void handleComplete(Trip t) {
        tripService.completeTrip(t.getTripId());
        loadData();
    }

    private void handleCancel(Trip t) {
        tripService.cancelTrip(t.getTripId());
        loadData();
    }
}