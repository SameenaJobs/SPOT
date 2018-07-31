package com.etatransit.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.etatransit.EtaAppController;
import com.etatransit.R;
import com.etatransit.activity.CreateRiderAlert;
import com.etatransit.activity.DataParser;
import com.etatransit.activity.DownloadUrl;
import com.etatransit.activity.GPSTracker;
import com.etatransit.activity.MainActivity;
import com.etatransit.adapter.ImageAdapter;
import com.etatransit.adapter.RiderAlertsAdapter;
import com.etatransit.database.DatabaseHandler;
import com.etatransit.database.FavStopsDBHandler;
import com.etatransit.event.AgencySelectedEvent;
import com.etatransit.event.BusProvider;
import com.etatransit.event.StopEtasUpdatedEvent;
import com.etatransit.event.StopsUpdatedEvent;
import com.etatransit.event.VehiclesUpdatedEvent;
import com.etatransit.model.Agency;
import com.etatransit.model.EnRoute;
import com.etatransit.model.MinutesToStop;
import com.etatransit.model.NearLocationInfo;
import com.etatransit.model.RiderAlert;
import com.etatransit.model.Route;
import com.etatransit.model.Schedule;
import com.etatransit.model.ScheduleInfo;
import com.etatransit.model.Stop;
import com.etatransit.model.StopEta;
import com.etatransit.model.StopImage;
import com.etatransit.model.Vehicle;
import com.etatransit.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.crash.FirebaseCrash;
import com.google.maps.android.PolyUtil;
import com.splunk.mint.Mint;
import com.splunk.mint.MintLog;
import com.squareup.otto.Subscribe;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import me.relex.circleindicator.CircleIndicator;
/**
 * Created by mark on 10/2/14.
 */
