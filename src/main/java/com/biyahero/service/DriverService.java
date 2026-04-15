package com.biyahero.service;

import com.biyahero.dao.DriverDAO;
import com.biyahero.dao.impl.DriverDAOImpl;
import com.biyahero.model.Driver;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DriverService {
    private final DriverDAO driverDAO = new DriverDAOImpl();

    public void addDriver(String licenseNo, String name, String contactNumber) {
        // validation before hitting the DAO
        if (licenseNo == null || licenseNo.trim().isEmpty()) {
            throw new IllegalArgumentException("License No cannot be empty.");
        }

        Driver driver = new Driver(licenseNo.trim().toUpperCase(), name, contactNumber);
        driverDAO.addDriver(driver);
    }

    public List<Driver> getAllDrivers() {
        return driverDAO.getAllDrivers();
    }

    public Driver getDriverById(int driverId) {
        Driver driver = driverDAO.getDriverById(driverId);
        if (driver == null) {
            throw new IllegalArgumentException("Driver not found with ID: " + driverId);
        }
        return driver;
    }

    public void updateDriver(int driverId, String licenseNo, String name, String contactNumber) {
        Driver existingDriver = getDriverById(driverId);

        if (licenseNo != null && !licenseNo.trim().isEmpty()) {
            existingDriver.setLicenseNo(licenseNo.trim().toUpperCase());
        }

        if (name != null && !name.trim().isEmpty()) {
            existingDriver.setName(name);
        }

        if (contactNumber != null && !contactNumber.trim().isEmpty()) {
            existingDriver.setContactNumber(contactNumber);
        }

        driverDAO.updateDriver(existingDriver);
    }   

    public void deleteDriver(int driverId) {
        driverDAO.deleteDriver(driverId);
    }

    // search, sort
    public List<Driver> searchDriver(String keyword) {
        return driverDAO.getAllDrivers().stream()
            .filter(v -> 
                (v.getLicenseNo() != null && v.getLicenseNo().toLowerCase().contains(keyword.toLowerCase())) ||
                (v.getName() != null && v.getName().toLowerCase().contains(keyword.toLowerCase())) ||
                String.valueOf(v.getFormattedId()).contains(keyword)
            )
            .collect(Collectors.toList());
    }

    public List<Driver> sortByDriverId(List<Driver> drivers) {
        drivers.sort(Comparator.comparingInt(Driver::getDriverId));
        return drivers;
    }

    public List<Driver> sortByLicenseNo(List<Driver> drivers) {
        drivers.sort(Comparator.comparing(Driver::getLicenseNo));
        return drivers;
    }
}