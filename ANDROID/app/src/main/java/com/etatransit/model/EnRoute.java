package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mark on 10/13/14.
 */
public class EnRoute implements Parcelable
{
    public String equipmentID;
    public int minutes = 0;
    public String schedule = "";
    public String status= "";
    public  String directionAbbr = "";
    public  int track = 0;
    public int routeID = 0;

    public EnRoute()
    {

    }


    public EnRoute( Parcel source )
    {
        equipmentID = source.readString();
        minutes = source.readInt();

        schedule = source.readString();
        status = source.readString();
        try{
            directionAbbr = source.readString();

        }catch (Exception e){
            directionAbbr = "";

        }
        try{
            track = source.readInt();
        }catch (Exception e){

            track = 0;
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
        dest.writeString( equipmentID );
        dest.writeInt(minutes);
        dest.writeInt(routeID);
        dest.writeString( schedule );
        dest.writeString( status );
        dest.writeString( directionAbbr );
        dest.writeInt(track);
    }


    public static Creator<EnRoute> CREATOR = new Creator<EnRoute>()
    {
        @Override
        public EnRoute createFromParcel( Parcel source )
        {
            return new EnRoute( source );
        }

        @Override
        public EnRoute[] newArray( int size )
        {
            return new EnRoute[size];
        }
    };
}
