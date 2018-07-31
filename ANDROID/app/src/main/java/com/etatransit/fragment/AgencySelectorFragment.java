package com.etatransit.fragment;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.etatransit.EtaAppController;
import com.etatransit.R;
import com.etatransit.adapter.AgencySelectorAdapter;
import com.etatransit.event.AgenciesUpdatedEvent;
import com.etatransit.event.AgencySelectedEvent;
import com.etatransit.event.BusProvider;
import com.etatransit.event.LocationUpdatedEvent;
import com.etatransit.model.Agency;
import com.etatransit.util.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 9/29/14.
 */
public class AgencySelectorFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private static final String TAG = AgencySelectorFragment.class.getSimpleName();

    private static final int MAX_DISTANCE = 50;
    public static AgencySelectorFragment newInstance()
    {
        return new AgencySelectorFragment();
    }


    private ListView mListView;
    private AgencySelectorAdapter mAdapter;


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.fragment_agency_selector, container, false );
    }


    @Override
    public void onViewCreated( View view, Bundle savedInstanceState )
    {
        super.onViewCreated( view, savedInstanceState) ;
        mListView = (ListView) view.findViewById( R.id.agency_selector_list );
        mListView.setOnItemClickListener( this );
//        TextView emptyText = (TextView)view.findViewById(R.id.tvNOI);
//        emptyText.setText("Loading Agencies...");
//        mListView.setEmptyView(emptyText);
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        loadAgencies();
        initAdapter();

    }


    @Override
    public void onResume()
    {
        super.onResume();
        BusProvider.getInstance().register( this );
        EtaAppController.getInstance().loadAgencies();

    }


    @Override
    public void onPause()
    {
        BusProvider.getInstance().unregister( this );
        super.onPause();
    }

    private void loadAgencies()
    {

    }


    private void initAdapter()
    {
        mAdapter = new AgencySelectorAdapter(getActivity());
        mListView.setAdapter( mAdapter );
    }

    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
    {
        Agency agency = (Agency) mAdapter.getItem( position );
        if( agency != null )
        {
            Agency selectedAgency = EtaAppController.getInstance().getSelectedAgency();
            if( selectedAgency == null || !selectedAgency.name.equals( agency.name ) )
            {
                EtaAppController.getInstance().setSelectedAgency( agency );
                BusProvider.getInstance().post( new AgencySelectedEvent() );
            }else
            {
//                BusProvider.getInstance().post( new CloseSelectorEvent() );
                EtaAppController.getInstance().setSelectedAgency( agency );
                BusProvider.getInstance().post(new AgencySelectedEvent());
            }
        }

    }


    @Subscribe
    public void onAgenciesUpdated( AgenciesUpdatedEvent event )
    {
        mAdapter.addAllAgencies(EtaAppController.getInstance().getAgencies());
        mListView.setAdapter( mAdapter );
        onLocationUpdated( null );

    }


    @Subscribe
    public void onLocationUpdated( LocationUpdatedEvent event )
    {
        LatLng location = EtaAppController.getInstance().getCurrentLocation();
        List<Agency> agencies = EtaAppController.getInstance().getAgencies();
        List<Agency> localAgencies = new ArrayList<Agency>();
        if( location != null && agencies != null && !agencies.isEmpty() )
        {
            for( Agency a : agencies )
            {
                double distance = Utils.distance( location, a.getLocation(), 'M' );
                if( distance <= MAX_DISTANCE )
                {
                    localAgencies.add( a );
                }
            }

            mAdapter.addLocalAgencies( localAgencies );
        }

    }
}
