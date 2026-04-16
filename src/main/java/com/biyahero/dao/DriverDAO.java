package com.biyahero.dao;
import com.biyahero.model.Driver;
import java.util.List;

public interface DriverDAO {
    void addDriver(Driver driver);
    Driver getDriverById(int id);
    Driver getDriverByName(String name);
    Driver getDriverByLicenseNo(String licenseNo);
    void updateDriver(Driver driver);
    void updateDriverStatus(int id, String status); // e.g., 'Available', 'On Trip', 'Off Duty'
    List<Driver> getAllDrivers();
    void deleteDriver(int id);
}