public class EtaMapFragment extends MapFragment implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private static final String TAG = EtaMapFragment.class.getSimpleName();
    private static final String NOT_IN_SERIVE = "NIS";

    public static EtaMapFragment newInstance() {
        return new EtaMapFragment();
    }

    private boolean mHasResumed = false;
    private boolean mMapHasSize = false;
    private Map<String, Marker> mVisibleVehicleMarkers = new HashMap<String, Marker>();
    private Map<String, Marker> mVehicleMarkers = new HashMap<String, Marker>();
    private Map<Marker, Object> mDataObjects = new HashMap<Marker, Object>();
    private Map<Integer, Polyline> mPolyLines = new HashMap<Integer, Polyline>();
    private Map<Integer, List<Marker>> mStopMarkersForRoute = new HashMap<Integer, List<Marker>>();
    private Map<Integer, List<Marker>> mArrowMarkersForRoute = new HashMap<Integer, List<Marker>>();

    private ArrayList<NearLocationInfo> nearLocInfoList = new ArrayList<NearLocationInfo>();
    String stop_show_direction = "false";
    String stop_show_platform = "false";

    TextView percentText, time_text, btime_text, status_text, bstatus_text, time1, time2, time3, btime1, btime2;
    TextView btime3, status1, status2, status3, bstatus1, bstatus2, bstatus3, track1, track2, track3, dir1, dir2;
    TextView dir3, dir_text, track_text, nearName, nearAddress, nearDistance,bdir1,bdir2,bdir3,bdir_text;

    ImageView image, image1, image2, image3, bimage, bimage1, bimage2, bimage3, square1, square2, square3, square4,nearInfoClose;
    public boolean mHasDataForStops = false;
    int showScheduleNumber = 0;
    String vehicle_scheduleNumber = "", show_vehicleCapacity = "false";
    double percentLvl;
    public Dialog dialog, infowindowDialog,nearwindowDailog;
    Button addRiderAlert;
    String selectedAgency, selectedAgencyUrl,googlePlacesData,url;

    private ArrayList<String> stopIdlist = new ArrayList<String>();
    private List<String> amenitieslist = new ArrayList<String>();
    private ArrayList<String> photoLocationList = new ArrayList<String>();
    private List<ScheduleInfo> scheduleInfoList = new ArrayList<ScheduleInfo>();
    List<RiderAlert> arrayOfList;
    public List<Integer> arrayOfFavStopList = new ArrayList<Integer>();
    RiderAlert riderAlert;
    private DatabaseHandler dbobj;
    private FavStopsDBHandler favdboj;
    Cursor cursor,stopIdCount;
    private TextView no_alerts;
    private ListView alertsList;
    LinearLayout loaddetails;
    String markerType;
    public String endpointUrl = null;
    public static String PACKAGE_NAME;
    private static ViewPager mPager;
    private static int currentPage = 0;
    private GoogleMap mMap;
    private int PROXIMITY_RADIUS = 2000;
    GoogleApiClient mGoogleApiClient;
    int type;
    List<Marker> placesMarkers = new ArrayList<Marker>();
    private ToggleButton addFavoriteButton;
    private ProgressBar mProgressBar;
    WindowManager wm;
    public DisplayMetrics metrics;
    public TextView emptyText;
    public Location mCurrentlocation;
    public Spinner filterby;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getMapAsync(this);
        Log.d(TAG, "onMapReady: Initail");
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        infowindowDialog = new Dialog(getActivity());
        infowindowDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nearwindowDailog = new Dialog(getActivity());
        nearwindowDailog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onActivityCreated(savedInstanceState);
        FindFragment currentFrag = new FindFragment();
        currentFrag.setFragment(TAG);
        PACKAGE_NAME = getActivity().getPackageName();
        final Button clearMap = (Button)getActivity().findViewById(R.id.clear);
        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            getActivity().finish();
        }
        else {
            Log.d("onCreate","Google Play Services available.");
        }
        try {
            //here you can get height of the device.
            wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        clearMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOverlays();
            }

        });

    }
    private void showStopPhotos(final ArrayList<Bitmap> list,Boolean imagesExist ) {
        mPager = (ViewPager) infowindowDialog.findViewById(R.id.pager);
        mPager.setAdapter(new ImageAdapter(getActivity(), list));

        CircleIndicator indicator = (CircleIndicator) infowindowDialog.findViewById(R.id.indicator);
        if (!imagesExist)
            indicator.setVisibility(View.GONE);
        else
            indicator.setVisibility(View.VISIBLE);
        indicator.setViewPager(mPager);

        // Auto start of viewpager
        final Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {

            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        };
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == list.size()) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };
    }
    private boolean haveInternetconnection() {
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

    public void createBusLoad() {

    }

    private void openAndQueryDatabase() {
        try {
            cursor = dbobj.fetchAllRiderAlerts();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        riderAlert = new RiderAlert();
                        String alertName = cursor.getString(cursor.getColumnIndex(DatabaseHandler.ALERT_NAME));
                        String alertId = cursor.getString(cursor.getColumnIndex(DatabaseHandler.ALERT_ID));

                        riderAlert.setAlertName(alertName);
                        riderAlert.setAlertID(alertId);

                        arrayOfList.add(riderAlert);
                    } while (cursor.moveToNext());
                }
            }
        } catch (SQLiteException se) {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        } finally {


        }
    }

    @Override
    public void onResume() {
        super.onResume();
        amenitieslist.clear();
        BusProvider.getInstance().register(this);
        mHasResumed = true;
        update();
    }


    @Override
    public void onPause() {
        super.onPause();
        dialog.dismiss();
        infowindowDialog.dismiss();
        amenitieslist.clear();
        BusProvider.getInstance().unregister(this);
        mHasResumed = false;
    }


    public void setSelectedRoutes(List<Route> selectedRoutes) {
        update();
//        Log.d("update", "setSelectedRoutes: ");
    }

    public void SelectedAgency(Agency agency) {
        selectedAgency = agency.name;
        selectedAgencyUrl = agency.serviceUrl;
    }


    private void createStops() {
        if (EtaAppController.getInstance().getStops() == null || EtaAppController.getInstance().getStops().isEmpty()) {
            return;
        }
        arrayOfFavStopList.clear();
        arrayOfFavStopList = findFavoritesFromDB();
        for (Route r : EtaAppController.getInstance().getRoutes()) {
            boolean isSelectedRoute = EtaAppController.getInstance().routeIsSelected(r);
            if (!mStopMarkersForRoute.containsKey(r.id)&&isSelectedRoute) {
                new CreateStopMarkersForRouteTask(getActivity(), mMap, r, EtaAppController.getInstance().getStops()).execute();

            }
        }
    }

    private void createArrows() {
        /*
        for( Route r : EtaAppController.getInstance().getRoutes() )
        {
            if( !mArrowMarkersForRoute.containsKey( r ) )
            {
                new CreateArrowMarkersForRouteTask( getActivity(), getMap(), r, PolyUtil.decode( r.encLine ) ).execute();
            }
        }
        */
    }


    private LatLngBounds createRoutesAndBounds() {
        if (!isAdded())
            return null;

        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean hasBounds = false;
        for (Route route : EtaAppController.getInstance().getRoutes()) {
            if (route != null)
            {
                List<LatLng> coordinates = PolyUtil.decode(route.encLine);
            if (route.encLine != "0") {
                drawRoute(coordinates, route);
                Polyline polyline = mPolyLines.get(route.id);
                if (EtaAppController.getInstance().routeIsSelected(route)) {
                    polyline.setVisible(true);
                    addToBounds(builder, coordinates);
                    hasBounds = true;
                } else {
                    polyline.setVisible(false);
                }
            }
        }
        }

        if (hasBounds)
            return builder.build();
        else
            return null;
    }


    public void update() {
        if (!isAdded() || EtaAppController.getInstance().getSelectedRoutes() == null || mMap == null || !mHasResumed)
            return;
        final LatLngBounds bounds = createRoutesAndBounds();
        favdboj = new FavStopsDBHandler(getActivity());
        createArrows();
        createStops();
        if (EtaAppController.getInstance().getVehicles() != null && !EtaAppController.getInstance().getVehicles().isEmpty()) {
            updateVehicles();
        } else {
//            Log.d("update", " clearVehicleMarkers in update");
            clearVehicleMarkers();
        }

        updateArrows();
        updateStops();
        if (bounds != null) {
            final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 25);
            if (!mMapHasSize) {
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        if (mMap == null)
                            return;
                        mMap.setOnCameraChangeListener(null);
                        if (cu != null) {
                            try {
                                mMap.animateCamera(cu);
                            } catch (Exception e) {
                                Mint.leaveBreadcrumb("Animating map threw exception.  Cannot animate map to location");
                            }
                        }
                        mMapHasSize = true;
                    }
                });
            } else {
                try {
                    mMap.animateCamera(cu);
                } catch (Exception e) {
                    Mint.leaveBreadcrumb("Animating map threw exception.  Cannot animate map to location");
                }
            }
        }
    }


    private void drawRoute(List<LatLng> coordinates, Route route) {
        if (route != null) {
            if (!isAdded() || mPolyLines.containsKey(route.id))
                return;
        }
        if (route != null) {
            int routeColor = Utils.convertColorStringToHex(route.color);
            PolylineOptions options = new PolylineOptions();
            options.color(routeColor);
            options.width(5f);
            for (LatLng coords : coordinates) {
                options.add(coords);
            }

            Polyline line = mMap.addPolyline(options);
            line.setVisible(EtaAppController.getInstance().routeIsSelected(route));
            mPolyLines.put(route.id, line);
        }
    }

    private void addToBounds(LatLngBounds.Builder builder, List<LatLng> coords) {
        for (LatLng l : coords) {
            builder.include(l);
        }
    }


    private void updateVehicles() {
        List<Vehicle> vehicles = EtaAppController.getInstance().getVehicles();
        try {
            for (Vehicle v : vehicles) {
                if (v.routeID != 777) {
                    Route route = EtaAppController.getInstance().getRouteForId(v.routeID);
                    int routeColor = 0;
                    if (v.inService >= 1 && v.nextStopID != null && v.scheduleNumber != null && !(v.scheduleNumber.equalsIgnoreCase("NIS")) && route != null && route.color != null) {
                        boolean visible = false;
                        if (EtaAppController.getInstance().routeIsSelected(v.routeID) && v.inService >= 1 && v.nextStopID != null && !(NOT_IN_SERIVE.equalsIgnoreCase(v.scheduleNumber))) {
                            visible = true;
                            routeColor = Utils.convertColorStringToHex(route.color);
                        }
                        Marker m;
                        if ( mVehicleMarkers.size()>0 && mVehicleMarkers.containsKey(v.equipmentID))
                        {
                            m = mVehicleMarkers.get(v.equipmentID);
                            if (m!=null){
                                m.setIcon(BitmapDescriptorFactory.fromBitmap(Utils.updateVehicleMarker(getActivity(),v)));
                                mDataObjects.put(m, v);
                                m.setVisible(visible);
                                m.setPosition(new LatLng(v.lat, v.lng));
                            }
                        } else if(visible) {
                            MarkerOptions options = Utils.drawVehicleMarker(getActivity(), routeColor, v);
                            m = mMap.addMarker(options);
                            m.setTag(v.equipmentID);
                            m.setVisible(visible);
                            m.setPosition(new LatLng(v.lat, v.lng));
                            mVehicleMarkers.put(v.equipmentID, m);
                            mDataObjects.put(m, v);

                        }
                        if (!visible) {
                            mVehicleMarkers.remove(v.equipmentID);
                        }
                    }
                    else if(mVehicleMarkers.containsKey(v.equipmentID)&&v.inService ==0)
                    {
                        Marker m = mVehicleMarkers.get(v.equipmentID);
                        m.remove();
                        mVehicleMarkers.remove(v.equipmentID);
                    }
                }
                else if(mVehicleMarkers.containsKey(v.equipmentID)&&v.inService ==0)
                {
                    Marker m = mVehicleMarkers.get(v.equipmentID);
                    m.remove();
                    mVehicleMarkers.remove(v.equipmentID);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void updateStops() {
        for (int routeId : mStopMarkersForRoute.keySet()) {
            boolean isSelectedRoute = EtaAppController.getInstance().routeIsSelected(routeId);
            for (Marker m : mStopMarkersForRoute.get(routeId)) {
                m.setVisible(isSelectedRoute);
            }
        }
    }


    private void updateArrows() {
        for (int routeId : mArrowMarkersForRoute.keySet()) {
            boolean isSelectedRoute = EtaAppController.getInstance().routeIsSelected(routeId);

            List<Marker> markers = mArrowMarkersForRoute.get(routeId);
            for (Marker m : markers) {
                m.setVisible(isSelectedRoute);
            }
        }
    }


    private void clearArrowMarkers() {
        for (int routeId : mArrowMarkersForRoute.keySet()) {
            List<Marker> markers = mArrowMarkersForRoute.get(routeId);
            for (Marker m : markers) {
                m.remove();
            }
            markers.clear();
        }
        mArrowMarkersForRoute.clear();
        mArrowMarkersForRoute = new HashMap<Integer, List<Marker>>();
    }


    private void clearStopMarkers() {
        for (int routeId : mStopMarkersForRoute.keySet()) {
            List<Marker> markers = mStopMarkersForRoute.get(routeId);
            for (Marker m : markers) {
                m.setVisible(false);
                m.remove();
            }
            markers.clear();
        }
        mStopMarkersForRoute.clear();
        mStopMarkersForRoute = new HashMap<Integer, List<Marker>>();
    }

    private void checkIfMultipleStopsExists(Boolean isChecked, Stop s){
        List<Stop> stops = EtaAppController.getInstance().getStops();
        for (Stop sameStop:stops)
        {
            if (s.id==sameStop.id)
            {

            }
        }
    }
    private int changeFavToStopMarkerWithId(Object tag){
        int routeIdWhereMarkerToBeSet=0;
        for (int routeId : mStopMarkersForRoute.keySet()) {
            List<Marker> markers = mStopMarkersForRoute.get(routeId);
            for (Marker m : markers) {
                if (m.getTag().equals(tag)) {
                    final Route r = EtaAppController.getInstance().getRouteForId(routeId);
                    m.setIcon(BitmapDescriptorFactory.fromBitmap(Utils.updateStopIcon(r,getActivity())));
                }
            }
        }
        return routeIdWhereMarkerToBeSet;
    }
    private void changeStopToFavMarkerWithId(Object tag) {
        for (int routeId : mStopMarkersForRoute.keySet()) {
            List<Marker> markers = mStopMarkersForRoute.get(routeId);
            for (Marker m : markers) {
                if (m.getTag().equals(tag)) {
                    Log.d("insert_fav_stops", "m.getTag: "+m.getTag());
                    m.setIcon(BitmapDescriptorFactory.fromBitmap(Utils.drawFavStop(getActivity())));
                }
            }
        }
    }

    private void clearVehicleMarkers() {

        for (String key : mVehicleMarkers.keySet()) {
            Marker m = mVehicleMarkers.get(key);
            m.remove();
        }
        mVehicleMarkers.clear();
        mVehicleMarkers = new HashMap<String, Marker>();
    }


    private void clearPolyLines() {
        for (int routeId : mPolyLines.keySet()) {
            Polyline p = mPolyLines.get(routeId);
            p.remove();
        }
        mPolyLines.clear();
        mPolyLines = new HashMap<Integer, Polyline>();
    }


    private void clearDataObjects() {
        mDataObjects.clear();
        mDataObjects = new HashMap<Marker, Object>();
    }


    @Subscribe
    public void onAgencySelected(AgencySelectedEvent event) {
        clearVehicleMarkers();
        clearArrowMarkers();
        clearStopMarkers();
        clearPolyLines();
        clearDataObjects();
        mMap.clear();
    }


    @Subscribe
    public void onVehiclesUpdated(VehiclesUpdatedEvent event) {
        if (!isAdded())
            return;

        updateVehicles();
    }


    @Subscribe
    public void onStopsUpdated(StopsUpdatedEvent event) {
        if (!isAdded())
            return;

        updateStops();
    }


    @Subscribe
    public void onStopEtasUpdated(StopEtasUpdatedEvent event) {
        if (!isAdded())
            return;

        updateStops();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged",location.getLongitude()+"onLocationChanged: "+location.getLatitude());

    }
    public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {
        Marker originMarker;
        String distance;
        LatLng originLatLng;

        @Override
        protected String doInBackground(Object... params) {
            try {
                mMap = (GoogleMap) params[0];
                url = (String) params[1];
                type = (int) params[2];
                originMarker = (Marker)params[3];
                DownloadUrl downloadUrl = new DownloadUrl();
                googlePlacesData = downloadUrl.readUrl(url);
            } catch (Exception e) {
                Log.d("GooglePlacesReadTask", e.toString());
            }
            return googlePlacesData;
        }

        @Override
        protected void onPostExecute(String result) {
            List<HashMap<String, String>> nearbyPlacesList = null;
            DataParser dataParser = new DataParser();
            if (result.contains("error_message")){
                Toast.makeText(getActivity(), "Error in accessing Places API", Toast.LENGTH_LONG).show();
            }
            else if(result.contains("ZERO_RESULTS")){
                Toast.makeText(getActivity(), "There are no such Near By Places", Toast.LENGTH_LONG).show();
            }
            else{
                nearbyPlacesList = dataParser.parse(result);
                ShowNearbyPlaces(nearbyPlacesList);
            }
        }

        private void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {
            clearOverlays();
            double oLat = originMarker.getPosition().latitude;
            double oLng = originMarker.getPosition().longitude;
            originLatLng = new LatLng(oLat,oLng);
            ArrayList<MarkerOptions> placesList = new ArrayList<MarkerOptions>();
            for (int i = 0; i < nearbyPlacesList.size(); i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                NearLocationInfo nearLocInfoItem = new NearLocationInfo();
                HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
                double lat = Double.parseDouble(googlePlace.get("lat"));
                double lng = Double.parseDouble(googlePlace.get("lng"));
                LatLng destlatLng = new LatLng(lat, lng);
                try {
                    distance = GetDistance(originLatLng,destlatLng);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String placeName = googlePlace.get("place_name");
                String vicinity = googlePlace.get("vicinity");
                markerOptions.position(destlatLng);
                markerOptions.title(placeName+ " : "+vicinity);
                markerOptions.snippet(distance);
                nearLocInfoItem.nearName = placeName;
                nearLocInfoItem.nearAddress = vicinity;
                nearLocInfoItem.nearDistance = distance;
                nearLocInfoItem.nearMarker = destlatLng;
                nearLocInfoList.add(nearLocInfoItem);
                BitmapDescriptor bm = BitmapDescriptorFactory.fromResource(type);
                markerOptions.icon(bm);
                placesList.add(markerOptions);
            }
            addAttractionsToMap(placesList);
            placesList.clear();
            infowindowDialog.dismiss();
        }
    }
    private void addAttractionsToMap(ArrayList<MarkerOptions> placeList){
//        Adding selected near by places to Map
        for (MarkerOptions m:placeList) {
            Marker attractionMarker = mMap.addMarker(m);
            attractionMarker.setTag("attraction");
            placesMarkers.add(attractionMarker);
        }
    }
    private void clearOverlays(){
        if (placesMarkers.size()>0) {
            for (Marker m : placesMarkers) {
                m.remove();
            }
            placesMarkers.clear();
            nearLocInfoList.clear();
        }
    }
    private class LoadImage extends AsyncTask<ArrayList<String>, Void, ArrayList<Bitmap>> {

        public LoadImage() {

        }

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }
        @Override
        protected ArrayList<Bitmap> doInBackground(ArrayList<String>... params) {
            try {
                ArrayList<String> passed = params[0];
                return downloadBitmap(passed);
            } catch (Exception e) {
                // log error
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> imagesList) {
            if (isCancelled()) {
                imagesList = null;
            }
            if (imagesList != null && imagesList.size()>0) {
                resetAndShowContent();
                showStopPhotos(imagesList,true);
            }

        }
        @Override
        protected void onCancelled(){

        }
        private ArrayList<Bitmap> downloadBitmap(ArrayList<String> url ) {
            ArrayList<Bitmap> imgList = new ArrayList<Bitmap>();
            HttpURLConnection urlConnection = null;
            for (int i=0;i<url.size();i++) {
                try {
                    URL uri = new URL(url.get(i));
                    urlConnection = (HttpURLConnection) uri.openConnection();
                    int statusCode = urlConnection.getResponseCode();
                    if (statusCode != HttpStatus.SC_OK) {
                        return null;
                    }

                    InputStream inputStream = urlConnection.getInputStream();
                    if (inputStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null && !(imgList.contains(bitmap))) {
                            imgList.add(bitmap);
                        }
                    }
                } catch (Exception e) {
                    urlConnection.disconnect();
                    Log.w("ImageDownloader", "Error downloading image from " + url);
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return imgList;
        }
    }
    private class CreateStopMarkersForRouteTask extends AsyncTask<Void, Void, Map<Stop, MarkerOptions>>
    {
        private Context context;
        private GoogleMap map;
        private Route route;
        private List<Stop> stops;


        public CreateStopMarkersForRouteTask( Context context, GoogleMap map, Route route, List<Stop> stops )
        {
            this.context = context.getApplicationContext();
            this.map = map;
            this.route = route;
            this.stops = stops;
        }


        @Override
        protected Map<Stop, MarkerOptions> doInBackground( Void... params )
        {
            Map<Stop, MarkerOptions> options = new HashMap<Stop, MarkerOptions>();
                if (route.color != null) {
                    int routeColor = Utils.convertColorStringToHex(route.color);
                    for (Stop s : stops) {
                        if (route.stops.contains(String.valueOf(s.id))&&s.rid==route.id) {
                            MarkerOptions opt = Utils.drawStop(context, routeColor, s);
                                options.put(s, opt);
                        }
                    }
                }
                return options;
        }


        @Override
        protected void onPostExecute( Map<Stop, MarkerOptions> stopMarkerOptionsMap ) {
            super.onPostExecute(stopMarkerOptionsMap);
            boolean isSelectedRoute = EtaAppController.getInstance().routeIsSelected(route);
                List<Marker> markers = new ArrayList<Marker>();
            for (Stop s : stopMarkerOptionsMap.keySet()) {
                MarkerOptions opt = stopMarkerOptionsMap.get(s);
                Marker m = map.addMarker(opt);
                m.setTag(s.id);
//              Replace stop marker icon if it is already selected as Favorite stop
                try {
                    if (arrayOfFavStopList.size() > 0) {
                        for (int st : arrayOfFavStopList) {
                            if (st == s.id) {
                                m.setIcon(BitmapDescriptorFactory.fromBitmap(Utils.drawFavStop(context)));
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                markers.add(m);
                m.setVisible(isSelectedRoute);
                mDataObjects.put(m, s);
            }
            mStopMarkersForRoute.put(route.id, markers);

        }
    }


    private class CreateArrowMarkersForRouteTask extends AsyncTask<Void, Void, List<MarkerOptions>>
    {
        private Route route;
        private List<LatLng> coordinates;
        private Context context;
        private GoogleMap map;


        public CreateArrowMarkersForRouteTask( Context context, GoogleMap map, Route route, List<LatLng> coordinates )
        {
            this.context = context.getApplicationContext();
            this.map = map;
            this.route = route;
            this.coordinates = coordinates;
        }


        @Override
        protected List<MarkerOptions> doInBackground(Void... params)
        {
            List<MarkerOptions> options = new ArrayList<MarkerOptions>();
            for( int i = 0; i < coordinates.size() - 1; i++ )
            {
                LatLng from = coordinates.get( i );
                LatLng to = coordinates.get( i + 1 );
                options.add(Utils.drawArrowHead( context, Utils.convertColorStringToHex( route.color ), from, to ) );
            }

            return options;
        }


        @Override
        protected void onPostExecute( List<MarkerOptions> arrowMarkerOptions )
        {
            super.onPostExecute( arrowMarkerOptions );
            boolean isSelectedRoute = EtaAppController.getInstance().routeIsSelected( route );
            List<Marker> markers = new ArrayList<Marker>();
            for( MarkerOptions options : arrowMarkerOptions )
            {
                Marker m = map.addMarker( options );
                markers.add( m );

                m.setVisible( isSelectedRoute );
            }

            mArrowMarkersForRoute.put( route.id, markers );
        }
    }

    private String getComments(int stopID)
    {
        String comment = "No comments available";
        List<StopImage> stopdetailsData = EtaAppController.getInstance().getStopImages();
        for (StopImage s : stopdetailsData)  {
            if (s.stopid == stopID) {
                if (s.comments!=null && s.comments!="")
                    comment = s.comments;
            }
        }
        return comment;
    }
    private String getStopDescription(int stopID)
    {
        String desc = "";
        List<StopImage> stopdetailsData = EtaAppController.getInstance().getStopImages();
        for (StopImage s : stopdetailsData)  {
            if (s.stopid == stopID) {
                if (s.stoplocdesc!=null)
                    desc = s.stoplocdesc;
            }
        }
        return desc;
    }
    private String getStopType(int stopID)
    {
        String type = "Basic";
        List<StopImage> stopdetailsData = EtaAppController.getInstance().getStopImages();
        for (StopImage s : stopdetailsData)  {
            if (s.stopid == stopID) {
                if (s.type!=null) {
                    type = s.type;
                }
                else
                    type = "Basic";
            }
        }
        return type;
    }
    private List<String> getAmenities(int stopID)
    {
        List<String> amenitiesArray = new ArrayList<String>();
        List<StopImage> stopdetailsData = EtaAppController.getInstance().getStopImages();
        for (StopImage s : stopdetailsData)  {
            if (s.stopid == stopID && s.amenities!=null) {
                String string[] =s.amenities.split(",");
                for(String y:string){
                    y=y.replaceAll("[-()/ ]","");
                    amenitiesArray.add(y);
                }
            }
        }
        return amenitiesArray;
    }
    private ArrayList<String> getStopPhotos(int stopID)
    {
        ArrayList<String> photoLocations = new ArrayList<String>();
        String serviceUrl = EtaAppController.getInstance().getSelectedAgency().serviceUrl;
        if (null != serviceUrl && serviceUrl.length() > 0 )
        {
            int endIndex = serviceUrl.lastIndexOf( "/" );
            if( endIndex != -1 )
            {
                endpointUrl = serviceUrl.substring( 0, endIndex );
            }
        }
        List<StopImage> stopdetailsData = EtaAppController.getInstance().getStopImages();
        for (StopImage s : stopdetailsData)  {
            if (s.stopid == stopID && s.images!=null) {
                for (int i=0;i<s.images.size();i++) {
                    if (s.images.get(i) != null)
                    {
                        String url = s.images.get(i);
                        url = endpointUrl.concat("/" + url);
                        if (s.images.get(i) != null && !(s.images.get(i) == ""))
                            photoLocations.add(url);
                    }
                }
            }
        }
        return photoLocations;
    }
    private List<ScheduleInfo> getCorridorName(int stopID)
    {
        scheduleInfoList.clear();
        List<Schedule> schddetailsData =EtaAppController.getInstance().getSchedule();
        List<Route> routeDetailsData = EtaAppController.getInstance().getRoutes();
        for (Schedule s : schddetailsData)  {
            for (int i=0;i<routeDetailsData.size();i++) {
                if(s.corridorID == routeDetailsData.get(i).id)
                {
                    ScheduleInfo sDetails = new ScheduleInfo();
                    sDetails.routeName = routeDetailsData.get(i).name;
                    sDetails.scheduleTime = s.stopTime;
                    scheduleInfoList.add(sDetails);
                }
            }

        }
        return scheduleInfoList;
    }
    private String getInfoWindowItemStopText( MinutesToStop item, Stop stop )
    {
        if( item.minutes >= 2 ) {
            return stop.name+"  ";
        }else
        {
            return stop.name+"  ";
        }
    }

    private String getInfoWindowItemStopText_Singleline( MinutesToStop item, Stop stop )
    {
        if( item.minutes >= 2 ) {
            String convertedMinutes = getETAString(item.minutes);
            return stop.name +"  "+ convertedMinutes;
        }else
        {
            return stop.name + "   Less than 2 mins";
        }
    }



    private String  getInfoWindowItemStopText_Time( MinutesToStop item, Stop stop )
    {
        if(!item.schedule.equals("")){
            time_text.setVisibility(View.VISIBLE);

            return    item.schedule;
        }

        return "";

    }

    private String  getInfoWindowItemStopText_Status( MinutesToStop item, Stop stop )
    {
        if(!item.status.equals("")) {
            status_text.setVisibility(View.VISIBLE);

            return  item.status;
        }
        return  "";
    }

    private String  getInfoWindowItemStopText_Track( MinutesToStop item, Stop stop )
    {
        if(!item.track.equals("") ) {
            track_text.setVisibility(View.VISIBLE);

            return  item.track;
        }
        return  "";
    }

    private String getInfoWindowItemEnRouteText( EnRoute enRoute )
    {
        if(showScheduleNumber == 1) {
            if( enRoute.minutes >= 2 ) {
                return vehicle_scheduleNumber+"   ";
            }else
            {
                return vehicle_scheduleNumber+"   ";
            }

        }else{
            if( enRoute.minutes >= 2 ) {
                return enRoute.equipmentID+"   ";
            }else
            {
                return enRoute.equipmentID+"   " ;
            }
        }


    }

    private String getInfoWindowItemEnRouteRouteName( EnRoute enRoute )
    {
        Route route = EtaAppController.getInstance().getRouteForId( enRoute.routeID );
        if(!route.name.equals(null) || route != null)
            return route.name.trim();
        else
            return null;

    }
    private String getInfoWindowItemEnRouteRouteType( EnRoute enRoute )
    {
        Route route = EtaAppController.getInstance().getRouteForId( enRoute.routeID );
        List<Vehicle> vehiclesList = EtaAppController.getInstance().getVehicles();
        for (Vehicle v:vehiclesList)
        {
            if (v.equipmentID == enRoute.equipmentID) {
                return v.vehicleType;
            }

        }
        if(route != null && !(route.vType.equals(null)) )
            return route.vType.trim();
        else
            return null;

    }
    private String getInfoWindowItemEnRouteText_Single( EnRoute enRoute )
    {
        if(enRoute.minutes != 0) {
            if (showScheduleNumber == 1) {
                if (enRoute.minutes >= 2) {
                    String convertedMinutes = getETAString(enRoute.minutes);
                    return vehicle_scheduleNumber + ": " +convertedMinutes;
                } else {
                    return vehicle_scheduleNumber + ": Less than 2 mins";
                }

            } else {
                if (enRoute.minutes >= 2) {
                    String convertedMinutes = getETAString(enRoute.minutes);
                    return enRoute.equipmentID + ": " +convertedMinutes;
                } else {
                    return enRoute.equipmentID + ": Less than 2 mins";
                }
            }
        }
        else
        {
            if (showScheduleNumber == 1) {
                return vehicle_scheduleNumber + ": Less than 2 mins";
            } else {
                return enRoute.equipmentID + ": Less than 2 mins";
            }

        }

    }

    private String getInfoWindowItemEnRouteSchedule( EnRoute enRoute )
    {
        if( !enRoute.schedule.equals("") ) {
            time_text.setVisibility(View.VISIBLE);
            return enRoute.schedule+"   ";
        }

        return  "";
    }

    private String getInfoWindowItemEnRouteDirection( EnRoute enRoute )
    {
        if( !enRoute.directionAbbr.equals("") ) {
            dir_text.setVisibility(View.VISIBLE);
            return enRoute.directionAbbr+"   ";
        }

        return  "";
    }

    private String getInfoWindowItemEnRouteTrack( EnRoute enRoute )
    {
        if( enRoute.track != 0 ) {
            track_text.setVisibility(View.VISIBLE);
            return enRoute.track+"   ";
        }

        return  "";
    }

    private String getInfoWindowItemEnRouteStatus( EnRoute enRoute )
    {
        if(!enRoute.status.equals("") ) {
            status_text.setVisibility(View.VISIBLE);
            return enRoute.status+"   ";
        }

        return  "";
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        update();
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @SuppressWarnings("deprecation")
            @Override
            public boolean onMarkerClick(final Marker marker) {

                dbobj = new DatabaseHandler(getActivity());
                arrayOfList = new ArrayList<>();
                infowindowDialog.setContentView(R.layout.view_info_window);
                nearwindowDailog.setContentView(R.layout.custom_info_window);
                final LinearLayout nearMarkerLayout = (LinearLayout) nearwindowDailog.findViewById(R.id.marker_content);
                nearName = (TextView)nearwindowDailog.findViewById(R.id.name);
                nearAddress = (TextView)nearwindowDailog.findViewById(R.id.address);
                nearDistance = (TextView)nearwindowDailog.findViewById(R.id.distance);
                nearInfoClose = (ImageView)nearwindowDailog.findViewById(R.id.nearwindowClose);
                boolean shouldReturnView = false;
                mHasDataForStops = false;
                final View titleBar = infowindowDialog.findViewById(R.id.title_bar);
                TextView title = (TextView) infowindowDialog.findViewById(R.id.title);
                filterby = (Spinner) infowindowDialog.findViewById(R.id.filterby);
                try {
                    Field popup = Spinner.class.getDeclaredField("mPopup");
                    popup.setAccessible(true);
                    android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(filterby);
                    popupWindow.setHeight(275);
                }
                catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
                }
                List<String> list = new ArrayList<String>();
                list.add("Filter By");
                list.add("School");
                list.add("Restaurant");
                list.add("Hospital");
                list.add("Park");
                list.add("Spa");
                list.add("Bank");
                list.add("Bar");
                list.add("Movie Theater");
                list.add("Night Club");
                list.add("Gym");
                list.add("ATM");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, list);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                filterby.setAdapter(adapter);

                filterby.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(
                                    AdapterView<?> parent, View view, int position, long id) {
                                // On selecting a spinner item
                                String item = parent.getItemAtPosition(position).toString();
                                filterby.setBackgroundResource(R.drawable.spinner);
                                if (!item.replaceAll(" ","").equalsIgnoreCase("filterby")) {
                                    String typeOfAttraction =item.replaceAll(" ","").toLowerCase();
                                    String url = getUrl(marker.getPosition().latitude, marker.getPosition().longitude,typeOfAttraction);
                                    Object[] DataTransfer = new Object[4];
                                    DataTransfer[0] = mMap;
                                    DataTransfer[1] = url;
                                    DataTransfer[2] = getIconType(typeOfAttraction);
                                    DataTransfer[3] = marker;
                                    new GetNearbyPlacesData().execute(DataTransfer);
                                    Toast.makeText(getActivity(), item + " is Selected", Toast.LENGTH_LONG).show();
                                }
                            }
                            public void onNothingSelected(AdapterView<?> parent) {
                                filterby.setBackgroundResource(R.drawable.spinner);
                                Log.d("spinner", "onNothingSelected: ");
                            }
                        });
                final LinearLayout routedetails = (LinearLayout) infowindowDialog.findViewById(R.id.route_details);
                final LinearLayout stopdetails = (LinearLayout) infowindowDialog.findViewById(R.id.stop_details);
                TextView stopTypeVal = (TextView) infowindowDialog.findViewById(R.id.stopTypeVal);
                ImageView stopTypeImage = (ImageView) infowindowDialog.findViewById(R.id.stopTypeImage);
                final Button directionDetails = (Button) infowindowDialog.findViewById(R.id.Directions);
                directionDetails.setPaintFlags(directionDetails.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                final LinearLayout adddetails = (LinearLayout) infowindowDialog.findViewById(R.id.Add);
                Button scheduleDetails = (Button) infowindowDialog.findViewById(R.id.ViewScheduleInfo);
                scheduleDetails.setPaintFlags(scheduleDetails.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                final LinearLayout amenityDetails = (LinearLayout) infowindowDialog.findViewById(R.id.Amenities);
                HorizontalScrollView horizontalScrollView1 = (HorizontalScrollView) infowindowDialog.findViewById(R.id.horizontalScrollView1);
                final LinearLayout slider=(LinearLayout) infowindowDialog.findViewById(R.id.slider);
                final LinearLayout Photos_Stop=(LinearLayout) infowindowDialog.findViewById(R.id.Photos_Stop);
                final LinearLayout scheduledialog = (LinearLayout)infowindowDialog.findViewById(R.id.scheduledialog);
                final LinearLayout stopDescription= (LinearLayout)infowindowDialog.findViewById(R.id.stopDescription);
                final LinearLayout commentTitle = (LinearLayout)infowindowDialog.findViewById(R.id.commentHeaderLayout);
                final LinearLayout commentDetails = (LinearLayout)infowindowDialog.findViewById(R.id.commentsBodyLayout);
                final TableLayout schedule_container = (TableLayout) infowindowDialog.findViewById(R.id.schedule_container);
                final TextView scheduleTitle = (TextView)infowindowDialog.findViewById(R.id.scheduleTitle);
                final TextView noScheduleInfo = (TextView)infowindowDialog.findViewById(R.id.noScheduleInfo);
                loaddetails = (LinearLayout) infowindowDialog.findViewById(R.id.load_details);
                percentText = (TextView)infowindowDialog.findViewById(R.id.busloadPercent);
                square1 =(ImageView)infowindowDialog.findViewById(R.id.box1);
                square2 =(ImageView)infowindowDialog.findViewById(R.id.box2);
                square3 =(ImageView)infowindowDialog.findViewById(R.id.box3);
                square4 =(ImageView)infowindowDialog.findViewById(R.id.box4);
                final LinearLayout attractionDetails = (LinearLayout) infowindowDialog.findViewById(R.id.Attraction);
                TextView direction_full = (TextView) infowindowDialog.findViewById(R.id.direction_full);
                TextView stopDesc = (TextView) infowindowDialog.findViewById(R.id.stopDesc);
                mProgressBar = (ProgressBar) infowindowDialog.findViewById( R.id.progress_bar );
                TextView textView1 = (TextView) infowindowDialog.findViewById(R.id.text_1); // Train name
                TextView textView2 = (TextView) infowindowDialog.findViewById(R.id.text_2);
                TextView textView3 = (TextView) infowindowDialog.findViewById(R.id.text_3);

                TextView textViewb1 = (TextView) infowindowDialog.findViewById(R.id.btext_1); //Stops name
                TextView textViewb2 = (TextView) infowindowDialog.findViewById(R.id.btext_2);
                TextView textViewb3 = (TextView) infowindowDialog.findViewById(R.id.btext_3);

                emptyText = (TextView) infowindowDialog.findViewById(R.id.empty_text);

                ImageView infowindowClose = (ImageView) infowindowDialog.findViewById(R.id.infowindowClose);
                addFavoriteButton =(ToggleButton)infowindowDialog.findViewById(R.id.star);
                infowindowClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        infowindowDialog.dismiss();
                    }
                });
                nearInfoClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nearwindowDailog.dismiss();
                    }
                });

                TextView routeText = (TextView) infowindowDialog.findViewById(R.id.Routetext); //Train table header 'Route'
                TextView brouteText = (TextView) infowindowDialog.findViewById(R.id.bRoutetext); //Bus table header 'Route'

                TextView route1 = (TextView) infowindowDialog.findViewById(R.id.route_1); //Table values for trains
                TextView route2 = (TextView) infowindowDialog.findViewById(R.id.route_2);
                TextView route3 = (TextView) infowindowDialog.findViewById(R.id.route_3);

                TextView broute1 = (TextView) infowindowDialog.findViewById(R.id.broute_1); //Table values for buses
                TextView broute2 = (TextView) infowindowDialog.findViewById(R.id.broute_2);
                TextView broute3 = (TextView) infowindowDialog.findViewById(R.id.broute_3);

                time1 = (TextView) infowindowDialog.findViewById(R.id.time_1); // time values for trains
                time2 = (TextView) infowindowDialog.findViewById(R.id.time_2);
                time3 = (TextView) infowindowDialog.findViewById(R.id.time_3);

                btime1 = (TextView) infowindowDialog.findViewById(R.id.btime_1); // time value for buses
                btime2 = (TextView) infowindowDialog.findViewById(R.id.btime_2);
                btime3 = (TextView) infowindowDialog.findViewById(R.id.btime_3);

                time_text = (TextView) infowindowDialog.findViewById(R.id.time); //Train table header 'Time'
                btime_text = (TextView) infowindowDialog.findViewById(R.id.btime); //Train table header 'Bus'
                TextView eta_text = (TextView) infowindowDialog.findViewById(R.id.text); //Train table header 'Train'
                TextView beta_text = (TextView) infowindowDialog.findViewById(R.id.btext); //Bus table header 'Stop'
                status_text = (TextView) infowindowDialog.findViewById(R.id.status); //Train table header 'Status'
                bstatus_text = (TextView) infowindowDialog.findViewById(R.id.bstatus); //Bus table header 'Status'

                dir_text = (TextView) infowindowDialog.findViewById(R.id.dir_text); //Train table header 'Dir'
                bdir_text = (TextView) infowindowDialog.findViewById(R.id.bdir); //Bus table header 'Dir'
                track_text = (TextView) infowindowDialog.findViewById(R.id.track_text); //Train table header 'Track'


                status1 = (TextView) infowindowDialog.findViewById(R.id.status_1); //status values for trains
                status2 = (TextView) infowindowDialog.findViewById(R.id.status_2);
                status3 = (TextView) infowindowDialog.findViewById(R.id.status_3);

                bstatus1 = (TextView) infowindowDialog.findViewById(R.id.bstatus_1); //status values for buses
                bstatus2 = (TextView) infowindowDialog.findViewById(R.id.bstatus_2);
                bstatus3 = (TextView) infowindowDialog.findViewById(R.id.bstatus_3);


                track1 = (TextView) infowindowDialog.findViewById(R.id.track_1); //track values for trains
                track2 = (TextView) infowindowDialog.findViewById(R.id.track_2);
                track3 = (TextView) infowindowDialog.findViewById(R.id.track_3);


                dir1 = (TextView) infowindowDialog.findViewById(R.id.dir_1); //direction values for trains
                dir2 = (TextView) infowindowDialog.findViewById(R.id.dir_2);
                dir3 = (TextView) infowindowDialog.findViewById(R.id.dir_3);

                bdir1 = (TextView) infowindowDialog.findViewById(R.id.bdir_1); //direction values for buses
                bdir2 = (TextView) infowindowDialog.findViewById(R.id.bdir_2);
                bdir3 = (TextView) infowindowDialog.findViewById(R.id.bdir_3);

                final TextView commentsTextView=(TextView) infowindowDialog.findViewById(R.id.commentsTextView);

                image = (ImageView) infowindowDialog.findViewById(R.id.image);
                image1 = (ImageView) infowindowDialog.findViewById(R.id.image_1); // train images for stops
                image2 = (ImageView) infowindowDialog.findViewById(R.id.image_2);
                image3 = (ImageView) infowindowDialog.findViewById(R.id.image_3);
//                ImageView directionsfrom = (ImageView) infowindowDialog.findViewById(R.id.directionsfrom);
                bimage1 = (ImageView) infowindowDialog.findViewById(R.id.bimage_1); // bus images for stops
                bimage2 = (ImageView) infowindowDialog.findViewById(R.id.bimage_2);
                bimage3 = (ImageView) infowindowDialog.findViewById(R.id.bimage_3);
                bimage = (ImageView) infowindowDialog.findViewById(R.id.bimage);


                final View stopContainer = infowindowDialog.findViewById(R.id.stop_container); //Table for trains
                final View bstopContainer = infowindowDialog.findViewById(R.id.bstop_container); //Table for buses
                View etaContainer = infowindowDialog.findViewById(R.id.container);
                View etaContainer1 = infowindowDialog.findViewById(R.id.container_1); // 3 routes for trains
                View etaContainer2 = infowindowDialog.findViewById(R.id.container_2);
                View etaContainer3 = infowindowDialog.findViewById(R.id.container_3);
                View betaContainer = infowindowDialog.findViewById(R.id.bcontainer);
                View betaContainer1 = infowindowDialog.findViewById(R.id.bcontainer_1); // 3 routes for buses
                View betaContainer2 = infowindowDialog.findViewById(R.id.bcontainer_2);
                View betaContainer3 = infowindowDialog.findViewById(R.id.bcontainer_3);
                slider.setVisibility(View.GONE);
                final Button riderAlerts = (Button) infowindowDialog.findViewById(R.id.rideralerts);
                final ImageView left = (ImageView) infowindowDialog.findViewById(R.id.left);
                final ImageView stopPhotoArrow = (ImageView) infowindowDialog.findViewById(R.id.stopPhotoArrow);
                final ImageView infoArrow = (ImageView) infowindowDialog.findViewById(R.id.infoArrow);
                left.setVisibility(View.GONE);
                scheduledialog.setVisibility(View.GONE);
                schedule_container.setVisibility(View.GONE);
                if (mVehicleMarkers.containsKey(marker.getTag())) {
                    stopDescription.setVisibility(View.GONE);
                    stopdetails.setVisibility(View.GONE);
                    adddetails.setVisibility(View.GONE);
                    directionDetails.setVisibility(View.GONE);
                    scheduleDetails.setVisibility(View.GONE);
                    amenityDetails.setVisibility(View.GONE);
                    Photos_Stop.setVisibility(View.GONE);
                    shouldReturnView = true;
                    Vehicle vehicle = (Vehicle) mDataObjects.get(marker);
                    markerType = "vehicle";
                    Route route = EtaAppController.getInstance().getRouteForId(vehicle.routeID);
                    if (route != null) {
                        String route_type = "bus";
                        String type = "";
                        String show_direction = "false";
                        String show_platform = "false";

                        try {
                            route_type = route.vType;
                        } catch (Exception e) {
                            route_type = "bus";
                        }
                        try {
                            type = route.type;

                        } catch (Exception e) {
                            type = "";
                        }
                        try {

                            show_direction = route.showDirection;

                        } catch (Exception e) {
                            show_direction = "false";

                        }
                        try {

                            show_platform = route.showPlatform;
                        } catch (Exception e) {

                            show_platform = "false";

                        }
                        try {

                            show_vehicleCapacity = route.showVehicleCapacity;
                        } catch (Exception e) {

                            show_vehicleCapacity = "false";

                        }
                        try {

                            showScheduleNumber = route.showScheduleNumber;
                        } catch (Exception e) {

                            showScheduleNumber = 0;

                        }

                        if (showScheduleNumber == 1) {
                            title.setText(vehicle.scheduleNumber);
                        } else {
                            title.setText(vehicle.equipmentID);
                        }

                        if (show_vehicleCapacity.equals("false"))
                            loaddetails.setVisibility(View.GONE);
                        else
                            loaddetails.setVisibility(View.VISIBLE);
                        if (route_type.equals("Train")) {
                            image1.setImageResource(R.drawable.ic_station);
                            image2.setImageResource(R.drawable.ic_station);
                            image3.setImageResource(R.drawable.ic_station);
                            emptyText.setText("Train Not in Service");
                            eta_text.setText("Station  ");
                            beta_text.setText("Stop  ");

                        } else {
                            emptyText.setText("Bus Not in service");
                        }
                        titleBar.setBackgroundColor(Utils.convertColorStringToHex(route.color));
                        //Forming Bus load Content
                        loaddetails.setBackgroundColor(Utils.convertColorStringToHex(route.color));
//                        createBusLoad();
                        double capacity = vehicle.load / vehicle.capacity;
                        percentLvl = (vehicle.load / vehicle.capacity) * 100;
                        if (percentLvl > 100)
                            percentLvl = 100;
                        else if (percentLvl == 0.0)
                            percentLvl = 0;
                        else if (Double.isNaN(percentLvl))
                            percentLvl = 0;
                        else
                            percentLvl = Math.round(percentLvl);
                        if (capacity > 0.25 && capacity <= 0.5) {
                            square1.setBackgroundColor(Utils.convertColorStringToHex("#387A36"));
                            square2.setBackgroundColor(Utils.convertColorStringToHex("#387A36"));
                        } else if (capacity <= 0.25 && capacity > 0.0)
                            square1.setBackgroundColor(Utils.convertColorStringToHex("#387A36"));
                        else if (capacity > 0.5 && capacity <= 0.75) {
                            square1.setBackgroundColor(Utils.convertColorStringToHex("#387A36"));
                            square2.setBackgroundColor(Utils.convertColorStringToHex("#387A36"));
                            square3.setBackgroundColor(Utils.convertColorStringToHex("#FF8A00"));
                        } else if (capacity > 0.75) {
                            square1.setBackgroundColor(Utils.convertColorStringToHex("#387A36"));
                            square2.setBackgroundColor(Utils.convertColorStringToHex("#387A36"));
                            square3.setBackgroundColor(Utils.convertColorStringToHex("#FF8A00"));
                            square4.setBackgroundColor(Utils.convertColorStringToHex("#E32F21"));
                        }
                        List boxes = new ArrayList();
                        boxes.add(square1);
                        boxes.add(square2);
                        boxes.add(square3);
                        boxes.add(square4);
                        percentText.setText((int) percentLvl + "% Full");
                        List<MinutesToStop> minutesToStops = vehicle.getMinutesToNextStopList();
                        if (minutesToStops != null && !minutesToStops.isEmpty()) {
                            List<Stop> stops = EtaAppController.getInstance().getStops();
                            int i, count = 0;
                            MinutesToStop item;
                            for (i = 0; i < minutesToStops.size(); i++) {
                                item = minutesToStops.get(i);
                                if (item.minutes != 0) {
                                    for (Stop s : stops) {
                                        if (item.stopID == s.id) {
                                            if (show_direction.equals("true")) {
                                                direction_full.setText(" - " + item.direction);
                                            }
                                            if (count == 0) {
                                                if (type.equals("Inbound") || type.equals("Schedule")) {
                                                    if (route_type.equals("Train")) {
                                                        image1.setVisibility(View.VISIBLE);
                                                        time1.setVisibility(View.VISIBLE);
                                                        status1.setVisibility(View.VISIBLE);
                                                        routeText.setVisibility(View.VISIBLE);
                                                        track1.setVisibility(View.VISIBLE);
                                                        route1.setVisibility(View.VISIBLE);
                                                        route1.setText(getInfoWindowItemStopText(item, s));
                                                        time1.setText(getInfoWindowItemStopText_Time(item, s));
                                                        status1.setText(getInfoWindowItemStopText_Status(item, s));
                                                        routeText.setText("Station");
                                                        if (show_platform.equals("true")) {
                                                            track1.setText(getInfoWindowItemStopText_Track(item, s));
                                                        }
                                                    } else {
                                                        bimage1.setVisibility(View.VISIBLE);
                                                        btime1.setVisibility(View.VISIBLE);
                                                        bstatus1.setVisibility(View.VISIBLE);
                                                        broute1.setVisibility(View.VISIBLE);
                                                        brouteText.setVisibility(View.VISIBLE);
                                                        bstatus_text.setVisibility(View.VISIBLE);
                                                        brouteText.setText("Stop");
                                                        btime_text.setVisibility(View.VISIBLE);
                                                        broute1.setText(getInfoWindowItemStopText(item, s));
                                                        btime1.setText(getInfoWindowItemStopText_Time(item, s));
                                                        bstatus1.setText(getInfoWindowItemStopText_Status(item, s));
                                                    }

                                                } else {
                                                    if (route_type.equals("Train")) {
                                                        eta_text.setText("Station & ETA in minutes");
                                                        textView1.setText(getInfoWindowItemStopText_Singleline(item, s));
                                                        eta_text.setVisibility(View.VISIBLE);
                                                        image1.setVisibility(View.VISIBLE);
                                                    } else {
                                                        beta_text.setText("Stop & ETA in minutes ");
                                                        broute1.setVisibility(View.VISIBLE);
                                                        broute1.setText(getInfoWindowItemStopText_Singleline(item, s));
                                                        beta_text.setVisibility(View.VISIBLE);
                                                        bimage1.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            } else if (count == 1) {
                                                if (type.equals("Inbound") || type.equals("Schedule")) {
                                                    if (route_type.equals("Train")) {
                                                        image2.setVisibility(View.VISIBLE);
                                                        time2.setVisibility(View.VISIBLE);
                                                        status2.setVisibility(View.VISIBLE);
                                                        route2.setVisibility(View.VISIBLE);
                                                        track2.setVisibility(View.VISIBLE);
                                                        route2.setText(getInfoWindowItemStopText(item, s));
                                                        time2.setText(getInfoWindowItemStopText_Time(item, s));
                                                        status2.setText(getInfoWindowItemStopText_Status(item, s));
                                                        if (show_platform.equals("true")) {
                                                            track2.setText(getInfoWindowItemStopText_Track(item, s));
                                                        }
                                                    } else {
                                                        bimage2.setVisibility(View.VISIBLE);
                                                        btime2.setVisibility(View.VISIBLE);
                                                        bstatus2.setVisibility(View.VISIBLE);
                                                        broute2.setVisibility(View.VISIBLE);
                                                        broute2.setText(getInfoWindowItemStopText(item, s));
                                                        btime2.setText(getInfoWindowItemStopText_Time(item, s));
                                                        bstatus2.setText(getInfoWindowItemStopText_Status(item, s));

                                                    }
                                                } else {
                                                    if (route_type.equals("Train")) {
                                                        eta_text.setText("Station & ETA in minutes");
                                                        textView2.setVisibility(View.VISIBLE);
                                                        textView2.setText(getInfoWindowItemStopText_Singleline(item, s));
                                                        eta_text.setVisibility(View.VISIBLE);
                                                        image2.setVisibility(View.VISIBLE);
                                                    } else {
                                                        beta_text.setText("Stop & ETA in minutes ");
                                                        broute2.setVisibility(View.VISIBLE);
                                                        broute2.setText(getInfoWindowItemStopText_Singleline(item, s));
                                                        beta_text.setVisibility(View.VISIBLE);
                                                        bimage2.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            } else if (count == 2) {
                                                if (type.equals("Inbound") || type.equals("Schedule")) {
                                                    if (route_type.equals("Train")) {
                                                        image3.setVisibility(View.VISIBLE);
                                                        time3.setVisibility(View.VISIBLE);
                                                        status3.setVisibility(View.VISIBLE);
                                                        track3.setVisibility(View.VISIBLE);
                                                        route3.setVisibility(View.VISIBLE);
                                                        route3.setText(getInfoWindowItemStopText(item, s));
                                                        route3.setMaxWidth(150);
                                                        time3.setText(getInfoWindowItemStopText_Time(item, s));
                                                        status3.setText(getInfoWindowItemStopText_Status(item, s));
                                                        if (show_platform.equals("true")) {
                                                            track3.setText(getInfoWindowItemStopText_Track(item, s));
                                                        }
                                                    } else {
                                                        bimage3.setVisibility(View.VISIBLE);
                                                        btime3.setVisibility(View.VISIBLE);
                                                        bstatus3.setVisibility(View.VISIBLE);
                                                        broute3.setVisibility(View.VISIBLE);
                                                        broute3.setText(getInfoWindowItemStopText(item, s));
                                                        broute3.setMaxWidth(150);
                                                        btime3.setText(getInfoWindowItemStopText_Time(item, s));
                                                        bstatus3.setText(getInfoWindowItemStopText_Status(item, s));

                                                    }
                                                } else {
                                                    if (route_type.equals("Train")) {
                                                        eta_text.setText("Station & ETA in minutes");
                                                        textView3.setVisibility(View.VISIBLE);
                                                        textView3.setText(getInfoWindowItemStopText_Singleline(item, s));
                                                        textView3.setMaxWidth(300);
                                                        eta_text.setVisibility(View.VISIBLE);
                                                        image3.setVisibility(View.VISIBLE);
                                                    } else {
                                                        beta_text.setText("Stop & ETA in minutes ");
                                                        broute3.setVisibility(View.VISIBLE);
                                                        broute3.setText(getInfoWindowItemStopText_Singleline(item, s));
                                                        broute3.setMaxWidth(300);
                                                        beta_text.setVisibility(View.VISIBLE);
                                                        bimage3.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            }

                                            break;
                                        }
                                    }
                                    count++;
                                }
                                else if ((item.minutes==0) && (minutesToStops.size()==1))
                                {
                                    emptyText.setVisibility(View.VISIBLE);
                                    emptyText.setText("No Next Stops");
                                }
                            }
                            if (route_type.equals("Train")) {
                                stopContainer.setVisibility(View.VISIBLE);
                                etaContainer.setVisibility(View.VISIBLE);
                                etaContainer1.setVisibility(View.VISIBLE);
                                etaContainer2.setVisibility(View.VISIBLE);
                                etaContainer3.setVisibility(View.VISIBLE);
                            } else {
                                bstopContainer.setVisibility(View.VISIBLE);
                                betaContainer.setVisibility(View.VISIBLE);
                                betaContainer1.setVisibility(View.VISIBLE);
                                betaContainer2.setVisibility(View.VISIBLE);
                                betaContainer3.setVisibility(View.VISIBLE);
                            }
                        } else {
                            stopContainer.setVisibility(View.GONE);
                            bstopContainer.setVisibility(View.GONE);
                            emptyText.setText("Service not available currently");
                            emptyText.setVisibility(View.VISIBLE);
                        }
                    }
                }
                else if (marker.getTag().equals("attraction"))
                {
                    markerType = "default";
                    LatLng currentLatLng = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
                    for (int i=0;i<nearLocInfoList.size();i++)
                    {

                        if (nearLocInfoList.get(i).nearMarker.equals(currentLatLng))
                        {
                            nearName.setText(nearLocInfoList.get(i).nearName);
                            nearAddress.setText(nearLocInfoList.get(i).nearAddress);
                            nearDistance.setText(nearLocInfoList.get(i).nearDistance);

                        }
                    }
                }
                else {
                    for (int routeId : mStopMarkersForRoute.keySet()) {
                        if (mStopMarkersForRoute.get(routeId) != null) {
                            List<Marker> stopMarkers = mStopMarkersForRoute.get(routeId);
                            for (Marker m: stopMarkers) {
//                                Log.d("changeColor", routeId + "m.getTag()" + m.getTag());
                                final Route r = EtaAppController.getInstance().getRouteForId(routeId);
                                if (m!=null && m.getTag().equals(marker.getTag())&& EtaAppController.getInstance().routeIsSelected(r.id)) {
                                    final Stop stop = (Stop) mDataObjects.get(marker);
//                                    Log.d("changeColor", "stopMarkers returned true: " + r.color);
                                    markerType = "stop";
                                    final Route rNew = EtaAppController.getInstance().getRouteForId(stop.rid);
//                                    Log.d("changeColor", "stopMarkers rNew: " + rNew.color);
                                    stopdetails.setBackgroundColor(Utils.convertColorStringToHex(r.color));
                                    adddetails.setBackgroundColor(Utils.convertColorStringToHex(r.color));
                                    directionDetails.setBackgroundColor(Utils.convertColorStringToHex(r.color));
                                    scheduleDetails.setBackgroundColor(Utils.convertColorStringToHex(r.color));
                                    EtaAppController.getInstance().loadScheduleForAgency(stop.id);
                                    String route_type_stop = "";
                                    String type_stop = "";
                                    routedetails.setVisibility(View.VISIBLE);
                                    stopDescription.setVisibility(View.VISIBLE);
                                    stopdetails.setVisibility(View.VISIBLE);
                                    adddetails.setVisibility(View.VISIBLE);
                                    directionDetails.setVisibility(View.VISIBLE);
                                    scheduleDetails.setVisibility(View.VISIBLE);
                                    Photos_Stop.setVisibility(View.VISIBLE);
                                    commentTitle.setVisibility(View.VISIBLE);
                                    attractionDetails.setVisibility(View.VISIBLE);
                                    arrayOfFavStopList.clear();
                                    arrayOfFavStopList = findFavoritesFromDB();
                                    if (arrayOfFavStopList.size() > 0) {
                                        //Check if stop already added as Favorite
                                        for (Integer f : arrayOfFavStopList) {
                                            if ((int)f == stop.id) {
                                                addFavoriteButton.setChecked(true);
                                                break;
                                            }
                                        }
                                    }
                                    try {
                                        route_type_stop = r.vType;

                                    } catch (Exception e) {
                                        route_type_stop = "";
                                    }
                                    try {
                                        type_stop = r.type;
                                    } catch (Exception e) {
                                        type_stop = "";
                                    }
                                    try {
                                        stop_show_direction = r.showDirection;
                                    } catch (Exception e) {
                                        stop_show_direction = "false";
                                    }
                                    try {
                                        stop_show_platform = r.showPlatform;
                                    } catch (Exception e) {
                                        stop_show_platform = "false";
                                    }
                                    try {
                                        showScheduleNumber = r.showScheduleNumber;
                                    } catch (Exception e) {
                                        showScheduleNumber = 0;
                                    }

//                            if(route_type_stop.equals("Train") ){
//
//                                image1.setImageResource(R.drawable.ic_station);
//                                image2.setImageResource(R.drawable.ic_station);
//                                image3.setImageResource(R.drawable.ic_station);
//
//                                emptyText.setText("No Trains In Service To This Station");
//
//                                time_text.setVisibility(View.VISIBLE);
//                                status_text.setVisibility(View.VISIBLE);
//                                eta_text.setText("Train   ");
//
//
//                            }else{
//                                beta_text.setText("Stop & ETA in minutes");
//
//                               // btime_text.setVisibility(View.VISIBLE);
//                                //bstatus_text.setVisibility(View.VISIBLE);
//                                beta_text.setText("Route   ");
//                            }
                                    bimage1.setImageResource(R.drawable.ic_stop);
                                    bimage2.setImageResource(R.drawable.ic_stop);
                                    bimage3.setImageResource(R.drawable.ic_stop);
                                    shouldReturnView = true;

                                    title.setText(stop.name);
                                    if (getStopDescription(stop.id) != null && getStopDescription(stop.id) != "") {
                                        stopDesc.setVisibility(View.VISIBLE);
                                        stopDescription.setBackgroundColor(Utils.convertColorStringToHex(r.color));
                                        stopDesc.setText(getStopDescription(stop.id));
                                    } else
                                        stopDescription.setVisibility(View.GONE);
                                    amenitieslist = getAmenities(stop.id);
                                    photoLocationList = getStopPhotos(stop.id);
                                    if (amenitieslist.size() > 0) {
                                        horizontalScrollView1.setVisibility(View.VISIBLE);
                                        amenityDetails.setVisibility(View.VISIBLE);
                                        amenityDetails.removeAllViews();
                                        horizontalScrollView1.setBackgroundColor(Utils.convertColorStringToHex(r.color));
                                        amenityDetails.setBackgroundColor(Utils.convertColorStringToHex(r.color));
                                        int resIDList[] = new int[amenitieslist.size()];
                                        for (int i = 0; i < amenitieslist.size(); i++) {
                                            resIDList[i] = getIconType(amenitieslist.get(i).toLowerCase());
                                            ImageView image = new ImageView(getActivity().getApplicationContext());
                                            image.setLayoutParams(new android.view.ViewGroup.LayoutParams(60, 40));
                                            image.setMaxHeight(20);
                                            image.setMaxWidth(20);
                                            image.setImageResource(resIDList[i]);
                                            amenityDetails.addView(image);
                                        }
                                    } else {
                                        amenityDetails.setVisibility(View.GONE);
                                        horizontalScrollView1.setVisibility(View.GONE);
                                    }
                                    stopTypeVal.setText(getStopType(stop.id));
                                    String stopType = getStopType(stop.id).replaceAll("[-']","").toLowerCase();
                                    int resID = getIconType(stopType.replace(" ",""));
                                    stopTypeImage.setImageResource(resID);
                                    Photos_Stop.setOnClickListener
                                            (
                                                    new View.OnClickListener() {
                                                        public void onClick(View v) {
                                                            if (slider.getVisibility() == View.VISIBLE) {
                                                                slider.setVisibility(View.GONE);
                                                                stopPhotoArrow.setImageResource(R.drawable.arrowdown_black);
                                                            } else {
                                                                if (photoLocationList.size() > 0) {
                                                                    new LoadImage().execute(photoLocationList);
                                                                } else {
                                                                    ArrayList<Bitmap> noImgBmp = new ArrayList<Bitmap>();
                                                                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);
                                                                    noImgBmp.add(bmp);
                                                                    showStopPhotos(noImgBmp, false);
                                                                }
                                                                slider.setVisibility(View.VISIBLE);
                                                                stopPhotoArrow.setImageResource(R.drawable.arrowup_black);
                                                            }
                                                        }
                                                    }
                                            );
                                    Button rideralerts = (Button) infowindowDialog.findViewById(R.id.rideralerts);

                                    rideralerts.setVisibility(View.VISIBLE);
                                    GradientDrawable bgShape = (GradientDrawable) rideralerts.getBackground();
                                    try {
                                        //color = r.color;
                                        bgShape.setColor(Utils.convertColorStringToHex(r.color));
                                        bgShape.setStroke(5, Utils.convertColorStringToHex(r.color));

                                    } catch (NullPointerException e) {
                                        if (!haveInternetconnection()) {
                                            startActivity(new Intent(getActivity(), MainActivity.class));
                                        }

                                    }
                                    final String stopid = stop.id + "";
                                    ArrayList<Integer> listOfRoutes = new ArrayList();
                                    final HashMap<String, Integer> routes = new HashMap<String, Integer>();
                                    if(routedetails.getChildCount() > 0)
                                         routedetails.removeAllViews();

                                    List views = new ArrayList();
                                    TextView routeCircleTextView = new TextView(infowindowDialog.getContext());
                                    routeCircleTextView.setTextColor(Color.WHITE);
                                    routeCircleTextView.setText("Route:");
                                    routedetails.addView(routeCircleTextView);
                                    for (Route NoOfRoutes : EtaAppController.getInstance().getRoutes()) {
                                        for (int i = 0; i < NoOfRoutes.stops.size(); i++) {
                                            if (stopid.equals(NoOfRoutes.stops.get(i))) {
                                                if (listOfRoutes.contains(NoOfRoutes.id)) {

                                                } else {
                                                    listOfRoutes.add(NoOfRoutes.id);
                                                    routes.put(NoOfRoutes.name, NoOfRoutes.id);

                                                    float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
                                                    Bitmap b = Bitmap.createBitmap((int) size, (int) size, Bitmap.Config.ARGB_8888);
                                                    GradientDrawable gd = (GradientDrawable) getResources().getDrawable(R.drawable.stop_marker);
                                                    gd.setBounds(0, 0, (int) size, (int) size);
                                                    gd.setColor(Color.parseColor(NoOfRoutes.color));
                                                    gd.draw(new Canvas(b));

                                                    ImageView circle = new ImageView(infowindowDialog.getContext());
                                                    LinearLayout.LayoutParams paramsExample = new LinearLayout.LayoutParams(18, 18);
                                                    paramsExample.setMargins(5, 2, 5, 0);
                                                    paramsExample.gravity = Gravity.CENTER_VERTICAL;

                                                    circle.setLayoutParams(paramsExample);
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                                        circle.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.stop_marker, null));
                                                    } else {
                                                        circle.setImageResource(R.drawable.stop_marker);
                                                    }
                                                    routedetails.addView(circle);
                                                    views.add(circle);
                                                }
                                            }
                                        }
                                    }
                                    try {
                                        String stopName = "NA";
                                        if (stop.name.contains("'")) {
                                            stopName = stop.name.replace("'", "");
                                        } else {
                                            stopName = stop.name;
                                        }
                                        stopIdCount = dbobj.fetchStopIdByID(String.valueOf(stop.id), stopName);

                                    } catch (Exception e1) {

                                    } finally {

                                    }

                                    rideralerts.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (stopIdCount.getCount() > 0) {
                                                infowindowDialog.dismiss();
                                                Toast.makeText(getActivity(), "Alert already added", Toast.LENGTH_LONG).show();
                                            } else {
                                                dialog.setContentView(R.layout.fragment_rideralerts);
                                                alertsList = (ListView) dialog.findViewById(R.id.alertList);
                                                no_alerts = (TextView) dialog.findViewById(R.id.no_alerts);
                                                openAndQueryDatabase();
                                                RiderAlertsAdapter adapter = new RiderAlertsAdapter(getActivity(), cursor);
                                                alertsList.setEmptyView(no_alerts);
                                                alertsList.setAdapter(adapter);
                                                LinearLayout alertsBar = (LinearLayout) dialog.findViewById(R.id.alertsBar);
                                                try {
                                                    alertsBar.setBackgroundColor(Utils.convertColorStringToHex(r.color));
                                                } catch (Exception e) {
                                                    if (!haveInternetconnection()) {
                                                        startActivity(new Intent(getActivity(), MainActivity.class));

                                                    }
                                                }
                                                final ImageView alertsListClose = (ImageView) dialog.findViewById(R.id.alertsListClose);
                                                alertsListClose.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                addRiderAlert = (Button) dialog.findViewById(R.id.addRiderAlert);
                                                addRiderAlert.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Cursor countCursor = dbobj.fetchAllRiderAlerts();
                                                        if (countCursor != null) {
                                                            if (countCursor.getCount() >= 5) {
                                                                dialog.dismiss();
                                                                Toast.makeText(getActivity(), "Rider Alerts Reached Maximum Limit.", Toast.LENGTH_LONG).show();
                                                            } else {
                                                                String stopName = "NA";
                                                                if (stop.name.contains("'")) {
                                                                    stopName = stop.name.replace("'", "");
                                                                } else {
                                                                    stopName = stop.name;
                                                                }
                                                                Intent intent = new Intent(getActivity(), CreateRiderAlert.class);
                                                                intent.putExtra("source", "map");
                                                                intent.putExtra("stopName", stopName);
                                                                intent.putExtra("stopId", stop.id);
                                                                intent.putExtra("selectedAgency", selectedAgency);
                                                                intent.putExtra("selectedAgencyUrl", selectedAgencyUrl);
                                                                intent.putExtra("routesList", routes);
                                                                intent.putExtra("direction", stop_show_direction);
                                                                startActivity(intent);
                                                            }
                                                        } else {
                                                            Toast.makeText(getActivity(), "Please Try Again.", Toast.LENGTH_LONG).show();

                                                        }
                                                    }
                                                });
                                                try {
                                                    dialog.show();
                                                } catch (Exception e) {
                                                    dialog.dismiss();
                                                }
                                            }
                                        }
                                    });
                                    try {
                                        titleBar.setBackgroundColor(Utils.convertColorStringToHex(r.color));
                                        routedetails.setBackgroundColor(Utils.convertColorStringToHex(r.color));

                                    } catch (NullPointerException e) {
                                        if (!haveInternetconnection()) {
                                            startActivity(new Intent(getActivity(), MainActivity.class));

                                        }
                                    }
                                    List<StopEta> stopEtas = EtaAppController.getInstance().getStopEtas();
                                    stopContainer.setVisibility(View.GONE);
                                    etaContainer1.setVisibility(View.GONE);
                                    etaContainer2.setVisibility(View.GONE);
                                    etaContainer3.setVisibility(View.GONE);
                                    bstopContainer.setVisibility(View.GONE);

                                    if (stopEtas != null && !stopEtas.isEmpty()) {
                                        for (final StopEta eta : stopEtas) {
                                            if (eta.id == stop.id) {
                                                for (int i = 0; i < eta.enRoute.size(); i++) {
                                                    mHasDataForStops = true;
                                                    EnRoute enRoute = eta.enRoute.get(i);
                                                    String enRouteType = getInfoWindowItemEnRouteRouteType(enRoute);
                                                    final Route r1 = EtaAppController.getInstance().getRouteForId(enRoute.routeID);
                                                    try {
                                                        route_type_stop = r1.vType;
                                                    } catch (Exception e) {
                                                        route_type_stop = "";
                                                    }
                                                    try {
                                                        showScheduleNumber = r1.showScheduleNumber;
                                                    } catch (Exception e) {
                                                        showScheduleNumber = 0;
                                                    }
                                                    try {
                                                        stop_show_direction = r1.showDirection;
                                                    } catch (Exception e) {
                                                        stop_show_direction = "false";
                                                    }
                                                    try {
                                                        stop_show_platform = r1.showPlatform;
                                                    } catch (Exception e) {
                                                        stop_show_platform = "false";
                                                    }
                                                    if (route_type_stop.equals("Train")) {
//                                            image1.setImageResource(R.drawable.ic_station);
//                                            image2.setImageResource(R.drawable.ic_station);
//                                            image3.setImageResource(R.drawable.ic_station);
                                                        emptyText.setText("No Trains In Service To This Station");
                                                        time_text.setVisibility(View.VISIBLE);
                                                        status_text.setVisibility(View.VISIBLE);
                                                    }
                                                    if (type_stop.equals("Inbound") || type_stop.equals("Schedule")) {
                                                        eta_text.setText("Train   ");
                                                        beta_text.setText("Bus   ");
                                                    } else {
                                                        eta_text.setText("Train & ETA in minutes ");
                                                        beta_text.setText("Bus & ETA in minutes ");
                                                    }
                                                    if (enRoute.routeID != 0 && enRouteType!=null&&enRouteType.equalsIgnoreCase("bus")) {
                                                        //routeText.setVisibility(View.VISIBLE);
                                                        brouteText.setVisibility(View.VISIBLE);
                                                    }

                                                    if (enRoute.routeID != 0 && enRouteType!=null&&enRouteType.equalsIgnoreCase("train")) {
                                                        routeText.setVisibility(View.VISIBLE);
                                                        //brouteText.setVisibility(View.VISIBLE);
                                                    }

                                                    View containerView = null;

                                                    List<Vehicle> tempvehicledata = EtaAppController.getInstance().getVehicles();
                                                    for (Vehicle vehicledata : tempvehicledata) {

                                                        if (enRoute.equipmentID.equals(vehicledata.equipmentID)) {
                                                            vehicle_scheduleNumber = vehicledata.scheduleNumber;
                                                            break;
                                                        } else
                                                            vehicle_scheduleNumber = "-";
                                                    }
                                                    TextView textView = null;
                                                    TextView status = null;
                                                    TextView time = null;
                                                    TextView route = null;
                                                    ImageView imageview = null;

                                                    TextView direction = null;
                                                    TextView track = null;

                                                    TextView stop_eta_text = null, stop_dir_text = null, stop_track_text = null;
                                                    TextView stop_time_text = null;
                                                    TextView stop_status_text = null;

                                                    if (enRouteType.equalsIgnoreCase("Bus")) {
                                                        stop_eta_text = beta_text;
                                                        stop_time_text = btime_text;
                                                        stop_status_text = bstatus_text;
                                                        stop_dir_text = bdir_text;
                                                    } else if (enRouteType.equalsIgnoreCase("Train")) {
                                                        stop_eta_text = eta_text;
                                                        stop_time_text = time_text;
                                                        stop_status_text = status_text;
                                                        stop_dir_text = dir_text;
                                                    }
                                                    stop_track_text = track_text;
                                                    if (type_stop.equals("Inbound") || type_stop.equals("Schedule")) {

                                                        stop_eta_text.setVisibility(View.VISIBLE);
                                                        stop_time_text.setVisibility(View.VISIBLE);
                                                        stop_status_text.setVisibility(View.VISIBLE);
                                                        if (stop_show_direction.equals("true")) {
                                                            stop_dir_text.setVisibility(View.VISIBLE);
                                                        }
                                                        if (stop_show_platform.equals("true")) {
                                                            stop_track_text.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                    if (i == 0 && enRouteType.equalsIgnoreCase("Train")) {
                                                        containerView = etaContainer1;
                                                        textView = textView1;
                                                        status = status1;
                                                        time = time1;
                                                        direction = dir1;
                                                        track = track1;
                                                        imageview = image1;
                                                        route = route1;
                                                        etaContainer1.setVisibility(View.VISIBLE);

                                                    } else if (i == 1 && enRouteType.equalsIgnoreCase("Train")) {
                                                        containerView = etaContainer2;
                                                        textView = textView2;
                                                        status = status2;
                                                        time = time2;
                                                        direction = dir2;
                                                        track = track2;
                                                        imageview = image2;
                                                        route = route2;
                                                        etaContainer2.setVisibility(View.VISIBLE);

                                                    } else if (i == 2 && enRouteType.equalsIgnoreCase("Train")) {
                                                        containerView = etaContainer3;
                                                        textView = textView3;
                                                        status = status3;
                                                        time = time3;
                                                        direction = dir3;
                                                        track = track3;
                                                        imageview = image3;
                                                        route = route3;
                                                        etaContainer3.setVisibility(View.VISIBLE);
                                                    } else if (i == 0 && enRouteType.equalsIgnoreCase("bus")) {
                                                        containerView = betaContainer1;
                                                        textView = textViewb1;
                                                        status = bstatus1;
                                                        time = btime1;
                                                        direction = bdir1;
                                                /*track = track1;*/

                                                        betaContainer1.setVisibility(View.VISIBLE);
                                                        if (type_stop.equals("Inbound") || type_stop.equals("Schedule"))
                                                            textViewb1.setGravity(Gravity.CENTER);
                                                        imageview = bimage1;
                                                        route = broute1;


                                                    } else if (i == 1 && enRouteType.equalsIgnoreCase("bus")) {
                                                        containerView = betaContainer2;
                                                        textView = textViewb2;
                                                        status = bstatus2;
                                                        time = btime2;
                                                        direction = bdir2;
                                                /*track = track2;*/

                                                        betaContainer2.setVisibility(View.VISIBLE);
                                                        if (type_stop.equals("Inbound") || type_stop.equals("Schedule"))
                                                            textViewb2.setGravity(Gravity.CENTER);
                                                        imageview = bimage2;
                                                        route = broute2;

                                                    } else if (i == 2 && enRouteType.equalsIgnoreCase("bus")) {
                                                        containerView = betaContainer3;
                                                        textView = textViewb3;
                                                        status = bstatus3;
                                                        time = btime3;
                                                        direction = bdir3;
                                                /*track = track3;*/

                                                        betaContainer3.setVisibility(View.VISIBLE);
                                                        if (type_stop.equals("Inbound") || type_stop.equals("Schedule"))
                                                            textViewb3.setGravity(Gravity.CENTER);
                                                        imageview = bimage3;
                                                        route = broute3;
                                                    } else if (i > 2)
                                                        break;
                                                    if (containerView != null) {
                                                        if (enRouteType.equalsIgnoreCase("Bus")) {
                                                            bstopContainer.setVisibility(View.VISIBLE);
                                                            betaContainer.setVisibility(View.VISIBLE);
                                                        } else {
                                                            stopContainer.setVisibility(View.VISIBLE);
                                                            etaContainer.setVisibility(View.VISIBLE);
                                                        }
                                                        imageview.setVisibility(View.GONE);
                                                        bimage.setVisibility(View.GONE);
                                                        image.setVisibility(View.GONE);
                                                        containerView.setVisibility(View.VISIBLE);

                                                        if (type_stop.equals("Inbound") || type_stop.equals("Schedule")) {
                                                            if (route_type_stop.equals("Train")) {
                                                                eta_text.setText("Train");

                                                            } else {
                                                                beta_text.setText("Bus ");
                                                            }
                                                            textView.setText(getInfoWindowItemEnRouteText(enRoute));
                                                            textView.setVisibility(View.VISIBLE);
                                                            time.setText(getInfoWindowItemEnRouteSchedule(enRoute));
                                                            time.setVisibility(View.VISIBLE);
                                                            status.setText(getInfoWindowItemEnRouteStatus(enRoute));
                                                            status.setVisibility(View.VISIBLE);
                                                            if (stop_show_direction.equals("true")) {
                                                                direction.setText(getInfoWindowItemEnRouteDirection(enRoute));
                                                                direction.setVisibility(View.VISIBLE);
                                                            }
                                                            if (enRouteType.equalsIgnoreCase("Train")) {
                                                                if (stop_show_platform.equals("true")) {
                                                                    track.setText(getInfoWindowItemEnRouteTrack(enRoute));
                                                                    track.setVisibility(View.VISIBLE);
                                                                }
                                                            }
                                                            route.setText(getInfoWindowItemEnRouteRouteName(enRoute));
                                                            route.setVisibility(View.VISIBLE);
                                                        } else {
                                                            textView.setText(getInfoWindowItemEnRouteText_Single(enRoute));
                                                            textView.setVisibility(View.VISIBLE);
                                                            if (enRoute.routeID != 0) {
                                                                route.setText(getInfoWindowItemEnRouteRouteName(enRoute));
                                                                route.setVisibility(View.VISIBLE);
                                                            }
                                                            if (route_type_stop.equals("Train")) {
                                                                eta_text.setText("Train & ETA in minutes ");

                                                            } else {
                                                                beta_text.setVisibility(View.VISIBLE);
                                                                beta_text.setText("Bus & ETA in minutes");
                                                            }
//                                                time.setText(getInfoWindowItemEnRouteSchedule(enRoute));
//                                                time.setVisibility(View.VISIBLE);
//                                                status.setText(getInfoWindowItemEnRouteStatus(enRoute));
//                                                status.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                }
                                                break;
                                            } else if (!mHasDataForStops) {
                                                emptyText.setVisibility(View.VISIBLE);
                                            }
                                        }
                                        if (!mHasDataForStops) {
                                            emptyText.setVisibility(View.VISIBLE);
                                        } else {
                                            emptyText.setVisibility(View.GONE);
                                        }
                                    }
                                    else {
//                                        emptyText.setText("No Service Available Currently");
                                        emptyText.setVisibility(View.VISIBLE);
                                    }
                                    left.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            schedule_container.removeAllViews();
                                            titleBar.setVisibility(View.VISIBLE);
                                            routedetails.setVisibility(View.VISIBLE);
                                            stopdetails.setVisibility(View.VISIBLE);
                                            stopContainer.setVisibility(View.VISIBLE);
                                            directionDetails.setVisibility(View.VISIBLE);
                                            adddetails.setVisibility(View.VISIBLE);
                                            amenityDetails.setVisibility(View.VISIBLE);
                                            bstopContainer.setVisibility(View.VISIBLE);
                                            Photos_Stop.setVisibility(View.VISIBLE);
                                            commentTitle.setVisibility(View.VISIBLE);
                                            attractionDetails.setVisibility(View.VISIBLE);
                                            riderAlerts.setVisibility(View.VISIBLE);
                                            left.setVisibility(View.GONE);
                                            stopPhotoArrow.setImageResource(R.drawable.arrowdown_black);
                                            infoArrow.setImageResource(R.drawable.arrowdown_black);
                                            scheduledialog.setVisibility(View.GONE);
                                            schedule_container.setVisibility(View.GONE);
                                            noScheduleInfo.setVisibility(View.GONE);
                                            if (mHasDataForStops) {
                                                emptyText.setVisibility(View.GONE);
                                            } else {
                                                emptyText.setVisibility(View.VISIBLE);
                                            }
                                            infowindowResize();

                                        }
                                    });
                                    scheduleDetails.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
//                                            infowindowResize();
                                            titleBar.setVisibility(View.GONE);
                                            stopDescription.setVisibility(View.GONE);
                                            directionDetails.setVisibility(View.GONE);
                                            routedetails.setVisibility(View.GONE);
                                            stopdetails.setVisibility(View.GONE);
                                            stopContainer.setVisibility(View.GONE);
                                            adddetails.setVisibility(View.GONE);
                                            amenityDetails.setVisibility(View.GONE);
                                            loaddetails.setVisibility(View.GONE);
                                            bstopContainer.setVisibility(View.GONE);
                                            Photos_Stop.setVisibility(View.GONE);
                                            emptyText.setVisibility(View.GONE);
                                            riderAlerts.setVisibility(View.GONE);
                                            slider.setVisibility(View.GONE);
                                            commentTitle.setVisibility(View.GONE);
                                            commentDetails.setVisibility(View.GONE);
                                            attractionDetails.setVisibility(View.GONE);
                                            left.setVisibility(View.VISIBLE);
                                            List<ScheduleInfo> scheduleList = getCorridorName(stop.id);
                                            scheduledialog.setVisibility(View.VISIBLE);
                                            scheduledialog.setBackgroundColor(Utils.convertColorStringToHex(r.color));
                                            scheduleTitle.setText(stop.name.concat(" Schedule"));
                                            if (scheduleList.size() > 0) {
                                                noScheduleInfo.setVisibility(View.GONE);
                                                schedule_container.setVisibility(View.VISIBLE);
                                                TableRow thead = new TableRow(getActivity().getApplicationContext());
                                                TextView td0 = new TextView(getActivity().getApplicationContext());
                                                td0.setText("Route Name");
                                                td0.setPadding(15, 15, 15, 15);
                                                td0.setTypeface(Typeface.DEFAULT_BOLD);
                                                td0.setTextColor(Color.BLACK);
                                                thead.addView(td0);
                                                TextView td1 = new TextView(getActivity().getApplicationContext());
                                                td1.setText("Scheduled Time");
                                                td1.setGravity(Gravity.RIGHT);
                                                td1.setTextColor(Color.BLACK);
                                                td1.setTypeface(Typeface.DEFAULT_BOLD);
                                                thead.addView(td1);
                                                schedule_container.addView(thead, 0);
                                                for (int i = 0; i < scheduleList.size(); i++) {
                                                    //Create the tablerows
                                                    TableRow row = new TableRow(getActivity().getApplicationContext());
                                                    TextView t1v = new TextView(getActivity().getApplicationContext());
                                                    TextView t2v = new TextView(getActivity().getApplicationContext());
                                                    t1v.setText(scheduleList.get(i).routeName);
                                                    t1v.setTextColor(Color.BLACK);
                                                    t1v.setPadding(15, 15, 15, 15);
                                                    row.addView(t1v);
                                                    t2v.setText(scheduleList.get(i).scheduleTime);
                                                    t2v.setTextColor(Color.BLACK);
                                                    t2v.setGravity(Gravity.RIGHT);
//                                            t2v.setPadding(25, 15, 15, 15);
                                                    row.addView(t2v);
                                                    schedule_container.addView(row, i + 1);
                                                }

                                            } else {
                                                noScheduleInfo.setVisibility(View.VISIBLE);
                                            }
                                        }

                                    });
//                            Add/Remove as Favorite Stop
                                    addFavoriteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                            Log.d("newMarker", "onCheckedChanged:calling "+stop.id);
                                            addFavorite(isChecked, stop.id, marker, stop,r );
                                        }
                                    });
//                            Navigate to google directions
                                    directionDetails.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // GPSTracker class
                                            GPSTracker gps = new GPSTracker(getActivity());
                                            // check if GPS enabled
                                            if (gps.canGetLocation()) {
                                                mCurrentlocation = gps.getLocation();
                                                if (mCurrentlocation != null) {
                                                    LatLng currentLatLng = new LatLng(mCurrentlocation.getLatitude(), mCurrentlocation.getLongitude());
                                                    List<Stop> stopsData = EtaAppController.getInstance().getStops();
                                                    for (int i = 0; i < stopsData.size(); i++) {
                                                        if (stop.id == stopsData.get(i).id) {
                                                            Stop currentStop = stopsData.get(i);
                                                            LatLng stopLatLng = new LatLng(currentStop.lat, currentStop.lng);
                                                            showDirections(currentLatLng, stopLatLng);
//                                                    Object[] DataTransfer = new Object[2];
//                                                    DataTransfer[0] = currentLatLng;
//                                                    DataTransfer[1] = stopLatLng;
//                                                    new GeocodeAsyncTask().execute(DataTransfer);

                                                        }
                                                    }
                                                } else
                                                    Log.d(TAG, "Not enabled: ");
                                            } else {
                                                // can't get location
                                                // GPS or Network is not enabled
                                                // Ask user to enable GPS/network in settings
                                                gps.showSettingsAlert();
                                            }
                                        }
                                    });
