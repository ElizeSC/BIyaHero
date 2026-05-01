package com.biyahero.service;

import com.biyahero.util.DBUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Supported import type:
// SQL backup (.sql) — wipes the DB via embedded schema, then runs INSERTs from the file.
public class ImportService {

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

    // ── Full SQL Backup Import ────────────────────────────────────────────────

    // Step 1: wipe + recreate all tables using the embedded schema.sql
    // Step 2: run only the INSERT statements from the provided .sql backup file
    //         (converted to REPLACE INTO so duplicates are overwritten, not rejected)
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

    // ── SQL file helpers ──────────────────────────────────────────────────────

    // Reads a .sql file and returns statements based on mode:
    //   insertsOnly = true  → return only INSERT statements, converted to REPLACE INTO
    //                         so duplicates are overwritten rather than rejected
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

                // Skip SET FOREIGN_KEY_CHECKS lines — executeStatements handles this
                if (trimmed.toUpperCase().startsWith("SET FOREIGN_KEY_CHECKS")) continue;

                boolean isInsert = trimmed.toUpperCase().startsWith("INSERT");

                // Filter based on mode
                if ( insertsOnly && !isInsert) continue;
                if (!insertsOnly &&  isInsert) continue;

                // Convert INSERT INTO → REPLACE INTO so duplicates are
                // overwritten instead of throwing a duplicate key error
                if (insertsOnly) {
                    line = line.replaceFirst("(?i)INSERT\\s+INTO", "REPLACE INTO");
                }

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
}