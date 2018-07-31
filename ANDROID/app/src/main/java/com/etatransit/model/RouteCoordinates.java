package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by mark on 10/5/14.
 */
public class RouteCoordinates implements Parcelable
{
    public int id;
    public RouteColor color;
    public List<double[]> coords;

    public RouteCoordinates(){}


    public RouteCoordinates( Parcel source )
    {
        id = source.readInt();
        color = source.readParcelable( RouteColor.class.getClassLoader() );
        String jsonString = source.readString();
        Type listType = new TypeToken<List<double[]>>() {}.getType();
        coords = new Gson().fromJson( jsonString, listType );
    }


    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
        dest.writeInt( id );
        dest.writeParcelable( color, flags );
        String jsonString = new Gson().toJson( coords );
        dest.writeString( jsonString );
    }


    public static Creator<RouteCoordinates> CREATOR = new Creator<RouteCoordinates>()
    {
        @Override
        public RouteCoordinates createFromParcel( Parcel source )
        {
            return new RouteCoordinates( source );
        }

        @Override
        public RouteCoordinates[] newArray( int size )
        {
            return new RouteCoordinates[size];
        }
    };
}
