package com.biyahero.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    // Load credentials once when the class is first used
    static {
        Properties props = new Properties();
        try (InputStream in = DBUtil.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (in == null) {
                throw new RuntimeException(
                        "config.properties not found. " +
                                "Copy config.properties.example to config.properties " +
                                "and fill in your database credentials.");
            }

            props.load(in);
            URL      = props.getProperty("db.url");
            USER     = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
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
        return USER.equals(user) && PASSWORD.equals(pass);
    }
}