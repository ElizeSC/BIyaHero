package com.biyahero.controller;

import com.biyahero.service.UserService;
import com.biyahero.util.UIUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import com.biyahero.util.UIUtil;

public class RegistrationController {
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();
    private final UIUtil uiUtil = new UIUtil();

    @FXML
    private void handleRegister() {
        // Reset error label on every click
        errorLabel.setText("");

        String fullName = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        try {
            // UI Check: Empty Fields
            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in all fields to continue.");
                return;
            }

            // Logic Check: Database & Alphanumeric
            String dbName = username.toLowerCase().replaceAll("\\s+", "_");
            userService.registerUser(username, dbName, password);

            // Success can still be a popup since it's a big event!
            showInfo("Success", "Account created successfully! Please log in.");
            handleBackToLogin(); // Go back to login automatically

        } catch (IllegalArgumentException e) {
            // Show groupmate's "3-50 alphanumeric" error in the red label
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            errorLabel.getStyleClass().add("error-text");
            e.printStackTrace();
        }
    }

    // Keep using this helper method from your AddTripController
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        uiUtil.applyTheme(alert);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("BiyaHero: Action Successful"); // The bold header part
        alert.setContentText(content);

        uiUtil.applyTheme(alert);

        alert.showAndWait();
    }


    @FXML
    private void handleBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/biyahero/view/login-view.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        uiUtil.applyTheme(alert);
        alert.showAndWait();
    }
}