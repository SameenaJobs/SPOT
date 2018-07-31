package com.etatransit;

import android.os.Bundle;
import android.util.Log;

import com.etatransit.event.AgenciesUpdatedEvent;
import com.etatransit.event.AgencyUpdatedEvent;
import com.etatransit.event.BusProvider;
import com.etatransit.event.CloseSelectorEvent;
import com.etatransit.event.LocationUpdatedEvent;
import com.etatransit.event.MessagesUpdatedEvent;
import com.etatransit.event.RoutesUpdatedEvent;
import com.etatransit.event.SelectedRoutesChangedEvent;
import com.etatransit.event.StopEtasUpdatedEvent;
import com.etatransit.event.StopsUpdatedEvent;
import com.etatransit.event.VehiclesUpdatedEvent;
import com.etatransit.http.EtaAgencyService;
import com.etatransit.http.EtaService;
import com.etatransit.model.Agency;
import com.etatransit.model.AgencyList;
import com.etatransit.model.AnnouncementList;
import com.etatransit.model.AnnouncementSection;
import com.etatransit.model.EtaAppModel;
import com.etatransit.model.Route;
import com.etatransit.model.RouteList;
import com.etatransit.model.Schedule;
import com.etatransit.model.ScheduleList;
import com.etatransit.model.Stop;
import com.etatransit.model.StopEta;
import com.etatransit.model.StopEtaList;
import com.etatransit.model.StopImage;
import com.etatransit.model.StopImageList;
import com.etatransit.model.StopList;
import com.etatransit.model.Vehicle;
import com.etatransit.model.VehicleList;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.splunk.mint.Mint;
import com.splunk.mint.MintLog;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by mark on 10/6/14.
 */
public class EtaAppController extends EtaApplication
{

    private static final String TAG = EtaAppController.class.getSimpleName();
    private static final String KEY_APP_MODEL = "eta_app_model";

    private static EtaAppController instance;


    public static EtaAppController getInstance()
    {
        if( instance == null )
            instance = new EtaAppController();

        return instance;
    }

    private EtaAppModel mAppModel;
    private RestAdapter mAgencyRestAdapter;
    private RestAdapter mRestAdapter;
    private EtaAgencyService mEtaAgencyService;
    private EtaService mEtaService;


    private EtaAppController()
    {
        mAppModel = new EtaAppModel();
        mAgencyRestAdapter = new RestAdapter.Builder().setEndpoint( EtaAgencyService.SERVICE_ENDPOINT ).build();
        mEtaAgencyService = mAgencyRestAdapter.create( EtaAgencyService.class );
    }