//                            show/hide INFORMATION section
                                    commentTitle.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (commentDetails.getVisibility() == View.VISIBLE) {
                                                commentDetails.setVisibility(View.GONE);
                                                infoArrow.setImageResource(R.drawable.arrowdown_black);
                                            }
                                            else {
                                                infoArrow.setImageResource(R.drawable.arrowup_black);
                                                commentDetails.setVisibility(View.VISIBLE);
                                                commentsTextView.setText(getComments(stop.id));
                                            }
                                        }
                                    });
                                    break;
                                }
                            }
                        }
                    }
                }
                if (markerType!=null && markerType.equalsIgnoreCase("default") ) {
//                    show info window on clicking respective attraction
                    try {
                        nearwindowDailog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
//                    show info window on clicking stop/vehicle
                    try {
                        infowindowResize();
                        infowindowDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }


        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
            }
        });

    }
    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(getActivity());
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(getActivity(), result,
                        0).show();
            }
            return false;
        }

        Log.e("CheckGooglePlayServices", String.valueOf(result));
        return true;
    }
    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        // get the Position And build url to get nearby places data.
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyCmCzyLg4mm1e_8AporXom1fQbyT7F3oyE");
        return (googlePlacesUrl.toString());
    }
    private int getIconType(String type)
    {
        int resID = getResources().getIdentifier(type, "drawable", PACKAGE_NAME);
        return resID;
    }
    private String GetDistance(LatLng origin,LatLng dest) throws JSONException,IOException {
//        Calculate distance between selected stop and near by  places
        String miles;
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/distancematrix/json?");
        urlString.append("origins=");//from
        urlString.append(origin.latitude);
        urlString.append(",");
        urlString.append(origin.longitude);
        urlString.append("&destinations=");//to
        urlString.append(dest.latitude);
        urlString.append(",");
        urlString.append(dest.longitude);
        urlString.append("&travelMode=DRIVING&unitSystem=IMPERIAL&avoidHighways=false&avoidTolls=false");

        // get the JSON And parse it to get the directions data.
        HttpURLConnection urlConnection= null;
        URL url = null;

        try {
            url = new URL(urlString.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        urlConnection=(HttpURLConnection)url.openConnection();
        try {
            urlConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.connect();

        InputStream inStream = null;
        inStream = urlConnection.getInputStream();
        BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));

        String temp, response = "";
        while((temp = bReader.readLine()) != null){
            //Parse data
            response += temp;
        }
        //Close the reader, stream & connection
        bReader.close();
        inStream.close();
        urlConnection.disconnect();

        //Sortout JSONresponse
        JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
        JSONArray rowsArry = object.getJSONArray("rows");
        JSONObject elementsObj = rowsArry.getJSONObject(0);
        JSONArray elementsArry = elementsObj.getJSONArray("elements");
        JSONObject disobj = elementsArry.getJSONObject(0);
        JSONObject aJsonString = disobj.getJSONObject("distance");
        try {
            double km = Double.parseDouble(aJsonString.getString("text").replace(" km", ""));
            DecimalFormat df = new DecimalFormat("#.##");
            //... Convert it
            double mi = km * 0.621;
            miles  = df.format(mi) + " mi away";
            return miles;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public List<Integer> findFavoritesFromDB(){
        arrayOfFavStopList.clear();
        Agency agency = EtaAppController.getInstance().getSelectedAgency();
        String agencyname = agency.name.replaceAll("[-']","");
        agencyname = agencyname.replaceAll(" ","");
        try {
            cursor = favdboj.fetchAllFavStops(agencyname);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String stopId = cursor.getString(cursor.getColumnIndex(FavStopsDBHandler.STOP_ID));
                        int stopID =  Integer.parseInt(stopId);
//                        Log.d("newMarker", "Favcursor: is stopID "+stopID);
                        arrayOfFavStopList.add(stopID);
                    } while (cursor.moveToNext());
                }
            }
        } catch (SQLiteException se) {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        } finally {

        }
        return arrayOfFavStopList;
    }
    private void addFavorite(boolean isChecked,int stopID,Marker mFavMarker,Stop stop,Route r)
    {
        List<Stop> stopsData = EtaAppController.getInstance().getStops();
        String agencyNameToInsertinDB = selectedAgency.replaceAll("[-']", "");
        agencyNameToInsertinDB = agencyNameToInsertinDB.replaceAll(" ","");
        long rowInserted;
//      check if selected stop already added as Favorite or not
        boolean isStopFav = getStopDataWithId(stopID);
        if (isChecked) {
            if (!isStopFav)
            {
                boolean favorite_is = false;
                for (int i=0;i<stopsData.size();i++)
                {
                    if (stopID == stopsData.get(i).id) {
                        favorite_is = true;
                        break;
                    }
                }
                if (favorite_is) {
                    try {
                        rowInserted = favdboj.insert_fav_stops(agencyNameToInsertinDB, stopID + "");
                        if (rowInserted != -1) {
//                            Added selected stop as Favorite in local DB
//                            mFavMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Utils.drawFavStop(getActivity())));
                            changeStopToFavMarkerWithId(stopID);
                            addFavoriteButton.setChecked(true);
                        }
                        else if(rowInserted == -1)
                        {
                            addFavoriteButton.setChecked(false);
                            Toast.makeText(getActivity(),"Error while adding Favorite Stop:Retry", Toast.LENGTH_LONG).show();
                        }
                    }
                    catch (SQLiteException se)
                    {
                     se.printStackTrace();
                    }
                }

            }
            else {
//                 Already Added selected stop as Favorite in local DB
                Log.d("newMarker", "Already added as Fav ");
            }
        }
        else {
            if (isStopFav) {
                    boolean c = favdboj.deleteFavStop(agencyNameToInsertinDB, stopID + "");
                    if (c) {
//                            Removed selected stop as Favorite in local DB
                        addFavoriteButton.setChecked(false);
                        changeFavToStopMarkerWithId(stopID);
//                        mFavMarker.setIcon(BitmapDescriptorFactory.fromBitmap(Utils.updateStopIcon(r,getActivity())));
                    }
            }
        }
    }

    private void showProgressBar()
    {
        if( !isAdded() )
            return;
        mProgressBar.startAnimation( AnimationUtils.loadAnimation( getActivity(), android.R.anim.fade_in ) );
        mProgressBar.setVisibility( View.VISIBLE );
    }
    private void resetAndShowContent()
    {
        mProgressBar.startAnimation( AnimationUtils.loadAnimation( getActivity(), android.R.anim.fade_out ) );
        mProgressBar.setVisibility( View.GONE );
    }
    private boolean getStopDataWithId(int id){
        arrayOfFavStopList.clear();
        arrayOfFavStopList = findFavoritesFromDB();
        for (int i=0;i<arrayOfFavStopList.size();i++)
        {
            int stopId = arrayOfFavStopList.get(i);
            if (id == stopId)
                return true;
        }
        return false;

    }
    public void infowindowResize(){

//        if(metrics.heightPixels< 400 || markerType.equalsIgnoreCase("vehicle")) {
            infowindowDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        }
