package dao.impl;

import dao.RouteDAO;
import model.Route;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteDAOImpl implements RouteDAO {

    @Override
    public void addRoute(Route route) {
        String sql = "INSERT INTO route (route_name, base_fare) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, route.getRouteName());
            stmt.setDouble(2, route.getBaseFare());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding route: " + e.getMessage());
        }
    }

    @Override
    public Route getRouteById(int id) {
        String sql = "SELECT * FROM route WHERE route_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

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
            while (rs.next()) {
                routes.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all routes: " + e.getMessage());
        }
        return routes;
    }

    @Override
    public void updateBaseFare(int id, double newFare) {
        String sql = "UPDATE route SET base_fare = ? WHERE route_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newFare);
            stmt.setInt(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating base fare: " + e.getMessage());
        }
    }

    // converts a raw DB row from ResultSet into a usable Van object
    private Route mapRow(ResultSet rs) throws SQLException {
        return new Route(
            rs.getInt("route_id"),
            rs.getString("route_name"),
            rs.getDouble("base_fare")
        );
    }
}