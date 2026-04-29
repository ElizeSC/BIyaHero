package com.biyahero.cli;

import com.biyahero.model.TripReport;
import com.biyahero.service.FileService;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DT_FORMATTER   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // tracks the last viewed/searched list so exports operate on the same data
    private static List<TripReport> lastViewedReports = null;
    private static String           lastViewedLabel   = null;

    public static void show(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== DATA REPORTS ===");
            System.out.println("[1] View All Reports");
            System.out.println("[2] View Reports by Date Range");
            System.out.println("[3] Search Reports");
            System.out.println("[4] Export a Specific Trip");
            System.out.println("[0] Back");

            // show export section only after something has been viewed
            if (lastViewedLabel != null) {
                System.out.println("\n--- Last Viewed: " + lastViewedLabel + " ---");
                System.out.println("[5] Export Last Result to CSV");
                System.out.println("[6] Export Last Result to JSON");
                System.out.println("[7] Export Last Result to SQL Backup");
                System.out.println("[8] Export Last Result to PDF");
            }

            System.out.print("\nSelect: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewAllReports();
                case "2" -> viewByDateRange(scanner);
                case "3" -> searchReports(scanner);
                case "4" -> exportSpecificTrip(scanner);
                case "5" -> exportCurrentReports(scanner, "csv");
                case "6" -> exportCurrentReports(scanner, "json");
                case "7" -> exportCurrentReports(scanner, "sql");
                case "8" -> exportCurrentReports(scanner, "pdf");
                case "0" -> running = false;
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    // ── VIEW ─────────────────────────────────────────────────────────────────

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

        lastViewedReports = reports;
        lastViewedLabel   = "All Completed Trips";
    }

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

            lastViewedReports = reports;
            lastViewedLabel   = startInput + " to " + endInput;

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void searchReports(Scanner scanner) {
        System.out.print("Search (Trip ID, Driver, or Route): ");
        String keyword = scanner.nextLine().trim();

        List<TripReport> results = reportService.searchReports(keyword);
        if (results.isEmpty()) {
            System.out.println("No reports found.");
            return;
        }

        System.out.println("\n--- Search Results for \"" + keyword + "\" ---");
        printReportHeader();
        results.forEach(ReportMenu::printReport);
        printSummary(results);

        lastViewedReports = results;
        lastViewedLabel   = "Search Results for \"" + keyword + "\"";
    }

    // ── SPECIFIC TRIP EXPORT ──────────────────────────────────────────────────

    private static void exportSpecificTrip(Scanner scanner) {
        System.out.print("Enter Trip ID (e.g. TRP0001 or just 1): ");
        String input = scanner.nextLine().trim();

        // normalize: strip "TRP" prefix if present so both "TRP0001" and "1" work
        String keyword = input.toUpperCase().startsWith("TRP") ? input : "TRP" + String.format("%04d", parseIntSafe(input));

        List<TripReport> results = reportService.searchReports(keyword);

        // filter to exact match only
        List<TripReport> exact = results.stream()
                .filter(r -> r.getFormattedTripId().equalsIgnoreCase(keyword))
                .toList();

        if (exact.isEmpty()) {
            System.out.println("No trip found with ID: " + keyword);
            return;
        }

        TripReport r = exact.get(0);
        System.out.println("\nFound:");
        printReportHeader();
        printReport(r);

        System.out.println("\nExport as:");
        System.out.println("[1] CSV");
        System.out.println("[2] JSON");
        System.out.println("[3] SQL Backup");
        System.out.println("[4] PDF");
        System.out.println("[0] Cancel");
        System.out.print("Select: ");

        String fmt = switch (scanner.nextLine().trim()) {
            case "1" -> "csv";
            case "2" -> "json";
            case "3" -> "sql";
            case "4" -> "pdf";
            default  -> null;
        };

        if (fmt == null) {
            System.out.println("Export cancelled.");
            return;
        }

        String defaultName = "trip_" + keyword + "." + (fmt.equals("sql") ? "sql" : fmt);
        System.out.print("Output file path (press Enter for \"" + defaultName + "\"): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) path = defaultName;

        String label = "Trip " + keyword;
        doExport(exact, path, fmt, label);

        // also update lastViewed so [5]-[8] can re-export it
        lastViewedReports = exact;
        lastViewedLabel   = label;
    }

    // ── EXPORT LAST VIEWED ────────────────────────────────────────────────────

    private static void exportCurrentReports(Scanner scanner, String format) {
        if (lastViewedReports == null || lastViewedReports.isEmpty()) {
            System.out.println("No reports loaded. Use [1]–[4] to view or select a trip first.");
            return;
        }

        String defaultName = switch (format) {
            case "csv"  -> "trip_report.csv";
            case "json" -> "trip_report.json";
            case "sql"  -> "biyahero_backup.sql";
            case "pdf"  -> "trip_report.pdf";
            default     -> "export";
        };

        System.out.print("Output file path (press Enter for \"" + defaultName + "\"): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) path = defaultName;

        doExport(lastViewedReports, path, format, lastViewedLabel);
    }

    // EXPORT LOGIC 

    private static void doExport(List<TripReport> reports, String path, String format, String label) {
        try {
            switch (format) {
                case "csv"  -> fileService.exportTripReportsToCSV(reports, path);
                case "json" -> fileService.exportTripReportsToJSON(reports, path);
                case "sql"  -> fileService.exportTripReportsToSQL(reports, path);
                case "pdf"  -> fileService.exportTripReportsToPDF(reports, path, label);
            }
            System.out.println(format.toUpperCase() + " exported successfully → " + path);
        } catch (IOException e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    // ── DISPLAY HELPERS ───────────────────────────────────────────────────────

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
        System.out.println("(Use [5]-[8] to export this result set)");
    }

    // ── UTILITIES ─────────────────────────────────────────────────────────────

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }
}