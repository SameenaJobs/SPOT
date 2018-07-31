package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mark on 10/13/14.
 */
public class Announcement implements Parcelable
{
    public static final String DATE_FORMAT = "yyyy-M-d HH:mm:ss";


    public String text;
    public String start;
    public String end;


    public Announcement()
    {

    }


    public Announcement( Parcel source )
    {
        text = source.readString();
        start = source.readString();
        end = source.readString();
    }



    public String getStartDateFormatted( String format ) throws ParseException
    {
        return getDateFormatted( format, start );
    }


    public String getEndDateFormatted( String format ) throws ParseException
    {
        return getDateFormatted( format, end );
    }


    private String getDateFormatted( String format, String dateString ) throws ParseException
    {
        SimpleDateFormat parser = new SimpleDateFormat( DATE_FORMAT );
        Date date = parser.parse( dateString );

        SimpleDateFormat outputFormat = new SimpleDateFormat( format );

        return outputFormat.format( date );
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
        dest.writeString( text );
        dest.writeString( start );
        dest.writeString( end );
    }


    public static Creator<Announcement> CREATOR = new Creator<Announcement>()
    {
        @Override
        public Announcement createFromParcel( Parcel source )
        {
            return new Announcement( source );
        }

        @Override
        public Announcement[] newArray( int size )
        {
            return new Announcement[size];
        }
    };
}
