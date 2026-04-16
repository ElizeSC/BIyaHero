package com.biyahero.service;

import com.biyahero.dao.TripDAO;
import com.biyahero.dao.impl.TripDAOImpl;
import com.biyahero.model.Driver;
import com.biyahero.model.Trip;
import com.biyahero.model.Van;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TripService {
    private final TripDAO tripDAO = new TripDAOImpl();
    private final VanService vanService = new VanService();
    private final DriverService driverService = new DriverService();

    public void createTrip(int routeId, int vanId, int driverId, LocalDateTime departureTime) {
        if (routeId <= 0){
            throw new IllegalArgumentException("Route ID must be a positive integer.");
        } 

        if (vanId <= 0){
            throw new IllegalArgumentException("Van ID must be a positive integer.");
        }

        if (driverId <= 0){
            throw new IllegalArgumentException("Driver ID must be a positive integer.");
        }

        if (departureTime == null){
            throw new IllegalArgumentException("Departure time cannot be null.");
        }

        Van van = vanService.getVanById(vanId);
        if (!"Available".equalsIgnoreCase(van.getVanStatus())) {
            throw new IllegalStateException("Van is not available. Current status: " + van.getVanStatus());
        }

        Driver driver = driverService.getDriverById(driverId);
        if (!"Available".equalsIgnoreCase(driver.getDriverStatus())) {
            throw new IllegalStateException("Driver is not available. Current status: " + driver.getDriverStatus());
        }

        Trip trip = new Trip(routeId, vanId, driverId, departureTime, "Scheduled", null, null);
        tripDAO.createTrip(trip);

        vanService.updateVanStatus(vanId, "On Trip");
        driverService.updateDriverStatus(driverId, "On Trip");
    }

    public Trip getTripById(int tripId) {
        Trip trip = tripDAO.getTripById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }
        return trip;
    }

    public List<Trip> getAllTrips() {
        return tripDAO.getAllTrips();
    }

    public List<Trip> getTripsByRoute(int routeId) {
        return tripDAO.getTripsByRoute(routeId);
    }

    public void updateTrip(int tripId, Integer routeId, Integer vanId, Integer driverId, 
                       LocalDateTime departureTime, LocalDateTime arrivalDt, Integer currentStopId) {
        Trip trip = getTripById(tripId);

        if (routeId != null && routeId > 0){
            trip.setRouteId(routeId);
        } 
        if (vanId != null && vanId > 0){
            trip.setVanId(vanId);
        }
        if (driverId != null && driverId > 0){
            trip.setDriverId(driverId);
        }
        if (departureTime != null){
            trip.setDepartureTime(departureTime);
        } 
        if (arrivalDt != null){
            trip.setArrivalDt(arrivalDt);
        }
        if (currentStopId != null){
            trip.setCurrentStopId(currentStopId);
        }

        tripDAO.updateTrip(trip);
    }

    public List<Van> getAvailableVans() {
        return vanService.getAvailableVans();
    }

    public List<Driver> getAvailableDrivers() {
        return driverService.getAvailableDrivers();
    }

    public void updateCurrentStop(int tripId, int stopId) {
        Trip trip = getTripById(tripId);
        if (!"En Route".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Can only update stop for En Route trips.");
        }
        trip.setCurrentStopId(stopId);
        tripDAO.updateTrip(trip);
    }

    public void updateTripStatus(int tripId, String status) {
        getTripById(tripId);
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty.");
        }
        tripDAO.updateTripStatus(tripId, status.trim());
    }

    // trip progress
    public void startTrip(int tripId) {
        Trip trip = getTripById(tripId);
        if (!"Scheduled".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Only Scheduled trips can be started. Current status: " + trip.getTripStatus());
        }
        tripDAO.updateTripStatus(tripId, "En Route");
    }

    public void completeTrip(int tripId) {
        Trip trip = getTripById(tripId);
        if (!"En Route".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Only En Route trips can be completed. Current status: " + trip.getTripStatus());
        }
        tripDAO.updateTripStatus(tripId, "Completed");
        vanService.updateVanStatus(trip.getVanId(), "Available");
        driverService.updateDriverStatus(trip.getDriverId(), "Available");
    }

    public void cancelTrip(int tripId) {
        Trip trip = getTripById(tripId);
        if ("Completed".equals(trip.getTripStatus()) || "Cancelled".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Cannot cancel a trip that is already " + trip.getTripStatus() + ".");
        }
        tripDAO.updateTripStatus(tripId, "Cancelled");
        vanService.updateVanStatus(trip.getVanId(), "Available");
        driverService.updateDriverStatus(trip.getDriverId(), "Available");
    }

    // active trips
    public List<Trip> getActiveTrips() {
        return tripDAO.getAllTrips().stream()
            .filter(t -> "Scheduled".equals(t.getTripStatus()) || "En Route".equals(t.getTripStatus()))
            .collect(Collectors.toList());
    }

    public List<Trip> getActiveTripsByRoute(int routeId) {
        return tripDAO.getTripsByRoute(routeId).stream()
            .filter(t -> "Scheduled".equals(t.getTripStatus()) || "En Route".equals(t.getTripStatus()))
            .collect(Collectors.toList());
    }

    // search, filter, sort
    public List<Trip> searchTrip(String keyword) {
        return tripDAO.getAllTrips().stream()
            .filter(t ->
                String.valueOf(t.getTripId()).contains(keyword) ||
                t.getFormattedId().toLowerCase().contains(keyword.toLowerCase()) ||
                (t.getTripStatus() != null && t.getTripStatus().toLowerCase().contains(keyword.toLowerCase()))
            )
            .collect(Collectors.toList());
    }

    public List<Trip> filterByStatus(String status) {
        return tripDAO.getAllTrips().stream()
            .filter(t -> t.getTripStatus() != null && t.getTripStatus().equalsIgnoreCase(status))
            .collect(Collectors.toList());
    }

    public List<Trip> sortByTripId(List<Trip> trips) {
        return trips.stream()
            .sorted(Comparator.comparingInt(Trip::getTripId))
            .collect(Collectors.toList());
    }

    public List<Trip> sortByDepartureTime(List<Trip> trips) {
        return trips.stream()
            .sorted(Comparator.comparing(Trip::getDepartureTime))
            .collect(Collectors.toList());
    }
}