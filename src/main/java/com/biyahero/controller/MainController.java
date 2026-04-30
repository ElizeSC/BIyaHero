package com.biyahero.controller;

import com.biyahero.util.DBUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import java.io.IOException;

// For the Logout Menu
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

// For the Profile Click Event
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane; // Or whatever container your profile circle is in

// For the Scene Switching
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class MainController {

    @FXML private Button btnDashboard, btnVans, btnScheduled, btnReports;
    @FXML private VBox mainContentArea;
    @FXML private StackPane profileContainer;
    private ContextMenu profileMenu;

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

    @FXML
    private void handleProfileClick(MouseEvent event) {
        // Only build the menu once!
        if (profileMenu == null) {
            profileMenu = new ContextMenu();
            profileMenu.getStyleClass().add("profile-menu"); // We will style this in CSS!

            // 1. The Import Option
            MenuItem importItem = new MenuItem("Import Data");
            importItem.setOnAction(e -> {
                System.out.println("Import clicked!");
                // TODO: Call your import method here!
            });

            // 2. The Logout Option
            MenuItem logoutItem = new MenuItem("Logout");
            logoutItem.getStyleClass().add("menu-item-danger");
            logoutItem.setOnAction(e -> handleLogout());
            profileMenu.getItems().addAll(importItem, logoutItem);
        }


        Node source = (Node) event.getSource();
        profileMenu.show(source, Side.BOTTOM, -30, 5);
    }

    private void handleLogout() {
        // 1. Reset the Database connection to master for safety
        DBUtil.setDatabase("biyahero_master");

        try {
            // 2. Load the Login View
            Parent loginView = FXMLLoader.load(getClass().getResource("/com/biyahero/view/login-view.fxml"));
            Stage stage = (Stage) profileContainer.getScene().getWindow();

            // 3. Switch scene
            stage.setScene(new Scene(loginView));
            stage.centerOnScreen();

            System.out.println("User logged out. Connection reset to master.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}