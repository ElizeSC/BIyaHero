package com.biyahero;

import com.biyahero.util.DBUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
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

                // 👉 ATTACH YOUR CSS HERE
                applyTheme(dialog);

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

                        // 👉 ATTACH YOUR CSS HERE
                        applyTheme(alert);

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

            // 👉 ATTACH YOUR CSS HERE
            applyTheme(alert);

            alert.showAndWait();
        }
    }

    /**
     * Helper method to inject the dark mode CSS into any Alert or Dialog.
     */
    /**
     * Helper method to inject the CSS and custom Logo into any Alert or Dialog.
     */
    private void applyTheme(Dialog<?> dialog) {
        try {
            // 1. Apply your beautiful light-mode CSS
            String cssPath = getClass().getResource("/com/biyahero/view/style.css").toExternalForm();
            dialog.getDialogPane().getStylesheets().add(cssPath);

            // 2. Replace the default Question Mark / Error icon with your logo
            try {
                // CHANGE "logo.png" to your actual image file name!
                javafx.scene.image.Image logoImage = new javafx.scene.image.Image(getClass().getResourceAsStream("/com/biyahero/assets/logo-car.png"));

                javafx.scene.image.ImageView customIcon = new javafx.scene.image.ImageView(logoImage);
                customIcon.setFitHeight(48); // Adjust size so it isn't massive
                customIcon.setFitWidth(48);
                customIcon.setPreserveRatio(true);

                // Set the image inside the dialog box
                dialog.setGraphic(customIcon);

                // BONUS: Also set the tiny icon in the actual window title bar!
                javafx.stage.Stage stage = (javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow();
                stage.getIcons().add(logoImage);

            } catch (NullPointerException e) {
                // If you don't have a logo file yet, or the name is wrong,
                // this safely removes the ugly question mark anyway to keep it clean!
                dialog.setGraphic(null);
            }

        } catch (NullPointerException e) {
            System.err.println("Could not apply theme: style.css not found in resources!");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}