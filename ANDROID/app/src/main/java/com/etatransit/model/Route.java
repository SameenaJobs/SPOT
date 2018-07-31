package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 10/1/14.
 */
public class Route implements Parcelable
{
    public int id;
    public String name;
    public List<String> stops = new ArrayList<String>();
    public String encLine;
    public String color;
    public String type;
    public String vType = "bus";
    public int showScheduleNumber;
    public String showDirection = "false",showVehicleCapacity = "false";
    public String showPlatform = "false";


    public Route(){}

    public Route( Parcel source )
    {
        try {
            id = source.readInt();
        }
        catch (Exception e){
            id = 0;
        }
        name = source.readString();
        stops = source.createStringArrayList();
        //source.readStringList( stops );
        encLine = source.readString();
        color = source.readString();
        type = source.readString();
        try {
            vType = source.readString();
            showDirection = source.readString();
            showPlatform = source.readString();
            showVehicleCapacity = source.readString();
        }catch (Exception e){
            vType = "bus";
            showDirection = "false";
            showPlatform = "false";
            showVehicleCapacity = "false";
        }
        showScheduleNumber = source.readInt();
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
        dest.writeString( name );
        dest.writeStringList( stops );
        dest.writeString( encLine );
        dest.writeString( color );
        dest.writeString( type );
        dest.writeString(vType);
        dest.writeInt(showScheduleNumber);
        dest.writeString( showDirection );
        dest.writeString(showPlatform);
        dest.writeString(showVehicleCapacity);

    }


    public static Creator<Route> CREATOR = new Creator<Route>()
    {
        @Override
        public Route createFromParcel( Parcel source )
        {
            return new Route( source );
        }

        @Override
        public Route[] newArray( int size )
        {
            return new Route[size];
        }
    };
}