package com.biyahero.service;

import com.biyahero.util.DBUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Supported import types:
// SQL backup (.sql) — wipes the DB via embedded schema, then runs INSERTs from the file.
// CSV trip report   — reads the BiyaHero trip_report CSV and inserts into the trip table.
// JSON trip report  — reads the BiyaHero trip_report JSON and inserts into the trip table.
public class ImportService {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── Result wrapper ────────────────────────────────────────────────────────

    public static class ImportResult {
        public final boolean success;
        public final String  message;
        public final int     rowsAffected;

        public ImportResult(boolean success, String message, int rowsAffected) {
            this.success      = success;
            this.message      = message;
            this.rowsAffected = rowsAffected;
        }

        @Override
        public String toString() {
            return (success ? "[OK] " : "[FAILED] ") + message +
                   (rowsAffected >= 0 ? " (" + rowsAffected + " rows)" : "");
        }
    }

    // ── 1. Full SQL Backup Import ─────────────────────────────────────────────

    // Step 1: wipe + recreate all tables using the embedded schema.sql
    // Step 2: run only the INSERT statements from the provided .sql backup file
    public ImportResult importFromSQL(String filePath) {
        try {
            // Step 1: clean slate via embedded schema
            ImportResult schemaResult = runEmbeddedSchema();
            if (!schemaResult.success) return schemaResult;

            // Step 2: extract only INSERT statements from the backup file and run them
            List<String> inserts = parseSqlFile(filePath, true); // true = INSERTs only
            int rows = executeStatements(inserts);

            return new ImportResult(true,
                    "SQL backup imported successfully from: " + filePath, rows);

        } catch (IOException e) {
            return new ImportResult(false, "Cannot read file: " + e.getMessage(), -1);
        } catch (SQLException e) {
            return new ImportResult(false, "SQL error during import: " + e.getMessage(), -1);
        } catch (Exception e) {
            return new ImportResult(false, "Unexpected error: " + e.getMessage(), -1);
        }
    }

    // Runs the embedded /com/biyahero/sql/schema.sql to get a clean, correctly structured database.
    private ImportResult runEmbeddedSchema() throws SQLException, IOException {
        try (InputStream is = getClass().getResourceAsStream("/com/biyahero/sql/schema.sql")) {
            if (is == null) {
                return new ImportResult(false,
                        "Built-in schema.sql not found in resources. " +
                        "Cannot proceed with import.", -1);
            }

            String script = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            List<String> statements = new ArrayList<>();
            for (String stmt : script.split(";")) {
                String trimmed = stmt.trim();
                if (!trimmed.isEmpty()) {
                    statements.add(trimmed);
                }
            }

            int rows = executeStatements(statements);
            return new ImportResult(true,
                    "Database wiped and recreated successfully.", rows);
        }
    }

    // ── 2. CSV Trip Report Import ─────────────────────────────────────────────

    // Reads the BiyaHero trip report CSV (as exported by FileService) and inserts
    // trip rows into the current database.
    // route_id, van_id, driver_id are set to 0 since the CSV doesn't carry those IDs.
    // Duplicate trip_ids are skipped via INSERT IGNORE.
    public ImportResult importFromCSV(String filePath) {
        int inserted  = 0;
        int skipped   = 0;
        int malformed = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String headerLine = br.readLine(); // skip header row
            if (headerLine == null) {
                return new ImportResult(false, "CSV file is empty.", -1);
            }

            String sql = "INSERT IGNORE INTO trip " +
                         "(trip_id, route_id, van_id, driver_id, departure_dt, arrival_dt, trip_status) " +
                         "VALUES (?, 0, 0, 0, ?, ?, 'Completed')";

            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String line;
                int lineNum = 1;

                while ((line = br.readLine()) != null) {
                    lineNum++;
                    if (line.isBlank()) continue;

                    String[] fields = parseCsvLine(line);

                    // Need at least 5 fields: TripID, Driver, Route, Departure, Arrival
                    if (fields.length < 5) {
                        malformed++;
                        System.err.println("ImportService: skipping malformed line " + lineNum + ": " + line);
                        continue;
                    }

                    try {
                        // Field 0: Trip ID — strip "TRP" prefix → integer
                        String rawId = fields[0].trim().toUpperCase();
                        int tripId = rawId.startsWith("TRP")
                                ? Integer.parseInt(rawId.substring(3))
                                : Integer.parseInt(rawId);

                        // Field 3: Departure datetime (may be empty)
                        LocalDateTime departure = null;
                        if (!fields[3].isBlank()) {
                            departure = LocalDateTime.parse(fields[3].trim(), DT_FMT);
                        }

                        // Field 4: Arrival datetime (may be empty)
                        LocalDateTime arrival = null;
                        if (fields.length > 4 && !fields[4].isBlank()) {
                            arrival = LocalDateTime.parse(fields[4].trim(), DT_FMT);
                        }

                        pstmt.setInt(1, tripId);
                        pstmt.setObject(2, departure != null ? Timestamp.valueOf(departure) : null);
                        pstmt.setObject(3, arrival   != null ? Timestamp.valueOf(arrival)   : null);

                        int affected = pstmt.executeUpdate();
                        if (affected > 0) inserted++;
                        else              skipped++; // trip_id already exists

                    } catch (NumberFormatException | DateTimeParseException e) {
                        malformed++;
                        System.err.println("ImportService: parse error on line " + lineNum
                                + " — " + e.getMessage());
                    }
                }
            }

