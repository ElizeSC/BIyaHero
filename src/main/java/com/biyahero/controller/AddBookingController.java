package com.biyahero.controller;

import com.biyahero.model.Trip;
import com.biyahero.service.BookingService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddBookingController {

    // FXML IDs: Ensure these match your add-booking-dialog.fxml exactly
    @FXML private TextField passengerNameField;
    @FXML private TextField contactField;
    @FXML private TextField addressField;
    @FXML private TextField seatNumberField;
    @FXML private TextField pickupStopField;
    @FXML private TextField dropoffStopField;
    @FXML private TextField fareField;

    private final BookingService bookingService = new BookingService();
    private Trip selectedTrip;

    /**
     * Called by TripController when the 'Book' button is clicked.
     */
    public void setTripData(Trip trip) {
        this.selectedTrip = trip;
        // Optional: Pre-fill a default fare if needed
        if (fareField != null) fareField.setText("0.00");
    }

    @FXML
    private void handleSaveBooking() {
        if (selectedTrip == null) {
            showError("No trip selected.");
            return;
        }

        try {
            // 1. Gather numerical data
            int tripId = selectedTrip.getTripId();
            int seatNum = Integer.parseInt(seatNumberField.getText());
            int pickupId = Integer.parseInt(pickupStopField.getText());
            int dropoffId = Integer.parseInt(dropoffStopField.getText());
            double fare = Double.parseDouble(fareField.getText());

            // 2. Gather text data
            String name = passengerNameField.getText();
            String contact = contactField.getText();
            String address = addressField.getText();

            // 3. Call the specific Service method (handles Passenger + Booking)
            bookingService.createBooking(
                    tripId,
                    seatNum,
                    pickupId,
                    dropoffId,
                    fare,
                    name,
                    contact,
                    address
            );

            // 4. Success! Close the dialog
            closeWindow();

        } catch (NumberFormatException e) {
            showError("Invalid Input: Seat, Stops, and Fare must be numbers.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            // This catches service errors (e.g., Seat Taken, Trip Cancelled)
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) passengerNameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Booking Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setSeatNumber(int seatNumber) {
        if (seatNumberField != null) {
            seatNumberField.setText(String.valueOf(seatNumber));
            seatNumberField.setEditable(false); // User can't change it now
        }
    }
}