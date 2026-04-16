package com.biyahero.model;

import java.time.LocalDateTime;

public class Trip {
    private int tripId;
    private int routeId;
    private int vanId;
    private int driverId;
    private LocalDateTime departureTime;
    private String tripStatus;
    private LocalDateTime arrivalDt;
    private Integer currentStopId; // nullable, boxed

    public Trip() {}

    public Trip(int tripId, int routeId, int vanId, int driverId, LocalDateTime departureTime, 
                String tripStatus, LocalDateTime arrivalDt, Integer currentStopId) {
        this.tripId = tripId;
        this.routeId = routeId;
        this.vanId = vanId;
        this.driverId = driverId;
        this.departureTime = departureTime;
        this.tripStatus = tripStatus;
        this.arrivalDt = arrivalDt;
        this.currentStopId = currentStopId;
    }

    // For INSERT (no tripId yet, auto-incremented by DB)
    public Trip(int routeId, int vanId, int driverId, LocalDateTime departureTime, 
                String tripStatus, LocalDateTime arrivalDt, Integer currentStopId) {
        this.routeId = routeId;
        this.vanId = vanId;
        this.driverId = driverId;
        this.departureTime = departureTime;
        this.tripStatus = tripStatus;
        this.arrivalDt = arrivalDt;
        this.currentStopId = currentStopId;
    }

    // Getters and Setters
    public int getTripId() { 
        return tripId; 
    }
    public void setTripId(int tripId) { 
        this.tripId = tripId; 
    }

    public int getRouteId() { 
        return routeId; 
    }
    public void setRouteId(int routeId) { 
        this.routeId = routeId; 
    }

    public int getVanId() { 
        return vanId; 
    }
    public void setVanId(int vanId) { 
        this.vanId = vanId; 
    }

    public int getDriverId() { 
        return driverId; 
    }
    public void setDriverId(int driverId) { 
        this.driverId = driverId; 
    }

    public LocalDateTime getDepartureTime() { 
        return departureTime; 
    }
    public void setDepartureTime(LocalDateTime departureTime) { 
        this.departureTime = departureTime; 
    }

    public String getTripStatus() { 
        return tripStatus; 
    }
    public void setTripStatus(String tripStatus) { 
        this.tripStatus = tripStatus; 
    }

    public LocalDateTime getArrivalDt() { 
        return arrivalDt; 
    }
    public void setArrivalDt(LocalDateTime arrivalDt) { 
        this.arrivalDt = arrivalDt; 
    }

    public Integer getCurrentStopId() { 
        return currentStopId; 
    }
    public void setCurrentStopId(Integer currentStopId) { 
        this.currentStopId = currentStopId; 
    }

    public String getFormattedId() { 
        return String.format("TRP%03d", tripId); 
    }

    @Override
    public String toString() {
        return "Trip{" +
                "tripId=" + tripId +
                ", routeId=" + routeId +
                ", vanId=" + vanId +
                ", driverId=" + driverId +
                ", departureTime=" + departureTime +
                ", tripStatus='" + tripStatus + '\'' +
                '}';
    }
}