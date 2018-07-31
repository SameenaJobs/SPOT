package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Innovate on 8/12/2017.
 */
public class StopImage implements Parcelable {
    public int stopid;
    public String type;
    public String stoplocdesc;
    public String amenities;
    public String comments;
    public ArrayList<String> images = new ArrayList<String>();


    public StopImage()
    {}


    public StopImage( Parcel source )
    {
        stopid = source.readInt();
        type = source.readString();
        stoplocdesc = source.readString();
        amenities = source.readString();
        comments = source.readString();
        source.readStringList(images);
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt( stopid );
        dest.writeString( type );
        dest.writeString(stoplocdesc);
        dest.writeString(amenities);
        dest.writeString(comments);
        dest.writeStringList(images);
    }


    public static Creator<StopImage> CREATOR = new Creator<StopImage>()
    {
        @Override
        public StopImage createFromParcel(Parcel source )
        {

            return new StopImage( source );
        }

        @Override
        public StopImage[] newArray(int size )
        {

            return new StopImage[size];
        }
    };
}
