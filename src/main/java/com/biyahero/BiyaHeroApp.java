package com.biyahero;

import com.biyahero.util.DBUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class BiyaHeroApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        File configFile = new File("db_config.properties");
        if (!configFile.exists()) {
            boolean connected = false;
            while (!connected) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("First-Time Setup");
                dialog.setHeaderText("BiyaHero: Database Configuration");
                dialog.setContentText("Please enter your MySQL root password:");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String password = result.get();
                    // 1. Check if it actually works before saving!
                    if (DBUtil.testConnection("root", password)) {
                        DBUtil.saveConfig("root", password);
                        DBUtil.loadConfig(); // 2. CRITICAL: Force DBUtil to read the new file now!
                        connected = true;
                    } else {
                        // Show error alert if password is wrong
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Incorrect password or MySQL is not running. Please try again.");
                        alert.showAndWait();
                    }
                } else {
                    System.exit(0);
                }
            }
        }

        try {
            // This line finds the FXML file in your resources folder
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/biyahero/view/login-view.fxml"));

            // Safety check: prints to console if the file path is wrong
            if (fxmlLoader.getLocation() == null) {
                System.err.println("❌ FXML file not found! Check your resources folder path.");
                return;
            }

            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("BiyaHero Dispatcher System");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}