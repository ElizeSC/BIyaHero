package dao;
import model.Driver;
import java.util.List;

public interface DriverDAO {
    void addDriver(Driver driver);
    Driver getDriverById(int id);
    Driver getDriverByName(String name);
    Driver getDriverByLicenseNo(String licenseNo);
    List<Driver> getAllDrivers();
    void deleteDriver(int id);
}