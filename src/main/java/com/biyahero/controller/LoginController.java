package com.biyahero.controller;

import com.biyahero.service.AuthService;
import com.biyahero.service.UserService;
import com.biyahero.util.DBUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button btnLogin;

    // Inject the service
    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        String userDb = userService.authenticate(user, pass);

        if (userDb != null) {
            // THE CLUTCH MOMENT: Switch the entire app to THEIR database
            DBUtil.setDatabase(userDb);

            showSuccess("Logged in to " + user + "'s Workspace");
            transitionToDashboard();
        } else {
            showError("Invalid credentials.");
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

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleShowRegistration() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/biyahero/view/registration-view.fxml"));
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}