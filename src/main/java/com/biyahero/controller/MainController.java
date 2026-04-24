package com.biyahero.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class MainController {

    @FXML private Button btnDashboard, btnVans, btnScheduled, btnReports;
    @FXML private VBox mainContentArea; // Matches your main-layout.fxml

    public void initialize() {
        // CRITICAL: This stops the StackOverflowError by breaking the loop
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

    @FXML
    private void showScheduledTrips() {
        updateActiveTab(btnScheduled);
        loadView("/com/biyahero/view/trips-view.fxml");
    }

    @FXML
    private void showReportsView() {
        updateActiveTab(btnReports);
        loadView("/com/biyahero/view/reports-view.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();

            if (mainContentArea != null) {
                mainContentArea.getChildren().setAll(node);
            }
        } catch (IOException e) {
            System.err.println("Error loading: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void updateActiveTab(Button activeBtn) {
        Button[] navButtons = {btnDashboard, btnVans, btnScheduled, btnReports};
        for (Button btn : navButtons) {
            if (btn != null) btn.getStyleClass().remove("nav-button-active");
        }
        if (activeBtn != null) activeBtn.getStyleClass().add("nav-button-active");
    }
}