package com.biyahero;

import java.util.Scanner;
import com.biyahero.util.DBUtil;

public class CLIApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== BIYAHERO SYSTEM LOGIN ===");
        System.out.print("Enter Database Username: ");
        String inputUser = scanner.nextLine();

        System.out.print("Enter Database Password: ");
        String inputPass = scanner.nextLine();

        // Pass these credentials to your utility to see if they work
        if (DBUtil.testConnection(inputUser, inputPass)) {
            System.out.println("\n✅ ACCESS GRANTED.");
            System.out.println("Welcome, Dispatcher. System initialized.");
        } else {
            System.out.println("\n❌ ACCESS DENIED.");
            System.out.println("Invalid Administrative Credentials.");
        }
    }
}