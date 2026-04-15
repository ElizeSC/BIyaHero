package com.biyahero;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUIApp extends Application {
    @Override
    public void start(Stage stage) {
        javafx.scene.control.Button btn = new javafx.scene.control.Button("I AM ALIVE");
        Scene scene = new Scene(new javafx.scene.layout.StackPane(btn), 400, 300);
        stage.setScene(scene);
        stage.show();
        System.out.println("✅ If you see this, the GUI engine is working!");
    }

    public static void main(String[] args) {
        launch();
    }
}