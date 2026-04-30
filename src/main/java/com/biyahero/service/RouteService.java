package com.biyahero.service;

import com.biyahero.dao.RouteDAO;
import com.biyahero.dao.RouteStopDAO;
import com.biyahero.dao.StopDAO;
import com.biyahero.dao.impl.RouteDAOImpl;
import com.biyahero.dao.impl.RouteStopDAOImpl;
import com.biyahero.dao.impl.StopDAOImpl;
import com.biyahero.model.Route;
import com.biyahero.model.RouteStop;
import com.biyahero.model.Stop;

import java.util.List;
import java.util.stream.Collectors;

public class RouteService {
    private final RouteDAO routeDAO = new RouteDAOImpl();
    private final StopDAO stopDAO = new StopDAOImpl();
    private final RouteStopDAO routeStopDAO = new RouteStopDAOImpl();

    // ── Route ───────────────────────────────────────────────────────────────

    public List<Route> getAllRoutes() {
        return routeDAO.getAllRoutes();
    }

    public Route getRouteById(int routeId) {
        Route route = routeDAO.getRouteById(routeId);
        if (route == null) throw new IllegalArgumentException("Route not found with ID: " + routeId);
        return route;
    }

    public void updateBaseFare(int routeId, double newFare) {
        if (newFare < 0) throw new IllegalArgumentException("Base fare cannot be negative.");
        getRouteById(routeId);
        routeDAO.updateBaseFare(routeId, newFare);
    }

    /**
     * Creates a brand-new Route with its ordered stops in a single transaction.
     *
     * @param routeName  the display name for the route
     * @param baseFare   the base fare (>= 0)
     * @param orderedStops the stops in sequence, already carrying stop_order values
     * @return the newly created Route (with its generated ID)
     */
    public Route createRoute(String routeName, double baseFare, List<RouteStop> orderedStops) {
        if (routeName == null || routeName.trim().isEmpty())
            throw new IllegalArgumentException("Route name cannot be empty.");
        if (baseFare < 0)
            throw new IllegalArgumentException("Base fare cannot be negative.");
        if (orderedStops == null || orderedStops.size() < 2)
            throw new IllegalArgumentException("A route must have at least 2 stops.");

        Route newRoute = new Route(routeName.trim(), baseFare);
        int generatedId = routeDAO.saveRouteWithStops(newRoute, orderedStops);
        if (generatedId == -1)
            throw new IllegalStateException("Failed to save the route. Check logs for details.");

        newRoute.setRouteId(generatedId);
        return newRoute;
    }

    // ── Stop ────────────────────────────────────────────────────────────────

    public List<Stop> getAllStops() {
        return stopDAO.getAllStops();
    }

    public Stop getStopById(int stopId) {
        Stop stop = stopDAO.getStopById(stopId);
        if (stop == null) throw new IllegalArgumentException("Stop not found with ID: " + stopId);
        return stop;
    }

    // ── RouteStop ────────────────────────────────────────────────────────────

    public List<Stop> getStopsForRoute(int routeId) {
        getRouteById(routeId);
        return routeStopDAO.getStopsForRoute(routeId).stream()
                .map(rs -> stopDAO.getStopById(rs.getStopId()))
                .collect(Collectors.toList());
    }

    public Stop saveNewStop(Stop stop) {
        return stopDAO.saveStop(stop);
    }

    public List<RouteStop> getRouteStops(int routeId) {
        getRouteById(routeId);
        return routeStopDAO.getStopsForRoute(routeId);
    }
}