package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mark on 10/5/14.
 */
public class Stop implements Parcelable
{
    public int id;
    public int rid;
    public String name;
    public double lat;
    public double lng;


    public Stop(){}


    public Stop( Parcel source )
    {
        id = source.readInt();
        rid = source.readInt();
        name = source.readString();
        lat = source.readDouble();
        lng = source.readDouble();
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt( id );
        dest.writeInt( rid );
        dest.writeString( name );
        dest.writeDouble( lat );
        dest.writeDouble( lng );
    }


    public static Creator<Stop> CREATOR = new Creator<Stop>()
    {
        @Override
        public Stop createFromParcel( Parcel source )
        {
            return new Stop( source );
        }

        @Override
        public Stop[] newArray( int size )
        {
            return new Stop[size];
        }
    };
}
