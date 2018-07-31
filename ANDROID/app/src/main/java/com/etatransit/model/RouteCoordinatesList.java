package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 10/5/14.
 */
public class RouteCoordinatesList implements Parcelable
{

    public List<RouteCoordinates> routes = new ArrayList<RouteCoordinates>();


    public RouteCoordinatesList(){}


    public RouteCoordinatesList( Parcel source )
    {
        source.readTypedList( routes, RouteCoordinates.CREATOR );
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
        dest.writeTypedList( routes );
    }


    public static Creator<RouteCoordinatesList> CREATOR = new Creator<RouteCoordinatesList>()
    {
        @Override
        public RouteCoordinatesList createFromParcel( Parcel source )
        {
            return new RouteCoordinatesList( source );
        }

        @Override
        public RouteCoordinatesList[] newArray( int size )
        {
            return new RouteCoordinatesList[size];
        }
    };
}
