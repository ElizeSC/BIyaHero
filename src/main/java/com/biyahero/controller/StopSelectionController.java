package com.biyahero.controller;

import com.biyahero.model.RouteStop;
import com.biyahero.model.Stop;
import com.biyahero.service.RouteService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StopSelectionController {

    @FXML private ComboBox<String> stopComboBox;
    @FXML private Button           confirmButton;
    @FXML private Label            currentStopLabel;
    @FXML private Label            noStopsLabel;
    @FXML private Label            finalStopWarning;

    private final RouteService routeService = new RouteService();

    // All stops on the route from current position onwards
    private List<RouteStop> forwardStops = new ArrayList<>();
    private int             lastStopId   = -1;
    private int             selectedStopId = -1;
    private boolean         confirmed    = false;

    /**
     * @param allStops      ordered list of every RouteStop on this route
     * @param currentStopId the stopId the van is currently at, or -1 if not
     *                      yet at any stop (just departed)
     */
    public void setStops(List<RouteStop> allStops, int currentStopId) {
        if (allStops == null || allStops.isEmpty()) {
            disableAll("No stops defined for this route.");
            return;
        }

        // Record the final stop so we can warn the dispatcher
        lastStopId = allStops.get(allStops.size() - 1).getStopId();

        // Show where the van currently is
        if (currentStopId == -1) {
            currentStopLabel.setText("Departed — no stop reached yet");
        } else {
            Stop cur = safeGetStop(currentStopId);
            currentStopLabel.setText(cur != null ? cur.getStopName() : "Unknown stop");
        }

        // Find the index of the current stop (-1 means "before first stop")
        int currentIndex = -1;
        for (int i = 0; i < allStops.size(); i++) {
            if (allStops.get(i).getStopId() == currentStopId) {
                currentIndex = i;
                break;
            }
        }

        // Only show stops that are AHEAD of the current position
        // If currentIndex == -1 (not at any stop yet), show all stops
        forwardStops = new ArrayList<>(
                allStops.subList(currentIndex + 1, allStops.size()));

        if (forwardStops.isEmpty()) {
            disableAll("Van is already at the final stop.");
            return;
        }

        // Populate comboBox with stop names
        List<String> names = forwardStops.stream()
                .map(rs -> {
                    Stop s = safeGetStop(rs.getStopId());
                    return s != null ? s.getStopName() : "Stop #" + rs.getStopId();
                })
                .collect(Collectors.toList());

        stopComboBox.setItems(FXCollections.observableArrayList(names));
        stopComboBox.getSelectionModel().selectFirst();
        updateFinalStopWarning(); // check immediately for the first selection

        // Re-check warning whenever selection changes
        stopComboBox.getSelectionModel().selectedIndexProperty().addListener(
                (obs, old, idx) -> updateFinalStopWarning());
    }

    /** Shows the "this will complete the trip" warning if last stop selected */
    private void updateFinalStopWarning() {
        int idx = stopComboBox.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= forwardStops.size()) return;
        boolean isLast = forwardStops.get(idx).getStopId() == lastStopId;
        finalStopWarning.setVisible(isLast);
        finalStopWarning.setManaged(isLast);
    }

    @FXML
    private void handleConfirm() {
        int idx = stopComboBox.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= forwardStops.size()) return;
        selectedStopId = forwardStops.get(idx).getStopId();
        confirmed = true;
        closeStage();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        closeStage();
    }

    /** Returns the chosen stopId, or -1 if the dispatcher cancelled */
    public int getSelectedStopId() {
        return confirmed ? selectedStopId : -1;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void disableAll(String message) {
        confirmButton.setDisable(true);
        stopComboBox.setDisable(true);
        stopComboBox.setPromptText("No further stops");
        noStopsLabel.setText(message);
        noStopsLabel.setVisible(true);
        noStopsLabel.setManaged(true);
    }

    private Stop safeGetStop(int stopId) {
        try { return routeService.getStopById(stopId); }
        catch (Exception e) { return null; }
    }

    private void closeStage() {
        ((Stage) stopComboBox.getScene().getWindow()).close();
    }
}