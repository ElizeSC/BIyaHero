package com.biyahero.dao.impl;

import com.biyahero.dao.RouteDAO;
import com.biyahero.model.Route;
import com.biyahero.model.RouteStop;
import com.biyahero.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteDAOImpl implements RouteDAO {

    @Override
    public Route getRouteById(int id) {
        String sql = "SELECT * FROM route WHERE route_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error fetching route by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Route> getAllRoutes() {
        String sql = "SELECT * FROM route";
        List<Route> routes = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) routes.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching all routes: " + e.getMessage());
        }
        return routes;
    }

    @Override
    public void updateRouteFares(int id, double newBaseFare, double newPerStopFare) {
        // 🔥 Now it updates both columns at the same time!
        String sql = "UPDATE route SET base_fare = ?, per_stop_fare = ? WHERE route_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newBaseFare);
            stmt.setDouble(2, newPerStopFare);
            stmt.setInt(3, id);

            stmt.executeUpdate();
            System.out.println("Successfully updated fares for Route ID: " + id);

        } catch (SQLException e) {
            System.err.println("Error updating route fares: " + e.getMessage());
        }
    }

    /**
     * Saves a new Route and its ordered RouteStop entries in a single transaction.
     * @return the generated route_id, or -1 on failure.
     */
    @Override
    public int saveRouteWithStops(Route route, List<RouteStop> stops) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Insert Route
            int generatedRouteId;
            String insertRoute = "INSERT INTO route (route_name, base_fare, per_stop_fare) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertRoute, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, route.getRouteName());
                ps.setDouble(2, route.getBaseFare());
                ps.setDouble(3, route.getPerStopFare());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    generatedRouteId = keys.getInt(1);
                } else {
                    throw new SQLException("Failed to obtain generated route_id.");
                }
            }

            // 2. Insert RouteStop entries
            String insertRouteStop =
                    "INSERT INTO routestop (route_id, stop_id, stop_order, dist_from_prev) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertRouteStop)) {
                for (RouteStop rs : stops) {
                    ps.setInt(1, generatedRouteId);
                    ps.setInt(2, rs.getStopId());
                    ps.setInt(3, rs.getStopOrder());
                    ps.setDouble(4, rs.getDistanceFromPrev());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return generatedRouteId;

        } catch (SQLException e) {
            System.err.println("Transaction failed, rolling back: " + e.getMessage());
            return -1;
        }
    }

    private Route mapRow(ResultSet rs) throws SQLException {
        return new Route(
                rs.getInt("route_id"),
                rs.getString("route_name"),
                rs.getDouble("base_fare"),
                rs.getDouble("per_stop_fare") // 🔥 Make sure it reads it back out of the database!
        );
    }
}