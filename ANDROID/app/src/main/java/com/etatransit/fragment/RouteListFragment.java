package com.etatransit.fragment;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.etatransit.EtaAppController;
import com.etatransit.R;
import com.etatransit.adapter.RouteListAdapter;
import com.etatransit.event.AgencyHasRoutesEvent;
import com.etatransit.event.AgencySelectedEvent;
import com.etatransit.event.BusProvider;
import com.etatransit.event.RoutesUpdatedEvent;
import com.etatransit.event.StopsUpdatedEvent;
import com.etatransit.model.Route;
import com.google.gson.Gson;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by mark on 10/1/14.
 */
public class RouteListFragment extends ListFragment
{

    private static final String TAG = RouteListFragment.class.getSimpleName();
    public static final String PREFS_SELECTED_ROUTES = "prefs_selected_routes";
    public static RouteListFragment newInstance()
    {
        return new RouteListFragment();
    }

    private RouteListAdapter mAdapter;
    private TextView selectall;
    private boolean mShouldSetFirstRouteSelected = false;
    private Handler networkHandler;
    View header;



    @Override
    public void onActivityCreated( Bundle savedInstanceState )
    {
        super.onActivityCreated( savedInstanceState );

        getListView().setChoiceMode( ListView.CHOICE_MODE_MULTIPLE );

        header = View.inflate(getActivity(),R.layout.routelist_header,null);
        selectall = (TextView) header
                .findViewById(R.id.selectall);
        getListView().addHeaderView(header);

        selectall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectall.getText().toString().equals("Select All")){

                    int itemcount = getListView().getCount()-getListView().getHeaderViewsCount();
                    for(int i=0;i < itemcount ;i++){
                        getListView().setItemChecked( i+1, true );
                    }
                    EtaAppController.getInstance().addAllRoutes();

                    selectall.setText("DeSelect All");

                    setListShown(true);
                    storeSelectedRoutes();
                }else if(selectall.getText().toString().equals("DeSelect All")) {
                    EtaAppController.getInstance().clearSelectedRoutes();
                    getListView().clearChoices();
                    selectall.setText("Select All");
                    setListShown(true);
                    storeSelectedRoutes();

                }
            }
        });
        networkHandler = new Handler();
