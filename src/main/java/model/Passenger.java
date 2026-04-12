package model;

public class Passenger {
    private int passengerId;
    private String name;
    private String contactNumber;
    private String address;

    public Passenger() {}

    public Passenger(int passengerId, String name, String contactNumber, String address) {
        this.passengerId = passengerId;
        this.name = name;
        this.contactNumber = contactNumber;
        this.address = address;
    }

    // For INSERT (no passengerId yet, auto-incremented by DB)
    public Passenger(String name, String contactNumber, String address) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.address = address;
    }

    // Getters and Setters
    public int getPassengerId() { 
        return passengerId; 
    }
    public void setPassengerId(int passengerId) { 
        this.passengerId = passengerId; 
    }

    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public String getContactNumber() { 
        return contactNumber; 
    }
    public void setContactNumber(String contactNumber) { 
        this.contactNumber = contactNumber; 
    }

    public String getAddress() { 
        return address; 
    }
    public void setAddress(String address) { 
        this.address = address; 
    }

    public String getFormattedId() { 
        return String.format("PSG%03d", passengerId); 
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "passengerId=" + passengerId +
                ", name='" + name + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}