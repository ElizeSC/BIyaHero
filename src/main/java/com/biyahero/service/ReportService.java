package com.biyahero.service;

import com.biyahero.model.Booking;
import com.biyahero.model.Driver;
import com.biyahero.model.Route;
import com.biyahero.model.Trip;
import com.biyahero.model.TripReport;
import com.biyahero.model.Van;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ReportService {
    private final TripService tripService = new TripService();
    private final DriverService driverService = new DriverService();
    private final RouteService routeService = new RouteService();
    private final BookingService bookingService = new BookingService();
    private final VanService vanService = new VanService();

    // builds a TripReport for a single completed trip
    private TripReport buildReport(Trip trip) {
        // resolve driver name
        String driverName;
        try {
            Driver driver = driverService.getDriverById(trip.getDriverId());
            driverName = driver.getName();
        } catch (IllegalArgumentException e) {
            driverName = "Unknown";
        }

        // resolve route name
        String routeName;
        try {
            Route route = routeService.getRouteById(trip.getRouteId());
            routeName = route.getRouteName();
        } catch (IllegalArgumentException e) {
            routeName = "Unknown";
        }

        // resolve van capacity
        int capacity;
        try {
            Van van = vanService.getVanById(trip.getVanId());
            capacity = van.getCapacity();
        } catch (IllegalArgumentException e) {
            capacity = 0;
        }

        // get bookings for this trip
        List<Booking> bookings = bookingService.getBookingsByTrip(trip.getTripId());

        // count only reserved/non-cancelled bookings
        int bookedSeats = (int) bookings.stream()
            .filter(b -> !"Cancelled".equals(b.getBookingStatus()))
            .count();

        // sum revenue
        double totalRevenue = bookings.stream()
            .filter(b -> !"Cancelled".equals(b.getBookingStatus()))
            .mapToDouble(Booking::getFarePaid)
            .sum();

        return new TripReport(
            trip.getTripId(),
            trip.getFormattedId(),
            driverName,
            routeName,
            bookedSeats,
            capacity,
            totalRevenue,
            trip.getDepartureTime(),
            trip.getArrivalDt()
        );
    }

    // get all completed trip reports (export all)
    public List<TripReport> getAllReports() {
        return tripService.getCompletedTrips().stream()
            .map(this::buildReport)
            .collect(Collectors.toList());
    }

    // get reports filtered by date range
    public List<TripReport> getReportsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.atTime(23, 59, 59);

        return tripService.getCompletedTrips().stream()
            .filter(t -> t.getDepartureTime() != null
                && !t.getDepartureTime().isBefore(startDt)
                && !t.getDepartureTime().isAfter(endDt))
            .map(this::buildReport)
            .collect(Collectors.toList());
    }

    // search within reports by trip ID
    public List<TripReport> searchReports(String keyword) {
        return getAllReports().stream()
            .filter(r ->
                r.getFormattedTripId().toLowerCase().contains(keyword.toLowerCase()) ||
                r.getDriverName().toLowerCase().contains(keyword.toLowerCase()) ||
                r.getRouteName().toLowerCase().contains(keyword.toLowerCase())
            )
            .collect(Collectors.toList());
    }
}