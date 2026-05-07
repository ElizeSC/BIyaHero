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

    public void setRouteData(Route route) {
        this.currentRoute = route;
        routeNameLabel.setText(route.getRouteName());

        baseFareField.setText(String.valueOf(route.getBaseFare()));
        perStopFareField.setText(String.valueOf(route.getPerStopFare()));
    }

    @FXML
    private void handleSave() {
        try {
            double newBase = Double.parseDouble(baseFareField.getText());
            double newPerStop = Double.parseDouble(perStopFareField.getText());

            routeService.updateRouteFares(currentRoute.getRouteId(), newBase, newPerStop);

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