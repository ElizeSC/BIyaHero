package com.biyahero.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    @FXML
    private VBox contentArea; // Change StackPane to VBox here
    // This runs the moment the dashboard opens
    public void initialize() {
        Platform.runLater(this::showDashboard);
    }

    @FXML
    private void showDashboard() {
        loadView("/com/biyahero/view/dashboard-view.fxml");
    }

    @FXML
    private void showVans() {
        loadView("/com/biyahero/view/vans-view.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Helper method so you don't repeat code for every button
    private void changeView(String fxmlPath) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            System.err.println("Could not load FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}