package com.biyahero.dao;
import com.biyahero.model.Trip;
import java.util.List;

public interface TripDAO {
    void createTrip(Trip trip);
    Trip getTripById(int id);
    List<Trip> getTripsByRoute(int routeId);
    void updateTripStatus(int id, String status); // e.g., 'Scheduled', 'En Route', 'Completed'
}