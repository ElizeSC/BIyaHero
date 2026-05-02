package com.biyahero.dao;

import com.biyahero.model.Stop;
import java.util.List;

public interface StopDAO {
    Stop getStopById(int id);
    List<Stop> getAllStops();
    Stop saveStop(Stop stop);


}