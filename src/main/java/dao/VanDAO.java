package dao;

import model.Van;
import java.util.List;

public interface VanDAO {
    void addVan(Van van);
    Van getVanById(int id);
    List<Van> getAllVans();
    void updateVanStatus(int id, String status); // e.g., 'Available', 'In Transit', 'Maintenance'
    void deleteVan(int id);
}