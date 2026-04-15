package com.biyahero.controller;

import com.biyahero.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (AuthService.authenticate(user, pass)) {
            try {
                // 1. Get the current "Stage" (the window)
                Stage stage = (Stage) usernameField.getScene().getWindow();

                // 2. Load the Main Layout frame
// Change "main-layout.fxml" to "main-dashboard.fxml"
                // Inside handleLogin
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/biyahero/view/main-layout.fxml"));
                Scene scene = new Scene(loader.load());

                // 3. Set the new scene and show it
                stage.setScene(scene);
                stage.setTitle("BiyaHero - Admin Dashboard");
                stage.centerOnScreen(); // Makes it look professional
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("❌ Failed to load Dashboard.");
            }
        } else {
            errorLabel.setText("❌ Invalid Credentials.");
        }
    }
}