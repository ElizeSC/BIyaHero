package com.biyahero.dao.impl;

import com.biyahero.dao.PassengerDAO;
import com.biyahero.model.Passenger;
import com.biyahero.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerDAOImpl implements PassengerDAO {

    @Override
    public int addPassenger(Passenger passenger) {
        String sql = "INSERT INTO passenger (name, contact_number, address) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, passenger.getName());
            stmt.setString(2, passenger.getContactNumber());
            stmt.setString(3, passenger.getAddress());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int generatedId = keys.getInt(1);
                passenger.setPassengerId(generatedId); 
                return generatedId;
            }

        } catch (SQLException e) {
            System.err.println("Error adding passenger: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public List<Passenger> getPassengersByBooking(int bookingId) {
        String sql = """
                SELECT p.* FROM passenger p
                JOIN booking b ON p.passenger_id = b.passenger_id
                WHERE b.booking_id = ?
                """;
        List<Passenger> passengers = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                passengers.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching passengers for booking: " + e.getMessage());
        }
        return passengers;
    }

    // converts a raw DB row from ResultSet into a usable Passenger object
    private Passenger mapRow(ResultSet rs) throws SQLException {
        return new Passenger(
            rs.getInt("passenger_id"),
            rs.getString("name"),
            rs.getString("contact_number"),
            rs.getString("address")
        );
    }
}