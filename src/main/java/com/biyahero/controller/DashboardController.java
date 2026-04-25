package com.biyahero.controller;

import com.biyahero.model.Trip;
import com.biyahero.service.TripService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {
    @FXML private VBox activeTripsContainer;
    @FXML private TextField searchField;

    private TripService tripService;

    @FXML
    public void initialize() {
        try {
            this.tripService = new TripService();
            refreshDashboard();
            setupSearch();
        } catch (Exception e) {
            System.err.println("Service init error: " + e.getMessage());
        }
    }

    private void setupSearch() {
        if (searchField == null) return;
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                refreshDashboard();
            } else {
                renderTrips(tripService.searchActiveTrips(newVal.trim()));
            }
        });
    }

    public void refreshDashboard() {
        List<Trip> active = tripService.getAllTrips().stream()
                .filter(t -> "En Route".equalsIgnoreCase(t.getTripStatus()))
                .collect(Collectors.toList());
        renderTrips(active);
    }

    private void renderTrips(List<Trip> trips) {
        activeTripsContainer.getChildren().clear();

        if (trips.isEmpty()) {
            Label empty = new Label("No active trips at the moment.");
            empty.setStyle("-fx-text-fill: #7E869E; -fx-font-size: 14px; -fx-padding: 20;");
            activeTripsContainer.getChildren().add(empty);
            return;
        }

        for (Trip trip : trips) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/biyahero/view/trip-card.fxml"));
                VBox card = loader.load();
                TripCardController ctrl = loader.getController();
                ctrl.setTripData(trip, this);
                activeTripsContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}