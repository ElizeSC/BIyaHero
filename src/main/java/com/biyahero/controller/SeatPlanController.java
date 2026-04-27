package com.biyahero.controller;

import com.biyahero.model.Trip;
import com.biyahero.service.BookingService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class SeatPlanController {

    @FXML private GridPane seatGrid;
    private final BookingService bookingService = new BookingService();
    private Trip currentTrip;

    public void setTripData(Trip trip) {
        this.currentTrip = trip;
        renderSeats();
    }

    private void renderSeats() {
        seatGrid.getChildren().clear();
        List<Integer> occupied = bookingService.getOccupiedSeats(currentTrip.getTripId());

        int seatNum = 1;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 3; col++) {
                if (seatNum > 15) break;

                Button btn = new Button(String.valueOf(seatNum));
                btn.setPrefSize(50, 50);

                final int seat = seatNum;

                if (occupied.contains(seat)) {
                    btn.setStyle(
                            "-fx-background-color: #F1C40F; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 8;");
                    btn.setOnAction(e -> {
                        Alert a = new Alert(Alert.AlertType.WARNING);
                        a.setHeaderText(null);
                        a.setContentText("Seat " + seat + " is already booked.");
                        a.showAndWait();
                    });
                } else {
                    btn.setStyle(
                            "-fx-background-color: #2ECC71; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 8; " +
                                    "-fx-cursor: hand;");
                    btn.setOnAction(e -> openBookingForm(seat));
                }

                seatGrid.add(btn, col, row);
                seatNum++;
            }
        }
    }

    /**
     * Routes to the appropriate form based on trip status:
     *   En Route  → simplified walk-in dialog (no personal info needed)
     *   Scheduled → full booking form
     */
    private void openBookingForm(int seat) {
        if ("En Route".equals(currentTrip.getTripStatus())) {
            openWalkInForm(seat);
        } else {
            openFullBookingForm(seat);
        }
    }

    private void openWalkInForm(int seat) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/biyahero/view/walk-in-dialog.fxml"));
            Parent root = loader.load();

            WalkInBookingController ctrl = loader.getController();
            ctrl.setData(currentTrip, seat);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Quick Boarding — Seat " + seat);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            renderSeats(); // refresh seat colours after booking
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFullBookingForm(int seat) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/biyahero/view/add-booking-dialog.fxml"));
            Parent root = loader.load();

            AddBookingController ctrl = loader.getController();
            ctrl.setTripData(currentTrip);
            ctrl.setSeatNumber(seat);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("New Booking — Seat " + seat);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            renderSeats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) seatGrid.getScene().getWindow()).close();
    }
}