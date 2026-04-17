package com.biyahero.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    @FXML private Button btnDashboard, btnVans, btnScheduled, btnReports;


    @FXML
    private VBox contentArea; // Change StackPane to VBox here
    // This runs the moment the dashboard opens
    public void initialize() {
        Platform.runLater(this::showDashboard);
    }

    @FXML
    private void showDashboard() {
        updateActiveTab(btnDashboard);
        loadView("/com/biyahero/view/dashboard-view.fxml");
    }

    @FXML
    private void showVans() {
        updateActiveTab(btnVans);
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

    private void updateActiveTab(Button activeBtn) {
        // 1. Create a list of all your navbar buttons
        Button[] navButtons = {btnDashboard, btnVans, btnScheduled, btnReports};

        for (Button btn : navButtons) {
            // 2. Remove the active style from everyone
            btn.getStyleClass().remove("nav-button-active");
        }

        // 3. Add the active style ONLY to the one you just clicked
        if (!activeBtn.getStyleClass().contains("nav-button-active")) {
            activeBtn.getStyleClass().add("nav-button-active");
        }
    }

    @FXML
    private void showScheduledTrips() {
        updateActiveTab(btnScheduled);
        try {
            // This path must match exactly where you saved the new fxml
            Parent view = FXMLLoader.load(getClass().getResource("/com/biyahero/view/trips-view.fxml"));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Could not find trips-view.fxml");
        }
    }
}