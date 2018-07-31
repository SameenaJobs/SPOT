package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Innovate on 8/19/2017.
 */
public class Schedule implements Parcelable {
    public int corridorID;
    public String scheduleNumber;
    public int scheduleID;
    public String scheduleName;
    public int stationID;
    public int patternStopID;
    public String stationName;
    public String stopTime;
    public int timePoint;
    public String weekdays;
    public String direction;
    public String directionLabel;
    public String track;

    public Schedule(){

    }
    public Schedule(Parcel source){
        corridorID = source.readInt();
        scheduleNumber = source.readString();
        scheduleID = source.readInt();
        scheduleName = source.readString();
        stationID = source.readInt();
        patternStopID = source.readInt();
        stationName = source.readString();
        stopTime = source.readString();
        timePoint = source.readInt();
        weekdays = source.readString();
        direction = source.readString();
        directionLabel = source.readString();
        track = source.readString();
    }
    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(corridorID);
        dest.writeString(scheduleNumber);
        dest.writeInt(scheduleID);
        dest.writeString(scheduleName);
        dest.writeInt(stationID);
        dest.writeInt(patternStopID);
        dest.writeString(stationName);
        dest.writeString(stopTime);
        dest.writeInt(timePoint);
        dest.writeString(weekdays);
        dest.writeString(direction);
        dest.writeString(directionLabel);
        dest.writeString(track);
    }
    public static Creator<Schedule> CREATOR = new Creator<Schedule>()
    {
        @Override
        public Schedule createFromParcel(Parcel source )
        {

            return new Schedule( source );
        }

        @Override
        public Schedule[] newArray(int size )
        {

            return new Schedule[size];
        }
    };
}
