package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    // Make sure these match your local MySQL settings!
    private static final String URL = "jdbc:mysql://localhost:3306/biyahero_db";
    private static final String USER = "biyahero_admin";
    private static final String PASSWORD = "biyahero123";

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // This string is for the mysql-connector-j driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean testConnection(String user, String pass) {
        // Check if the input matches your final constants
        return USER.equals(user) && PASSWORD.equals(pass);
    }
}