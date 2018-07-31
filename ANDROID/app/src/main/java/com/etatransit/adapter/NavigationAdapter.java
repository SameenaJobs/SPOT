package com.etatransit.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etatransit.R;
import com.etatransit.activity.WebViewActivity;
import com.etatransit.event.BusProvider;
import com.etatransit.event.WebUrlClickedEvent;
import com.splunk.mint.Mint;

/**
 * Created by mark on 9/29/14.
 */
public class NavigationAdapter extends BaseAdapter
{
    @Override
    public int getCount()
    {
        return 6;
    }


    @Override
    public Object getItem( int position )
    {
        return null;
    }


    @Override
    public long getItemId( int position )
    {
        return 0;
    }


    @Override
    public View getView( int position, View convertView, final ViewGroup parent )
    {
        if( convertView == null )
        {
            if( position < getCount()-1)
                convertView = LayoutInflater.from( parent.getContext() ).inflate( R.layout.view_navigation_list_item, parent, false );
            else
                convertView = LayoutInflater.from( parent.getContext() ).inflate( R.layout.view_developed_by, parent, false );
        }

        TextView tv = (TextView) convertView.findViewById( R.id.label );
        ImageView iv = (ImageView) convertView.findViewById( R.id.image_view );

        Context context = parent.getContext();
        if( position == 0 )
        {
            tv.setText( context.getString( R.string.text_select_an_agency ) );
            iv.setImageResource( R.drawable.ic_menu_agency );
        }else if( position == 1 )
        {
            tv.setText( context.getString( R.string.text_leave_feedback ) );
            iv.setImageResource( R.drawable.ic_menu_feedback );
        }else if( position == 2 )
        {
            tv.setText( context.getString( R.string.text_rideralert ) );
            iv.setImageResource( R.drawable.alarm );
        } else if( position == 3 )
        {
            tv.setText( context.getString( R.string.text_rate_our_app ) );
            iv.setImageResource( R.drawable.ic_menu_rate );
        }else if( position == 4 )
        {
            tv.setText( context.getString( R.string.text_help ) );
            iv.setImageResource( R.drawable.ic_menu_help );
        }else if( position == 5 )
        {
//            convertView.findViewById( R.id.logo_radiant ).setOnClickListener( new View.OnClickListener()
//            {
//                @Override
//                public void onClick( View v )
//                {
//                    BusProvider.getInstance().post( new WebUrlClickedEvent( "http://www.radiantinteractive.com/" ) );
//                }
//            });
        }

        return convertView;
    }
}
