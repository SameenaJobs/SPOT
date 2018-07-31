package com.etatransit.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.etatransit.EtaAppController;
import com.etatransit.R;
import com.etatransit.event.BusProvider;
import com.etatransit.event.ViewStopsForRouteEvent;
import com.etatransit.model.Route;
import com.etatransit.model.Stop;
import com.etatransit.util.Utils;

import java.util.List;

/**
 * Created by mark on 10/2/14.
 */
public class RouteListItem extends FrameLayout
{

    private static final String TAG = RouteListItem.class.getSimpleName();


    private TextView mViewOnMap;
    private ImageView mCheckBox;
    private TextView mNameText;
    private VerticalLabelView mStopsText;
    private ViewGroup mStopButton;

    private Route mRoute;
    private List<Stop> mStops = null;


    public RouteListItem(Context context)
    {
        this(context, null);
    }

    public RouteListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RouteListItem(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        View.inflate( context, R.layout.view_route_list_item, this );

        mViewOnMap = (TextView) findViewById( R.id.view_on_map );
        mCheckBox = (ImageView) findViewById( R.id.check_box );
        mNameText = (TextView) findViewById( R.id.route_name );
        mStopsText = (VerticalLabelView) findViewById( R.id.stops_text );
        mStopButton = (ViewGroup) findViewById( R.id.stops_button );

        mStopButton.setVisibility( View.GONE );
    }


    public void setName( String name )
    {
        mNameText.setText( name );
    }


    public void setRoute( Route route )
    {
        mRoute = route;
        setName( route.name );
        updateDrawable();
        updateStopsButton();
    }


    public void setStops( List<Stop> stops )
    {
        mStops = stops;
        updateStopsButton();
    }


    private void updateDrawable()
    {
        int normalColor = Utils.convertColorStringToHex( mRoute.color );
        GradientDrawable gradientDrawable = (GradientDrawable) mCheckBox.getBackground();
        gradientDrawable.setColor( normalColor );
        mStopsText.setBackgroundColor( normalColor );
    }


    private void updateStopsButton()
    {
        if( mRoute != null && mRoute.stops != null && mRoute.stops.size() > 0 && mStops != null && mStops.size() > 0 )
        {
            mStopButton.setVisibility( View.VISIBLE );
            mStopButton.setOnClickListener( new OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    BusProvider.getInstance().post( new ViewStopsForRouteEvent( mRoute.id ) );
                }
            });
        }else
        {
            mStopButton.setVisibility( View.GONE );
        }
    }

}
