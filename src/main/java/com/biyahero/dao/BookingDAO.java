package dao;
import model.Booking;
import java.util.List;

public interface BookingDAO {
    void createBooking(Booking booking);
    Booking getBookingById(int id);
    List<Booking> getBookingsByTrip(int tripId);
    void cancelBooking(int id);
    List<Integer> getOccupiedSeats(int tripId);
    List<Integer> getAvailableSeats(int tripId);
    void updateBookingStatus(int bookingId, String status);
}