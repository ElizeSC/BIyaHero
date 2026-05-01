package com.biyahero.service;

import com.biyahero.dao.BookingDAO;
import com.biyahero.dao.PassengerDAO;
import com.biyahero.dao.impl.BookingDAOImpl;
import com.biyahero.dao.impl.PassengerDAOImpl;
import com.biyahero.model.Booking;
import com.biyahero.model.Passenger;

import java.util.List;
import java.util.stream.Collectors;

import static com.biyahero.cli.TripMenu.routeService;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAOImpl();
    private final PassengerDAO passengerDAO = new PassengerDAOImpl();
    private final TripService tripService = new TripService();

    // booking management and passenger records
    public Booking createBooking(int tripId, int seatNumber, int pickupStopId, int dropoffStopId,
                                 double farePaid, String passengerName, String contactNumber, String address) {

        var trip = tripService.getTripById(tripId);
        if ("Completed".equals(trip.getTripStatus()) || "Cancelled".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Cannot book a seat on a " + trip.getTripStatus() + " trip.");
        }

        if (!isValidSeat(seatNumber)) {
            throw new IllegalArgumentException("Invalid seat number: " + seatNumber + ". Must be between 1 and 15.");
        }

        // Use intelligent segment checking here too!
        if (isSeatTaken(tripId, seatNumber, pickupStopId, dropoffStopId)) {
            throw new IllegalStateException("Seat " + seatNumber + " is already taken for this segment.");
        }

        if (passengerName == null || passengerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Passenger name cannot be empty.");
        }

        Passenger passenger = new Passenger(passengerName.trim(), contactNumber, address);
        int passengerId = passengerDAO.addPassenger(passenger);
        if (passengerId == -1) {
            throw new IllegalStateException("Failed to create passenger record.");
        }

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
        getBookingById(bookingId);
        return passengerDAO.getPassengerByBookingId(bookingId);
    }

    public void cancelBooking(int bookingId) {
        Booking booking = getBookingById(bookingId);
        if ("Cancelled".equals(booking.getBookingStatus()) || "Vacated".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Booking is already " + booking.getBookingStatus() + ".");
        }
        bookingDAO.cancelBooking(bookingId);
    }

    public boolean isValidSeat(int seatNumber) {
        return seatNumber >= 1 && seatNumber <= 15;
    }

    // NEW: Intelligent segment check for seat availability
    public boolean isSeatTaken(int tripId, int seatNumber, int pickupStopId, int dropoffStopId) {
        return bookingDAO.getOccupiedSeats(tripId, pickupStopId, dropoffStopId).contains(seatNumber);
    }

    public List<Integer> getOccupiedSeats(int tripId) {
        return bookingDAO.getOccupiedSeats(tripId);
    }

    // NEW: The method the SeatPlanController relies on!
    public List<Integer> getOccupiedSeats(int tripId, int pickupStopId, int dropoffStopId) {
        var trip = tripService.getTripById(tripId);

        // 1. Get the stops in their correct order
        List<com.biyahero.model.RouteStop> routeStops = routeService.getRouteStops(trip.getRouteId());

        // 2. Map each stopId to its sequence number (Index 0, 1, 2...)
        java.util.Map<Integer, Integer> stopSequence = new java.util.HashMap<>();
        for (int i = 0; i < routeStops.size(); i++) {
            stopSequence.put(routeStops.get(i).getStopId(), i);
        }

        int newPickupIdx = stopSequence.getOrDefault(pickupStopId, -1);
        int newDropoffIdx = stopSequence.getOrDefault(dropoffStopId, -1);

        List<Integer> occupiedSeats = new java.util.ArrayList<>();

        // 3. Get all bookings for this trip from the database
        List<Booking> allBookings = bookingDAO.getBookingsByTrip(tripId);

        // 4. Check each booking to see if it overlaps with our new request
        for (Booking b : allBookings) {
            if ("Cancelled".equals(b.getBookingStatus()) ||
                    "Completed".equals(b.getBookingStatus()) ||
                    "Vacated".equals(b.getBookingStatus())) {
                continue; // Ignore old/cancelled bookings
            }

            int existingPickupIdx = stopSequence.getOrDefault(b.getPickupStopId(), -1);
            int existingDropoffIdx = stopSequence.getOrDefault(b.getDropoffStopId(), -1);

            // MAGIC OVERLAP FORMULA: Existing_Start < New_End AND Existing_End > New_Start
            if (existingPickupIdx < newDropoffIdx && existingDropoffIdx > newPickupIdx) {
                occupiedSeats.add(b.getSeatNumber());
            }
        }

        return occupiedSeats;
    }

    public List<Integer> getAvailableSeats(int tripId) {
        return bookingDAO.getAvailableSeats(tripId);
    }

    public Booking assignWalkInPassenger(int tripId, int seatNumber, int pickupStopId, int dropoffStopId,
                                         double farePaid, String passengerName, String contactNumber, String address) {

        var trip = tripService.getTripById(tripId);
        if (!"En Route".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Walk-in assignment only allowed for En Route trips.");
        }
        return createBooking(tripId, seatNumber, pickupStopId, dropoffStopId, farePaid, passengerName, contactNumber, address);
    }

    public void vacateSeat(int bookingId) {
        Booking booking = getBookingById(bookingId);
        if (!"Reserved".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Cannot vacate a booking with status: " + booking.getBookingStatus());
        }
        bookingDAO.updateBookingStatus(bookingId, "Vacated");
    }

    public List<Passenger> getPassengersByTrip(int tripId) {
        List<Booking> bookings = bookingDAO.getBookingsByTrip(tripId);
        return bookings.stream()
                .map(b -> passengerDAO.getPassengerByBookingId(b.getBookingId()))
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    public Booking createWalkInBooking(int tripId, int seatNumber,
                                       int pickupStopId, int dropoffStopId,
                                       double farePaid) {

        var trip = tripService.getTripById(tripId);
        if (!"En Route".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Walk-in boarding is only allowed for En Route trips.");
        }
        if (!isValidSeat(seatNumber)) {
            throw new IllegalArgumentException("Invalid seat: " + seatNumber + ". Must be 1–15.");
        }

        // Intelligent Check here!
        if (isSeatTaken(tripId, seatNumber, pickupStopId, dropoffStopId)) {
            throw new IllegalStateException("Seat " + seatNumber + " is already taken for this segment.");
        }

        Passenger passenger = new Passenger("Walk-in (Seat " + seatNumber + ")", "", "");
        int passengerId = passengerDAO.addPassenger(passenger);
        if (passengerId == -1) {
            throw new IllegalStateException("Failed to create passenger record.");
        }

        Booking booking = new Booking(tripId, passengerId, seatNumber, pickupStopId, dropoffStopId, farePaid, "Reserved");
        bookingDAO.createBooking(booking);
        return booking;
    }
}