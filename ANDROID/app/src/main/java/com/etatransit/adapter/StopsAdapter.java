package com.etatransit.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etatransit.EtaAppController;
import com.etatransit.R;
import com.etatransit.model.EnRoute;
import com.etatransit.model.Route;
import com.etatransit.model.Stop;
import com.etatransit.model.StopEta;
import com.etatransit.model.Vehicle;
import com.etatransit.view.RouteCircleView;
import com.splunk.mint.Mint;
import com.splunk.mint.MintLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 10/6/14.
 */
public class StopsAdapter extends ArrayAdapter<Stop>
{
    private static final String TAG = StopsAdapter.class.getSimpleName();

    private List<StopEta> mEtas;
    private String route_type = "",show_direction = "",show_platform = "";
    private int showschedule_number = 0;
    String vehicle_scheduleNumber = "";

    public StopsAdapter( Context context )
    {
        super(context, R.layout.view_stop_list_item);
        mEtas = EtaAppController.getInstance().getStopEtas();
    }


    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        ViewHolder holder = null;
        if( convertView == null )
        {
            convertView = LayoutInflater.from( getContext() ).inflate( R.layout.view_stop_list_item, parent, false );
            holder = new ViewHolder( convertView );
            convertView.setTag( holder );
        }

        if( holder == null )
            holder = (ViewHolder) convertView.getTag();

        Stop s = getItem( position );

        holder.stopName.setText( Html.fromHtml( s.name ) );
        holder.routeCircleView.setData( s.id, EtaAppController.getInstance().getRoutes() );
        if(route_type.equals("Train")) {
            holder.text_stop_routes.setText("This station is on the following routes");
            holder.img1.setImageResource(R.drawable.ic_station);
            holder.img2.setImageResource(R.drawable.ic_station);
            holder.img3.setImageResource(R.drawable.ic_station);
        }

        for( StopEta eta : mEtas )
        {
            if( eta.id == s.id )
            {

                if( eta.enRoute != null && !eta.enRoute.isEmpty() )
                {
                    holder.container1.setVisibility( View.GONE );
                    holder.container2.setVisibility( View.GONE );
                    holder.container3.setVisibility( View.GONE );
                    for( int i = 0 ; i < eta.enRoute.size(); i++ )
                    {
                        View container = null;
                        TextView textView = null;
                        ImageView img = null;
                        TextView Direction = null;
                        TextView Track = null;
                        if( i == 0 )
                        {
                            container = holder.container1;
                            textView = holder.text1;
                            img = holder.img1;
                            Track = holder.track_1;
                            Direction = holder.direction1;
                        }else if( i == 1 )
                        {
                            container = holder.container2;
                            img = holder.img2;
                            textView = holder.text2;
                            Track = holder.track_2;
                            Direction = holder.direction2;

                        }else if( i == 2 )
                        {
                            container = holder.container3;
                            img = holder.img3;
                            textView = holder.text3;
                            Track = holder.track_3;
                            Direction = holder.direction3;
                        }else
                        {
                            break;
                        }

                        if( container != null )
                        {
                            container.setVisibility( View.VISIBLE );
                            img.setVisibility( View.VISIBLE );
                            textView.setVisibility( View.VISIBLE );
                            Track.setVisibility( View.VISIBLE );
                            Direction.setVisibility( View.VISIBLE );


                            List<Vehicle> tempvehicledata = EtaAppController.getInstance().getVehicles();
                            for (Vehicle vehicledata : tempvehicledata){

                                if(eta.enRoute.get( i ).equipmentID.equals(vehicledata.equipmentID)){
                                    vehicle_scheduleNumber = vehicledata.scheduleNumber;
                                }
                            }
                            textView.setText( getTextForEnRoute( eta.enRoute.get( i ) ) );

                            Track.setText(getTextForEnRouteTrack(eta.enRoute.get(i)));
                            if(show_direction.equals("true")){
                                Direction.setText(getTextForEnRouteDirection(eta.enRoute.get(i)));
                            }
                        }


                    }
                    holder.stopsContainer.setVisibility( View.VISIBLE );
                    holder.emptyText.setVisibility( View.GONE );
                }else
                {
                    holder.stopsContainer.setVisibility( View.GONE );
                    holder.emptyText.setVisibility( View.VISIBLE );
                    if(route_type.equals("Train")) {
                        holder.emptyText.setText("No Trains In Service To This Station");
                    }
                }
            }
        }

