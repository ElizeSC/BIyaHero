package com.biyahero.service;

import com.biyahero.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    /**
     * Checks if the credentials exist in the master account table.
     * Returns the name of the user's specific database if successful.
     */
    public String authenticate(String username, String password) {
        // We look specifically in the master table
        String sql = "SELECT db_name FROM biyahero_master.accounts WHERE username = ? AND password = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, DBUtil.hashPassword(password));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("db_name");
                }
            }
        } catch (SQLException e) {
            System.err.println("Auth Error: " + e.getMessage());
        }
        return null; // Login failed
    }
}