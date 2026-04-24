package com.biyahero.cli;

import com.biyahero.model.Booking;
import com.biyahero.model.Passenger;
import com.biyahero.model.Stop;
import com.biyahero.model.Trip;
import com.biyahero.service.BookingService;
import com.biyahero.service.TripService;

import java.util.List;
import java.util.Scanner;

public class BookingMenu {
    private static final BookingService bookingService = new BookingService();
    private static final TripService tripService = new TripService();

    public static void show(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== BOOKINGS ===");
            System.out.println("[1] View Seat Grid for a Trip");
            System.out.println("[2] Book a Seat");
            System.out.println("[3] Cancel Booking");
            System.out.println("[4] Vacate Seat (Passenger Reached Destination)");
            System.out.println("[5] Assign Walk-In Passenger");
            System.out.println("[6] View Passenger Manifest");
            System.out.println("[0] Back");
            System.out.print("Select: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewSeatGrid(scanner);
                case "2" -> bookSeat(scanner, false);
                case "3" -> cancelBooking(scanner);
                case "4" -> vacateSeat(scanner);
                case "5" -> bookSeat(scanner, true);
                case "6" -> viewPassengerManifest(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // SEAT GRID 

    private static void viewSeatGrid(Scanner scanner) {
        System.out.print("Enter Trip ID: ");
        try {
            int tripId = parseId(scanner.nextLine().trim(), "TRP");
            Trip trip = tripService.getTripById(tripId);

            if ("Completed".equals(trip.getTripStatus()) || "Cancelled".equals(trip.getTripStatus())) {
                System.out.println("Cannot view seat grid for a " + trip.getTripStatus() + " trip.");
                return;
            }

            printSeatGrid(tripId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printSeatGrid(int tripId) {
        List<Integer> occupied = bookingService.getOccupiedSeats(tripId);
        List<Integer> available = bookingService.getAvailableSeats(tripId);

        System.out.println("\n--- Seat Grid (Trip " + tripId + ") ---");
        System.out.println("[X] = Taken   [O] = Available");
        System.out.println();

        // 3 columns layout
        for (int seat = 1; seat <= 15; seat++) {
            String label = occupied.contains(seat) ? "[X]" : "[O]";
            System.out.printf("Seat %2d %s   ", seat, label);
            if (seat % 3 == 0) System.out.println();
        }
        System.out.println();
        System.out.println("Available: " + available.size() + " | Occupied: " + occupied.size());
    }

    // BOOK A SEAT
    private static void bookSeat(Scanner scanner, boolean isWalkIn) {
        System.out.println(isWalkIn ? "\n--- Assign Walk-In Passenger ---" : "\n--- Book a Seat ---");

        System.out.print("Enter Trip ID: ");
        int tripId;
        try {
            tripId = parseId(scanner.nextLine().trim(), "TRP");
            Trip trip = tripService.getTripById(tripId);

            if ("Completed".equals(trip.getTripStatus()) || "Cancelled".equals(trip.getTripStatus())) {
                System.out.println("Cannot book a seat on a " + trip.getTripStatus() + " trip.");
                return;
            }

            // walk-in guard
            if (isWalkIn && !"En Route".equals(trip.getTripStatus())) {
                System.out.println("Walk-in assignment only allowed for En Route trips.");
                return;
            }

            // show seat grid first
            printSeatGrid(tripId);

            // check if any seats available
            if (bookingService.getAvailableSeats(tripId).isEmpty()) {
                System.out.println("No available seats for this trip.");
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        // seat selection
        System.out.print("Select Seat Number (1-15): ");
        int seatNumber;
        try {
            seatNumber = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid seat number.");
            return;
        }

        // show stops for pickup/dropoff selection
        List<Stop> stops = tripService.getStopsForTrip(tripId);
        if (stops.isEmpty()) {
            System.out.println("No stops found for this trip's route.");
            return;
        }

        System.out.println("\nStops:");
        for (int i = 0; i < stops.size(); i++) {
            Stop s = stops.get(i);
            System.out.printf("[%d] %s, %s%n", i + 1, s.getStopName(), s.getCityProvince());
        }

        int pickupStopId;
        int dropoffStopId;
        try {
            System.out.print("Select Pickup Stop: ");
            int pickupChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (pickupChoice < 0 || pickupChoice >= stops.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            pickupStopId = stops.get(pickupChoice).getStopId();

            System.out.print("Select Dropoff Stop: ");
            int dropoffChoice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (dropoffChoice < 0 || dropoffChoice >= stops.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            if (dropoffChoice <= pickupChoice) {
                System.out.println("Dropoff must be after pickup stop.");
                return;
            }
            dropoffStopId = stops.get(dropoffChoice).getStopId();

        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }

        // fare
        System.out.print("Fare Paid: ");
        double farePaid;
        try {
            farePaid = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid fare.");
            return;
        }

        // passenger details
        System.out.print("Passenger Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Contact Number (optional, press Enter to skip): ");
        String contact = scanner.nextLine().trim();
        if (contact.isEmpty()) contact = null;

        System.out.print("Address (optional, press Enter to skip): ");
        String address = scanner.nextLine().trim();
        if (address.isEmpty()) address = null;

        // create booking
        try {
            Booking booking = isWalkIn
                ? bookingService.assignWalkInPassenger(tripId, seatNumber, pickupStopId, dropoffStopId, farePaid, name, contact, address)
                : bookingService.createBooking(tripId, seatNumber, pickupStopId, dropoffStopId, farePaid, name, contact, address);

            System.out.println("Booking created successfully. Booking ID: " + booking.getFormattedId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // CANCEL BOOKING

    private static void cancelBooking(Scanner scanner) {
        System.out.print("Enter Trip ID to view bookings: ");
        try {
            int tripId = parseId(scanner.nextLine().trim(), "TRP");
            List<Booking> bookings = bookingService.getBookingsByTrip(tripId);

            if (bookings.isEmpty()) {
                System.out.println("No bookings found for this trip.");
                return;
            }

            printBookingHeader();
            bookings.forEach(BookingMenu::printBooking);

            System.out.print("\nEnter Booking ID to cancel: ");
            int bookingId = parseId(scanner.nextLine().trim(), "BKG");

            System.out.print("Are you sure? (y/n): ");
            if ("y".equalsIgnoreCase(scanner.nextLine().trim())) {
                bookingService.cancelBooking(bookingId);
                System.out.println("Booking cancelled.");
            } else {
                System.out.println("Cancelled.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // VACATE SEAT 

    private static void vacateSeat(Scanner scanner) {
        System.out.print("Enter Trip ID to view bookings: ");
        try {
            int tripId = parseId(scanner.nextLine().trim(), "TRP");
            List<Booking> bookings = bookingService.getBookingsByTrip(tripId);

            // only show reserved bookings
            List<Booking> reserved = bookings.stream()
                .filter(b -> "Reserved".equals(b.getBookingStatus()))
                .toList();

            if (reserved.isEmpty()) {
                System.out.println("No active bookings found for this trip.");
                return;
            }

            printBookingHeader();
            reserved.forEach(BookingMenu::printBooking);

            System.out.print("\nEnter Booking ID to vacate: ");
            int bookingId = parseId(scanner.nextLine().trim(), "BKG");
            bookingService.vacateSeat(bookingId);
            System.out.println("Seat vacated. Seat is now available.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // PASSENGER MANIFEST 

    private static void viewPassengerManifest(Scanner scanner) {
        System.out.print("Enter Trip ID: ");
        try {
            int tripId = parseId(scanner.nextLine().trim(), "TRP");
            tripService.getTripById(tripId); // throws if not found

            List<Passenger> passengers = bookingService.getPassengersByTrip(tripId);
            if (passengers.isEmpty()) {
                System.out.println("No passengers found for this trip.");
                return;
            }

            System.out.println("\n--- Passenger Manifest (Trip " + tripId + ") ---");
            System.out.printf("%-8s %-25s %-15s %-30s%n", "No.", "Name", "Contact", "Address");
            System.out.println("-".repeat(80));
            for (int i = 0; i < passengers.size(); i++) {
                Passenger p = passengers.get(i);
                System.out.printf("%-8d %-25s %-15s %-30s%n",
                    i + 1,
                    p.getName(),
                    p.getContactNumber() != null ? p.getContactNumber() : "-",
                    p.getAddress() != null ? p.getAddress() : "-"
                );
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // DISPLAY HELPERS

    private static void printBookingHeader() {
        System.out.printf("%-10s %-10s %-8s %-12s %-12s %-10s %-12s%n",
            "Booking ID", "Passenger", "Seat", "Pickup", "Dropoff", "Fare", "Status");
        System.out.println("-".repeat(76));
    }

    private static void printBooking(Booking b) {
        System.out.printf("%-10s %-10d %-8d %-12d %-12d %-10.2f %-12s%n",
            b.getFormattedId(),
            b.getPassengerId(),
            b.getSeatNumber(),
            b.getPickupStopId(),
            b.getDropoffStopId(),
            b.getFarePaid(),
            b.getBookingStatus()
        );
    }

    private static int parseId(String input, String prefix) {
        String stripped = input.trim().toUpperCase().startsWith(prefix)
            ? input.trim().substring(prefix.length())
            : input.trim();
        return Integer.parseInt(stripped);
    }
}