    public void onSaveInstanceState( Bundle outState ) {
        Gson gson = new Gson();
        String json = gson.toJson(mAppModel);
        outState.putString(KEY_APP_MODEL, json);
    }


    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState != null && savedInstanceState.containsKey(KEY_APP_MODEL)) {
            String json = savedInstanceState.getString(KEY_APP_MODEL);
            Gson gson = new Gson();
            mAppModel = gson.fromJson(json, EtaAppModel.class);
            createEtaService();
        }
    }


    public void registerBus()
    {
        BusProvider.getInstance().register( this );
    }


    public void unregisterBus()
    {
        BusProvider.getInstance().unregister( this );
    }


    public List<Agency> getAgencies()
    {
        return mAppModel.getAgencies();
    }


    public void setAgencies( List<Agency> agencies )
    {
        mAppModel.setAgencies( agencies );
        BusProvider.getInstance().post( new AgenciesUpdatedEvent() );
    }


    public Agency getSelectedAgency()
    {
        return mAppModel.getSelectedAgency();

    }


    public void setSelectedAgency( Agency agency )
    {
        if( agency == null ) {
            return;
        }

        if( mAppModel.getSelectedAgency() != null && agency.name == mAppModel.getSelectedAgency().name )
        {
            BusProvider.getInstance().post( new CloseSelectorEvent() );
            return;
        }

        mAppModel.setSelectedAgency( agency );
        mAppModel.setSelectedRoutes( new ArrayList<Route>() );

        createEtaService();
    }


    private void createEtaService() {
        //retrofit is weird. we have to split up endpoints and paths.
        //unfortunately that's now how we receive the service endpoint url from eta

        String serviceUrl = mAppModel.getSelectedAgency().serviceUrl;
        String endpointUrl = null;
        if (null != serviceUrl && serviceUrl.length() > 0 )
        {
            int endIndex = serviceUrl.lastIndexOf( "/" );
            if( endIndex != -1 )
            {
                endpointUrl = serviceUrl.substring( 0, endIndex );
                mAppModel.setAgencyPath("service.php" );
            }
        }

        mRestAdapter = new RestAdapter.Builder().setEndpoint( endpointUrl ).build();
        mEtaService = mRestAdapter.create( EtaService.class );
        BusProvider.getInstance().post( new AgencyUpdatedEvent() );
    }



    public List<Route> getRoutes()
    {
        return mAppModel.getRoutes();
    }


    public void setRoutes( List<Route> routes )
    {
        mAppModel.setRoutes( routes );
        BusProvider.getInstance().post( new RoutesUpdatedEvent() );
    }


    public Route getRouteForId( int routeId )
    {
        for( Route r : mAppModel.getRoutes() )
        {
            if( r != null && r.id == routeId ) {
                return r;
            }
        }

        return null;
    }


    public List<Stop> getStops()
    {
        return mAppModel.getStops();

    }


    public void setStops( List<Stop> stops )
    {

        mAppModel.setStops( stops );
        BusProvider.getInstance().post( new StopsUpdatedEvent() );
    }


    public List<AnnouncementSection> getAnnouncements()
    {
        return mAppModel.getAnnouncements();
    }


    public void setAnnouncements( List<AnnouncementSection> announcements )
    {
        mAppModel.setAnnouncements( announcements );
        BusProvider.getInstance().post( new MessagesUpdatedEvent() );
    }


    public List<Vehicle> getVehicles()
    {
        return mAppModel.getVehicles();
    }


    public void setVehicles( List<Vehicle> vehicles )
    {

        mAppModel.setVehicles( vehicles );
        BusProvider.getInstance().post( new VehiclesUpdatedEvent() );
    }


    public List<StopEta> getStopEtas()
    {
        return mAppModel.getStopEtas();
    }


    public void setStopEtas( List<StopEta> stopEtas )
    {
        mAppModel.setStopEtas( stopEtas );
        BusProvider.getInstance().post( new StopEtasUpdatedEvent() );
    }


    public List<Route> getSelectedRoutes()
    {
        return mAppModel.getSelectedRoutes();
    }

    public List<StopImage> getStopImages()
    {
        return mAppModel.getStopImages();

    }

    public void setStopImages( List<StopImage> stopImages )
    {
        mAppModel.setStopImages(stopImages);
//        BusProvider.getInstance().post( new StopsUpdatedEvent() );
    }

    public List<Schedule> getSchedule()
    {
        return mAppModel.getSchedule();
    }

    public void setSchedule(List<Schedule> schedule)
    {
        mAppModel.setSchedule(schedule);
    }


    public void addSelectedRoute( Route route )
    {
        if( mAppModel.getSelectedRoutes() == null )
            mAppModel.setSelectedRoutes( new ArrayList<Route>() );

        List<Route> selectedRoutes = mAppModel.getSelectedRoutes();
        boolean found = false;
        for( Route r : selectedRoutes )
        {
            if( r.id == route.id )
            {
                found = true;
                break;
            }
        }

        if( !found )
        {
            mAppModel.getSelectedRoutes().add( route );
            BusProvider.getInstance().post( new SelectedRoutesChangedEvent() );
        }
    }


    public void addAllRoutes()
    {
        if( mAppModel.getSelectedRoutes() == null )
            mAppModel.setSelectedRoutes( new ArrayList<Route>() );

        List<Route> selectedRoutes = mAppModel.getSelectedRoutes();
        if( selectedRoutes == null )
            return;

        selectedRoutes.clear();
        List<Route> routeList = getRoutes();

        for (int i=0;i<routeList.size();i++ ){
            selectedRoutes.add(routeList.get(i));
        }


    }


    public void removeSelectedRoute( Route route )
    {
        List<Route> selectedRoutes = mAppModel.getSelectedRoutes();
        if( selectedRoutes == null )
            return;

        for( Route r : selectedRoutes )
        {
            if( r.id == route.id )
            {
                selectedRoutes.remove( r );
                BusProvider.getInstance().post( new SelectedRoutesChangedEvent() );
                break;
            }
        }
    }


    public void clearSelectedRoutes()
    {
        List<Route> selectedRoutes = mAppModel.getSelectedRoutes();
        if( selectedRoutes == null )
            return;

        selectedRoutes.clear();


//        mAppModel.getSelectedRoutes().clear();
//        mAppModel.setSelectedRoutes( null );
//        BusProvider.getInstance().post( new SelectedRoutesChangedEvent() );
    }


    public LatLng getCurrentLocation()
    {
        return mAppModel.getCurrentLocation();
    }


    public void setCurrentLocation( LatLng latLng )
    {
        mAppModel.setCurrentLocation( latLng );
        BusProvider.getInstance().post( new LocationUpdatedEvent() );
    }


    public boolean routeIsSelected( Route route )
    {
        return routeIsSelected( route.id );
    }


    public boolean routeIsSelected( int id )
    {
        for( Route r : getSelectedRoutes() )
        {
            if( r.id == id ){
                return true;
            }
        }

        return false;
    }


    public void loadAgencies()
    {
        mEtaAgencyService.getAllAgencies(new Callback<AgencyList>() {
            @Override
            public void success(AgencyList agencyList, Response response) {
                if (agencyList != null && agencyList.get_agencies != null && !agencyList.get_agencies.isEmpty()) {
                    setAgencies(agencyList.get_agencies);
                } else {
                    Mint.leaveBreadcrumb("Load Agencies return successfully, but no Agencies were returned in list.  Application cannot continue.");
                }
            }

            @Override
            public void failure(RetrofitError error) {
                setAgencies(new ArrayList<Agency>());
                if (error != null && error.getResponse() != null)
                    Mint.leaveBreadcrumb("Error Loading Agencies : Server Status : " + error.getResponse().getStatus() + " : URL : " + error.getResponse().getUrl());
            }
        });
    }


    public void loadAllRoutesForAgency()
    {
        mEtaService.getAllRoutes(mAppModel.getAgencyPath(), new Callback<RouteList>() {
            @Override
            public void success(RouteList routeList, Response response) {
                if (routeList != null && routeList.get_routes != null && !routeList.get_routes.isEmpty()) {
                    setRoutes(routeList.get_routes);
                } else {
                    BusProvider.getInstance().post(new RoutesUpdatedEvent());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("get_routes", "error: "+error.getMessage());
                setRoutes(new ArrayList<Route>());
                if (error != null && error.getResponse() != null)
                    Mint.leaveBreadcrumb("Error Loading Routes For Agency : " + mAppModel.getSelectedAgency().name + " : Server Status : " + error.getResponse().getStatus() + " : URL : " + error.getResponse().getUrl());

            }
        });


    }


    public void loadAllStopsForAgency()
    {
        Mint.leaveBreadcrumb("EtaAppController loadAllStopsForAgency");
        mEtaService.getAllStops(mAppModel.getAgencyPath(), new Callback<StopList>() {
            @Override
            public void success(StopList stopList, Response response) {
                if (stopList != null && stopList.get_stops != null && !stopList.get_stops.isEmpty()) {
                    setStops(stopList.get_stops);

                } else {
                    BusProvider.getInstance().post(new StopsUpdatedEvent());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                setStops(new ArrayList<Stop>());
                if (error != null && error.getResponse() != null)
                    Mint.leaveBreadcrumb("Error Loading Stops For Agency : Server Status : " + error.getResponse().getStatus() + " : URL : " + error.getResponse().getUrl());
            }
        });
    }


    public void loadAnnouncements()
    {
        mEtaService.getAnnouncements(mAppModel.getAgencyPath(), new Callback<AnnouncementList>() {
            @Override
            public void success(AnnouncementList announcementList, Response response) {

                if (announcementList != null && announcementList.get_service_announcements != null) {
                    setAnnouncements(announcementList.get_service_announcements);
                } else {
                    Mint.leaveBreadcrumb("Load Announcements for Agency return successfully, but no announcements were returned in list. Selected Agency : " + mAppModel.getSelectedAgency().name);
                    BusProvider.getInstance().post(new MessagesUpdatedEvent());
                }

            }


            @Override
            public void failure(RetrofitError error) {
                setAnnouncements(new ArrayList<AnnouncementSection>());
                if (error != null && error.getResponse() != null)
                    Mint.leaveBreadcrumb("Error Loading Announcements For Agency : Server Status : " + error.getResponse().getStatus() + " : URL : " + error.getResponse().getUrl());
            }
        });
    }


    public void loadVehicles()
    {
        mEtaService.getVehicles(mAppModel.getAgencyPath(), new Callback<VehicleList>() {
            @Override
            public void success(VehicleList vehicleList, Response response) {
                if (vehicleList != null && vehicleList.get_vehicles != null) {
                    setVehicles(vehicleList.get_vehicles);
                } else {
                    Mint.leaveBreadcrumb("Load Vehicles for Agency return successfully, but no vehicles were returned in list. Selected Agency : " + mAppModel.getSelectedAgency().name);
                    BusProvider.getInstance().post(new VehiclesUpdatedEvent());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                setVehicles(new ArrayList<Vehicle>());
                if (error != null && error.getResponse() != null)
                    Mint.leaveBreadcrumb("Error Loading Vehicles For Agency : Server Status : " + error.getResponse().getStatus() + " : URL : " + error.getResponse().getUrl());
            }
        });
    }


    public void loadEtas()
    {
        mEtaService.getStopEtas(mAppModel.getAgencyPath(), new Callback<StopEtaList>() {
            @Override
            public void success(StopEtaList stopEtaList, Response response) {
                if (stopEtaList != null && stopEtaList.get_stop_etas != null) {
                    if(String.valueOf(stopEtaList.get_stop_etas.get(0).id) != null){
                        setStopEtas(stopEtaList.get_stop_etas);
                    }

                } else {
                    Exception ex = new Exception("stopEtaList is null"+response.getStatus());
                    Mint.logException(ex);
                    MintLog.w("Empty getstopetas",EtaAppController.getInstance().getSelectedAgency()+"");
                    Mint.leaveBreadcrumb("No stopetas" +response.getUrl());
                    BusProvider.getInstance().post(new StopEtasUpdatedEvent());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                setStopEtas(new ArrayList<StopEta>());
                if (error != null && error.getResponse() != null) {
                    Log.d("emptyInfo", "failure: "+error.getResponse());
                    Exception ex = new Exception("getStopEtas is null"+error.getResponse());
                    Mint.logException(ex);
                    Mint.logEvent("stopEtaList == null,URL"+error.getResponse());
                    MintLog.w("Empty getstopetas",EtaAppController.getInstance().getSelectedAgency()+"");
                    MintLog.e(TAG, "Error Loading Stop ETA's For Agency: Server Status : " + error.getResponse() + " : URL : " + error.getResponse().getUrl() + "errorrrr" + error);
                }
            }
        });
    }
    public void loadStopImagesForAgency()
    {
        mEtaService.getStopImages(mAppModel.getAgencyPath(), new Callback<StopImageList>() {

            @Override
            public void success(StopImageList stopImageList, Response response) {
                if (stopImageList != null && stopImageList.get_stopimages != null && !stopImageList.get_stopimages.isEmpty()) {
                    Mint.leaveBreadcrumb("EtaAppController loadStopImagesForAgency ");
                    setStopImages(stopImageList.get_stopimages);
                } else {
                    Mint.leaveBreadcrumb("Load Stop Images for Agency return successfully, but no stops were returned in list.");
//                    BusProvider.getInstance().post(new StopsUpdatedEvent());
                }
            }


            @Override
            public void failure(RetrofitError error) {
                setStopImages(new ArrayList<StopImage>());
//                Log.d(TAG,"stopdetailsData: 3"+error.getCause());
                if (error != null && error.getResponse() != null)
                    MintLog.e(TAG, "Error Loading Stops For Agency Server Status URL : ");
            }
        });
    }

    public void loadScheduleForAgency(int stopID)
    {
        Mint.leaveBreadcrumb("EtaAppController loadScheduleForAgency");
        mEtaService.get_schedules(mAppModel.getAgencyPath(),stopID, new Callback<ScheduleList>() {

            @Override
            public void success(ScheduleList scheduleList, Response response) {
                if (scheduleList != null && scheduleList.get_schedules != null && !scheduleList.get_schedules.isEmpty()) {
                    Mint.leaveBreadcrumb("EtaAppController loadStopImagesForAgency ");
                    setSchedule(scheduleList.get_schedules);
                } else {
                    Mint.leaveBreadcrumb("Load Schedules for Agency return successfully, but no routes were returned in list. Selected Agency : ");
//                    BusProvider.getInstance().post(new StopsUpdatedEvent());
                }
            }


            @Override
            public void failure(RetrofitError error) {
                setSchedule(new ArrayList<Schedule>());
                if (error != null && error.getResponse() != null)
                    Log.d(TAG,"stopdetailsData: 3"+error.getCause());
                    MintLog.e(TAG, "Error Loading Schedules For Agency Server Status ");
            }
        });
    }
}