//        else {
//            if (emptyText.getVisibility()== View.VISIBLE) {
//                infowindowDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            }
//            else {
//                Log.d(TAG, "infowindowResize: "+metrics.heightPixels);
//                infowindowDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, metrics.heightPixels-
//                        (metrics.heightPixels/ 4));
//            }
//        }
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
    public class GeocodeAsyncTask extends AsyncTask<Object, Void, HashMap<String, Address>> {

        String errorMessage = "";
        @Override
        protected HashMap<String, Address> doInBackground(Object... params) {
            Geocoder geocoder = new Geocoder(infowindowDialog.getContext(), Locale.getDefault());
            List<Address> currAddresses = null;
            List<Address> destAddresses = null;
            HashMap<String, Address> h = new HashMap<String,Address>();
            LatLng currlatlng = (LatLng) params[1];
            LatLng stoplatlng = (LatLng) params[0];

            try {
                currAddresses = geocoder.getFromLocation(currlatlng.latitude, currlatlng.longitude, 1);
                destAddresses = geocoder.getFromLocation(stoplatlng.latitude, stoplatlng.longitude, 1);
            } catch (IOException ioException) {
                errorMessage = "Service Not Available";
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                errorMessage = "Invalid Latitude or Longitude Used";
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + currlatlng.latitude + ", Longitude = " +
                        currlatlng.longitude, illegalArgumentException);
            }
            if(currAddresses != null && currAddresses.size() > 0&&destAddresses != null && destAddresses.size() > 0) {
                h.put("ToAddress",currAddresses.get(0));
                h.put("FromAddress",destAddresses.get(0));
                return h;
            }

            return null;
        }

        protected void onPostExecute(HashMap<String, Address> address) {
            String sourceAddressName = "";
            String destAddressName = "";
            if(address == null) {
                Toast.makeText(getActivity(),errorMessage+" -Retry", Toast.LENGTH_LONG).show();
                 Log.d("onPostExecute", "No Address: ");
            }
            else {
                Address s = address.get("ToAddress");
                Address d = address.get("FromAddress");
                for(int i = 0; i < s.getMaxAddressLineIndex(); i++) {
                    if (i==0)
                        sourceAddressName = s.getAddressLine(i);
                    else
                        sourceAddressName += "," + s.getAddressLine(i);
//                    Log.d("getAddressOfLocation", "Sourceaddress: "+s.getAddressLine(i));
                }
                for(int i = 0; i < d.getMaxAddressLineIndex(); i++) {
                    if (i==0)
                        destAddressName = d.getAddressLine(i);
                    else
                        destAddressName += "," + d.getAddressLine(i);
//                    Log.d("getAddressOfLocation", "Destinationaddress: "+d.getAddressLine(i));
                }
//                showDirections(sourceAddressName,destAddressName);
            }

        }
    }
    public void showDirections(LatLng source,LatLng dest){
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+source.latitude+","+source.longitude+"&daddr="+dest.latitude+","+dest.longitude));
        startActivity(intent);

    }

