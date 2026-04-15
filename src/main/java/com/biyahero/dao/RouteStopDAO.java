package com.biyahero.dao;

import com.biyahero.model.RouteStop;
import java.util.List;

public interface RouteStopDAO {
    void addStopToRoute(RouteStop routeStop);
    List<RouteStop> getStopsForRoute(int routeId);
}