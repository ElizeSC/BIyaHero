package com.biyahero.dao.impl;

import com.biyahero.dao.VanDAO;
import com.biyahero.model.Van;
import com.biyahero.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VanDAOImpl implements VanDAO {

    @Override
    public void addVan(Van van) {
        String sql = "INSERT INTO van (plate_number, model, capacity, van_status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, van.getPlateNumber());
            stmt.setString(2, van.getModel());
            stmt.setInt(3, van.getCapacity());
            stmt.setString(4, van.getVanStatus());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding van: " + e.getMessage());
        }
    }

    @Override
    public Van getVanById(int id) {
        String sql = "SELECT * FROM van WHERE van_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching van by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Van getVanByPlateNumber(String plateNumber) {
        String sql = "SELECT * FROM van WHERE plate_number = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, plateNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching van by plate number: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Van getVanByModel(String model) {
        String sql = "SELECT * FROM van WHERE model = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, model);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching van by model: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Van> getAllVans() {
        String sql = "SELECT * FROM van";
        List<Van> vans = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                vans.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all vans: " + e.getMessage());
        }
        return vans;
    }

    @Override
    public void updateVan(Van van) {
        String sql = "UPDATE van SET plate_number = ?, model = ?, capacity = ?, van_status = ? WHERE van_id = ?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, van.getPlateNumber());
            stmt.setString(2, van.getModel());
            stmt.setInt(3, van.getCapacity());
            stmt.setString(4, van.getVanStatus());
            stmt.setInt(5, van.getVanId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating van: " + e.getMessage());
        }
    }

    @Override
    public void updateVanStatus(int id, String status) {
        String sql = "UPDATE van SET van_status = ? WHERE van_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating van status: " + e.getMessage());
        }
    }

    @Override
    public void deleteVan(int id) {
        String sql = "DELETE FROM van WHERE van_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error deleting van: " + e.getMessage());
        }
    }

    // converts a raw DB row from ResultSet into a usable Van object
    private Van mapRow(ResultSet rs) throws SQLException {
        return new Van(
            rs.getInt("van_id"),
            rs.getString("plate_number"),
            rs.getString("model"),
            rs.getInt("capacity"),
            rs.getString("van_status")
        );
    }
}