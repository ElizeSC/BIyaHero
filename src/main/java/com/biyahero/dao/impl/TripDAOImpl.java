package com.biyahero.dao.impl;

import com.biyahero.dao.TripDAO;
import com.biyahero.model.Trip;
import com.biyahero.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripDAOImpl implements TripDAO {

    @Override
    public void createTrip(Trip trip) {
        String sql = "INSERT INTO trip (route_id, van_id, driver_id, departure_dt, trip_status, arrival_dt, current_stop_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, trip.getRouteId());
            stmt.setInt(2, trip.getVanId());
            stmt.setInt(3, trip.getDriverId());
            stmt.setTimestamp(4, Timestamp.valueOf(trip.getDepartureTime()));
            stmt.setString(5, trip.getTripStatus());
            stmt.setTimestamp(6, trip.getArrivalDt() != null ? Timestamp.valueOf(trip.getArrivalDt()) : null);
            stmt.setObject(7, trip.getCurrentStopId()); // setObject handles null Integer
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error creating trip: " + e.getMessage());
        }
    }

    @Override
    public Trip getTripById(int id) {
        String sql = "SELECT * FROM trip WHERE trip_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching trip by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Trip> getAllTrips() {
        String sql = "SELECT * FROM trip";
        List<Trip> trips = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                trips.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all trips: " + e.getMessage());
        }
        return trips;
    }

    @Override
    public List<Trip> getTripsByRoute(int routeId) {
        String sql = "SELECT * FROM trip WHERE route_id = ?";
        List<Trip> trips = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, routeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trips.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching trips by route: " + e.getMessage());
        }
        return trips;
    }

    @Override
    public void updateTrip(Trip trip) {
        String sql = "UPDATE trip SET route_id=?, van_id=?, driver_id=?, departure_dt=?, arrival_dt=?, current_stop_id=?, trip_status=? WHERE trip_id=?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, trip.getRouteId());
            stmt.setInt(2, trip.getVanId());
            stmt.setInt(3, trip.getDriverId());
            stmt.setTimestamp(4, Timestamp.valueOf(trip.getDepartureTime()));
            stmt.setTimestamp(5, trip.getArrivalDt() != null ? Timestamp.valueOf(trip.getArrivalDt()) : null);
            stmt.setObject(6, trip.getCurrentStopId()); // setObject handles null for Integer
            stmt.setString(7, trip.getTripStatus());
            stmt.setInt(8, trip.getTripId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating trip: " + e.getMessage());
        }
    }

    @Override
    public void updateTripStatus(int id, String status) {
        String sql = "UPDATE trip SET trip_status = ? WHERE trip_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating trip status: " + e.getMessage());
        }
    }

    // converts a raw DB row from ResultSet into a usable Trip object
    private Trip mapRow(ResultSet rs) throws SQLException {
        Timestamp arrivalTs = rs.getTimestamp("arrival_dt");
        int currentStopId = rs.getInt("current_stop_id");

        return new Trip(
            rs.getInt("trip_id"),
            rs.getInt("route_id"),
            rs.getInt("van_id"),
            rs.getInt("driver_id"),
            rs.getTimestamp("departure_dt").toLocalDateTime(),
            rs.getString("trip_status"),
            arrivalTs != null ? arrivalTs.toLocalDateTime() : null,
            rs.wasNull() ? null : currentStopId
        );
    }
}