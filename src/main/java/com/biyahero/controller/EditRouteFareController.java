package com.biyahero.controller;

import com.biyahero.model.Route;
import com.biyahero.service.RouteService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditRouteFareController {

    @FXML private Label routeNameLabel;
    @FXML private TextField baseFareField;
    @FXML private TextField perStopFareField;

    private final RouteService routeService = new RouteService();
    private Route currentRoute;

    // The Schedule Trip dialog will call this method to pass the route data in!
    public void setRouteData(Route route) {
        this.currentRoute = route;
        routeNameLabel.setText(route.getRouteName());

        // Pre-fill the current prices so the admin knows what they are editing
        baseFareField.setText(String.valueOf(route.getBaseFare()));
        perStopFareField.setText(String.valueOf(route.getPerStopFare()));
    }

    @FXML
    private void handleSave() {
        try {
            double newBase = Double.parseDouble(baseFareField.getText());
            double newPerStop = Double.parseDouble(perStopFareField.getText());

            // 🔥 Send it to the database!
            routeService.updateRouteFares(currentRoute.getRouteId(), newBase, newPerStop);

            // Update the object in memory just in case the parent window needs it
            currentRoute.setBaseFare(newBase);
            currentRoute.setPerStopFare(newPerStop);

            closeWindow();
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format entered.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) baseFareField.getScene().getWindow()).close();
    }
}