//        checkInternetConnection();
        mAdapter = new RouteListAdapter( getActivity() );
        setListAdapter( mAdapter );

        setListShown( true );
        setEmptyText( "Loading Routes... Please wait!" );
        FindFragment currentFrag = new FindFragment();
        currentFrag.setFragment(TAG);
    }


    private void  checkInternetConnection() {
        Log.e("connected", String.valueOf(haveNetworkConnection()));
        if(haveNetworkConnection()){
            setEmptyText( "Loading Routes... Please wait!" );
        }
        else{
            setEmptyText( "Unable to connect to the internet.Please check your connection and try again." );
        }
        networkHandler.postDelayed(r,3000);
    }
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            haveNetworkConnection();
        }
    };

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
    public void onResume()
    {
        super.onResume();
        BusProvider.getInstance().register( this );

        if( EtaAppController.getInstance().getRoutes() == null || EtaAppController.getInstance().getRoutes().isEmpty() )
        {
            EtaAppController.getInstance().loadAllRoutesForAgency();
            mShouldSetFirstRouteSelected = true;
        }else
        {
            onRoutesUpdated( null );
        }

        if( EtaAppController.getInstance().getStops() == null || EtaAppController.getInstance().getStops().isEmpty() )
        {
            EtaAppController.getInstance().loadAllStopsForAgency();

        }else
        {
            onStopsUpdated( null );
        }


    }


    @Override
    public void onPause()
    {
        super.onPause();
        BusProvider.getInstance().unregister( this );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        networkHandler.removeCallbacks(r);
    }

    @Override
    public void onListItemClick( ListView l, View v, int position, long id )
    {
        super.onListItemClick( l, v, position, id );
        SparseBooleanArray checked = getListView().getCheckedItemPositions();
        if(position == 0){
            if(selectall.getText().toString().equals("Select All")){

            }
        }else{
            Route item = (Route) l.getItemAtPosition(position);

            if( checked.get( position ) )
            {
                EtaAppController.getInstance().addSelectedRoute( item );
            }else
            {
                EtaAppController.getInstance().removeSelectedRoute( item );
            }

            List<Route> selectedRoutes = EtaAppController.getInstance().getSelectedRoutes();
            int[] routeIds = new int[selectedRoutes.size()];

            if(routeIds.length == mAdapter.getCount()){
                selectall.setText("DeSelect All");
            }if(routeIds.length < mAdapter.getCount()){
                selectall.setText("Select All");
            }
        }

        storeSelectedRoutes();
    }


    public void onStopsError( RetrofitError error )
    {
        Log.e( TAG, "Error Loading Stop" );
    }


    @Subscribe
    public void onAgencySelected( AgencySelectedEvent event )
    {
        clearStoredRoutes();

        mAdapter.setNotifyOnChange( false );
        mAdapter.clear();
        mAdapter.setNotifyOnChange( true );
        mAdapter.notifyDataSetChanged();
        setListShown( false );

        EtaAppController.getInstance().loadAllRoutesForAgency();
        mShouldSetFirstRouteSelected = true;

        EtaAppController.getInstance().loadAllStopsForAgency();
    }


    @Subscribe
    public void onRoutesUpdated( RoutesUpdatedEvent event )
    {
        if( !isAdded() )
            return;

        if (checkHasRoutes()) {
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            mAdapter.setNotifyOnChange(true);
            mAdapter.addAll(EtaAppController.getInstance().getRoutes());

            if (getListView().getCount() - getListView().getHeaderViewsCount() <= 1) {
                selectall.setVisibility(View.GONE);
            } else {
                selectall.setVisibility(View.VISIBLE);
            }

            if (EtaAppController.getInstance().getSelectedRoutes() == null || EtaAppController.getInstance().getSelectedRoutes().isEmpty()) {
                for (int i = 0; i < getListView().getCount() - getListView().getHeaderViewsCount(); i++) {
                    getListView().setItemChecked(i + 1, false);
                }

                int[] ids = getRouteIds();
                if (ids != null && ids.length > 0) {
                    for (int i = 0; i < ids.length; i++) {
                        Route route = EtaAppController.getInstance().getRouteForId(ids[i]);
                        if (route != null) {
                            EtaAppController.getInstance().addSelectedRoute(route);
                            for (int j = 0; j < getListView().getCount() - getListView().getHeaderViewsCount(); j++) {
                                if (getListView().getItemAtPosition(j) != null && mAdapter.getItem(j).id == route.id) {
                                    getListView().setItemChecked(i + 1, true);
                                    break;
                                }
                            }
                        }
                    }
                    if (EtaAppController.getInstance().getSelectedRoutes().size() == mAdapter.getCount()) {
                        selectall.setText("DeSelect All");
                    } else {
                        selectall.setText("Select All");
                    }
                } else {


                    if (EtaAppController.getInstance().getRoutes() != null && EtaAppController.getInstance().getRoutes().size() > 0) {
                        EtaAppController.getInstance().addSelectedRoute(EtaAppController.getInstance().getRoutes().get(0));
                        getListView().setItemChecked(1, true);
                    }
                    if (EtaAppController.getInstance().getSelectedRoutes().size() == mAdapter.getCount()) {

                        selectall.setText("DeSelect All");
                    } else {
                        selectall.setText("Select All");
                    }
                }

            } else {
                for (int i = 0; i < getListView().getCount() - getListView().getHeaderViewsCount(); i++) {
                    getListView().setItemChecked(i + 1, false);

                    for (Route r : EtaAppController.getInstance().getSelectedRoutes()) {
                        if (r != null) {
                            if (mAdapter.getItem(i).id == r.id) {
                                getListView().setItemChecked(i + 1, true);
                                break;
                            }
                        }
                    }
                }

                if (EtaAppController.getInstance().getSelectedRoutes().size() == mAdapter.getCount()) {
                    selectall.setText("DeSelect All");
                } else {
                    selectall.setText("Select All");
                }
            }


            setListShown(true);
        }
        else
        {
//            setListShown( false );
//            mAdapter.setNotifyOnChange(false);
//            mAdapter.clear();
//            mAdapter.setNotifyOnChange(true);
            setEmptyText( "No Routes Available. Try Again Later." );
        }
    }


    @Subscribe
    public void onStopsUpdated( StopsUpdatedEvent event )
    {
        if( !isAdded() )
            return;

        mAdapter.setStops( EtaAppController.getInstance().getStops() );
    }


    private boolean checkHasRoutes()
    {
        boolean hasRoutes = ( EtaAppController.getInstance().getRoutes() != null && !EtaAppController.getInstance().getRoutes().isEmpty() );
        BusProvider.getInstance().post( new AgencyHasRoutesEvent( hasRoutes ) );
        return  hasRoutes;
    }


    private void storeSelectedRoutes(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = prefs.edit();
        List<Route> selectedRoutes = EtaAppController.getInstance().getSelectedRoutes();
        int[] routeIds = new int[selectedRoutes.size()];
        for( int i = 0; i < selectedRoutes.size(); i++) {
            routeIds[i] = selectedRoutes.get(i).id;
        }
        Gson gson = new Gson();
        String routeIdString = gson.toJson(routeIds);
        edit.putString(PREFS_SELECTED_ROUTES, routeIdString);
        edit.commit();
    }


    private int[] getRouteIds(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String jsonString = prefs.getString(PREFS_SELECTED_ROUTES, "");
        Gson gson = new Gson();
        return gson.fromJson(jsonString, int[].class);
    }


    private void clearStoredRoutes(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PREFS_SELECTED_ROUTES);
        editor.commit();
    }
}
