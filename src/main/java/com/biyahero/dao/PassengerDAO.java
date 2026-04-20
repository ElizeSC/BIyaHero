package com.biyahero.dao;
import com.biyahero.model.Passenger;

public interface PassengerDAO {
    int addPassenger(Passenger passenger);
    Passenger getPassengerByBookingId(int bookingId);
}