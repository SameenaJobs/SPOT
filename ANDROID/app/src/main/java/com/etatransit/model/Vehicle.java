package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 10/13/14.
 */
public class Vehicle implements Parcelable
{
    public String equipmentID;
    public double lat;
    public double lng;
    public int routeID;
    public Integer nextStopID;
    public String scheduleNumber;
    public int inService;
    public int onSchedule;
    public long receiveTime;
    public Object minutesToNextStops;
    public String vehicleType = "";
    public double load;
    public double capacity;

    public Vehicle()
    {

    }


    public Vehicle( Parcel source )
    {
        equipmentID = source.readString();
        lat = source.readDouble();
        lng = source.readDouble();
        routeID = source.readInt();
        nextStopID = source.readInt();
        scheduleNumber = source.readString();
        inService = source.readInt();
        onSchedule = source.readInt();
        receiveTime = source.readLong();
        try {
            vehicleType = source.readString();
        }catch (Exception e){
            vehicleType = "";
        }

        String minutesToStopString = source.readString();
        minutesToNextStops = new Gson().fromJson( minutesToStopString, Object.class );
        load = source.readDouble();
        capacity = source.readDouble();
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
        dest.writeDouble( lat );
        dest.writeDouble( lng );
        dest.writeInt( routeID );
        dest.writeInt( nextStopID );
        dest.writeString( scheduleNumber );
        dest.writeInt( inService );
        dest.writeInt( onSchedule );
        dest.writeLong( receiveTime );
        dest.writeString( new Gson().toJson( minutesToNextStops ).toString() );
        dest.writeString( vehicleType );
        dest.writeDouble(load);
        dest.writeDouble(capacity);
    }


    public static Creator<Vehicle> CREATOR = new Creator<Vehicle>()
    {
        @Override
        public Vehicle createFromParcel( Parcel source )
        {
            return new Vehicle( source );
        }

        @Override
        public Vehicle[] newArray( int size )
        {
            return new Vehicle[size];
        }
    };


    public List<MinutesToStop> getMinutesToNextStopList()
    {

        if( minutesToNextStops != null )
        {
            Gson gson = new Gson();
            Type listOfMinutesToStop = new TypeToken<List<MinutesToStop>>(){}.getType();

            String jsonString = gson.toJson( minutesToNextStops ).toString();
//            Log.d("jsonstring",jsonString);
            try
            {
//                Log.d("gson",gson.fromJson( jsonString, listOfMinutesToStop )+"");
                return gson.fromJson( jsonString, listOfMinutesToStop );
            }catch( Exception e ){
                Log.d("ERRRRRRRRRRRRRRRRRRR",e.getMessage());
            }
        }

        return new ArrayList<MinutesToStop>();
    }
}
