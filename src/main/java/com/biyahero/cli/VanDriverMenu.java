package com.biyahero.cli;

import com.biyahero.model.Driver;
import com.biyahero.model.Van;
import com.biyahero.service.DriverService;
import com.biyahero.service.VanService;

import java.util.List;
import java.util.Scanner;

public class VanDriverMenu {
    private static final VanService vanService = new VanService();
    private static final DriverService driverService = new DriverService();

    public static void show(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== VANS & DRIVERS ===");
            System.out.println("[1] Vans");
            System.out.println("[2] Drivers");
            System.out.println("[0] Back");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> showVanMenu(scanner);
                case "2" -> showDriverMenu(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // VAN MENU

    private static void showVanMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== VANS ===");
            System.out.println("[1] View All Vans");
            System.out.println("[2] Search Vans");
            System.out.println("[3] Filter by Status");
            System.out.println("[4] Add Van");
            System.out.println("[5] Edit Van");
            System.out.println("[6] Delete Van");
            System.out.println("[0] Back");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewAllVans();
                case "2" -> searchVans(scanner);
                case "3" -> filterVansByStatus(scanner);
                case "4" -> addVan(scanner);
                case "5" -> editVan(scanner);
                case "6" -> deleteVan(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void viewAllVans() {
        List<Van> vans = vanService.getAllVans();
        if (vans.isEmpty()) {
            System.out.println("No vans found.");
            return;
        }
        System.out.println("\n--- All Vans ---");
        printVanHeader();
        vans.forEach(VanDriverMenu::printVan);
    }

    private static void searchVans(Scanner scanner) {
        System.out.print("Search keyword: ");
        String keyword = scanner.nextLine().trim();
        List<Van> results = vanService.searchVans(keyword);
        if (results.isEmpty()) {
            System.out.println("No vans found.");
            return;
        }
        printVanHeader();
        results.forEach(VanDriverMenu::printVan);
    }

    private static void filterVansByStatus(Scanner scanner) {
        System.out.println("Status: [1] Available  [2] On Trip  [3] Under Maintenance");
        System.out.print("Select: ");
        String status = switch (scanner.nextLine().trim()) {
            case "1" -> "Available";
            case "2" -> "On Trip";
            case "3" -> "Under Maintenance";
            default -> null;
        };
        if (status == null) {
            System.out.println("Invalid option.");
            return;
        }
        List<Van> results = vanService.filterByStatus(status);
        if (results.isEmpty()) {
            System.out.println("No vans with status: " + status);
            return;
        }
        printVanHeader();
        results.forEach(VanDriverMenu::printVan);
    }

    private static void addVan(Scanner scanner) {
        System.out.println("\n--- Add Van ---");
        System.out.print("Plate Number: ");
        String plate = scanner.nextLine().trim();

        System.out.print("Model: ");
        String model = scanner.nextLine().trim();

        System.out.print("Capacity: ");
        int capacity;
        try {
            capacity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid capacity.");
            return;
        }

        try {
            vanService.addVan(plate, model, capacity, "Available");
            System.out.println("Van added successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void editVan(Scanner scanner) {
        System.out.print("Enter Van ID to edit: ");
        int vanId = parseId(scanner.nextLine().trim(), "VAE");
        try {
            vanId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        try {
            Van existing = vanService.getVanById(vanId);
            System.out.println("Editing: " + existing.getFormattedId() + " - " + existing.getPlateNumber());
            System.out.println("(Press Enter to keep current value)");

            System.out.print("Plate Number [" + existing.getPlateNumber() + "]: ");
            String plate = scanner.nextLine().trim();

            System.out.print("Model [" + existing.getModel() + "]: ");
            String model = scanner.nextLine().trim();

            System.out.print("Capacity [" + existing.getCapacity() + "]: ");
            String capInput = scanner.nextLine().trim();
            int capacity = capInput.isEmpty() ? 0 : Integer.parseInt(capInput);

            System.out.print("Status [" + existing.getVanStatus() + "]: ");
            String status = scanner.nextLine().trim();

            vanService.updateVan(vanId,
                plate.isEmpty() ? null : plate,
                model.isEmpty() ? null : model,
                capacity,
                status.isEmpty() ? null : status
            );
            System.out.println("Van updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteVan(Scanner scanner) throws NumberFormatException {
        System.out.print("Enter Van ID to delete: ");
        try {
            int vanId = parseId(scanner.nextLine().trim(), "VAE");
            vanService.getVanById(vanId); // throws if not found
            System.out.print("Are you sure? (y/n): ");
            if ("y".equalsIgnoreCase(scanner.nextLine().trim())) {
                vanService.deleteVan(vanId);
                System.out.println("Van deleted.");
            } else {
                System.out.println("Cancelled.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printVanHeader() {
        System.out.printf("%-8s %-12s %-20s %-10s %-15s%n",
            "ID", "Plate", "Model", "Capacity", "Status");
        System.out.println("-".repeat(67));
    }

    private static void printVan(Van v) {
        System.out.printf("%-8s %-12s %-20s %-10d %-15s%n",
            v.getFormattedId(), v.getPlateNumber(), v.getModel(),
            v.getCapacity(), v.getVanStatus());
    }

    // DRIVER MENU

    private static void showDriverMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== DRIVERS ===");
            System.out.println("[1] View All Drivers");
            System.out.println("[2] Search Drivers");
            System.out.println("[3] Add Driver");
            System.out.println("[4] Edit Driver");
            System.out.println("[5] Delete Driver");
            System.out.println("[0] Back");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewAllDrivers();
                case "2" -> searchDrivers(scanner);
                case "3" -> addDriver(scanner);
                case "4" -> editDriver(scanner);
                case "5" -> deleteDriver(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void viewAllDrivers() {
        List<Driver> drivers = driverService.getAllDrivers();
        if (drivers.isEmpty()) {
            System.out.println("No drivers found.");
            return;
        }
        System.out.println("\n--- All Drivers ---");
        printDriverHeader();
        drivers.forEach(VanDriverMenu::printDriver);
    }

    private static void searchDrivers(Scanner scanner) {
        System.out.print("Search keyword: ");
        String keyword = scanner.nextLine().trim();
        List<Driver> results = driverService.searchDriver(keyword);
        if (results.isEmpty()) {
            System.out.println("No drivers found.");
            return;
        }
        printDriverHeader();
        results.forEach(VanDriverMenu::printDriver);
    }

    private static void addDriver(Scanner scanner) {
        System.out.println("\n--- Add Driver ---");
        System.out.print("License No: ");
        String licenseNo = scanner.nextLine().trim();

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Contact Number: ");
        String contact = scanner.nextLine().trim();

        try {
            driverService.addDriver(licenseNo, name, contact, "Available");
            System.out.println("Driver added successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void editDriver(Scanner scanner) throws NumberFormatException {
        System.out.print("Enter Driver ID to edit: ");
        try {
            int driverId = parseId(scanner.nextLine().trim(), "DRV");
            Driver existing = driverService.getDriverById(driverId);
            System.out.println("Editing: " + existing.getFormattedId() + " - " + existing.getName());
            System.out.println("(Press Enter to keep current value)");

            System.out.print("License No [" + existing.getLicenseNo() + "]: ");
            String licenseNo = scanner.nextLine().trim();

            System.out.print("Name [" + existing.getName() + "]: ");
            String name = scanner.nextLine().trim();

            System.out.print("Contact Number [" + existing.getContactNumber() + "]: ");
            String contact = scanner.nextLine().trim();

            driverService.updateDriver(driverId,
                licenseNo.isEmpty() ? null : licenseNo,
                name.isEmpty() ? null : name,
                contact.isEmpty() ? null : contact
            );
            System.out.println("Driver updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteDriver(Scanner scanner) throws NumberFormatException {
        System.out.print("Enter Driver ID to delete: ");
        try {
            int driverId = parseId(scanner.nextLine().trim(), "DRV");
            driverService.getDriverById(driverId); // throws if not found
            System.out.print("Are you sure? (y/n): ");
            if ("y".equalsIgnoreCase(scanner.nextLine().trim())) {
                driverService.deleteDriver(driverId);
                System.out.println("Driver deleted.");
            } else {
                System.out.println("Cancelled.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printDriverHeader() {
        System.out.printf("%-8s %-15s %-25s %-15s %-15s%n",
            "ID", "License No", "Name", "Contact", "Status");
        System.out.println("-".repeat(80));
    }

    private static void printDriver(Driver d) {
        System.out.printf("%-8s %-15s %-25s %-15s %-15s%n",
            d.getFormattedId(), d.getLicenseNo(), d.getName(),
            d.getContactNumber(), d.getDriverStatus());
    }

    private static int parseId(String input, String prefix) {
        String stripped = input.trim().toUpperCase().startsWith(prefix)
            ? input.trim().substring(prefix.length())
            : input.trim();
        return Integer.parseInt(stripped);
    }
}