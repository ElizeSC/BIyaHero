package com.biyahero.model;

public class Stop {
    private int stopId;
    private String stopName;
    private String cityProvince;

    public Stop() {}

    public Stop(int stopId, String stopName, String cityProvince) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.cityProvince = cityProvince;
    }

    // For INSERT (no stopId yet, auto-incremented by DB)
    public Stop(String stopName, String cityProvince) {
        this.stopName = stopName;
        this.cityProvince = cityProvince;
    }

    // Getters and Setters
    public int getStopId() { 
        return stopId; 
    }
    public void setStopId(int stopId) { 
        this.stopId = stopId; 
    }

    public String getStopName() { 
        return stopName; 
    }
    public void setStopName(String stopName) { 
        this.stopName = stopName; 
    }

    public String getCityProvince() { 
        return cityProvince; 
    }
    public void setCityProvince(String cityProvince) { 
        this.cityProvince = cityProvince; 
    }

    public String getFormattedId() { 
        return String.format("STP%03d", stopId); 
    }

    @Override
    public String toString() {
        return "Stop{" +
                "stopId=" + stopId +
                ", stopName='" + stopName + '\'' +
                ", cityProvince='" + cityProvince + '\'' +
                '}';
    }
}