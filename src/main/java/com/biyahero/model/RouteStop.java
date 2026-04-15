package com.biyahero.model;

public class RouteStop {
    private int routeId;
    private int stopId;
    private int stopOrder;
    private double distanceFromPrev;

    public RouteStop() {}

    public RouteStop(int routeId, int stopId, int stopOrder, double distanceFromPrev) {
        this.routeId = routeId;
        this.stopId = stopId;
        this.stopOrder = stopOrder;
        this.distanceFromPrev = distanceFromPrev;
    }

    // Getters and Setters
    public int getRouteId() { 
        return routeId; 
    }
    public void setRouteId(int routeId) { 
        this.routeId = routeId; 
    }

    public int getStopId() { 
        return stopId; 
    }
    public void setStopId(int stopId) { 
        this.stopId = stopId; 
    }

    public int getStopOrder() { 
        return stopOrder; 
    }
    public void setStopOrder(int stopOrder) { 
        this.stopOrder = stopOrder; 
    }

    public double getDistanceFromPrev() { 
        return distanceFromPrev; 
    }
    public void setDistanceFromPrev(double distanceFromPrev) { 
        this.distanceFromPrev = distanceFromPrev; 
    }

    @Override
    public String toString() {
        return "RouteStop{" +
                "routeId=" + routeId +
                ", stopId=" + stopId +
                ", stopOrder=" + stopOrder +
                ", distanceFromPrev=" + distanceFromPrev +
                '}';
    }
}