        return convertView;
    }


    public String getTextForEnRoute( EnRoute enRoute )
    {
        if(showschedule_number == 1){
            return vehicle_scheduleNumber ;
        }else{
            return enRoute.equipmentID ;
        }


    }

    public String getTextForEnRouteDirection( EnRoute enRoute )
    {
        if( !enRoute.directionAbbr.equals(""))
        {
            return enRoute.directionAbbr;
        }else
        {
            return  "";
        }
    }

    public String getTextForEnRouteTrack( EnRoute enRoute )
    {
        String minutes = getETAString(enRoute.minutes);
        if( enRoute.minutes >= 2 ) {
            if(show_platform.equals("true") ){
                if( enRoute.track != 0){
                    return " - " + " Track " + String.valueOf(enRoute.track) + " : " + minutes;
                }else{
                    return ": Less than 2 min";
                }
            }
            else{
                return  ": " + minutes;
            }

        }else{
            if(show_platform.equals("true") ){
                if( enRoute.track != 0){
                    return " - " + " Track " + String.valueOf(enRoute.track) + " : " + minutes;
                }else{
                    return ": Less than 2 min";
                }
            }else{
                return  ": Less than 2 min";
            }


        }

    }


    @Override
    public boolean isEnabled( int position )
    {
        return false;
    }


    public void setData( int routeId )
    {
        Route route = EtaAppController.getInstance().getRouteForId( routeId );
        if (route != null) {
            try {
                route_type = route.vType;
            } catch (Exception e) {
                route_type = "bus";

            }

            try {
                show_direction = route.showDirection;
            } catch (Exception e) {
                show_direction = "";
            }

            try {
                show_platform = route.showPlatform;
            } catch (Exception e) {
                show_platform = "";
            }

            try {
                showschedule_number = route.showScheduleNumber;
            } catch (Exception e) {
                showschedule_number = 0;
            }


            List<Stop> stops = EtaAppController.getInstance().getStops();
            List<Stop> stopsForRoute = new ArrayList<Stop>();

            setNotifyOnChange(false);
            clear();
            setNotifyOnChange(true);

            for (String s : route.stops) {
                int stopId = -1;
                try {
                    stopId = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                }


                if (stopId > -1) {
                    for (Stop stop : stops) {
                        if (stop.id == stopId) {
                            stopsForRoute.add(stop);
                            break;
                        }
                    }
                }

            }

            addAll(stopsForRoute);
        }
    }


    @Override
    public void notifyDataSetChanged()
    {
        mEtas = EtaAppController.getInstance().getStopEtas();
        super.notifyDataSetChanged();
    }


    private class ViewHolder
    {
        TextView emptyText;
        TextView stopName;
        RouteCircleView routeCircleView;
        View stopsContainer;
        View container1;
        View container2;
        View container3;
        TextView text1;
        TextView text2;
        TextView text3;
        TextView text_stop_routes;
        ImageView img1,img2,img3;
        TextView direction1,direction2,direction3,track_1,track_2,track_3;


        public ViewHolder( View convertView )
        {
            emptyText = (TextView) convertView.findViewById( R.id.empty_text );
            stopName = (TextView) convertView.findViewById( R.id.stop_name );
            routeCircleView = (RouteCircleView) convertView.findViewById( R.id.route_circles );
            stopsContainer = convertView.findViewById( R.id.stop_container );
            container1 = convertView.findViewById( R.id.container_1 );
            container2 = convertView.findViewById( R.id.container_2 );
            container3 = convertView.findViewById( R.id.container_3 );
            text1 = (TextView) convertView.findViewById( R.id.text_1 );
            text2 = (TextView) convertView.findViewById( R.id.text_2 );
            text3 = (TextView) convertView.findViewById( R.id.text_3 );
            text_stop_routes = (TextView) convertView.findViewById(R.id.text_stop_routes);
            img1 = (ImageView) convertView.findViewById( R.id.img1 );
            img2 = (ImageView) convertView.findViewById( R.id.img2 );
            img3 = (ImageView) convertView.findViewById( R.id.img3 );
            direction1 = (TextView) convertView.findViewById( R.id.direction1 );
            direction2 = (TextView) convertView.findViewById( R.id.direction2 );
            direction3 = (TextView) convertView.findViewById( R.id.direction3 );

            track_1 = (TextView) convertView.findViewById( R.id.track_1 );
            track_2 = (TextView) convertView.findViewById( R.id.track_2);
            track_3 = (TextView) convertView.findViewById( R.id.track_3);

        }
    }
    private String getETAString(int minutes){
        String etaString;
        int hours;

        if (minutes > 60) {
            hours = (int)(Math.floor(minutes / 60));
            minutes = (int)Math.floor(minutes % 60);
            etaString = hours + " hr " + minutes + " mins";
        } else if(minutes < 2)
        {
            etaString = "less than 2 min";
        }
        else
            etaString = (int)Math.floor(minutes) + " mins";
        return etaString;
    }
}
