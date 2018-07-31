package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 10/13/14.
 */
public class StopEta implements Parcelable
{

    public int id;
    public List<EnRoute> enRoute = new ArrayList<EnRoute>();


    public StopEta()
    {

    }


    public StopEta( Parcel source )
    {
        id = source.readInt();
        source.readTypedList( enRoute, EnRoute.CREATOR );
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
        dest.writeTypedList( enRoute );
    }


    public static Creator<StopEta> CREATOR = new Creator<StopEta>()
    {
        @Override
        public StopEta createFromParcel( Parcel source )
        {
            return new StopEta( source );
        }

        @Override
        public StopEta[] newArray( int size )
        {
            return new StopEta[size];
        }
    };
}
