package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mark on 10/5/14.
 */
public class RouteColor implements Parcelable
{
    public String light;
    public String normal;
    public String text;
    public String etaText;


    public RouteColor(){}


    public RouteColor( Parcel source )
    {
        light = source.readString();
        normal = source.readString();
        text = source.readString();
        etaText = source.readString();
    }


    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
        dest.writeString(light);
        dest.writeString( normal );
        dest.writeString( text );
        dest.writeString( etaText );
    }


    public static Creator<RouteColor> CREATOR = new Creator<RouteColor>()
    {
        @Override
        public RouteColor createFromParcel( Parcel source )
        {
            return new RouteColor( source );
        }

        @Override
        public RouteColor[] newArray( int size )
        {
            return new RouteColor[size];
        }
    };
}
