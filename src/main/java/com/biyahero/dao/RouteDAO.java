package com.biyahero.dao;

import com.biyahero.model.Route;
import com.biyahero.model.RouteStop;
import java.util.List;

public interface RouteDAO {
    Route getRouteById(int id);
    List<Route> getAllRoutes();
    void updateBaseFare(int id, double newFare);

    int saveRouteWithStops(Route route, List<RouteStop> stops);
}