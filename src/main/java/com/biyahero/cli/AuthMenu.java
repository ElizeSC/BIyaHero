package com.biyahero.cli;

import com.biyahero.service.AuthService;

import java.util.Scanner;

public class AuthMenu {
    private final Scanner scanner;
    AuthService authService = new AuthService();

    public AuthMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public boolean show() {
        System.out.println("       Welcome to BiyaHero      ");
        System.out.println();

        int attempts = 3;
        while (attempts > 0) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            // use Console for masked password input
            String password;
            java.io.Console console = System.console();
            if (console != null) {
                char[] passwordChars = console.readPassword("Password: ");
                password = new String(passwordChars);
                java.util.Arrays.fill(passwordChars, ' '); // clear from memory
            } else {
                // fallback if console unavailable (e.g. running inside IDE)
                System.out.print("Password (not masked - run from terminal for masking): ");
                password = scanner.nextLine().trim();
            }
// 1. Create an instance of the service
            AuthService authService = new AuthService();

// 2. Call the method and store the result
            String dbName = authService.authenticate(username, password);

// 3. Check if the result is NOT null (meaning login was successful)
            if (dbName != null) {
                // Switch the DB context so the CLI works with that user's data!
                com.biyahero.util.DBUtil.setDatabase(dbName);

                System.out.println("\nLogin successful. Welcome, " + username + "!");
                return true;
            } else {
                attempts--;
                if (attempts > 0) {
                    System.out.println("Invalid credentials. " + attempts + " attempt(s) remaining.\n");
                } else {
                    System.out.println("Too many failed attempts. Exiting.");
                }
            }
        }
        return false;
    }
}