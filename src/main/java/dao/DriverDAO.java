package dao;
import model.Driver;
import java.util.List;

public interface DriverDAO {
    void addDriver(Driver driver);
    Driver getDriverById(int id);
    List<Driver> getAllDrivers();
    void updateDriverStatus(int id, String status);
}