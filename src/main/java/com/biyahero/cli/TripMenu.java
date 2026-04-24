package com.biyahero.cli;

import com.biyahero.model.Driver;
import com.biyahero.model.Route;
import com.biyahero.model.Stop;
import com.biyahero.model.Trip;
import com.biyahero.model.Van;
import com.biyahero.service.RouteService;
import com.biyahero.service.TripService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class TripMenu {
    private static final TripService tripService = new TripService();
    private static final RouteService routeService = new RouteService();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void show(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== TRIPS ===");
            System.out.println("[1] Scheduled Trips");
            System.out.println("[2] Active Trips (En Route)");
            System.out.println("[3] Create Trip");
            System.out.println("[4] Edit Trip");
            System.out.println("[5] Cancel Trip");
            System.out.println("[0] Back");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> showScheduledTrips(scanner);
                case "2" -> showActiveTrips(scanner);
                case "3" -> createTrip(scanner);
                case "4" -> editTrip(scanner);
                case "5" -> cancelTrip(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // SCHEDULED TRIPS

    private static void showScheduledTrips(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== SCHEDULED TRIPS ===");
            System.out.println("[1] View All");
            System.out.println("[2] Search by Trip ID");
            System.out.println("[3] Start Trip");
            System.out.println("[0] Back");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> {
                    List<Trip> trips = tripService.getScheduledTrips();
                    if (trips.isEmpty()) {
                        System.out.println("No scheduled trips.");
                    } else {
                        printTripHeader();
                        trips.forEach(TripMenu::printTrip);
                    }
                }
                case "2" -> {
                    System.out.print("Search Trip ID: ");
                    String keyword = scanner.nextLine().trim();
                    List<Trip> results = tripService.searchScheduledTrips(keyword);
                    if (results.isEmpty()) {
                        System.out.println("No scheduled trips found.");
                    } else {
                        printTripHeader();
                        results.forEach(TripMenu::printTrip);
                    }
                }
                case "3" -> startTrip(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void startTrip(Scanner scanner) {
        System.out.print("Enter Trip ID to start: ");
        try {
            int tripId = parseId(scanner.nextLine().trim(), "TRP");
            tripService.startTrip(tripId);
            System.out.println("Trip started. Status is now En Route.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ACTIVE TRIPS

    private static void showActiveTrips(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== ACTIVE TRIPS (EN ROUTE) ===");
            System.out.println("[1] View All");
            System.out.println("[2] Search by Trip ID");
            System.out.println("[3] Update Checkpoint");
            System.out.println("[4] View Current Checkpoint");
            System.out.println("[0] Back");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> {
                    List<Trip> trips = tripService.getActiveTrips();
                    if (trips.isEmpty()) {
                        System.out.println("No active trips.");
                    } else {
                        printTripHeader();
                        trips.forEach(TripMenu::printTrip);
                    }
                }
                case "2" -> {
                    System.out.print("Search Trip ID: ");
                    String keyword = scanner.nextLine().trim();
                    List<Trip> results = tripService.searchActiveTrips(keyword);
                    if (results.isEmpty()) {
                        System.out.println("No active trips found.");
                    } else {
                        printTripHeader();
                        results.forEach(TripMenu::printTrip);
                    }
                }
                case "3" -> updateCheckpoint(scanner);
                case "4" -> viewCurrentCheckpoint(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void updateCheckpoint(Scanner scanner) {
        System.out.print("Enter Trip ID: ");
        try {
            int tripId = parseId(scanner.nextLine().trim(), "TRP");

            // show stops for this trip's route
            List<Stop> stops = tripService.getStopsForTrip(tripId);
            if (stops.isEmpty()) {
                System.out.println("No stops found for this trip's route.");
                return;
            }

            System.out.println("\nStops along this route:");
            for (int i = 0; i < stops.size(); i++) {
                Stop s = stops.get(i);
                System.out.printf("[%d] %s, %s%n", i + 1, s.getStopName(), s.getCityProvince());
            }

            System.out.print("Select stop number: ");
            int choice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (choice < 0 || choice >= stops.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            int stopId = stops.get(choice).getStopId();
            tripService.updateCurrentStop(tripId, stopId);

            // check if trip was auto-completed
            Trip updated = tripService.getTripById(tripId);
            if ("Completed".equals(updated.getTripStatus())) {
                System.out.println("Last stop reached. Trip automatically marked as Completed.");
            } else {
                System.out.println("Checkpoint updated: " + stops.get(choice).getStopName());
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewCurrentCheckpoint(Scanner scanner) {
        System.out.print("Enter Trip ID: ");
        try {
            int tripId = parseId(scanner.nextLine().trim(), "TRP");
            Stop currentStop = tripService.getCurrentStop(tripId);
            if (currentStop == null) {
                System.out.println("Trip has not reached any checkpoint yet.");
            } else {
                System.out.println("Current checkpoint: " + currentStop.getStopName() + ", " + currentStop.getCityProvince());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // CREATE TRIP

    private static void createTrip(Scanner scanner) {
        System.out.println("\n--- Create Trip ---");

        // show available routes
        List<Route> routes = routeService.getAllRoutes();
        if (routes.isEmpty()) {
            System.out.println("No routes available.");
            return;
        }
        System.out.println("\nAvailable Routes:");
        for (int i = 0; i < routes.size(); i++) {
            Route r = routes.get(i);
            System.out.printf("[%d] %s (Base Fare: %.2f)%n", i + 1, r.getRouteName(), r.getBaseFare());
        }
        System.out.print("Select route: ");
        int routeChoice;
        try {
            routeChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (routeChoice < 0 || routeChoice >= routes.size()) {
                System.out.println("Invalid selection.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }
        int routeId = routes.get(routeChoice).getRouteId();

        // show available vans
        List<Van> vans = tripService.getAvailableVans();
        if (vans.isEmpty()) {
            System.out.println("No available vans.");
            return;
        }
        System.out.println("\nAvailable Vans:");
        for (int i = 0; i < vans.size(); i++) {
            Van v = vans.get(i);
            System.out.printf("[%d] %s - %s (Capacity: %d)%n", i + 1, v.getFormattedId(), v.getPlateNumber(), v.getCapacity());
        }
        System.out.print("Select van: ");
        int vanChoice;
        try {
            vanChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (vanChoice < 0 || vanChoice >= vans.size()) {
                System.out.println("Invalid selection.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }
        int vanId = vans.get(vanChoice).getVanId();

        // show available drivers
        List<Driver> drivers = tripService.getAvailableDrivers();
        if (drivers.isEmpty()) {
            System.out.println("No available drivers.");
            return;
        }
        System.out.println("\nAvailable Drivers:");
        for (int i = 0; i < drivers.size(); i++) {
            Driver d = drivers.get(i);
            System.out.printf("[%d] %s - %s (%s)%n", i + 1, d.getFormattedId(), d.getName(), d.getLicenseNo());
        }
        System.out.print("Select driver: ");
        int driverChoice;
        try {
            driverChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (driverChoice < 0 || driverChoice >= drivers.size()) {
                System.out.println("Invalid selection.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }
        int driverId = drivers.get(driverChoice).getDriverId();

        // departure time
        System.out.print("Departure Time (yyyy-MM-dd HH:mm): ");
        try {
            LocalDateTime departureTime = LocalDateTime.parse(scanner.nextLine().trim(), FORMATTER);
            tripService.createTrip(routeId, vanId, driverId, departureTime);
            System.out.println("Trip created successfully.");
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use yyyy-MM-dd HH:mm.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // EDIT TRIP

    private static void editTrip(Scanner scanner) {
        System.out.print("Enter Trip ID to edit: ");
        try {
            int tripId = parseId(scanner.nextLine().trim(), "TRP");
            Trip existing = tripService.getTripById(tripId);

            System.out.println("Editing: " + existing.getFormattedId());
            System.out.println("(Press Enter to keep current value)");

            // departure time
            System.out.print("Departure Time [" + existing.getDepartureTime().format(FORMATTER) + "]: ");
            String dtInput = scanner.nextLine().trim();
            LocalDateTime departureTime = null;
            if (!dtInput.isEmpty()) {
                try {
                    departureTime = LocalDateTime.parse(dtInput, FORMATTER);
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Keeping current.");
                }
            }

            tripService.updateTrip(tripId, null, null, null, departureTime, null, null);
            System.out.println("Trip updated successfully.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // CANCEL TRIP 

    private static void cancelTrip(Scanner scanner) {
        System.out.print("Enter Trip ID to cancel: ");
        try {
            int tripId = parseId(scanner.nextLine().trim(), "TRP");
            Trip trip = tripService.getTripById(tripId);
            System.out.println("Trip: " + trip.getFormattedId() + " | Status: " + trip.getTripStatus());
            System.out.print("Are you sure you want to cancel this trip? (y/n): ");
            if ("y".equalsIgnoreCase(scanner.nextLine().trim())) {
                tripService.cancelTrip(tripId);
                System.out.println("Trip cancelled.");
            } else {
                System.out.println("Cancelled.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // DISPLAY HELPERS AND UTILITY METHODS

    private static void printTripHeader() {
        System.out.printf("%-8s %-10s %-10s %-10s %-18s %-12s%n",
            "ID", "Route ID", "Van ID", "Driver ID", "Departure", "Status");
        System.out.println("-".repeat(70));
    }

    private static void printTrip(Trip t) {
        System.out.printf("%-8s %-10d %-10d %-10d %-18s %-12s%n",
            t.getFormattedId(),
            t.getRouteId(),
            t.getVanId(),
            t.getDriverId(),
            t.getDepartureTime().format(FORMATTER),
            t.getTripStatus()
        );
    }

    private static int parseId(String input, String prefix) {
        String stripped = input.trim().toUpperCase().startsWith(prefix)
            ? input.trim().substring(prefix.length())
            : input.trim();
        return Integer.parseInt(stripped);
    }
}