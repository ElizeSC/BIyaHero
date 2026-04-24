package com.biyahero.service;

import com.biyahero.dao.TripDAO;
import com.biyahero.dao.impl.TripDAOImpl;
import com.biyahero.model.Driver;
import com.biyahero.model.RouteStop;
import com.biyahero.model.Stop;
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
    private final RouteService routeService = new RouteService();

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

        // van reassignment
        if (vanId != null && vanId > 0 && vanId != trip.getVanId()) {
            Van newVan = vanService.getVanById(vanId);
            if (!"Available".equalsIgnoreCase(newVan.getVanStatus())) {
                throw new IllegalStateException("New van is not available. Current status: " + newVan.getVanStatus());
            }
            vanService.updateVanStatus(trip.getVanId(), "Available"); // free old van
            vanService.updateVanStatus(vanId, "On Trip");             // assign new van
            trip.setVanId(vanId);
        }

        // trip reassignment
        if (driverId != null && driverId > 0 && driverId != trip.getDriverId()) {
            Driver newDriver = driverService.getDriverById(driverId);
            if (!"Available".equalsIgnoreCase(newDriver.getDriverStatus())) {
                throw new IllegalStateException("New driver is not available. Current status: " + newDriver.getDriverStatus());
            }
            driverService.updateDriverStatus(trip.getDriverId(), "Available"); // free old driver
            driverService.updateDriverStatus(driverId, "On Trip");             // assign new driver
            trip.setDriverId(driverId);
        }

        if (routeId != null && routeId > 0){
            trip.setRouteId(routeId);
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

        // get ordered stops for this trip's route
        List<RouteStop> routeStops = routeService.getRouteStops(trip.getRouteId());
        int lastStopId = routeStops.get(routeStops.size() - 1).getStopId();

        trip.setCurrentStopId(stopId);

        if (stopId == lastStopId) {
            // automatically complete the trip
            trip.setTripStatus("Completed");
            trip.setArrivalDt(LocalDateTime.now());
            tripDAO.updateTrip(trip);
            vanService.updateVanStatus(trip.getVanId(), "Available");
            driverService.updateDriverStatus(trip.getDriverId(), "Available");
        } else {
            tripDAO.updateTrip(trip);
        }
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

    public void cancelTrip(int tripId) {
        Trip trip = getTripById(tripId);
        if ("Completed".equals(trip.getTripStatus()) || "Cancelled".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Cannot cancel a trip that is already " + trip.getTripStatus() + ".");
        }
        tripDAO.updateTripStatus(tripId, "Cancelled");
        vanService.updateVanStatus(trip.getVanId(), "Available");
        driverService.updateDriverStatus(trip.getDriverId(), "Available");
    }

    public List<Stop> getStopsForTrip(int tripId) {
        Trip trip = getTripById(tripId);
        return routeService.getStopsForRoute(trip.getRouteId());
    }

    public Stop getCurrentStop(int tripId) {
        Trip trip = getTripById(tripId);
        if (trip.getCurrentStopId() == null) {
            return null; // trip hasn't reached any checkpoint yet
        }
        return routeService.getStopById(trip.getCurrentStopId());
    }

    // active trips
    public List<Trip> getActiveTrips() { // show raw en route trips
        return tripDAO.getAllTrips().stream()
                .filter(t -> "En Route".equals(t.getTripStatus()))
                .collect(Collectors.toList());
    }

    public List<Trip> getScheduledTrips() { // show raw scheduled trips
        return tripDAO.getAllTrips().stream()
                .filter(t -> "Scheduled".equals(t.getTripStatus()))
                .collect(Collectors.toList());
    }

    public List<Trip> getCompletedTrips() { // returns raw all completed trips, for report
        return tripDAO.getAllTrips().stream()
                .filter(t -> "Completed".equals(t.getTripStatus()))
                .collect(Collectors.toList());
    }

    // search, filter, sort

    public List<Trip> searchActiveTrips(String keyword) { // search in en route trips dashboard
        return tripDAO.getAllTrips().stream()
                .filter(t -> "En Route".equals(t.getTripStatus()))
                .filter(t ->
                        String.valueOf(t.getTripId()).contains(keyword) ||
                                t.getFormattedId().toLowerCase().contains(keyword.toLowerCase())
                )
                .collect(Collectors.toList());
    }

    public List<Trip> searchScheduledTrips(String keyword) { // search in scheduled trips dashboard
        return tripDAO.getAllTrips().stream()
                .filter(t -> "Scheduled".equals(t.getTripStatus()))
                .filter(t ->
                        String.valueOf(t.getTripId()).contains(keyword) ||
                                t.getFormattedId().toLowerCase().contains(keyword.toLowerCase())
                )
                .collect(Collectors.toList());
    }

    public List<Trip> searchCompletedTrips(String keyword) { // search for a completed trip in reports
        return tripDAO.getAllTrips().stream()
                .filter(t -> "Completed".equals(t.getTripStatus()))
                .filter(t ->
                        String.valueOf(t.getTripId()).contains(keyword) ||
                                t.getFormattedId().toLowerCase().contains(keyword.toLowerCase())
                )
                .collect(Collectors.toList());
    }

    public List<Trip> filterByStatus(String status) { // works for all status but for will use only completed or cancelled in reports
        return tripDAO.getAllTrips().stream()
                .filter(t -> t.getTripStatus() != null && t.getTripStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public List<Trip> sortByTripId(List<Trip> trips) { // default sorted list for en route trips, and a sorting option for completed trips
        return trips.stream()
                .sorted(Comparator.comparingInt(Trip::getTripId))
                .collect(Collectors.toList());
    }

    public List<Trip> sortByDepartureTime(List<Trip> trips) { // default sorted list for scheduled trips, and a sorting option for completed trips
        return trips.stream()
                .sorted(Comparator.comparing(Trip::getDepartureTime))
                .collect(Collectors.toList());
    }
}