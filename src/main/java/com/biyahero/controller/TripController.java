package com.biyahero.controller;

import com.biyahero.model.RouteStop;
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
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TripController {


    @FXML private TableView<Trip> tripTable;
    @FXML private TableColumn<Trip, Integer> colTripId;
    @FXML private TableColumn<Trip, String>  colRoute;
    @FXML private TableColumn<Trip, String>  colDeparture;
    @FXML private TableColumn<Trip, String>  colAssignment;
    @FXML private TableColumn<Trip, Void>    colActions;
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
        colTripId.setCellValueFactory(new PropertyValueFactory<>("tripId"));

        // Route name lookup
        colRoute.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setText(null); return; }
                var route = routeService.getRouteById(getTableRow().getItem().getRouteId());
                setText(route != null ? route.getRouteName() : "—");
            }
        });

        // Departure formatted
        colDeparture.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setText(null); return; }
                var dt = getTableRow().getItem().getDepartureTime();
                setText(dt != null ? formatter.format(dt) : "—");
            }
        });

        // Van plate + Driver ID (replaces the useless Arrival column)
        colAssignment.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setText(null); return; }
                Trip t = getTableRow().getItem();
                setText("Van " + t.getVanId() + "  •  Driver " + t.getDriverId());
            }
        });

        setupActionButtons();
    }

    private void setupActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button startBtn  = new Button("Depart");
            private final Button cancelBtn = new Button("Cancel");
            private final Button bookBtn   = new Button("Book");
            private final HBox   box       = new HBox(10, bookBtn, startBtn, cancelBtn);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                startBtn .getStyleClass().add("btn-table-edit");
                cancelBtn.getStyleClass().add("btn-table-delete");
                bookBtn  .getStyleClass().add("primary-button");

                startBtn .setOnAction(e -> handleStart(getTableView().getItems().get(getIndex())));
                cancelBtn.setOnAction(e -> handleCancel(getTableView().getItems().get(getIndex())));
                bookBtn  .setOnAction(e -> handleBooking(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Trip trip   = getTableRow().getItem();
                    String status = trip.getTripStatus();

                    bookBtn  .setDisable(!("Scheduled".equals(status) || "En Route".equals(status)));
                    startBtn .setDisable(!"Scheduled".equals(status));
                    cancelBtn.setDisable("Completed".equals(status) || "Cancelled".equals(status));

                    setGraphic(box);
                }
            }
        });
    }

    @FXML
    public void refreshTable() {
        List<Trip> allTrips = tripService.getAllTrips();

        // FILTER: Only show "Scheduled" in this view.
        // "En Route" trips will move to your Dashboard.
        List<Trip> scheduledOnly = allTrips.stream()
                .filter(t -> "Scheduled".equals(t.getTripStatus()))
                .collect(Collectors.toList());

        tripTable.setItems(FXCollections.observableArrayList(scheduledOnly));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                refreshTable();
            } else {
                // Filters search results to keep only Scheduled trips
                List<Trip> results = tripService.searchScheduledTrips(newVal).stream()
                        .filter(t -> "Scheduled".equals(t.getTripStatus()))
                        .collect(Collectors.toList());
                tripTable.setItems(FXCollections.observableArrayList(results));
            }
        });
    }

    @FXML
    private void handleAddTrip() {
        openDialog("/com/biyahero/view/add-trip-dialog.fxml", "Schedule New Trip", null);
    }

    // Inside TripController.java
    private void handleBooking(Trip trip) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/biyahero/view/seat-plan-dialog.fxml"));
            Parent root = loader.load();

            SeatPlanController controller = loader.getController();
            controller.setTripData(trip);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Select Seat - Trip #" + trip.getTripId());
            stage.setScene(new Scene(root, 800, 600));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper to open windows and avoid "Location not set" repetition
    private void openDialog(String fxmlPath, String title, Trip tripData) {
        try {
            URL location = getClass().getResource(fxmlPath);
            if (location == null) {
                System.err.println("FXML File not found at: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            // Pass data to controller if it's a booking
            if (tripData != null && loader.getController() instanceof AddBookingController) {
                AddBookingController controller = loader.getController();
                controller.setTripData(tripData);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleStart(Trip t) {
        // 1. UPDATE STATUS FIRST: This unlocks the ability to set a location
        tripService.startTrip(t.getTripId());

        // 2. SET INITIAL STOP SECOND: Now the service won't complain
        var stops = routeService.getRouteStops(t.getRouteId());
        if (stops != null && !stops.isEmpty()) {
            stops.sort(Comparator.comparingInt(RouteStop::getStopOrder));
            int firstStopId = stops.get(0).getStopId();
            tripService.updateCurrentStop(t.getTripId(), firstStopId);
        }

        refreshTable();
    }

    private void handleComplete(Trip t) {
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
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        };
    }
}