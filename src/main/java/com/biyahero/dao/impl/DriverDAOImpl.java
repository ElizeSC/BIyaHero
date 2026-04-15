package com.biyahero.dao.impl;

import com.biyahero.dao.DriverDAO;
import com.biyahero.model.Driver;
import com.biyahero.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DriverDAOImpl implements DriverDAO {

    @Override
    public void addDriver(Driver driver) {
        String sql = "INSERT INTO driver (license_no, name, contact_number) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, driver.getLicenseNo());
            stmt.setString(2, driver.getName());
            stmt.setString(3, driver.getContactNumber());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding driver: " + e.getMessage());
        }
    }

    @Override
    public Driver getDriverById(int id) {
        String sql = "SELECT * FROM driver WHERE driver_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching driver by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Driver getDriverByName(String name) {
        String sql = "SELECT * FROM driver WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching driver by name: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Driver getDriverByLicenseNo(String licenseNo) {
        String sql = "SELECT * FROM driver WHERE license_no = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, licenseNo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching driver by license number: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Driver> getAllDrivers() {
        String sql = "SELECT * FROM driver";
        List<Driver> drivers = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                drivers.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all drivers: " + e.getMessage());
        }
        return drivers;
    }

    @Override
    public void deleteDriver(int id) {
        String sql = "DELETE FROM driver WHERE driver_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error deleting driver: " + e.getMessage());
        }
    }

    // converts a raw DB row from ResultSet into a usable Van object
    private Driver mapRow(ResultSet rs) throws SQLException {
        return new Driver(
            rs.getInt("driver_id"),
            rs.getString("license_no"),
            rs.getString("name"),
            rs.getString("contact_number")
        );
    }
}