//    private GoogleMap.InfoWindowAdapter mInfoWinderAdapter = new GoogleMap.InfoWindowAdapter()
//    {
//        @Override
//        public View getInfoWindow( Marker marker )
//        {
//            if( !isAdded() )
//                return null;
//
//            boolean shouldReturnView = false;
//
//            View v = LayoutInflater.from( getActivity() ).inflate( R.layout.view_info_window, null );
//            View titleBar = v.findViewById( R.id.title_bar );
//            TextView title = (TextView) v.findViewById( R.id.title );
//
//            LinearLayout routedetails = (LinearLayout) v.findViewById( R.id.route_details );
//
//            TextView direction_full = (TextView) v.findViewById( R.id.direction_full );
//
//            TextView textView1 = (TextView) v.findViewById( R.id.text_1 );
//            TextView textView2 = (TextView) v.findViewById( R.id.text_2 );
//            TextView textView3 = (TextView) v.findViewById( R.id.text_3 );
//            TextView emptyText = (TextView) v.findViewById( R.id.empty_text );
//
//            TextView routeText = (TextView) v.findViewById( R.id.Routetext );
//            TextView route1 = (TextView) v.findViewById( R.id.route_1 );
//            TextView route2 = (TextView) v.findViewById( R.id.route_2 );
//            TextView route3 = (TextView) v.findViewById( R.id.route_3 );
//
//            time1 = (TextView) v.findViewById( R.id.time_1 );
//            time2 = (TextView) v.findViewById( R.id.time_2 );
//            time3 = (TextView) v.findViewById( R.id.time_3 );
//
//            time_text = (TextView) v.findViewById( R.id.time );
//            TextView eta_text = (TextView) v.findViewById( R.id.text );
//            status_text = (TextView) v.findViewById( R.id.status );
//
//            dir_text = (TextView) v.findViewById(R.id.dir_text);
//            track_text = (TextView) v.findViewById(R.id.track_text);
//
//
//            status1 = (TextView) v.findViewById( R.id.status_1 );
//            status2 = (TextView) v.findViewById( R.id.status_2 );
//            status3 = (TextView) v.findViewById( R.id.status_3 );
//
//
//            track1 = (TextView) v.findViewById( R.id.track_1);
//            track2 = (TextView) v.findViewById( R.id.track_2 );
//            track3 = (TextView) v.findViewById( R.id.track_3 );
//
//
//            dir1 = (TextView) v.findViewById( R.id.dir_1);
//            dir2 = (TextView) v.findViewById( R.id.dir_2 );
//            dir3 = (TextView) v.findViewById( R.id.dir_3 );
//
//
//             image1 = (ImageView) v.findViewById(R.id.image_1);
//             image2 = (ImageView) v.findViewById(R.id.image_2);
//             image3 = (ImageView) v.findViewById(R.id.image_3);
//
//
//            View stopContainer = v.findViewById( R.id.stop_container );
//            View etaContainer1 = v.findViewById( R.id.container_1 );
//            View etaContainer2 = v.findViewById( R.id.container_2 );
//            View etaContainer3 = v.findViewById( R.id.container_3 );
//
//            if( mVehicleMarkers.containsValue( marker ) )
//            {
//                shouldReturnView = true;
//                Vehicle vehicle = (Vehicle) mDataObjects.get( marker );
//
//                Route route = EtaAppController.getInstance().getRouteForId( vehicle.routeID );
//                String route_type = "bus";
//                String type = "";
//
//
//                String show_direction = "false";
//                String show_platform = "false";
//
//                try{
//                    route_type = route.vType;
//
//                }catch (Exception e){
//                    route_type = "bus";
//                }
//
//                try{
//                    type = route.type;
//
//                }catch (Exception e){
//                    type = "";
//                }
//                try{
//
//                    show_direction = route.showDirection;
//
//                }catch (Exception e){
//                     show_direction = "false";
//
//                }
//                try{
//
//                    show_platform = route.showPlatform;
//                }catch (Exception e){
//
//                    show_platform = "false";
//
//                }
//                try{
//
//                    showScheduleNumber = route.showScheduleNumber;
//                }catch (Exception e){
//
//                    showScheduleNumber = 0;
//
//                }
//
//                if(showScheduleNumber == 1) {
//                    title.setText( vehicle.scheduleNumber );
//
//                }else{
//                    title.setText( vehicle.equipmentID );
//                }
//
//
//
//                if(route_type.equals("Train")  ) {
//                    Log.d("route_type vehicle",route_type);
//                    image1.setImageResource(R.drawable.ic_station);
//                    image2.setImageResource(R.drawable.ic_station);
//                    image3.setImageResource(R.drawable.ic_station);
//                    emptyText.setText("Train Not In Service");
//                    eta_text.setText("Station  ");
//
//                }else{
//                    emptyText.setText("Bus Not in service");
//                }
//                if( route != null )
//                    titleBar.setBackgroundColor( Utils.convertColorStringToHex( route.color ) );
//                Log.d("vehicle",vehicle.equipmentID);
//
//                List<MinutesToStop> minutesToStops = vehicle.getMinutesToNextStopList();
//                Log.d("minutes to stp",minutesToStops.toString());
//                if( minutesToStops != null && !minutesToStops.isEmpty() )
//                {
//                    List<Stop> stops = EtaAppController.getInstance().getStops();
////                    MinutesToStop item = minutesToStops.get( 0 );
//                    int i,count = 0;
//                    MinutesToStop item;
//                    for( i = 0; i < minutesToStops.size() ; i ++ ){
//                        item= minutesToStops.get(i);
//                        if(item.minutes != 0){
//                            for( Stop s : stops )
//                            {
//                                if( item.stopID == s.id )
//                                {
//                                    if(show_direction.equals("true")){
//                                        direction_full.setText(" - "+item.direction);
//                                    }
//                                    if(count == 0){
//                                        if(type.equals("Inbound") || type.equals("Schedule")){
//                                            image1.setVisibility(View.VISIBLE);
//                                            time1.setVisibility(View.VISIBLE);
//                                            status1.setVisibility(View.VISIBLE);
//                                            track1.setVisibility(View.VISIBLE);
//                                            textView1.setText(getInfoWindowItemStopText(item, s));
//                                            time1.setText(getInfoWindowItemStopText_Time(item, s));
//                                            status1.setText(getInfoWindowItemStopText_Status(item,s));
//                                            if(show_platform.equals("true")){
//                                                track1.setText(getInfoWindowItemStopText_Track(item, s));
//                                            }
//
//                                        }
//                                        else{
//                                            if(route_type.equals("Train")) {
//                                                eta_text.setText("Station & ETA in minutes");
//                                            }
//                                            else{
//                                                eta_text.setText("Stop & ETA in minutes ");
//                                            }
//                                            textView1.setText(getInfoWindowItemStopText_Singleline(item, s));
//                                            image1.setVisibility(View.VISIBLE);
//                                        }
//                                    }
//
//                                    else if(count == 1) {
//                                        if (type.equals("Inbound") || type.equals("Schedule")) {
//                                            image2.setVisibility(View.VISIBLE);
//                                            time2.setVisibility(View.VISIBLE);
//                                            status2.setVisibility(View.VISIBLE);
//                                            textView2.setVisibility(View.VISIBLE);
//
//                                            track2.setVisibility(View.VISIBLE);
//                                            textView2.setText(getInfoWindowItemStopText(item, s));
//
//                                            time2.setText(getInfoWindowItemStopText_Time(item, s));
//                                            status2.setText(getInfoWindowItemStopText_Status(item, s));
//                                            if(show_platform.equals("true")){
//                                                track2.setText(getInfoWindowItemStopText_Track(item, s));
//                                            }
//                                        } else {
//                                            if(route_type.equals("Train")) {
//                                                eta_text.setText("Station & ETA in minutes");
//                                            }
//                                            else{
//                                                eta_text.setText("Stop & ETA in minutes ");
//                                            }
//                                            textView2.setVisibility(View.VISIBLE);
//                                            textView2.setText(getInfoWindowItemStopText_Singleline(item, s));
//                                            image2.setVisibility(View.VISIBLE);
//                                        }
//                                    }
//                                    else if(count == 2) {
//                                        if (type.equals("Inbound") || type.equals("Schedule")) {
//                                            image3.setVisibility(View.VISIBLE);
//                                            time3.setVisibility(View.VISIBLE);
//                                            status3.setVisibility(View.VISIBLE);
//                                            track3.setVisibility(View.VISIBLE);
//                                            textView3.setVisibility(View.VISIBLE);
//                                            textView3.setText(getInfoWindowItemStopText(item, s));
//                                            time3.setText(getInfoWindowItemStopText_Time(item, s));
//                                            status3.setText(getInfoWindowItemStopText_Status(item, s));
//                                            if(show_platform.equals("true")){
//                                                track3.setText(getInfoWindowItemStopText_Track(item, s));
//                                            }
//                                        } else {
//                                            if(route_type.equals("Train")) {
//                                                eta_text.setText("Station & ETA in minutes");
//                                            }
//                                            else{
//                                                eta_text.setText("Stop & ETA in minutes ");
//                                            }
//                                            textView3.setVisibility(View.VISIBLE);
//                                            textView3.setText(getInfoWindowItemStopText_Singleline(item, s));
//
//                                            image3.setVisibility(View.VISIBLE);
//                                        }
//                                    }
//
//                                    break;
//
//                                }
//
//                            }
//                            count ++;
//
//                        }
//                    }
//                    stopContainer.setVisibility( View.VISIBLE );
//                    emptyText.setVisibility( View.GONE );
//
//                }else
//                {
//                    stopContainer.setVisibility( View.GONE );
//                    emptyText.setVisibility( View.VISIBLE );
//                }
//            }else
//            {
//                for( int routeId : mStopMarkersForRoute.keySet() )
//                {
//                    Log.e("map",mStopMarkersForRoute.keySet()+"");
//                    if( mStopMarkersForRoute.get( routeId ) != null && mStopMarkersForRoute.get( routeId ).contains( marker ) )
//                    {
//                        Route r = EtaAppController.getInstance().getRouteForId( routeId );
//                        routedetails.setVisibility(View.VISIBLE);
//
//                        String route_type_stop = "";
//                        String type_stop = "";
//                        String stop_show_direction = "false";
//                        String stop_show_platform = "false";
//                        try{
//                            route_type_stop = r.vType;
//                            type_stop = r.type;
//                            stop_show_direction = r.showDirection;
//                            stop_show_platform = r.showPlatform;
//                        }catch (Exception e){
//                            route_type_stop = "";
//                            type_stop = "";
//                            stop_show_direction = "false";
//                            stop_show_platform = "false";
//                        }
//
//                        if(route_type_stop.equals("Train") ){
//
//                            image1.setImageResource(R.drawable.ic_station);
//                            image2.setImageResource(R.drawable.ic_station);
//                            image3.setImageResource(R.drawable.ic_station);
//
//                            emptyText.setText("No Trains In Service To This Station");
//
//                            time_text.setVisibility(View.VISIBLE);
//                            status_text.setVisibility(View.VISIBLE);
//                            eta_text.setText("Train   ");
//
//
//                        }else{
//                            eta_text.setText("Bus   ");
//                        }
//
//                        shouldReturnView = true;
//
//
//
//                        Stop stop = (Stop) mDataObjects.get( marker );
//                        title.setText( stop.name );
//                        String stopid = stop.id+"";
//                        String routeNames = "";
//
//
//                        ArrayList<Integer> listOfStops = new ArrayList() ;
//
//                        List views=new ArrayList();
//
//                        for( Route NoOfRoutes : EtaAppController.getInstance().getRoutes() ){
//
//                            for(int i=0; i<NoOfRoutes.stops.size() ; i++){
//
//                                if(stopid.equals(NoOfRoutes.stops.get(i))){
//                                    if(listOfStops.contains(NoOfRoutes.id)){
//
//
//                                    }else{
//                                        listOfStops.add(NoOfRoutes.id);
//                                        float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, v.getResources().getDisplayMetrics());
//                                        Bitmap b = Bitmap.createBitmap( (int) size, (int) size, Bitmap.Config.ARGB_8888 );
//                                        GradientDrawable gd = (GradientDrawable) v.getResources().getDrawable( R.drawable.stop_marker );
//                                        gd.setBounds( 0, 0, (int) size, (int) size );
//                                        gd.setColor( Color.parseColor(NoOfRoutes.color) );
//                                        gd.draw( new Canvas( b ) );
//
//                                        ImageView circle = new ImageView(v.getContext());
//                                        LinearLayout.LayoutParams paramsExample = new LinearLayout.LayoutParams(20, 20);
//                                        paramsExample.setMargins(5, 10, 5, 0);
//
//                                        circle.setLayoutParams(paramsExample);
//                                        circle.setImageResource(R.drawable.stop_marker);
//                                        routedetails.addView(circle);
//                                             views.add(circle);
//
//                                    }
//                                }
//                            }
//                        }
//                        if( r != null )
//                            titleBar.setBackgroundColor( Utils.convertColorStringToHex( r.color ) );
//                        routedetails.setBackgroundColor( Utils.convertColorStringToHex( r.color ) );
//
//                        List<StopEta> stopEtas = EtaAppController.getInstance().getStopEtas();
//
//
//
//
//
//                        stopContainer.setVisibility( View.GONE );
//                        emptyText.setVisibility( View.VISIBLE );
//
//                        etaContainer1.setVisibility( View.GONE );
//                        etaContainer2.setVisibility( View.GONE );
//                        etaContainer3.setVisibility( View.GONE );
//                        stopContainer.setVisibility( View.GONE );
//
//                        if( stopEtas != null && !stopEtas.isEmpty() )
//                        {
//                            for( StopEta eta : stopEtas )
//                            {
//                                if( eta.id == stop.id )
//                                {
//                                    for( int i = 0; i < eta.enRoute.size(); i++ )
//                                    {
//                                        EnRoute enRoute = eta.enRoute.get( i );
//                                        if(enRoute.routeID != 0)
//                                            routeText.setVisibility(View.VISIBLE);
//                                        View containerView = null;
//                                        TextView textView = null;
//                                        TextView status = null;
//                                        TextView time = null;
//                                        TextView route = null;
//                                        ImageView imageview = null;
//
//                                        TextView direction = null;
//                                        TextView track = null;
//
//                                        TextView stop_eta_text = null , stop_dir_text = null , stop_track_text = null;
//                                        TextView stop_time_text = null;
//                                        TextView stop_status_text =  null;
//
//                                        stop_eta_text = eta_text;
//                                        stop_time_text = time_text;
//                                        stop_status_text = status_text;
//
//                                        stop_dir_text = dir_text;
//                                        stop_track_text = track_text;
//                                        if(type_stop.equals("Inbound") || type_stop.equals("Schedule")){
//
//                                            stop_eta_text.setVisibility(View.VISIBLE);
//                                            stop_time_text.setVisibility(View.VISIBLE);
//                                            stop_status_text.setVisibility(View.VISIBLE);
//                                            if(stop_show_direction.equals("true")){
//                                                stop_dir_text.setVisibility(View.VISIBLE);
//                                            }
//                                            if(stop_show_platform.equals("true")){
//                                                stop_track_text.setVisibility(View.VISIBLE);
//                                            }
//                                        }
//
//                                        if( i == 0 )
//                                        {
//                                            containerView = etaContainer1;
//                                            textView = textView1;
//                                            status = status1;
//                                            time = time1;
//                                            direction = dir1;
//                                            track = track1;
//                                            imageview = image1;
//                                                route = route1;
//
//
//                                        }else if( i == 1 )
//                                        {
//                                            containerView = etaContainer2;
//                                            textView = textView2;
//                                            status = status2;
//                                            time = time2;
//                                            direction = dir2;
//                                            track = track2;
//                                            imageview = image2;
//                                                route = route2;
//
//                                        }else if( i == 2 )
//                                        {
//                                            containerView = etaContainer3;
//                                            textView = textView3;
//                                            status = status3;
//                                            time = time3;
//                                            direction = dir3;
//                                            track = track3;
//                                            imageview = image3;
//                                                route = route3;
//                                        }else if( i > 2 )
//                                            break;
//
//                                        if( containerView != null )
//                                        {
//                                            stopContainer.setVisibility( View.VISIBLE );
//                                            emptyText.setVisibility( View.GONE );
//                                            imageview.setVisibility(View.INVISIBLE);
//
//                                            containerView.setVisibility( View.VISIBLE );
//                                            List<Vehicle> tempvehicledata = EtaAppController.getInstance().getVehicles();
//                                            for (Vehicle vehicledata : tempvehicledata){
//
//                                                if(enRoute.equipmentID.equals(vehicledata.equipmentID)){
//
//                                                    vehicle_scheduleNumber = vehicledata.scheduleNumber;
//
//                                                }
//                                            }
//
//                                            if(route_type_stop.equals("Train")){
//                                                eta_text.setText("Train");
//
//                                            }else{
//                                                eta_text.setText("Bus");
//                                            }
//                                            if(type_stop.equals("Inbound") || type_stop.equals("Schedule")) {
//                                                if(enRoute.routeID != 0){
//                                                    route.setText(getInfoWindowItemEnRouteRouteName(enRoute));
//                                                    route.setVisibility(View.VISIBLE);
//                                                }
//
//                                                textView.setText(getInfoWindowItemEnRouteText(enRoute));
//                                                textView.setVisibility(View.VISIBLE);
//                                                time.setText(getInfoWindowItemEnRouteSchedule(enRoute));
//                                                time.setVisibility(View.VISIBLE);
//                                                status.setText(getInfoWindowItemEnRouteStatus(enRoute));
//                                                status.setVisibility(View.VISIBLE);
//                                                if(stop_show_direction.equals("true")){
//                                                    direction.setText(getInfoWindowItemEnRouteDirection(enRoute));
//                                                    direction.setVisibility(View.VISIBLE);
//                                                }
//                                                if(stop_show_platform.equals("true")){
//                                                    track.setText(getInfoWindowItemEnRouteTrack(enRoute));
//                                                    track.setVisibility(View.VISIBLE);
//                                                }
//
//
//                                            }else{
//
//                                                textView.setText(getInfoWindowItemEnRouteText_Single(enRoute));
//                                                textView.setVisibility(View.VISIBLE);
//                                                if(enRoute.routeID != 0){
//                                                    route.setText(getInfoWindowItemEnRouteRouteName(enRoute));
//                                                    route.setVisibility(View.VISIBLE);
//                                                }
//                                                if(route_type_stop.equals("Train")){
//                                                    eta_text.setText("Train & ETA in minutes ");
//
//                                                }else{
//                                                    eta_text.setText("Bus & ETA in minutes");
//                                                }
////                                                time.setText(getInfoWindowItemEnRouteSchedule(enRoute));
////                                                time.setVisibility(View.VISIBLE);
////                                                status.setText(getInfoWindowItemEnRouteStatus(enRoute));
////                                                status.setVisibility(View.VISIBLE);
//                                            }
//                                        }
//                                    }
//
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            if( shouldReturnView )
//                return v;
//            else
//                return null;
//        }
//
//
//        @Override
//        public View getInfoContents( Marker marker )
//        {
//            return null;
//        }
//    };

}