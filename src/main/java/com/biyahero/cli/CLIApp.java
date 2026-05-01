package com.biyahero.cli;

import java.util.Scanner;

public class CLIApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AuthMenu authMenu = new AuthMenu(scanner);

        if (authMenu.show()) {
            showMainMenu(scanner);
        }

        scanner.close();
    }

    private static void showMainMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("[1] Vans & Drivers");
            System.out.println("[2] Trips");
            System.out.println("[3] Bookings");
            System.out.println("[4] Reports");
            System.out.println("[0] Exit");
            System.out.print("Select: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> VanDriverMenu.show(scanner);
                case "2" -> TripMenu.show(scanner);
                case "3" -> BookingMenu.show(scanner);
                case "4" -> ReportMenu.show(scanner);
                case "0" -> {
                    System.out.println("Exiting...");
                    running = false;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }
}