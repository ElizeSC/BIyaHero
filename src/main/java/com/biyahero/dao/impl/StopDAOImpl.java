package com.biyahero.dao.impl;

import com.biyahero.dao.StopDAO;
import com.biyahero.model.Stop;
import com.biyahero.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StopDAOImpl implements StopDAO {

    @Override
    public Stop getStopById(int id) {
        String sql = "SELECT * FROM stop WHERE stop_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching stop by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Stop> getAllStops() {
        String sql = "SELECT * FROM stop";
        List<Stop> stops = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stops.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all stops: " + e.getMessage());
        }
        return stops;
    }

    // converts a raw DB row from ResultSet into a usable Stop object
    private Stop mapRow(ResultSet rs) throws SQLException {
        return new Stop(
            rs.getInt("stop_id"),
            rs.getString("stop_name"),
            rs.getString("city_province")
        );
    }
}