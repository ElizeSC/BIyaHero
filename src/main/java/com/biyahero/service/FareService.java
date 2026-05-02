package com.biyahero.service;

public class FareService {

    private static final double DISCOUNT_RATE = 0.20;

    // 🔥 THE FIX: Make sure this method expects 4 arguments!
    // 1: baseFare, 2: perStopRate, 3: totalStops, 4: passengerType
    public double calculateFare(double baseFare, double perStopRate, int totalStops, String passengerType) {

        int extraStops = totalStops > 1 ? totalStops - 1 : 0;

        // Multiply by the dynamic rate passed from the database!
        double rawFare = baseFare + (extraStops * perStopRate);

        if (isDiscounted(passengerType)) {
            double discountAmount = rawFare * DISCOUNT_RATE;
            return rawFare - discountAmount;
        }

        return rawFare;
    }

    private boolean isDiscounted(String type) {
        if (type == null) return false;
        String t = type.toUpperCase();
        return t.equals("STUDENT") || t.equals("SENIOR") || t.equals("PWD");
    }
}