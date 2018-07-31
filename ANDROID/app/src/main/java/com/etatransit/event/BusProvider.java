package com.etatransit.event;

import com.squareup.otto.Bus;

/**
 * Created by mark on 9/29/14.
 */
public class BusProvider
{
    private static Bus instance;


    public static Bus getInstance()
    {
        if( instance == null )
            instance = new Bus();

        return instance;
    }


}
