package com.biyahero.controller;

import com.biyahero.model.Route;
import com.biyahero.model.Van;
import com.biyahero.model.Driver;
import com.biyahero.service.RouteService;
import com.biyahero.service.TripService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class AddTripController {

    // FXML IDs matching your dialog FXML
    @FXML private ComboBox<Route> routeComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private ComboBox<Van> vanComboBox;
    @FXML private ComboBox<Driver> driverComboBox;
    @FXML private Button btnManageRoute;

    private final RouteService routeService = new RouteService();
    private final TripService tripService = new TripService();

    @FXML
    public void initialize() {
        ObservableList<Route> routes = FXCollections.observableArrayList(routeService.getAllRoutes());

        Route addMoreOption = new Route();
        addMoreOption.setRouteId(-1);
        addMoreOption.setRouteName("+ Add New Route...");
        routes.add(addMoreOption);

        routeComboBox.setItems(routes);

        // 🔥 FIX: Properly handle the "Add New Route" option!
        routeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getRouteId() == -1) {
                // They selected "Add New Route" -> Disable manage button and open the builder!
                btnManageRoute.setDisable(true);
                handleOpenRouteBuilder();
            } else {
                // They selected a real route -> Enable the manage button!
                btnManageRoute.setDisable(newVal == null);
            }
        });

        vanComboBox.setItems(FXCollections.observableArrayList(tripService.getAvailableVans()));
        driverComboBox.setItems(FXCollections.observableArrayList(tripService.getAvailableDrivers()));

        setupComboBoxConverters();
    }

    @FXML
    private void openManageRouteDialog() {
        Route selectedRoute = routeComboBox.getValue();
        if (selectedRoute == null || selectedRoute.getRouteId() == -1) return;

        try {
            // 🔥 FIX: Put the correct folder path to your FXML!
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/biyahero/view/edit-route-fare.fxml"));
            Parent root = loader.load();

            EditRouteFareController controller = loader.getController();
            controller.setRouteData(selectedRoute);

            Stage stage = new Stage();
            stage.setTitle("Manage Route Fares");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            // Changed to print out the actual error popup so you know if it fails!
            showError("UI Error", "Could not load Edit Fare dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleOpenRouteBuilder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/biyahero/view/route-builder.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Create New Route");
            stage.setScene(new Scene(root));

            stage.showAndWait();

            javafx.application.Platform.runLater(() -> {
                refreshRoutes();
                routeComboBox.getSelectionModel().clearSelection();
            });

        } catch (IOException e) {
            showError("UI Error", "Could not load Route Builder: " + e.getMessage());
        }
    }

    private void refreshRoutes() {
        ObservableList<Route> routes = FXCollections.observableArrayList(routeService.getAllRoutes());
        Route addMoreOption = new Route();
        addMoreOption.setRouteId(-1);
        addMoreOption.setRouteName("+ Add New Route...");
        routes.add(addMoreOption);
        routeComboBox.setItems(routes);
    }

    private void setupComboBoxConverters() {
        routeComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Route r) { return r == null ? "" : r.getRouteName(); }
            @Override public Route fromString(String s) { return null; }
        });

        vanComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Van v) { return v == null ? "" : v.getPlateNumber() + " (" + v.getModel() + ")"; }
            @Override public Van fromString(String s) { return null; }
        });

        // Updated to use getName() as per your Driver model
        driverComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Driver d) {
                return d == null ? "" : d.getName();
            }
            @Override public Driver fromString(String s) { return null; }
        });
    }

    @FXML
    private void handleSave() {
        try {
            Route selectedRoute = routeComboBox.getValue();
            if (selectedRoute == null || selectedRoute.getRouteId() == -1) {
                throw new IllegalArgumentException("Please select a valid route.");
            }
            // Validate selections
            if (routeComboBox.getValue() == null || datePicker.getValue() == null ||
                    vanComboBox.getValue() == null || driverComboBox.getValue() == null) {
                throw new IllegalArgumentException("All fields are required.");
            }

            // Parse Date and Time
            LocalTime time;
            try {
                time = LocalTime.parse(timeField.getText());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid time format. Please use HH:mm (e.g., 14:30).");
            }

            LocalDateTime departureTime = LocalDateTime.of(datePicker.getValue(), time);

            // Execute the creation logic from TripService
            tripService.createTrip(
                    routeComboBox.getValue().getRouteId(),
                    vanComboBox.getValue().getVanId(),
                    driverComboBox.getValue().getDriverId(),
                    departureTime
            );

            closeDialog();
        } catch (Exception e) {
            showError("Scheduling Error", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) routeComboBox.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}