package com.biyahero.controller;

import com.biyahero.model.RouteStop;
import com.biyahero.model.Stop;
import com.biyahero.service.RouteService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import java.util.List;
import java.util.stream.Collectors;

public class StopSelectionController {

    @FXML private ComboBox<String> stopComboBox;
    @FXML private Button confirmButton;

    private final RouteService routeService = new RouteService();
    private List<RouteStop> filteredStops;
    private int selectedStopId = -1;
    private boolean confirmed = false;

    public void setStops(List<RouteStop> allStops, int currentStopId) {
        // 1. Find where the van is currently in the sequence
        int currentIndex = -1;
        for (int i = 0; i < allStops.size(); i++) {
            if (allStops.get(i).getStopId() == currentStopId) {
                currentIndex = i;
                break;
            }
        }

        // 2. Filter: Only allow moving FORWARD in the sequence
        // subList(from, to) -> from is inclusive, so currentIndex + 1 starts at the next stop
        this.filteredStops = allStops.subList(currentIndex + 1, allStops.size());

        // 3. Convert to Names for the UI
        List<String> stopNames = filteredStops.stream()
                .map(rs -> {
                    Stop s = routeService.getStopById(rs.getStopId());
                    return s != null ? s.getStopName() : "Unknown Stop";
                })
                .collect(Collectors.toList());

        stopComboBox.setItems(FXCollections.observableArrayList(stopNames));

        if (!stopNames.isEmpty()) {
            stopComboBox.getSelectionModel().selectFirst();
        } else {
            confirmButton.setDisable(true);
            stopComboBox.setPromptText("No further stops available");
        }
    }

    @FXML
    private void handleConfirm() {
        int selectedIdx = stopComboBox.getSelectionModel().getSelectedIndex();
        if (selectedIdx >= 0) {
            this.selectedStopId = filteredStops.get(selectedIdx).getStopId();
            this.confirmed = true;
            closeStage();
        }
    }

    @FXML
    private void handleCancel() {
        this.confirmed = false;
        closeStage();
    }

    private void closeStage() {
        ((Stage) stopComboBox.getScene().getWindow()).close();
    }

    public int getSelectedStopId() {
        return confirmed ? selectedStopId : -1;
    }
}