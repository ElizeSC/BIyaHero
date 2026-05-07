package com.biyahero.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class UIUtil {

    private static final String CSS_PATH = "/com/biyahero/view/style.css";
    private static final String LOGO_PATH = "/com/biyahero/assets/logo-car.png";

    public static void applyTheme(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();

        // 1. Apply CSS
        try {
            String css = UIUtil.class.getResource(CSS_PATH).toExternalForm();
            dialogPane.getStylesheets().add(css);
            dialogPane.getStyleClass().add("dialog-pane");
        } catch (Exception e) {
            System.err.println("UIUtil: CSS not found at " + CSS_PATH);
        }

        // 2. Apply Custom Logo & Window Icon
        try {
            Image logo = new Image(UIUtil.class.getResourceAsStream(LOGO_PATH));

            // Set big icon in the dialog content
            ImageView iconView = new ImageView(logo);
            iconView.setFitHeight(48);
            iconView.setFitWidth(48);
            iconView.setPreserveRatio(true);
            dialog.setGraphic(iconView);

            // Set small icon in the window title bar
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.getIcons().add(logo);

        } catch (Exception e) {
            // Fallback: hide the default JavaFX icons for a cleaner look
            dialog.setGraphic(null);
            System.err.println("UIUtil: Logo not found at " + LOGO_PATH);
        }
    }


    // Inside com.biyahero.util.UIUtil.java
    public static void showOccupiedAlert(String passengerName, String destination, String fare) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Seat Reservation Details");
        alert.setHeaderText("BiyaHero: Occupied Seat");

        // Formatting the content text for a clean look
        String content = String.format(
                "Passenger: %s\n" +
                        "Destination: %s\n" +
                        "Fare Paid: ₱%s",
                passengerName, destination, fare
        );

        alert.setContentText(content);

        // 🔥 Apply your global theme
        UIUtil.applyTheme(alert);

        alert.showAndWait();
    }
}