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
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;



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
            DBUtil.setDatabase(userDb);

            // 1. Grab the window BEFORE we transition and destroy the login screen
            Stage currentStage = (Stage) usernameField.getScene().getWindow();

            // 2. Transition to the Dashboard
            transitionToDashboard();

            // 3. Drop the Toast onto the safely captured screen!
            showSuccessToast(currentStage, "Logged in to " + user + "'s Workspace");

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

            stage.setResizable(true);

            stage.setMinWidth(1024);
            stage.setMinHeight(768);

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


    private void showSuccessToast(Stage stage, String message) {
        Popup toast = new Popup();

        Label label = new Label(message);
        // Styled using your BiyaHero Green (#16A34A) with a sleek pill shape and shadow!
        label.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 30; -fx-font-weight: bold; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");

        toast.getContent().add(label);

        // Start slightly higher and invisible for the slide-down effect
        label.setOpacity(0);
        label.setTranslateY(-20);

        // Center the toast horizontally at the top of the window
        toast.setOnShown(e -> {
            toast.setX(stage.getX() + (stage.getWidth() - label.getWidth()) / 2);
            toast.setY(stage.getY() + 40); // 40px down from the top bar
        });

        toast.show(stage);

        // 1. Fade in & slide down animation (0.3 seconds)
        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(label.opacityProperty(), 1),
                        new KeyValue(label.translateYProperty(), 0)
                )
        );

        // 2. Wait for 3 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(3));

        // 3. Fade out & slide up animation (0.3 seconds)
        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(label.opacityProperty(), 0),
                        new KeyValue(label.translateYProperty(), -20)
                )
        );
        fadeOut.setOnFinished(e -> toast.hide()); // Completely remove it when done

        // Chain them together
        fadeIn.setOnFinished(e -> delay.play());
        delay.setOnFinished(e -> fadeOut.play());

        // Start the sequence!
        fadeIn.play();
    }
}