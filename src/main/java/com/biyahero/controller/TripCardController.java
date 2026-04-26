package com.biyahero.controller;

import com.biyahero.model.*;
import com.biyahero.service.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Comparator;
import java.util.List;


public class TripCardController {
    @FXML private Label tripIdLabel, driverInfoLabel, routeLabel, occupancyLabel;
    @FXML private HBox progressHBox;

    private Trip currentTrip;
    private DashboardController parentController;

    private final TripService tripService       = new TripService();
    private final RouteService routeService     = new RouteService();
    private final BookingService bookingService = new BookingService();
    private final DriverService driverService = new DriverService();
    private final VanService vanService = new VanService();

    public void setTripData(Trip trip, DashboardController parent) {
        this.currentTrip     = trip;
        this.parentController = parent;

        List<RouteStop> stops = routeService.getRouteStops(trip.getRouteId());
        stops.sort(Comparator.comparingInt(RouteStop::getStopOrder));

        if (!stops.isEmpty()) {
            String origin = routeService.getStopById(stops.get(0).getStopId()).getStopName();
            String dest   = routeService.getStopById(stops.get(stops.size()-1).getStopId()).getStopName();
            routeLabel.setText(origin + " ➔ " + dest);
        } else {
            routeLabel.setText("Route stops not defined");
        }

        tripIdLabel.setText(trip.getFormattedId());
        String vanModel = vanService.getVanById(trip.getVanId()).getModel();
        String driverName = driverService.getDriverById(trip.getDriverId()).getName();

        driverInfoLabel.setText("Van: " + vanModel + " • Driver: " + driverName);

        int occupied = bookingService.getOccupiedSeats(trip.getTripId()).size();
        occupancyLabel.setText("Passengers: " + occupied + "/15");

        renderProgressBar();
    }

    private void renderProgressBar() {
        progressHBox.getChildren().clear();
        List<RouteStop> stops   = routeService.getRouteStops(currentTrip.getRouteId());
        Integer currentStopId   = currentTrip.getCurrentStopId(); // may be null
        boolean passedCurrent   = false;

        for (int i = 0; i < stops.size(); i++) {
            RouteStop rs  = stops.get(i);
            Stop stop     = routeService.getStopById(rs.getStopId());

            VBox dotContainer = new VBox(5);
            dotContainer.setAlignment(javafx.geometry.Pos.CENTER);

            Region dot = new Region();
            dot.getStyleClass().add(passedCurrent ? "progress-dot" : "progress-dot-active");

            Label name = new Label(stop.getStopName());
            name.setStyle("-fx-font-size: 10px;");
            dotContainer.getChildren().addAll(dot, name);
            progressHBox.getChildren().add(dotContainer);

            if (i < stops.size() - 1) {
                Region line = new Region();
                HBox.setHgrow(line, Priority.ALWAYS);
                line.getStyleClass().add(passedCurrent ? "progress-line" : "progress-line-active");
                progressHBox.getChildren().add(line);
            }

            // Null-safe: if no current stop yet, nothing is marked as "passed"
            if (currentStopId != null && rs.getStopId() == currentStopId) {
                passedCurrent = true;
            }
        }
    }

    @FXML
    private void handleUpdateLocation() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/biyahero/view/stop-selection-dialog.fxml"));
            Parent root = loader.load(); // <-- Parent, not VBox

            StopSelectionController ctrl = loader.getController();
            int currentStop = currentTrip.getCurrentStopId() != null
                    ? currentTrip.getCurrentStopId() : -1;
            ctrl.setStops(routeService.getRouteStops(currentTrip.getRouteId()), currentStop);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Update Location — " + currentTrip.getFormattedId());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            int next = ctrl.getSelectedStopId();
            if (next != -1) {
                tripService.updateCurrentStop(currentTrip.getTripId(), next);
                parentController.refreshDashboard();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateOccupancy() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/biyahero/view/seat-plan-dialog.fxml"));
            Parent root = loader.load();
            SeatPlanController ctrl = loader.getController();
            ctrl.setTripData(currentTrip);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Manage Seats — " + currentTrip.getFormattedId());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            parentController.refreshDashboard(); // refresh occupancy count
        } catch (Exception e) { e.printStackTrace(); }
    }
}