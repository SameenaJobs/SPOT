package com.etatransit.event;

import com.etatransit.model.Stop;

import java.util.List;

/**
 * Created by mark on 10/6/14.
 */
public class StopsLoadedEvent
{
    public List<Stop> stops;


    public StopsLoadedEvent( List<Stop> stops )
    {
        this.stops = stops;
    }
}
