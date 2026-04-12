package dao;
import model.Passenger;
import java.util.List;

public interface PassengerDAO {
    void addPassenger(Passenger passenger);
    List<Passenger> getPassengersByBooking(int bookingId);
}