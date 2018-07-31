package com.etatransit.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.etatransit.R;
import com.etatransit.model.Route;
import com.etatransit.model.Stop;
import com.etatransit.view.RouteListItem;
import com.etatransit.view.VerticalLabelView;
import com.splunk.mint.Mint;

import java.util.List;

/**
 * Created by mark on 10/1/14.
 */
public class RouteListAdapter extends ArrayAdapter<Route>
{
    private static final String TAG = RouteListAdapter.class.getSimpleName();


    private List<Stop> mStops = null;
    VerticalLabelView stops_text;


    public RouteListAdapter( Context context )
    {
        super( context, R.layout.view_route_list_item );
    }


    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        if( convertView == null )
        {
            convertView = new RouteListItem( parent.getContext() );
        }

        RouteListItem item = (RouteListItem) convertView;
        Route route = getItem( position );

        String route_type = "bus";
        try {
            route_type = route.vType;
        }catch (Exception e){
            route_type = "bus";
        }


        stops_text = (VerticalLabelView)convertView.findViewById(R.id.stops_text);
        if(route_type.equals("Train")) {
            stops_text.setText(" Stations");
        }else{
            stops_text.setText("Stops");
        }
        item.setRoute( route );
        item.setStops( mStops );

        return convertView;
    }


    public void setStops( List<Stop> stops )
    {
        mStops = stops;
        notifyDataSetChanged();
    }
}
