package com.biyahero.dao;
import com.biyahero.model.Passenger;
import java.util.List;

public interface PassengerDAO {
    int addPassenger(Passenger passenger); 
    List<Passenger> getPassengersByBooking(int bookingId);
}