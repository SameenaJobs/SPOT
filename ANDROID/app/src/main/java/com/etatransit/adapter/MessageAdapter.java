package com.etatransit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.etatransit.R;
import com.etatransit.model.Announcement;
import com.etatransit.model.AnnouncementSection;

import java.text.ParseException;
import java.util.List;

/**
 * Created by mark on 10/13/14.
 */
public class MessageAdapter extends BaseAdapter
{
    private static final String TAG = MessageAdapter.class.getSimpleName();


    private List<AnnouncementSection> mAnnouncements;


    @Override
    public int getCount()
    {
        if( mAnnouncements == null || mAnnouncements.isEmpty() )
            return 0;



        int count = 0;
        for( AnnouncementSection section : mAnnouncements )
        {
            if( section != null && section.announcements != null && !section.announcements.isEmpty() )
            {
                count += section.announcements.size();
            }
        }

        return count;
    }


    @Override
    public Announcement getItem( int position )
    {

        //test
        if( mAnnouncements == null || mAnnouncements.isEmpty() )
            return null;

        int count = 0;
        for( AnnouncementSection section : mAnnouncements )
        {
            if( section != null && section.announcements != null )
            {
                for( Announcement a : section.announcements )
                {
                    if( count == position )
                    {
                        return a;
                    }
                    count++;
                }
            }
        }

        return null;
    }


    @Override
    public long getItemId( int position )
    {
        return 0;
    }


    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        if( convertView == null )
        {
            convertView = LayoutInflater.from( parent.getContext() ).inflate( R.layout.view_message_list_item, parent, false );
        }

        Announcement message = getItem( position );
        TextView dateText = (TextView) convertView.findViewById( R.id.date_text );
        TextView messageText = (TextView) convertView.findViewById( R.id.message_text );
        try
        {
            String dateString = message.getStartDateFormatted("EEEE, MMMM d, yyyy" );
            dateText.setText( dateString );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        messageText.setText( message.text );

        if( getIsAlertAnnouncement( position ) )
        {
            dateText.setBackgroundColor( parent.getContext().getResources().getColor( R.color.eta_message_level_high ) );
        }else
        {
            dateText.setBackgroundColor( parent.getContext().getResources().getColor( R.color.eta_message_level_normal ) );
        }

        return convertView;
    }


    private boolean getIsAlertAnnouncement( int position )
    {

        if( mAnnouncements == null || mAnnouncements.isEmpty() ) {
            return false;
        }

        int count = 0;
        for( AnnouncementSection section : mAnnouncements )
        {
            if( section != null && section.announcements != null )
            {
                for( Announcement a : section.announcements )
                {

                    if( count == position )
                    {
                        return AnnouncementSection.TYPE_HIGH.equalsIgnoreCase( section.type );
                    }
                    count++;
                }
            }
        }

        return false;
    }


    public void setData( List<AnnouncementSection> sections )
    {
        mAnnouncements = sections;
        notifyDataSetChanged();
    }
}
