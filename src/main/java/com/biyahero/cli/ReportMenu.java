package com.biyahero.cli;

import com.biyahero.model.TripReport;
import com.biyahero.service.ReportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ReportMenu {
    private static final ReportService reportService = new ReportService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void show(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== DATA REPORTS ===");
            System.out.println("[1] View Reports by Date Range");
            System.out.println("[2] Export All (Full Historical Dataset)");
            System.out.println("[3] Search Reports");
            System.out.println("[0] Back");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewByDateRange(scanner);
                case "2" -> viewAllReports();
                case "3" -> searchReports(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // VIEW BY DATE RANGE 

    private static void viewByDateRange(Scanner scanner) {
        System.out.print("Start Date (yyyy-MM-dd): ");
        String startInput = scanner.nextLine().trim();

        System.out.print("End Date (yyyy-MM-dd): ");
        String endInput = scanner.nextLine().trim();

        try {
            LocalDate startDate = LocalDate.parse(startInput, DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(endInput, DATE_FORMATTER);

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

    // EXPORT ALL

    private static void viewAllReports() {
        List<TripReport> reports = reportService.getAllReports();
        if (reports.isEmpty()) {
            System.out.println("No completed trips found.");
            return;
        }

        System.out.println("\n--- Full Historical Dataset ---");
        printReportHeader();
        reports.forEach(ReportMenu::printReport);
        printSummary(reports);
    }

    // SEARCH REPORTS 

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

    // DISPLAY HELPERS

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
        double totalRevenue = reports.stream()
            .mapToDouble(TripReport::getTotalRevenue)
            .sum();
        int totalTrips = reports.size();
        double avgOccupancy = reports.stream()
            .mapToDouble(r -> r.getTotalCapacity() > 0
                ? (r.getBookedSeats() * 100.0) / r.getTotalCapacity()
                : 0)
            .average()
            .orElse(0);

        System.out.println("-".repeat(102));
        System.out.printf("Total Trips: %-10d Avg Occupancy: %-10.1f%% Total Revenue: PHP %.2f%n",
            totalTrips, avgOccupancy, totalRevenue);
    }
}