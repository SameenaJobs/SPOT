package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by innovator on 8/27/2016.
 */
public class OneStopEnRoute implements Parcelable {

    public String equipmentID;
    public int minutes;
    public String schedule = "";
    public String status= "";
    public String directionAbbr = "";
    public String direction = "NA";
    public  int track = 0;
    public int routeID = 0;

    protected OneStopEnRoute(Parcel in) {
        equipmentID = in.readString();
        minutes = in.readInt();
        schedule = in.readString();
        status = in.readString();
        directionAbbr = in.readString();
        direction = in.readString();
        track = in.readInt();
        routeID = in.readInt();
    }

    public static final Creator<OneStopEnRoute> CREATOR = new Creator<OneStopEnRoute>() {
        @Override
        public OneStopEnRoute createFromParcel(Parcel in) {
            return new OneStopEnRoute(in);
        }

        @Override
        public OneStopEnRoute[] newArray(int size) {
            return new OneStopEnRoute[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(equipmentID);
        parcel.writeInt(minutes);
        parcel.writeString(schedule);
        parcel.writeString(status);
        parcel.writeString(directionAbbr);
        parcel.writeString(direction);
        parcel.writeInt(track);
        parcel.writeInt(routeID);
    }
}
