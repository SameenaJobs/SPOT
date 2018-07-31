package com.etatransit.event;

/**
 * Created by mark on 12/17/14.
 */
public class AgencyHasRoutesEvent
{
    public boolean hasRoutes = false;


    public AgencyHasRoutesEvent( boolean hasRoutes )
    {
        this.hasRoutes = hasRoutes;
    }

}
