package com.biyahero.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
public class DBUtil {
    private static String currentDb = "biyahero_master";
    private static Properties config = new Properties();
    private static final String CONFIG_FILE = "config.properties";

    static {
        loadConfig();
    }

    public static void loadConfig() {
        String CONFIG_FILE = "config.properties";
        File externalFile = new File(CONFIG_FILE);

        try {
            if (externalFile.exists()) {
                // 1. Try loading from the file the wizard just created
                try (InputStream is = new FileInputStream(externalFile)) {
                    config.load(is);
                    System.out.println("Config loaded from root folder: " + externalFile.getAbsolutePath());
                    return; // Success!
                }
            }

            // 2. Fallback: Try loading from resources (for the prof's bundled version)
            try (InputStream is = DBUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (is != null) {
                    config.load(is);
                    System.out.println("Config loaded from resources folder.");
                } else {
                    System.out.println("Config file NOT found in root or resources.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
        }
    }

    public static void saveConfig(String user, String pass) throws IOException {
        config.setProperty("db.user", user);
        config.setProperty("db.password", pass); // Make sure this is "db.password"
        try (OutputStream os = new FileOutputStream("db_config.properties")) {
            config.store(os, "BiyaHero DB Configuration");
        }
    }

    public static boolean testConnection(String user, String pass) {
        // We connect to the MySQL server itself (no specific DB name needed for a test)
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // If we reach this line, the connection was successful
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            // Log the error so you can see why it failed (e.g., Access Denied)
            System.err.println("Test Connection Failed: " + e.getMessage());
            return false;
        }
    }

    public static Connection getConnection() throws SQLException {
        String user = config.getProperty("db.user");
        String pass = config.getProperty("db.password");

        System.out.println("DEBUG: Attempting connection with User: " + user);
        System.out.println("DEBUG: Password length: " + (pass != null ? pass.length() : "NULL"));

        // Build URL dynamically
        String url = "jdbc:mysql://localhost:3306/" + currentDb;
        return DriverManager.getConnection(url, user, pass);
    }

    // 1. Method to change the database on the fly
    public static void setDatabase(String dbName) {
        currentDb = dbName;
    }

    // 2. Method to check which database is currently active
    public static String getCurrentDb() {
        return currentDb;
    }
}