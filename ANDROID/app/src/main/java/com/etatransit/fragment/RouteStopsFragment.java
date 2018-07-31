package com.etatransit.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.etatransit.EtaAppController;
import com.etatransit.R;
import com.etatransit.adapter.StopsAdapter;
import com.etatransit.event.BusProvider;
import com.etatransit.event.StopEtasUpdatedEvent;
import com.etatransit.model.Route;
import com.etatransit.util.Utils;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

/**
 * Created by mark on 10/6/14.
 */
public class RouteStopsFragment extends Fragment
{
    private static final String TAG = RouteStopsFragment.class.getSimpleName();

    public static final String EXTRA_ROUTE_ID = "extra_route_id";


    public static RouteStopsFragment newInstance( int routeId )
    {
        RouteStopsFragment fragment = new RouteStopsFragment();
        Bundle args = new Bundle();
        args.putInt( EXTRA_ROUTE_ID, routeId );
        fragment.setArguments( args );
        return fragment;
    }


    private ViewGroup mStopsListContainer;
    private TextView mRouteName;
    private ViewGroup mBackButton;
    private ListView mListView;
    private StopsAdapter mAdapter;


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.fragment_route_stops, container, false );
    }


    @Override
    public void onViewCreated( View view, Bundle savedInstanceState )
    {
        super.onViewCreated( view, savedInstanceState );
        mStopsListContainer = (ViewGroup) view.findViewById( R.id.stops_list_container );
        mListView = (ListView) view.findViewById( android.R.id.list );
        mRouteName = (TextView) view.findViewById( R.id.route_name );
        mBackButton = (ViewGroup) view.findViewById( R.id.back_button );

        mStopsListContainer.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                //no-op
            }
        });
    }


    @Override
    public void onActivityCreated( Bundle savedInstanceState )
    {
        super.onActivityCreated(savedInstanceState);
        EtaAppController.getInstance().loadEtas();
        mAdapter = new StopsAdapter( getActivity() );
        int routeId = getArguments().getInt( EXTRA_ROUTE_ID );

        if(EtaAppController.getInstance().getStops() != null && routeId!=0)
        {
            mAdapter.setData( routeId );
        }

        mListView.setAdapter( mAdapter );
        Route route = EtaAppController.getInstance().getRouteForId( routeId );
        mStopsListContainer.setBackgroundColor( Utils.convertColorStringToHex( route.color ) );
        mRouteName.setText( route.name );
        mBackButton.setOnClickListener( new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                RouteStopsFragment.this.getActivity().onBackPressed();
            }
        });
    }


    @Override
    public void onResume()
    {
        super.onResume();
        BusProvider.getInstance().register( this );
    }


    @Override
    public void onPause()
    {
        super.onPause();
        BusProvider.getInstance().unregister( this );
    }

    @Subscribe
    public void onStopEtasUpdated( StopEtasUpdatedEvent event )
    {
        mAdapter.notifyDataSetChanged();
    }
}
