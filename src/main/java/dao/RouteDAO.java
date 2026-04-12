package dao;

import model.Route;
import java.util.List;

public interface RouteDAO {
    void addRoute(Route route);
    Route getRouteById(int id);
    List<Route> getAllRoutes();
    void updateBaseFare(int id, double newFare);
}