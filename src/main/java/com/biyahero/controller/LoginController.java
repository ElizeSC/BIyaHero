package com.biyahero.controller;

import com.biyahero.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        AuthService auth = new AuthService();

        // Reusing the SAME logic from your CLI!
        if (auth.authenticate(user, pass)) {
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("✅ Login Successful!");
        } else {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("❌ Invalid Credentials.");
        }
    }
}