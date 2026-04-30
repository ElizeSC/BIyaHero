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


        // The name must match what is in your DBUtil.java
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

                    // 1. Validate the password against MySQL
                    if (DBUtil.testConnection("root", password)) {
                        // 2. Save credentials to the local root folder
                        DBUtil.saveConfig("root", password);

                        // 3. Force DBUtil to load the newly created file into memory
                        DBUtil.loadConfig();

                        // 4. THE DREAM: Create the master database and tables automatically
                        DBUtil.initializeMasterDatabase();

                        connected = true;
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Connection Error");
                        alert.setHeaderText("Access Denied");
                        alert.setContentText("Incorrect password or MySQL server is offline. Please try again.");
                        alert.showAndWait();
                    }
                } else {
                    // User clicked 'Cancel' or closed the dialog
                    System.exit(0);
                }
            }
        } else {
            // If the file already exists, just load it into memory
            DBUtil.loadConfig();
        }

        // Launch the Login Scene
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/biyahero/view/login-view.fxml"));

            if (fxmlLoader.getLocation() == null) {
                System.err.println("❌ FXML file not found! Check your resources folder path.");
                return;
            }

            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("BiyaHero Dispatcher System");
            stage.setResizable(false); // Optional: keeps your layout pretty
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to load the application UI: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}