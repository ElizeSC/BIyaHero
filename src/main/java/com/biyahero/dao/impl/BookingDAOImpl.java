package dao.impl;

import dao.BookingDAO;
import model.Booking;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAOImpl implements BookingDAO {

    @Override
    public void createBooking(Booking booking) {
        String sql = "INSERT INTO booking (trip_id, passenger_id, seat_number, pickup_stop, dropoff_stop, fare_paid, booking_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, booking.getTripId());
            stmt.setInt(2, booking.getPassengerId());
            stmt.setInt(3, booking.getSeatNumber());
            stmt.setInt(4, booking.getPickupStopId());
            stmt.setInt(5, booking.getDropoffStopId());
            stmt.setDouble(6, booking.getFarePaid());
            stmt.setString(7, booking.getBookingStatus());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                booking.setBookingId(keys.getInt(1));
            }

        } catch (SQLException e) {
            System.err.println("Error creating booking: " + e.getMessage());
        }
    }

    @Override
    public Booking getBookingById(int id) {
        String sql = "SELECT * FROM booking WHERE booking_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching booking: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Booking> getBookingsByTrip(int tripId) {
        String sql = "SELECT * FROM booking WHERE trip_id = ?";
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tripId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookings.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching bookings for trip: " + e.getMessage());
        }
        return bookings;
    }

    @Override
    public void cancelBooking(int id) {
        String sql = "UPDATE booking SET booking_status = 'Cancelled' WHERE booking_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
        }
    }

    // seat availability
    @Override
    public List<Integer> getOccupiedSeats(int tripId) {
        String sql = "SELECT seat_number FROM booking " +
                     "WHERE trip_id = ? AND booking_status IN ('Reserved', 'Boarded')";
        List<Integer> occupied = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tripId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                occupied.add(rs.getInt("seat_number"));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching occupied seats: " + e.getMessage());
        }
        return occupied;
    }

    @Override
    public List<Integer> getAvailableSeats(int tripId) {
        String sql = "SELECT v.capacity FROM trip t JOIN van v ON t.van_id = v.van_id WHERE t.trip_id = ?";
        List<Integer> available = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tripId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int capacity = rs.getInt("capacity");
                List<Integer> occupied = getOccupiedSeats(tripId);

                for (int seat = 1; seat <= capacity; seat++) {
                    if (!occupied.contains(seat)) {
                        available.add(seat);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching available seats: " + e.getMessage());
        }
        return available;
    }

    @Override
    public void updateBookingStatus(int bookingId, String status) {
        String sql = "UPDATE booking SET booking_status = ? WHERE booking_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, bookingId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating booking status: " + e.getMessage());
        }
    }

    // converts a raw DB row from ResultSet into a usable Booking object
    private Booking mapRow(ResultSet rs) throws SQLException {
        return new Booking(
            rs.getInt("booking_id"),
            rs.getInt("trip_id"),
            rs.getInt("passenger_id"),
            rs.getInt("seat_number"),
            rs.getInt("pickup_stop"),
            rs.getInt("dropoff_stop"),
            rs.getDouble("fare_paid"),
            rs.getString("booking_status")
        );
    }
}