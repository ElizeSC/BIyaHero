package com.biyahero.service;

import com.biyahero.dao.BookingDAO;
import com.biyahero.dao.PassengerDAO;
import com.biyahero.dao.impl.BookingDAOImpl;
import com.biyahero.dao.impl.PassengerDAOImpl;
import com.biyahero.model.Booking;
import com.biyahero.model.Passenger;

import java.util.List;
import java.util.stream.Collectors;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAOImpl();
    private final PassengerDAO passengerDAO = new PassengerDAOImpl();
    private final TripService tripService = new TripService();

    // booking management and passenger records 
    public Booking createBooking(int tripId, int seatNumber, int pickupStopId, int dropoffStopId, 
                                double farePaid, String passengerName, String contactNumber, String address) {

        // validate if trip exists and is not completed/cancelled
        var trip = tripService.getTripById(tripId);
        if ("Completed".equals(trip.getTripStatus()) || "Cancelled".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Cannot book a seat on a " + trip.getTripStatus() + " trip.");
        }

        // validate seat
        if (!isValidSeat(seatNumber)) {
            throw new IllegalArgumentException("Invalid seat number: " + seatNumber + ". Must be between 1 and 15.");
        }
        if (isSeatTaken(tripId, seatNumber)) {
            throw new IllegalStateException("Seat " + seatNumber + " is already taken.");
        }

        // validate passenger name (to confirm pa if need din ba name for walk-ins)
        if (passengerName == null || passengerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Passenger name cannot be empty.");
        }

        // create passenger
        Passenger passenger = new Passenger(passengerName.trim(), contactNumber, address);
        int passengerId = passengerDAO.addPassenger(passenger);
        if (passengerId == -1) {
            throw new IllegalStateException("Failed to create passenger record.");
        }

        // create booking
        Booking booking = new Booking(tripId, passengerId, seatNumber, pickupStopId, dropoffStopId, farePaid, "Reserved");

        bookingDAO.createBooking(booking);
        return booking;
    }

    public Booking getBookingById(int bookingId) {
        Booking booking = bookingDAO.getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found with ID: " + bookingId);
        }
        return booking;
    }

    public List<Booking> getBookingsByTrip(int tripId) {
        return bookingDAO.getBookingsByTrip(tripId);
    }

    public Passenger getPassengerByBooking(int bookingId) {
        getBookingById(bookingId); // throws if not found
        return passengerDAO.getPassengerByBookingId(bookingId);
    }

    public void cancelBooking(int bookingId) {
        Booking booking = getBookingById(bookingId);
        if ("Cancelled".equals(booking.getBookingStatus()) || "Vacated".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Booking is already " + booking.getBookingStatus() + ".");
        }
        bookingDAO.cancelBooking(bookingId);
    }

    // seat availability management
    public boolean isValidSeat(int seatNumber) {
        return seatNumber >= 1 && seatNumber <= 15;
    }

    public boolean isSeatTaken(int tripId, int seatNumber) {
        return bookingDAO.getOccupiedSeats(tripId).contains(seatNumber);
    }

    public List<Integer> getOccupiedSeats(int tripId) {
        return bookingDAO.getOccupiedSeats(tripId);
    }

    public List<Integer> getAvailableSeats(int tripId) {
        return bookingDAO.getAvailableSeats(tripId);
    }

    // roadside passenger assignment
    public Booking assignWalkInPassenger(int tripId, int seatNumber, int pickupStopId, int dropoffStopId,
                                        double farePaid, String passengerName, String contactNumber, String address) {
        
                                            var trip = tripService.getTripById(tripId);
        if (!"En Route".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Walk-in assignment only allowed for En Route trips.");
        }

        // reuses createBooking logic. same form, just enforces en route
        return createBooking(tripId, seatNumber, pickupStopId, dropoffStopId, farePaid, passengerName, contactNumber, address);
    }

    // seat vacating for when passenger reach destination
    public void vacateSeat(int bookingId) {
        Booking booking = getBookingById(bookingId);
        if (!"Reserved".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Cannot vacate a booking with status: " + booking.getBookingStatus());
        }
        bookingDAO.updateBookingStatus(bookingId, "Vacated");
    }

    // getting all passengers of a given trip
    public List<Passenger> getPassengersByTrip(int tripId) {
        List<Booking> bookings = bookingDAO.getBookingsByTrip(tripId);
        return bookings.stream()
            .map(b -> passengerDAO.getPassengerByBookingId(b.getBookingId()))
            .filter(p -> p != null)
            .collect(Collectors.toList());
    }
}