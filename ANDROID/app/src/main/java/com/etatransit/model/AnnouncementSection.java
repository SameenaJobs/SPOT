package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 10/13/14.
 */
public class AnnouncementSection implements Parcelable
{
    public static final String TYPE_HIGH = "high";
    public static final String TYPE_NORMAL = "normal";

    public String type;
    public List<Announcement> announcements = new ArrayList<Announcement>();


    public AnnouncementSection()
    {

    }


    public AnnouncementSection( Parcel source )
    {
        type = source.readString();
        source.readTypedList( announcements, Announcement.CREATOR );
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
        dest.writeString( type );
        dest.writeTypedList( announcements );
    }


    public static Creator<AnnouncementSection> CREATOR = new Creator<AnnouncementSection>()
    {
        @Override
        public AnnouncementSection createFromParcel( Parcel source )
        {
            return new AnnouncementSection( source );
        }

        @Override
        public AnnouncementSection[] newArray( int size )
        {
            return new AnnouncementSection[size];
        }
    };
}
