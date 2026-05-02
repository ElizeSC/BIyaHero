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

    public void updateRouteFares(int routeId, double newBaseFare, double newPerStopFare) {
        routeDAO.updateRouteFares(routeId, newBaseFare, newPerStopFare);
    }

    public void createRoute(String name, double baseFare, double perStopFare, List<RouteStop> stops) {

        // Pass it into our new 4-parameter constructor
        Route route = new Route(0, name, baseFare, perStopFare);

        routeDAO.saveRouteWithStops(route, stops);
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