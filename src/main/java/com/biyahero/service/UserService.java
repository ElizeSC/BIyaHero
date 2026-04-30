package com.biyahero.service;

import com.biyahero.util.DBUtil;
import java.sql.*;
import java.util.List;

public class UserService {

    public boolean registerUser(String fullName, String username, String password) {
        String dbName = "biyahero_" + username.toLowerCase().replaceAll("\\s+", "_");

        try (Connection masterConn = DBUtil.getConnection()) {
            // 1. Create the physical database
            masterConn.createStatement().executeUpdate("CREATE DATABASE " + dbName);

            // 2. Add entry to master accounts table
            String sql = "INSERT INTO biyahero_master.accounts (username, password, db_name) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = masterConn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, dbName);
                pstmt.executeUpdate();
            }

            // 3. Setup the tables in the new DB
            initializeNewDatabase(dbName);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initializeNewDatabase(String dbName) throws SQLException {
        // Temporarily switch to the new DB to run setup
        String previousDb = DBUtil.getCurrentDb();
        DBUtil.setDatabase(dbName);

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            // Add ALL your CREATE TABLE scripts here
            String[] schema = {
                    "CREATE TABLE van (van_id INT AUTO_INCREMENT PRIMARY KEY, plate_number VARCHAR(20), model VARCHAR(50), capacity INT)",
                    "CREATE TABLE driver (driver_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), license_no VARCHAR(50))",
                    "CREATE TABLE route (route_id INT AUTO_INCREMENT PRIMARY KEY, origin VARCHAR(100), destination VARCHAR(100), base_fare DOUBLE)",
                    "CREATE TABLE trip (trip_id INT AUTO_INCREMENT PRIMARY KEY, van_id INT, driver_id INT, route_id INT, departure_dt DATETIME, trip_status VARCHAR(20))"
                    // Add booking, passenger, etc. as needed
            };

            for (String sql : schema) {
                stmt.executeUpdate(sql);
            }
        } finally {
            DBUtil.setDatabase(previousDb); // Switch back
        }
    }

    public String authenticate(String username, String password) {
        String sql = "SELECT db_name FROM biyahero_master.accounts WHERE username = ? AND password = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("db_name"); // Return the DB name for this user
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}