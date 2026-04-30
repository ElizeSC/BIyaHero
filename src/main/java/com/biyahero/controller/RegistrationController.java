package com.biyahero.controller;

import com.biyahero.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class RegistrationController {
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        if (userService.registerUser(name, user, pass)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created! Please log in.");
            handleBackToLogin(); // Send them back to login screen
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Username might already be taken.");
        }
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
        alert.showAndWait();
    }
}