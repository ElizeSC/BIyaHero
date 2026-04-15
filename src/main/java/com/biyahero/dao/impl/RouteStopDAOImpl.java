package com.biyahero.dao.impl;

import com.biyahero.dao.RouteStopDAO;
import com.biyahero.model.RouteStop;
import com.biyahero.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteStopDAOImpl implements RouteStopDAO {

    @Override
    public void addStopToRoute(RouteStop routeStop) {
        String sql = "INSERT INTO routestop (route_id, stop_id, stop_order, dist_from_prev) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, routeStop.getRouteId());
            stmt.setInt(2, routeStop.getStopId());
            stmt.setInt(3, routeStop.getStopOrder());
            stmt.setDouble(4, routeStop.getDistanceFromPrev());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding stop to route: " + e.getMessage());
        }
    }

    @Override
    public List<RouteStop> getStopsForRoute(int routeId) {
        String sql = "SELECT * FROM routestop WHERE route_id = ? ORDER BY stop_order ASC";
        List<RouteStop> stops = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, routeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stops.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching stops for route: " + e.getMessage());
        }
        return stops;
    }

    // converts a raw DB row from ResultSet into a usable RouteStop object
    private RouteStop mapRow(ResultSet rs) throws SQLException {
        return new RouteStop(
            rs.getInt("route_id"),
            rs.getInt("stop_id"),
            rs.getInt("stop_order"),
            rs.getDouble("dist_from_prev")
        );
    }
}