            String msg = String.format(
                    "CSV import complete — %d inserted, %d skipped (duplicate IDs), %d malformed.",
                    inserted, skipped, malformed);

            return new ImportResult(true, msg, inserted);

        } catch (FileNotFoundException e) {
            return new ImportResult(false, "File not found: " + filePath, -1);
        } catch (IOException e) {
            return new ImportResult(false, "Cannot read file: " + e.getMessage(), -1);
        } catch (SQLException e) {
            return new ImportResult(false, "Database error: " + e.getMessage(), -1);
        }
    }

    // ── 3. JSON Trip Report Import ────────────────────────────────────────────

    // Reads the BiyaHero trip report JSON (as exported by FileService) and inserts
    // trip rows into the current database.
    // Mirrors CSV import: route_id, van_id, driver_id set to 0. Duplicates skipped via INSERT IGNORE.
    public ImportResult importFromJSON(String filePath) {
        int inserted  = 0;
        int skipped   = 0;
        int malformed = 0;

        try {
            // Read entire file into a string
            String content;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
                content = sb.toString().trim();
            }

            // Locate the "trips" array — find opening [ after "trips":
            int tripsStart = content.indexOf("\"trips\"");
            if (tripsStart == -1) {
                return new ImportResult(false, "Invalid JSON: missing \"trips\" array.", -1);
            }
            int arrayOpen  = content.indexOf('[', tripsStart);
            int arrayClose = content.lastIndexOf(']');
            if (arrayOpen == -1 || arrayClose == -1 || arrayClose <= arrayOpen) {
                return new ImportResult(false, "Invalid JSON: malformed trips array.", -1);
            }

            // Split array content into individual trip objects by splitting on "},{"
            String arrayContent = content.substring(arrayOpen + 1, arrayClose).trim();
            // Normalize: remove outer braces left from split, split on object boundaries
            String[] rawObjects = arrayContent.split("\\},\\s*\\{");

            String sql = "INSERT IGNORE INTO trip " +
                         "(trip_id, route_id, van_id, driver_id, departure_dt, arrival_dt, trip_status) " +
                         "VALUES (?, 0, 0, 0, ?, ?, 'Completed')";

            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < rawObjects.length; i++) {
                    // Re-wrap each fragment into a valid object string
                    String obj = rawObjects[i].trim();
                    if (!obj.startsWith("{")) obj = "{" + obj;
                    if (!obj.endsWith("}"))   obj = obj + "}";

                    try {
                        String tripIdStr  = extractJsonString(obj, "tripId");
                        String departure  = extractJsonString(obj, "departure");
                        String arrival    = extractJsonString(obj, "arrival");

                        if (tripIdStr == null) { malformed++; continue; }

                        String rawId = tripIdStr.trim().toUpperCase();
                        int tripId = rawId.startsWith("TRP")
                                ? Integer.parseInt(rawId.substring(3))
                                : Integer.parseInt(rawId);

                        LocalDateTime depDt = (departure != null && !departure.isBlank())
                                ? LocalDateTime.parse(departure.trim(), DT_FMT) : null;
                        LocalDateTime arrDt = (arrival   != null && !arrival.isBlank())
                                ? LocalDateTime.parse(arrival.trim(),   DT_FMT) : null;

                        pstmt.setInt(1, tripId);
                        pstmt.setObject(2, depDt != null ? Timestamp.valueOf(depDt) : null);
                        pstmt.setObject(3, arrDt != null ? Timestamp.valueOf(arrDt) : null);

                        int affected = pstmt.executeUpdate();
                        if (affected > 0) inserted++;
                        else              skipped++;

                    } catch (NumberFormatException | DateTimeParseException e) {
                        malformed++;
                        System.err.println("ImportService: parse error on trip object " + i
                                + " — " + e.getMessage());
                    }
                }
            }

            String msg = String.format(
                    "JSON import complete — %d inserted, %d skipped (duplicate IDs), %d malformed.",
                    inserted, skipped, malformed);
            return new ImportResult(true, msg, inserted);

        } catch (FileNotFoundException e) {
            return new ImportResult(false, "File not found: " + filePath, -1);
        } catch (IOException e) {
            return new ImportResult(false, "Cannot read file: " + e.getMessage(), -1);
        } catch (SQLException e) {
            return new ImportResult(false, "Database error: " + e.getMessage(), -1);
        }
    }

    // Extracts the string value of a JSON key from a single-object JSON fragment.
    // Handles both string values ("key": "value") and numeric values ("key": 123).
    // Returns null if the key is not found.
    private String extractJsonString(String obj, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = obj.indexOf(search);
        if (keyIdx == -1) return null;

        int colon = obj.indexOf(':', keyIdx + search.length());
        if (colon == -1) return null;

        // Skip whitespace after colon
        int valueStart = colon + 1;
        while (valueStart < obj.length() && obj.charAt(valueStart) == ' ') valueStart++;

        if (valueStart >= obj.length()) return null;

        char first = obj.charAt(valueStart);

        if (first == '"') {
            // String value — read until closing unescaped quote
            int end = valueStart + 1;
            while (end < obj.length()) {
                if (obj.charAt(end) == '"' && obj.charAt(end - 1) != '\\') break;
                end++;
            }
            return obj.substring(valueStart + 1, end)
                      .replace("\\\"", "\"")
                      .replace("\\\\", "\\");
        } else {
            // Numeric / boolean — read until comma, }, or newline
            int end = valueStart;
            while (end < obj.length()) {
                char c = obj.charAt(end);
                if (c == ',' || c == '}' || c == '\n') break;
                end++;
            }
            return obj.substring(valueStart, end).trim();
        }
    }

    // ── SQL file helpers ──────────────────────────────────────────────────────

    // Reads a .sql file and returns statements based on mode:
    //   insertsOnly = true  → return only INSERT statements  (used by importFromSQL step 2)
    //   insertsOnly = false → return only non-INSERT statements, i.e. DDL
    private List<String> parseSqlFile(String filePath, boolean insertsOnly) throws IOException {
        List<String> statements = new ArrayList<>();
        StringBuilder current   = new StringBuilder();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();

                // Skip blank lines and comments
                if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;

                // Skip USE statements — always target the current active DB
                if (trimmed.toUpperCase().startsWith("USE ")) continue;

                boolean isInsert = trimmed.toUpperCase().startsWith("INSERT");

                // Filter based on mode
                if ( insertsOnly && !isInsert) continue;
                if (!insertsOnly &&  isInsert) continue;

                current.append(line).append("\n");

                if (trimmed.endsWith(";")) {
                    String stmt = current.toString().trim();
                    if (stmt.endsWith(";")) {
                        stmt = stmt.substring(0, stmt.length() - 1).trim();
                    }
                    if (!stmt.isEmpty()) {
                        statements.add(stmt);
                    }
                    current.setLength(0);
                }
            }

            // Trailing statement without semicolon
            String remainder = current.toString().trim();
            if (!remainder.isEmpty()) {
                statements.add(remainder);
            }
        }

        return statements;
    }

    // Executes a list of SQL statements against the current active database.
    // Foreign key checks are disabled so DROP order doesn't matter.
    private int executeStatements(List<String> statements) throws SQLException {
        int totalRows = 0;

        try (Connection conn = DBUtil.getConnection();
             Statement  stmt = conn.createStatement()) {

            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");

            for (String sql : statements) {
                try {
                    int affected = stmt.executeUpdate(sql);
                    if (affected > 0) totalRows += affected;
                } catch (SQLException e) {
                    throw new SQLException(
                            "Failed on statement:\n" + sql + "\n\nCause: " + e.getMessage(), e);
                }
            }

            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        }

        return totalRows;
    }

    // ── CSV parsing helper ────────────────────────────────────────────────────

    // RFC 4180 compliant CSV line parser.
    // Handles quoted fields and escaped double-quotes ("").
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString().trim());

        return fields.toArray(new String[0]);
    }
}