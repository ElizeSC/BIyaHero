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

// CSV trip report   — reads the BiyaHero trip_report CSV and inserts into the trip table.
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