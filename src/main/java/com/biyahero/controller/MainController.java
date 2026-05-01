package com.biyahero.controller;

import com.biyahero.service.ImportService;
import com.biyahero.service.ImportService.ImportResult;
import com.biyahero.util.DBUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainController {

    @FXML private Button btnDashboard, btnVans, btnScheduled, btnReports;
    @FXML private VBox mainContentArea;
    @FXML private StackPane profileContainer;

    private ContextMenu profileMenu;
    private final ImportService importService = new ImportService();

    public void initialize() {
        Platform.runLater(this::showDashboard);
    }

    @FXML private void showDashboard()       { updateActiveTab(btnDashboard); loadView("/com/biyahero/view/dashboard-view.fxml"); }
    @FXML private void showVans()            { updateActiveTab(btnVans);      loadView("/com/biyahero/view/vans-view.fxml"); }
    @FXML private void showScheduledTrips()  { updateActiveTab(btnScheduled); loadView("/com/biyahero/view/trips-view.fxml"); }
    @FXML private void showReportsView()     { updateActiveTab(btnReports);   loadView("/com/biyahero/view/reports-view.fxml"); }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();
            if (mainContentArea != null) mainContentArea.getChildren().setAll(node);
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

    // ── Profile dropdown ──────────────────────────────────────────────────────

    @FXML
    private void handleProfileClick(MouseEvent event) {
        if (profileMenu == null) {
            profileMenu = new ContextMenu();
            profileMenu.getStyleClass().add("profile-menu");

            MenuItem importItem = new MenuItem("Import Data");
            importItem.setOnAction(e -> handleImportData());

            MenuItem logoutItem = new MenuItem("Logout");
            logoutItem.getStyleClass().add("menu-item-danger");
            logoutItem.setOnAction(e -> handleLogout());

            profileMenu.getItems().addAll(importItem, logoutItem);
        }

        Node source = (Node) event.getSource();
        profileMenu.show(source, Side.BOTTOM, -30, 5);
    }

    // ── Import ────────────────────────────────────────────────────────────────

    private void handleImportData() {
        // Step 1: Ask the user which format they want to import
        ButtonType sqlChoice  = new ButtonType("SQL Backup");
        ButtonType csvChoice  = new ButtonType("CSV");
        ButtonType jsonChoice = new ButtonType("JSON");
        ButtonType cancel     = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert formatDialog = new Alert(Alert.AlertType.NONE);
        formatDialog.setTitle("Import Data");
        formatDialog.setHeaderText("What type of file are you importing?");
        formatDialog.setContentText(
                "SQL Backup — full restore (replaces all current data)\n" +
                "CSV / JSON — import trip records only (duplicates skipped)");
        formatDialog.getButtonTypes().setAll(sqlChoice, csvChoice, jsonChoice, cancel);
        formatDialog.getDialogPane().setMinWidth(420);

        ButtonType chosen = formatDialog.showAndWait().orElse(cancel);
        if (chosen == cancel) return;

        // Step 2: SQL needs a confirmation warning before opening the file picker
        if (chosen == sqlChoice) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Import SQL Backup");
            confirm.setHeaderText("This will delete all current data.");
            confirm.setContentText(
                    "Importing this SQL backup will wipe the current database and replace it " +
                    "with the contents of the file.\n\nThis cannot be undone. Continue?");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        }

        // Step 3: Open the file picker filtered to the chosen format
        FileChooser fc = new FileChooser();
        fc.setTitle("Select File to Import");

        if (chosen == sqlChoice) {
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Backup", "*.sql"));
        } else if (chosen == csvChoice) {
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
        } else {
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON File", "*.json"));
        }

        Stage stage = (Stage) profileContainer.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file == null) return; // user cancelled the file picker

        // Step 4: Run the import
        String path = file.getAbsolutePath();
        ImportResult result;

        if      (chosen == sqlChoice)  result = importService.importFromSQL(path);
        else if (chosen == csvChoice)  result = importService.importFromCSV(path);
        else                           result = importService.importFromJSON(path);

        // Step 5: Show result
        if (result.success) {
            showSuccess("Import Complete", result.message);
        } else {
            showError("Import Failed: " + result.message);
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    private void handleLogout() {
        DBUtil.setDatabase("biyahero_master");
        try {
            Parent loginView = FXMLLoader.load(
                    getClass().getResource("/com/biyahero/view/login-view.fxml"));
            Stage stage = (Stage) profileContainer.getScene().getWindow();
            stage.setScene(new Scene(loginView));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Alert helpers ─────────────────────────────────────────────────────────

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Import Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}