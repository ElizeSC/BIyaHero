package com.biyahero.controller;

import com.biyahero.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button btnLogin;

    // Inject the service
    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (authService.authenticate(user, pass)) {
            transitionToDashboard();
        } else {
            showError("Invalid username or password.");
        }
    }

    private void transitionToDashboard() {
        try {
            // Load your MainView (the one with the purple pill nav)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/biyahero/view/main-layout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showError("Failed to load dashboard.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        // Optional: Add a "shake" animation to the login card here later!
    }
}