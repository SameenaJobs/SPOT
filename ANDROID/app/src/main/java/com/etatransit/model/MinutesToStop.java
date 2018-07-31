package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mark on 10/13/14.
 */
public class MinutesToStop implements Parcelable
{
    public int stopID;
    public int minutes;
    public String time;
    public String status;
    public String schedule;
    public String direction = "";
    public String track ="";
    public int routeID;


    public MinutesToStop()
    {

    }


    public MinutesToStop( Parcel source )
    {
        stopID = source.readInt();
        minutes = source.readInt();
        time = source.readString();
        status = source.readString();
        schedule = source.readString();
        try{
            direction = source.readString();
            track = source.readString();
        }catch (Exception e){
            direction = "";
            track = "";
        }
        try{
            routeID = source.readInt();
        }catch (Exception e){
            routeID = 0;
        }

    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
        dest.writeInt( stopID );
        dest.writeInt(minutes );
        dest.writeString( time );
        dest.writeString( status );
        dest.writeString( schedule );
        dest.writeString( direction );
        dest.writeString( track );
        dest.writeInt(routeID);
    }


    public static Creator<MinutesToStop> CREATOR = new Creator<MinutesToStop>()
    {
        @Override
        public MinutesToStop createFromParcel( Parcel source )
        {
            return new MinutesToStop( source );
        }

        @Override
        public MinutesToStop[] newArray( int size )
        {
            return new MinutesToStop[size];
        }
    };
}
