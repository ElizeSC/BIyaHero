package com.biyahero;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BiyaHeroApp extends Application {
    @Override
    public void start(Stage stage) {
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