package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by mark on 9/29/14.
 */
public class Agency implements Parcelable
{
    public String name;
    public double centerLat;
    public double centerLng;
    public String bgColor;
    public String fgColor;
    public String serviceUrl;
    public String logoUrl;
    public String feedbackUrl;


    public Agency(){}

    public Agency( Parcel in )
    {
        name = in.readString();
        centerLat = in.readDouble();
        centerLng = in.readDouble();
        bgColor = in.readString();
        fgColor = in.readString();
        serviceUrl = in.readString();
        logoUrl = in.readString();
        feedbackUrl = in.readString();
    }


    public LatLng getLocation()
    {
        return new LatLng( centerLat, centerLng );
    }


    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
        dest.writeString( name );
        dest.writeDouble( centerLat );
        dest.writeDouble( centerLng );
        dest.writeString( bgColor );
        dest.writeString( fgColor );
        dest.writeString( serviceUrl );
        dest.writeString( logoUrl );
        dest.writeString( feedbackUrl );
    }

    public static Creator<Agency> CREATOR = new Creator<Agency>()
    {
        @Override
        public Agency createFromParcel( Parcel source )
        {
            return new Agency( source );
        }

        @Override
        public Agency[] newArray( int size )
        {
            return new Agency[size];
        }
    };
}
