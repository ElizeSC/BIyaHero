package com.biyahero.dao;

import com.biyahero.model.Van;
import java.util.List;

public interface VanDAO {
    void addVan(Van van);
    Van getVanById(int id);
    Van getVanByPlateNumber(String plateNumber);
    Van getVanByModel(String model);
    List<Van> getAllVans();
    void updateVan(Van van);
    void updateVanStatus(int id, String status); // e.g., 'Available', 'In Transit', 'Maintenance'
    void deleteVan(int id);
}