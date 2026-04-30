package com.biyahero.service;

import com.biyahero.util.DBUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

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
        String previousDb = DBUtil.getCurrentDb();
        DBUtil.setDatabase(dbName);

        // Path must match your resources exactly: /com/biyahero/sql/schema.sql
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             InputStream is = getClass().getResourceAsStream("/com/biyahero/sql/schema.sql")) {

            if (is == null) {
                throw new SQLException("SQL Schema file not found in resources!");
            }

            // Convert InputStream to String
            String script = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            // Split by semicolon to execute each command separately
            String[] commands = script.split(";");

            for (String command : commands) {
                String trimmedCommand = command.trim();
                if (!trimmedCommand.isEmpty()) {
                    stmt.executeUpdate(trimmedCommand);
                }
            }

            System.out.println("Successfully provisioned " + dbName + " using schema.sql");

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            DBUtil.setDatabase(previousDb);
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