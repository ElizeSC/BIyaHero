package model;

public class Driver {
    private int driverId;
    private String licenseNo;
    private String name;
    private String contactNumber;

    public Driver() {}

    public Driver(int driverId, String licenseNo, String name, String contactNumber) {
        this.driverId = driverId;
        this.licenseNo = licenseNo;
        this.name = name;
        this.contactNumber = contactNumber;
    }

    // For INSERT (no driverId yet, auto-incremented by DB)
    public Driver(String licenseNo, String name, String contactNumber) {
        this.licenseNo = licenseNo;
        this.name = name;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters
    public int getDriverId() { 
        return driverId; 
    }
    public void setDriverId(int driverId) { 
        this.driverId = driverId; 
    }

    public String getLicenseNo() { 
        return licenseNo; 
    }
    public void setLicenseNo(String licenseNo) { 
        this.licenseNo = licenseNo; 
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

    public String getFormattedId() { 
        return String.format("DRV%03d", driverId); 
    }

    @Override
    public String toString() {
        return "Driver{" +
                "driverId=" + driverId +
                ", licenseNo='" + licenseNo + '\'' +
                ", name='" + name + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                '}';
    }
}