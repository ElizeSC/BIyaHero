package com.biyahero.dao;

import com.biyahero.model.Stop;
import java.util.List;

public interface StopDAO {
    void addStop(Stop stop);
    Stop getStopById(int id);
    List<Stop> getAllStops();
}