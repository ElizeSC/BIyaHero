package com.biyahero.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.util.Duration;

public class UIUtils {
    private void showStyledAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Get the DialogPane and attach your new CSS
        DialogPane dialogPane = alert.getDialogPane();

        // IMPORTANT: Make sure this path matches your resources folder exactly!
        String cssPath = getClass().getResource("/com/biyahero/assets/dialogs.css").toExternalForm();
        dialogPane.getStylesheets().add(cssPath);

        alert.showAndWait();
    }
}