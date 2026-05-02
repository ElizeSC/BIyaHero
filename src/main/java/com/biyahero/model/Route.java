package com.biyahero.model;

public class Route {
    private int routeId;
    private String routeName;
    private double baseFare;
    private double perStopFare;

    public Route() {}

    public Route(int routeId, String routeName, double baseFare) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.baseFare = baseFare;
        this.perStopFare = 15.00; // Default fallback for old code
    }



    public Route(int routeId, String routeName, double baseFare, double perStopFare) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.baseFare = baseFare;
        this.perStopFare = perStopFare;
    }

    // For INSERT (no routeId yet, auto-incremented by DB)
    public Route(String routeName, double baseFare) {
        this.routeName = routeName;
        this.baseFare = baseFare;
    }

    // Getters and Setters
    public int getRouteId() { 
        return routeId; 
    }
    public void setRouteId(int routeId) { 
        this.routeId = routeId; 
    }

    public String getRouteName() { 
        return routeName; 
    }
    public void setRouteName(String routeName) { 
        this.routeName = routeName; 
    }

    public double getBaseFare() { 
        return baseFare; 
    }
    public void setBaseFare(double baseFare) { 
        this.baseFare = baseFare; 
    }

    public String getFormattedId() { 
        return String.format("RTE%03d", routeId); 
    }
    public double getPerStopFare() { return perStopFare; }
    public void setPerStopFare(double perStopFare) { this.perStopFare = perStopFare; }

    @Override
    public String toString() {
        return "Route{" +
                "routeId=" + routeId +
                ", routeName='" + routeName + '\'' +
                ", baseFare=" + baseFare +
                '}';
    }
}