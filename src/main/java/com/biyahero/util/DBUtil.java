package com.biyahero.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.sql.Statement;
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
    private static final String CONFIG_FILE = "db_config.properties";

    static {
        loadConfig();
    }

    public static void loadConfig() {
        String CONFIG_FILE = "db_config.properties";
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

        // Build URL dynamically
        String url = "jdbc:mysql://localhost:3306/" + currentDb;
        return DriverManager.getConnection(url, user, pass);
    }

    public static void setDatabase(String dbName) {
        currentDb = dbName;
    }

    public static String getCurrentDb() {
        return currentDb;
    }

    public static void initializeMasterDatabase() {
        // 1. Connect to MySQL without a specific DB first
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true";
        String user = config.getProperty("db.user");
        String pass = config.getProperty("db.password");

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {

            // 2. Create and switch to the master database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS biyahero_master");
            stmt.executeUpdate("USE biyahero_master");

            // 3. Create the 'accounts' table matching your terminal output
            String createAccountsTable = "CREATE TABLE IF NOT EXISTS accounts (" +
                    "account_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "db_name VARCHAR(100) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

            stmt.executeUpdate(createAccountsTable);

            // 4. (Optional) Create a default admin if the table is empty
            String checkAdmin = "SELECT COUNT(*) FROM accounts WHERE username = 'biyahero_admin'";
            var rs = stmt.executeQuery(checkAdmin);
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO accounts (username, password, db_name) " +
                        "VALUES ('biyahero_admin', 'biyahero123', 'biyahero_db')");
                System.out.println("✅ Default admin 'biyahero_admin' created.");
            }

            System.out.println("✅ BiyaHero Master System Initialized successfully.");

        } catch (SQLException e) {
            System.err.println("❌ Critical Error during Master DB Setup: " + e.getMessage());
        }
    }
}