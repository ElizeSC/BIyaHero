package com.biyahero.model;

public class Van {
    private int vanId;
    private String plateNumber;
    private String model;
    private int capacity;
    private String vanStatus;

    public Van() {}

    public Van(int vanId, String plateNumber, String model, int capacity, String vanStatus) {
        this.vanId = vanId;
        this.plateNumber = plateNumber;
        this.model = model;
        this.capacity = capacity;
        this.vanStatus = vanStatus;
    }

    // For INSERT (no vanId yet, auto-incremented by DB)
    public Van(String plateNumber, String model, int capacity, String vanStatus) {
        this.plateNumber = plateNumber;
        this.model = model;
        this.capacity = capacity;
        this.vanStatus = vanStatus;
    }

    // Getters and Setters
    public int getVanId() { 
        return vanId; 
    }
    public void setVanId(int vanId) { 
        this.vanId = vanId; 
    }

    public String getPlateNumber() { 
        return plateNumber; 
    }
    public void setPlateNumber(String plateNumber) { 
        this.plateNumber = plateNumber; 
    }

    public String getModel() { 
        return model; 
    }
    public void setModel(String model) { 
        this.model = model; 
    }

    public int getCapacity() { 
        return capacity; 
    }
    public void setCapacity(int capacity) { 
        this.capacity = capacity; 
    }

    public String getVanStatus() { 
        return vanStatus; 
    }
    public void setVanStatus(String vanStatus) { 
        this.vanStatus = vanStatus; 
    }

    public String getFormattedId() { 
        return String.format("VAN%03d", vanId); 
    }

    @Override
    public String toString() {
        return "Van{" +
                "vanId=" + vanId +
                ", plateNumber='" + plateNumber + '\'' +
                ", com.biyahero.model='" + model + '\'' +
                ", capacity=" + capacity +
                ", vanStatus='" + vanStatus + '\'' +
                '}';
    }
}