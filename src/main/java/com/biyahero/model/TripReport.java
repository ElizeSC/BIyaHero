package com.biyahero.model;

import java.time.LocalDateTime;

public class TripReport {
    private int tripId;
    private String formattedTripId;
    private String driverName;
    private String routeName;
    private int bookedSeats;
    private int totalCapacity;
    private double totalRevenue;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalDt;

    public TripReport(int tripId, String formattedTripId, String driverName, String routeName,
                      int bookedSeats, int totalCapacity, double totalRevenue,
                      LocalDateTime departureTime, LocalDateTime arrivalDt) {
        this.tripId = tripId;
        this.formattedTripId = formattedTripId;
        this.driverName = driverName;
        this.routeName = routeName;
        this.bookedSeats = bookedSeats;
        this.totalCapacity = totalCapacity;
        this.totalRevenue = totalRevenue;
        this.departureTime = departureTime;
        this.arrivalDt = arrivalDt;
    }

    // derived field - no need to store, computed on the fly
    public String getOccupancyRate() {
        if (totalCapacity == 0) return "N/A";
        int percent = (int) Math.round((bookedSeats * 100.0) / totalCapacity);
        return bookedSeats + "/" + totalCapacity + " (" + percent + "%)";
    }

    public int getTripId() { 
        return tripId; 
    }

    public String getFormattedTripId() { 
        return formattedTripId; 
    }

    public String getDriverName() { 
        return driverName; 
    }

    public String getRouteName() { 
        return routeName; 
    }

    public int getBookedSeats() { 
        return bookedSeats; 
    }

    public int getTotalCapacity() { 
        return totalCapacity; 
    }

    public double getTotalRevenue() { 
        return totalRevenue; 
    }

    public LocalDateTime getDepartureTime() { 
        return departureTime; 
    }

    public LocalDateTime getArrivalDt() { 
        return arrivalDt; 
    }

    @Override
    public String toString() {
        return "TripReport{" +
                "tripId=" + formattedTripId +
                ", driver=" + driverName +
                ", route=" + routeName +
                ", occupancy=" + getOccupancyRate() +
                ", revenue=" + totalRevenue +
                '}';
    }
}