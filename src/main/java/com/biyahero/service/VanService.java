package com.biyahero.service;

import com.biyahero.dao.VanDAO;
import com.biyahero.dao.impl.VanDAOImpl;
import com.biyahero.model.Van;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class VanService {
    private final VanDAO vanDAO = new VanDAOImpl();

    public void addVan(String plateNumber, String model, int capacity, String status) {
        // validation before hitting the DAO
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Plate number cannot be empty.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        }

        Van van = new Van(plateNumber.trim().toUpperCase(), model, capacity, status);
        vanDAO.addVan(van);
    }

    public List<Van> getAllVans() {
        return vanDAO.getAllVans();
    }

    public List<Van> getAvailableVans() { // for creating a trip
        return vanDAO.getAllVans().stream()
            .filter(d -> "Available".equalsIgnoreCase(d.getVanStatus()))
            .collect(Collectors.toList());
    }

    public Van getVanById(int vanId) {
        Van van = vanDAO.getVanById(vanId);
        if (van == null) {
            throw new IllegalArgumentException("Van not found with ID: " + vanId);
        }
        return van;
    }

    public void updateVan(int vanId, String plateNumber, String model, int capacity, String status) {
        Van existingVan = getVanById(vanId);

        if (plateNumber != null && !plateNumber.trim().isEmpty()) {
            existingVan.setPlateNumber(plateNumber.trim().toUpperCase());
        }

        if (model != null && !model.trim().isEmpty()) {
            existingVan.setModel(model);
        }

        if (capacity > 0) {
            existingVan.setCapacity(capacity);
        }

        if (status != null && !status.trim().isEmpty()) {
            existingVan.setVanStatus(status);
        }

        vanDAO.updateVan(existingVan);
    }

    public void updateVanStatus(int vanId, String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty.");
        }

        Van existingVan = getVanById(vanId);
        existingVan.setVanStatus(status.trim());
        vanDAO.updateVan(existingVan);
    }

    public void deleteVan(int vanId) {
        vanDAO.deleteVan(vanId);
    }

    // search, filter, sort
    public List<Van> searchVans(String keyword) {
        return vanDAO.getAllVans().stream()
            .filter(v -> 
                (v.getPlateNumber() != null && v.getPlateNumber().toLowerCase().contains(keyword.toLowerCase())) ||
                (v.getModel() != null && v.getModel().toLowerCase().contains(keyword.toLowerCase())) ||
                String.valueOf(v.getFormattedId()).contains(keyword)
            )
            .collect(Collectors.toList());
    }

    public List<Van> filterByStatus(String status) {
        return vanDAO.getAllVans().stream()
                .filter(v -> v.getVanStatus() != null && status != null && v.getVanStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public List<Van> sortByVanId(List<Van> vans) {
        vans.sort(Comparator.comparingInt(Van::getVanId));
        return vans;
    }

    public List<Van> sortByPlateNumber(List<Van> vans) {
        vans.sort(Comparator.comparing(Van::getPlateNumber));
        return vans;
    }
}