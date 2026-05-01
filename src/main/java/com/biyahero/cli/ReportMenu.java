package com.biyahero.cli;

import com.biyahero.model.TripReport;
import com.biyahero.service.FileService;
import com.biyahero.service.ImportService;
import com.biyahero.service.ImportService.ImportResult;
import com.biyahero.service.ReportService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ReportMenu {
    private static final ReportService reportService = new ReportService();
    private static final FileService   fileService   = new FileService();
    private static final ImportService importService = new ImportService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DT_FORMATTER   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void show(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== DATA REPORTS ===");
            System.out.println("[1] View Reports by Date Range");
            System.out.println("[2] View All Reports");
            System.out.println("[3] Search Reports");
            System.out.println("--- EXPORT ---");
            System.out.println("[4] Export to CSV");
            System.out.println("[5] Export to JSON");
            System.out.println("[6] Export SQL Backup");
            System.out.println("[7] Export to PDF");
            System.out.println("--- IMPORT ---");
            System.out.println("[8] Import SQL Backup");
            System.out.println("[9] Import from CSV");
            System.out.println("[10] Import from JSON");
            System.out.println("[0] Back");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewByDateRange(scanner);
                case "2" -> viewAllReports();
                case "3" -> searchReports(scanner);
                case "4" -> exportCSV(scanner);
                case "5" -> exportJSON(scanner);
                case "6" -> exportSQL(scanner);
                case "7" -> exportPDF(scanner);
                case "8" -> importSQL(scanner);
                case "9" -> importCSV(scanner);
                case "10" -> importJSON(scanner);
                case "0" -> running = false;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // ── VIEW ─────────────────────────────────────────────────────────────────

    private static void viewByDateRange(Scanner scanner) {
        System.out.print("Start Date (yyyy-MM-dd): ");
        String startInput = scanner.nextLine().trim();

        System.out.print("End Date (yyyy-MM-dd): ");
        String endInput = scanner.nextLine().trim();

        try {
            LocalDate startDate = LocalDate.parse(startInput, DATE_FORMATTER);
            LocalDate endDate   = LocalDate.parse(endInput,   DATE_FORMATTER);

            List<TripReport> reports = reportService.getReportsByDateRange(startDate, endDate);
            if (reports.isEmpty()) {
                System.out.println("No completed trips found in this date range.");
                return;
            }

            System.out.println("\n--- Trip Reports (" + startInput + " to " + endInput + ") ---");
            printReportHeader();
            reports.forEach(ReportMenu::printReport);
            printSummary(reports);

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewAllReports() {
        List<TripReport> reports = reportService.getAllReports();
        if (reports.isEmpty()) {
            System.out.println("No completed trips found.");
            return;
        }
        System.out.println("\n--- All Completed Trips ---");
        printReportHeader();
        reports.forEach(ReportMenu::printReport);
        printSummary(reports);
    }

    private static void searchReports(Scanner scanner) {
        System.out.print("Search (Trip ID, Driver, or Route): ");
        String keyword = scanner.nextLine().trim();

        List<TripReport> results = reportService.searchReports(keyword);
        if (results.isEmpty()) {
            System.out.println("No reports found.");
            return;
        }
        printReportHeader();
        results.forEach(ReportMenu::printReport);
        printSummary(results);
    }

    // ── EXPORT ───────────────────────────────────────────────────────────────

    private static void exportCSV(Scanner scanner) {
        List<TripReport> reports = reportService.getAllReports();
        if (reports.isEmpty()) { System.out.println("No reports to export."); return; }

        System.out.print("Save as (e.g. trip_report.csv): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) path = "trip_report.csv";

        try {
            fileService.exportTripReportsToCSV(reports, path);
            System.out.println("Exported → " + path);
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private static void exportJSON(Scanner scanner) {
        List<TripReport> reports = reportService.getAllReports();
        if (reports.isEmpty()) { System.out.println("No reports to export."); return; }

        System.out.print("Save as (e.g. trip_report.json): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) path = "trip_report.json";

        try {
            fileService.exportTripReportsToJSON(reports, path);
            System.out.println("Exported → " + path);
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private static void exportSQL(Scanner scanner) {
        System.out.print("Save as (e.g. biyahero_backup.sql): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) path = "biyahero_backup.sql";

        try {
            fileService.exportFullDatabaseToSQL(path);
            System.out.println("Exported → " + path);
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private static void exportPDF(Scanner scanner) {
        List<TripReport> reports = reportService.getAllReports();
        if (reports.isEmpty()) { System.out.println("No reports to export."); return; }

        System.out.print("Save as (e.g. trip_report.pdf): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) path = "trip_report.pdf";

        try {
            fileService.exportTripReportsToPDF(reports, path, "All Completed Trips");
            System.out.println("Exported → " + path);
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    // ── IMPORT ───────────────────────────────────────────────────────────────

    private static void importSQL(Scanner scanner) {
        System.out.println("\n  [!] WARNING: This will delete all current data and replace it");
        System.out.println("      with the contents of the backup file. This cannot be undone.");
        System.out.print("  Continue? (yes/no): ");
        if (!confirmYes(scanner)) {
            System.out.println("Import cancelled.");
            return;
        }

        System.out.print("Backup file path (e.g. biyahero_backup.sql): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            System.out.println("No file path provided. Import cancelled.");
            return;
        }

        System.out.println("Importing...");
        ImportResult result = importService.importFromSQL(path);
        System.out.println(result);
    }

    private static void importCSV(Scanner scanner) {
        System.out.println("\n  [i] Imports trip records from a BiyaHero CSV export.");
        System.out.println("      Trips that already exist in the database will be skipped.");

        System.out.print("CSV file path (e.g. trip_report.csv): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            System.out.println("No file path provided. Import cancelled.");
            return;
        }

        System.out.println("Importing...");
        ImportResult result = importService.importFromCSV(path);
        System.out.println(result);
    }

    private static void importJSON(Scanner scanner) {
        System.out.println("\n  [i] Imports trip records from a BiyaHero JSON export.");
        System.out.println("      Trips that already exist in the database will be skipped.");

        System.out.print("JSON file path (e.g. trip_report.json): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            System.out.println("No file path provided. Import cancelled.");
            return;
        }

        System.out.println("Importing...");
        ImportResult result = importService.importFromJSON(path);
        System.out.println(result);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private static boolean confirmYes(Scanner scanner) {
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("yes") || input.equals("y");
    }

    private static void printReportHeader() {
        System.out.printf("%-10s %-20s %-25s %-18s %-15s %-12s%n",
            "Trip ID", "Driver", "Route", "Departure", "Occupancy", "Revenue");
        System.out.println("-".repeat(102));
    }

    private static void printReport(TripReport r) {
        System.out.printf("%-10s %-20s %-25s %-18s %-15s PHP %-10.2f%n",
            r.getFormattedTripId(),
            r.getDriverName(),
            r.getRouteName(),
            r.getDepartureTime() != null ? r.getDepartureTime().format(DT_FORMATTER) : "-",
            r.getOccupancyRate(),
            r.getTotalRevenue()
        );
    }

    private static void printSummary(List<TripReport> reports) {
        double totalRevenue = reports.stream().mapToDouble(TripReport::getTotalRevenue).sum();
        double avgOccupancy = reports.stream()
            .mapToDouble(r -> r.getTotalCapacity() > 0
                ? (r.getBookedSeats() * 100.0) / r.getTotalCapacity() : 0)
            .average().orElse(0);

        System.out.println("-".repeat(102));
        System.out.printf("Total Trips: %-10d Avg Occupancy: %-10.1f%% Total Revenue: PHP %.2f%n",
            reports.size(), avgOccupancy, totalRevenue);
    }
}