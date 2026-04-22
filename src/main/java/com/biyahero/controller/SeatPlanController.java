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
        List<Integer> occupiedSeats = bookingService.getOccupiedSeats(currentTrip.getTripId());

        int seatNum = 1;
        // Typical van layout: 3 seats per row (adjust rows/cols as needed for your specific van)
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 3; col++) {
                if (seatNum > 15) break;

                Button seatBtn = new Button(String.valueOf(seatNum));
                seatBtn.setPrefSize(50, 50);

                final int selectedSeat = seatNum;

                if (occupiedSeats.contains(selectedSeat)) {
                    // Styled for "Occupied" (Yellow)
                    seatBtn.setStyle("-fx-background-color: #F1C40F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
                    seatBtn.setOnAction(e -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Seat Unavailable");
                        alert.setHeaderText(null);
                        alert.setContentText("Seat " + selectedSeat + " is already booked for this trip.");
                        alert.showAndWait();
                    });
                } else {
                    // Styled for "Available" (Green)
                    seatBtn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
                    seatBtn.setOnAction(e -> openBookingForm(selectedSeat));
                }

                seatGrid.add(seatBtn, col, row);
                seatNum++;
            }
        }
    }

    private void openBookingForm(int seatNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/biyahero/view/add-booking-dialog.fxml"));
            Parent root = loader.load();

            AddBookingController controller = loader.getController();
            controller.setTripData(currentTrip);
            controller.setSeatNumber(seatNumber); // Pass the clicked seat number!

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Booking Info - Seat " + seatNumber);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh the seat plan after the booking form closes to show the new yellow seat
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