package com.etatransit.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.etatransit.EtaAppController;
import com.etatransit.R;
import com.etatransit.adapter.RiderAlertsAdapter;
import com.etatransit.database.DatabaseHandler;
import com.etatransit.event.AgencyHasRoutesEvent;
import com.etatransit.event.AgencySelectedEvent;
import com.etatransit.event.BusProvider;
import com.etatransit.event.CloseFeedbackFragmentEvent;
import com.etatransit.event.CloseHelpFragmentEvent;
import com.etatransit.event.CloseSelectorEvent;
import com.etatransit.event.SectionSelectedEvent;
import com.etatransit.event.SelectedRoutesChangedEvent;
import com.etatransit.event.StopEtasUpdatedEvent;
import com.etatransit.event.VehiclesUpdatedEvent;
import com.etatransit.event.ViewStopsForRouteEvent;
import com.etatransit.event.WebUrlClickedEvent;
import com.etatransit.fragment.AgencySelectorFragment;
import com.etatransit.fragment.EtaMapFragment;
import com.etatransit.fragment.FindFragment;
import com.etatransit.fragment.MessagesFragment;
import com.etatransit.fragment.RouteListFragment;
import com.etatransit.fragment.RouteStopsFragment;
import com.etatransit.model.Agency;
import com.etatransit.model.Announcement;
import com.etatransit.model.AnnouncementSection;
import com.etatransit.model.EnRoute;
import com.etatransit.model.FavStop;
import com.etatransit.model.FavVehicleInfo;
import com.etatransit.model.MinutesToStop;
import com.etatransit.model.Route;
import com.etatransit.model.Stop;
import com.etatransit.model.StopEta;
import com.etatransit.model.Vehicle;
import com.etatransit.receiver.MyApplication;
import com.etatransit.service.AlertsService;
import com.etatransit.view.TriangleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import org.apache.http.NameValuePair;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements View.OnClickListener,OnMapReadyCallback
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String EXTRA_NETWORK_ID = "extra_network_id";
    private static final String EXTRA_AGENCY = "extra_agency";
    private static final String EXTRA_ROUTES = "extra_routes";
    private static final String EXTRA_SEL_ROUTES = "extra_sel_routes";
    private static final String EXTRA_CURRENT_FRAGMENT = "current_fragment";
    private List<Route> mSelRoutes = new ArrayList<Route>();
    //    public static Intent getStartingIntent( Context context )
//    {
//        Log.e("sadsa","sdsd");
//        Intent intent = new Intent( context, MainActivity.class );
//        return intent;
//    }
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private View mNavLayout;
    private View mRightDrawerLayout;
    private int mNetworkId = -1;
    private boolean mShouldSwitchFragments = false;
    private int mSectionId = 0;
    private Fragment mNewFragment;
    private ViewGroup mSubNavigation;
    private ViewGroup mSubNavigationArrows;
    private TriangleView mMapSubNavArrow;
    private TriangleView mRoutesSubNavArrow;
    private TriangleView mMessagesSubNavArrow;
    private AgencySelectorFragment mAgencySelectorFragment;
    private RouteListFragment mRouteListFragment;
    private EtaMapFragment mMapFragment;
    private MessagesFragment mMessagesFragment;
    private RouteStopsFragment mRouteStopsFragment;
    private boolean mIsVehicleLoading = false;
    private boolean mIsAnnouncementsLoading = false;
    private boolean mIsEtaLoading = false;
    private boolean mIsStopImagesLoading = false;
    private boolean mIsScheduleLoading = false;
    private Handler mVehicleLoadingHandler = new Handler();
    private Handler mEtaLoadingHandler = new Handler();
    private Handler mEtaAnnouncementsLoading = new Handler();
    private List<AnnouncementSection> mAnnouncements;
    LinearLayout mAlerts_Layout;
    TextView mAlert_text,no_alerts;
    private Dialog alertDialog;
    private DatabaseHandler dbobj;
    Cursor cursor;
    List agencies;
    String currentVersion;
    private final float LOW_MEMORY_THRESHOLD_PERCENT = 5.0f;
    TelephonyManager  telephonyManager;
    Context mContext;
    FindFragment lastRecentFrag;
    Spinner mapTypeSpinner;
    public Dialog favoritewindowDailog;
    ImageView favoritewindowClose;
    Button Favourite;
    ActionBar actionBar;
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        MultiDex.install(this);
        setContentView(R.layout.activity_main);
        com.nullwire.trace.ExceptionHandler.register(this,"http://etaqa2.etaspot.com/");
        Mint.initAndStartSession(MainActivity.this, getString(R.string.mint_key));
        actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
//            actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_title_layout);
        FirebaseApp.initializeApp(this);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        try {
            Intent intent1 = new Intent(getApplicationContext(), AlertsService.class);
            getApplicationContext().startService(intent1);
            initNavigationDrawer();
            mAlerts_Layout = (LinearLayout) findViewById(R.id.alert_layout);
            mAlert_text = (TextView) findViewById(R.id.alerts);
            mAlert_text.setSelected(true);
            mSubNavigation = (ViewGroup) findViewById(R.id.sub_navigation);
            mSubNavigationArrows = (ViewGroup) findViewById(R.id.sub_navigation_arrows);
            mMapSubNavArrow = (TriangleView) findViewById(R.id.sub_navigation_arrow_map);
            mRoutesSubNavArrow = (TriangleView) findViewById(R.id.sub_navigation_arrow_routes);
            mMessagesSubNavArrow = (TriangleView) findViewById(R.id.sub_navigation_arrow_messages);
            alertDialog = new Dialog(MainActivity.this);
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            if (savedInstanceState != null) {
                EtaAppController.getInstance().onRestoreInstanceState(savedInstanceState);
                if (savedInstanceState.containsKey(EXTRA_NETWORK_ID)) {
                    mNetworkId = savedInstanceState.getInt(EXTRA_NETWORK_ID);
                }

                if (savedInstanceState.containsKey(EXTRA_AGENCY)) {
                    String json = savedInstanceState.getString(EXTRA_AGENCY);
                    Agency agency = new Gson().fromJson(json, Agency.class);
                    EtaAppController.getInstance().setSelectedAgency(agency);
                }
            } else {
                generateLastKnownState();
            }

            dbobj = new DatabaseHandler(MainActivity.this);
            findViewById(R.id.sub_navigation_map).setOnClickListener(this);
            findViewById(R.id.sub_navigation_routes).setOnClickListener(this);
            findViewById(R.id.sub_navigation_messages).setOnClickListener(this);

            hideSubNavigation();
            if (EtaAppController.getInstance().getSelectedAgency() == null) {
                showSelectAgencyFragment();
            }
            else
            {
                mSectionId = -1;
                showRouteListFragment();
                showSubNavigation();
                showRoutesArrow();
                initEtaLoading();
                initVehicleLoading();
                initAnnouncementsLoading();
                initStopImageLoading();
                enableNavDrawers(true);
                Agency agency = EtaAppController.getInstance().getSelectedAgency();
//                getActionBar().setTitle(agency.name);
                mTitle = agency.name;
                mDrawerTitle = agency.name;
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setCustomView(R.layout.action_bar_title_layout);
                ((TextView) findViewById(R.id.action_bar_title)).setText(agency.name);
            }

        } catch (OutOfMemoryError e) {
            Log.e("Error", "OutOfMemory Exception");
        }
        checkAppMemory();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            checkInternetConnection();
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
//        try {
//            new FetchAppVersionFromGooglePlayStore().execute();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        favoritewindowDailog = new Dialog(this);
        favoritewindowDailog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        favoritewindowDailog.setContentView(R.layout.favourite_info_window);
        final TableLayout favorite_table = (TableLayout) favoritewindowDailog.findViewById(R.id.Favtable);
        final TextView NoFavData=(TextView) favoritewindowDailog.findViewById(R.id.NoFavData);
        mapTypeSpinner = (Spinner) findViewById(R.id.mapTypeSpinner);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(mapTypeSpinner);
            popupWindow.setHeight(140);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
        }
        List<String> list = new ArrayList<String>();
        list.add("Map");
        list.add("Hybrid");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.maptype_spinner_item, list);
        adapter.setDropDownViewResource(R.layout.maptype_spinner_dropdown_item);
        mapTypeSpinner.setAdapter(adapter);
        favoritewindowClose = (ImageView)favoritewindowDailog.findViewById(R.id.favoritewindowClose);
        Button clearbutton = (Button) findViewById(R.id.clear);
        Favourite = (Button) findViewById(R.id.Favourite);
        Favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Boolean isFavDataAvailable = false;
                Boolean isFavEnrouteDataAvailable = false;
                if( mMapFragment != null ) {
                    List<Integer> mMapfavorites = new ArrayList<Integer>();
                    mMapfavorites = mMapFragment.findFavoritesFromDB();
                    mMapFragment.infowindowDialog.dismiss();
                    List<FavStop> mFavContent = new ArrayList<FavStop>();
                    favorite_table.removeAllViews();
//                    Get Favorite Stations/Stops from DB
                    if (mMapfavorites.size() > 0)
                    {
                        for (int m : mMapfavorites){
                        for (StopEta s : EtaAppController.getInstance().getStopEtas()) {
                                if (s.id == m) {
                                    StopEta displayStopEta = s;
                                    isFavDataAvailable = true;
                                    if (displayStopEta.enRoute.size() > 0) {
                                        FavStop favContent = new FavStop();
                                        int numberOfVehicles = 0;
                                        if (displayStopEta.enRoute.size()>3)
                                            numberOfVehicles=3;
                                        else
                                            numberOfVehicles = displayStopEta.enRoute.size();
                                        for (int i = 0; i < numberOfVehicles; i++) {
                                            FavVehicleInfo favVehicleContent = new FavVehicleInfo();
                                            EnRoute enRoute = displayStopEta.enRoute.get(i);
                                            Vehicle v = getVehicleDataByID(enRoute);
                                            if (v != null && v.routeID != 777) {
//                                    Checking if any Vehicle come across the stop
                                                Route r = getRouteDataByID(v.routeID);
                                                favContent.favStopName = getStopNameByID(displayStopEta.id);
                                                isFavEnrouteDataAvailable = true;
                                                if (r != null) {
                                                    favContent.routeName = r.name;
                                                    String minutes = getETAString(enRoute.minutes);
                                                    List<MinutesToStop> minutesToStops = v.getMinutesToNextStopList();

                                                    for (int j = 0; j < minutesToStops.size(); j++) {
                                                        if (minutesToStops.get(j).stopID == displayStopEta.id) {
                                                            if (minutesToStops.get(j).minutes > 2)
                                                                minutes = getETAString(minutesToStops.get(j).minutes);
                                                            else
                                                                minutes = "less than 2 mins";
                                                            break;

                                                        }
                                                    }
                                                    favVehicleContent.vehicleIdentifier = v.equipmentID;
                                                    favVehicleContent.vehicleMinutes = minutes;
                                                    favContent.vehicleDataObjects.add(favVehicleContent);
                                                }
                                            }
                                        }
                                        if (favContent.favStopName!=null||favContent.routeName!=null) {
                                            mFavContent.add(favContent);
                                        }
                                    }
                                }
                        }
                        }
                }

                    if (mFavContent.size() > 0 &&isFavEnrouteDataAvailable ) {
                        NoFavData.setVisibility(View.GONE);
                        //Create the tableHead
                        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT);
                        TableRow favHead = new TableRow(getApplicationContext());
                        TextView routeHeadTextView = new TextView(getApplicationContext());
                        TextView favStopHeadTextView = new TextView(getApplicationContext());
                        TextView vehicleIdHeadTextView = new TextView(getApplicationContext());
                        favStopHeadTextView.setText("Stop Name ");
                        routeHeadTextView.setText("Route");
                        vehicleIdHeadTextView.setText("Bus#    ETA");
                        routeHeadTextView.setTextColor(Color.BLACK);
                        favStopHeadTextView.setTextColor(Color.BLACK);
                        vehicleIdHeadTextView.setTextColor(Color.BLACK);
                        favStopHeadTextView.setTypeface(Typeface.DEFAULT_BOLD);
                        routeHeadTextView.setTypeface(Typeface.DEFAULT_BOLD);
                        vehicleIdHeadTextView.setTypeface(Typeface.DEFAULT_BOLD);
                        vehicleIdHeadTextView.setGravity(Gravity.LEFT);
                        favHead.addView(favStopHeadTextView);
                        favHead.addView(routeHeadTextView);
                        favHead.addView(vehicleIdHeadTextView);
                        favHead.setPadding(0,14,0,12);
                        favorite_table.addView(favHead,0);
                        for (int i=0;i<mFavContent.size();i++)
                         {

                            //Create the tableBody
                            TableRow favRow = new TableRow(getApplicationContext());
//                             TableRow favLineRow = new TableRow(getApplicationContext());
//                             View view = new View(getApplicationContext());
//                             view.setLayoutParams(new TableRow.LayoutParams(150, 20));
//                             view.setBackgroundColor(Color.GRAY);
//                             Log.e("Viewfavorites", "favRow" + i);
//                             favLineRow.addView(view);

                            TextView routeNameTextView = new TextView(getApplicationContext());
                            TextView favStopNameTextView = new TextView(getApplicationContext());
                             TextView vehicleIdentifierTextView = new TextView(getApplicationContext());
                            routeNameTextView.setText(mFavContent.get(i).routeName);
                            favStopNameTextView.setText(mFavContent.get(i).favStopName);
                            routeNameTextView.setTextColor(Color.BLACK);
                            favStopNameTextView.setTextColor(Color.BLACK);
                             vehicleIdentifierTextView.setTextColor(Color.BLACK);
                             routeNameTextView.setMaxWidth(120);
                             favStopNameTextView.setMaxWidth(120);
                             vehicleIdentifierTextView.setMaxWidth(400);
                             favStopNameTextView.setPadding(0,0,5,0);
                             routeNameTextView.setPadding(0,0,6,0);
                             vehicleIdentifierTextView.setGravity(Gravity.LEFT);

                            favRow.addView(favStopNameTextView);
                            favRow.addView(routeNameTextView);
                             favRow.setGravity(Gravity.CENTER_VERTICAL);
                             if(mFavContent.get(i).vehicleDataObjects.size()>1) {
                                 for (int j = 0; j < mFavContent.get(i).vehicleDataObjects.size(); j++) {
                                     FavVehicleInfo fv = mFavContent.get(i).vehicleDataObjects.get(j);
                                     vehicleIdentifierTextView.append(fv.vehicleIdentifier +"\t\t"+ fv.vehicleMinutes+"\n");
                                 }
                             }
                             else if (mFavContent.get(i).vehicleDataObjects.size()==1)
                             {
                                 for (int j = 0; j < mFavContent.get(i).vehicleDataObjects.size(); j++) {
                                     FavVehicleInfo fv = mFavContent.get(i).vehicleDataObjects.get(j);
                                     vehicleIdentifierTextView.setText(fv.vehicleIdentifier+"\t\t"+ fv.vehicleMinutes);
                                 }
                             }
                             favRow.addView(vehicleIdentifierTextView);
                             favorite_table.addView(favRow,i+1);
//                             favorite_table.addView(favLineRow);
                         }

                    }
                    else if(mMapfavorites.size() == 0)
                    {
                        NoFavData.setVisibility(View.VISIBLE);
                        NoFavData.setText("No Favorite Stops Selected.");
                    }
                    else if(mMapfavorites.size()>1&&mFavContent.size() ==0){
                        NoFavData.setVisibility(View.VISIBLE);
                        NoFavData.setText("No Service for the Selected Favorite Stops.");
                    }
                    else if(mFavContent.size()>=0)
                    {
                        NoFavData.setVisibility(View.VISIBLE);
                        NoFavData.setText("No Service for the Selected Favorite Stops.");
                    }


                }
                ImageView dialogCloseButton = (ImageView)favoritewindowDailog. findViewById(R.id.favoritewindowClose);
                dialogCloseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        favoritewindowDailog.dismiss();
                    }
                });
                favoritewindowDailog.show();
            }
        });
    }
    @Override
    public void onMapReady(final GoogleMap map) {
        //changing maptype according to the  spinner position
        mapTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        map.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1:
                        map.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID);
                        break;
                }
            }
        });
    }
    private void checkAppMemory() {
        // Get app memory info
        long available = Runtime.getRuntime().maxMemory();
        long used = Runtime.getRuntime().totalMemory();

        // Check for & and handle low memory state
        float percentAvailable = 100f * (1f - ((float) used / available ));
        if( percentAvailable <= LOW_MEMORY_THRESHOLD_PERCENT ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Low on Memory!")
                    .setMessage("You are running low on memory, please close some applications.")
                    .setCancelable(false)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                    /*Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory( Intent.CATEGORY_HOME );
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);*/
                            //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void checkInternetConnection() {
        if (!haveNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("No Network")
                    .setMessage("Not Connected to Internet")
                    .setCancelable(false)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            try {
                PoorConnection poor = new PoorConnection(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                    poor.checkConnectionSpeed();
//            telephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
//            telephonyManager.listen(poor,poor.LISTEN_SIGNAL_STRENGTHS);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
    protected void onResume()
    {

        super.onResume();
        Mint.startSession( MainActivity.this );
        BusProvider.getInstance().register( this );
        EtaAppController.getInstance().registerBus();
        //.checkInternetConnection();
        MyApplication.activityResumed();
        try{
            List<Agency> agencies = EtaAppController.getInstance().getAgencies();
        }
        catch (Exception se){
            se.getStackTrace();
        }
        if( agencies == null || agencies.isEmpty() ) {
            EtaAppController.getInstance().loadAgencies();

        }

        if( EtaAppController.getInstance().getSelectedAgency() == null )
        {
            enableNavDrawers( false );
        }else
        {
            EtaAppController.getInstance().loadAllRoutesForAgency();
            EtaAppController.getInstance().loadAllStopsForAgency();
            EtaAppController.getInstance().loadVehicles();
            initVehicleLoading();
            initEtaLoading();
            initAnnouncementsLoading();
            initStopImageLoading();

        }
        UpdateAlerts();
        getFragmentManager().addOnBackStackChangedListener( mBackstackListener );

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        BusProvider.getInstance().unregister( this );
        EtaAppController.getInstance().unregisterBus();
        //checkInternetConnection();
        MyApplication.activityPaused();
        cancelLoading();
        getFragmentManager().removeOnBackStackChangedListener(mBackstackListener);
        /*if(alertDialog.isShowing()){
            Log.e("sad","sadsafdsf bbbbbbbbbbbbb");
            alertDialog.dismiss();
        }*/

    }

    @Override
    protected void onStop() {
        super.onStop();
        Mint.closeSession( MainActivity.this );
        Mint.flush();
    }

    @Override
    protected void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        outState.putInt( EXTRA_NETWORK_ID, mNetworkId );
        outState.putString( EXTRA_AGENCY, new Gson().toJson( EtaAppController.getInstance().getSelectedAgency() ) );
        EtaAppController.getInstance().onSaveInstanceState(outState);

    }

    private void enableNavDrawers( boolean enabled )
    {
        if( enabled )
        {
            mDrawerLayout.setDrawerListener( mDrawerToggle );
            getActionBar().setDisplayHomeAsUpEnabled( true );
            getActionBar().setHomeButtonEnabled( true );

            mRightDrawerLayout.setVisibility( View.VISIBLE );
            mNavLayout.setVisibility( View.VISIBLE );

            mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_UNLOCKED );
        }else
        {
            mDrawerLayout.setDrawerListener( null );
            getActionBar().setDisplayHomeAsUpEnabled( false );
            getActionBar().setHomeButtonEnabled( false );

            mRightDrawerLayout.setVisibility( View.GONE );
            mNavLayout.setVisibility( View.GONE );

            mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_LOCKED_CLOSED );
        }

    }

    private void initNavigationDrawer()
    {
        mNavLayout = findViewById( R.id.navigation_drawer );
        mRightDrawerLayout = findViewById( R.id.right_drawer );
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout );
        mDrawerToggle = new ActionBarDrawerToggle( this, mDrawerLayout, R.drawable.ic_navigation_drawer, R.string.text_drawer_open, R.string.text_drawer_closed )
        {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed( View drawerView )
            {
                if( drawerView.equals( mNavLayout ) )
                {
                    getActionBar().setTitle( mTitle );
                    invalidateOptionsMenu();
                    handleOnDrawerClosed();
                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened( View drawerView )
            {
                if( drawerView.equals( mNavLayout ) )
                {
                    getActionBar().setTitle( mDrawerTitle );
                    invalidateOptionsMenu();
                    handleOnDrawerOpened();
                }
            }

            @Override
            public void onDrawerSlide( View drawerView, float slideOffset )
            {
                if( drawerView.equals( mNavLayout ) )
                    super.onDrawerSlide( drawerView, slideOffset );
            }
        };

        if( EtaAppController.getInstance().getSelectedAgency() != null )
            enableNavDrawers( true );
        else
            enableNavDrawers( false );

        Fragment feedbackFragment = getFragmentManager().findFragmentByTag( "feedback" );
        getFragmentManager().beginTransaction().hide( feedbackFragment ).commit();
    }

    @Override
    protected void onPostCreate( Bundle savedInstanceState )
    {
        super.onPostCreate( savedInstanceState );
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();

    }

    @Override
    public void onConfigurationChanged( Configuration newConfig )
    {
        super.onConfigurationChanged( newConfig );
        mDrawerToggle.onConfigurationChanged( newConfig );

    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case android.R.id.home:
            {
                mDrawerToggle.onOptionsItemSelected( item );

                if( mDrawerLayout.isDrawerOpen( mRightDrawerLayout ) )
                    mDrawerLayout.closeDrawer( mRightDrawerLayout );

                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean getIsDrawerOpen()
    {
        return mDrawerLayout.isDrawerOpen( mNavLayout );
    }

    private void closeDrawer()
    {
        mDrawerLayout.closeDrawer( mNavLayout );
    }

    public void closeRightDrawer()
    {
        mDrawerLayout.closeDrawer( mRightDrawerLayout );
    }

    private void showHelpDrawer()
    {
        Fragment helpFragment = getFragmentManager().findFragmentByTag( "help" );
        Fragment feedbackFragment = getFragmentManager().findFragmentByTag( "feedback" );
        Fragment rideralertsFragment = getFragmentManager().findFragmentByTag( "rideralerts" );

        getFragmentManager().beginTransaction().hide( feedbackFragment ).show( helpFragment ).commit();
        mDrawerLayout.openDrawer( mRightDrawerLayout );
    }

    private void showFeedbackDrawer()
    {
        Fragment helpFragment = getFragmentManager().findFragmentByTag( "help" );
        Fragment rideralertsFragment = getFragmentManager().findFragmentByTag( "rideralerts" );
        Fragment feedbackFragment = getFragmentManager().findFragmentByTag( "feedback" );
        getFragmentManager().beginTransaction().hide( helpFragment ).show( feedbackFragment ).commit();
        mDrawerLayout.openDrawer( mRightDrawerLayout );
    }

    private void showRiderAlerts(){

        alertDialog.setContentView(R.layout.fragment_rideralerts);
        no_alerts = (TextView)alertDialog.findViewById(R.id.no_alerts);
        openAndQueryDatabase();
        final ListView alertsList = (ListView)alertDialog.findViewById(R.id.alertList);
        alertsList.setEmptyView(no_alerts);
        RiderAlertsAdapter adapter = new RiderAlertsAdapter(MainActivity.this,  cursor);
        alertsList.setAdapter(adapter);

        final ImageView alertsListClose = (ImageView)alertDialog.findViewById(R.id.alertsListClose);
        alertsListClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        alertDialog.dismiss();
                    }
                });
            }
        });

        Button addRiderAlert = (Button)alertDialog.findViewById(R.id.addRiderAlert);
        addRiderAlert.setVisibility(View.GONE);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Your dialog code.
                alertDialog.show();
            }
        });

    }

    private void openAndQueryDatabase() {
        try {
            cursor = dbobj.fetchAllRiderAlerts();

        } catch (SQLiteException se ) {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        } finally {

        }
    }

    private void showRateApp()
    {
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            ArrayList<NameValuePair> nameValuePairs = (ArrayList<NameValuePair>)getIntent().getSerializableExtra("FILE_TO_SEND");

        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void showSelectAgencyFragment()
    {
        if( mAgencySelectorFragment != null && mAgencySelectorFragment.isVisible() )

            return;

        enableNavDrawers( false );

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations( android.R.animator.fade_in, android.R.animator.fade_out );
        ft.disallowAddToBackStack();

        if( mAgencySelectorFragment == null )
            mAgencySelectorFragment = AgencySelectorFragment.newInstance();

        /*if( mMapFragment != null )
            ft.hide( mMapFragment );

        if( mRouteListFragment != null )
            ft.hide( mRouteListFragment );

        if( mMessagesFragment != null )
            ft.hide( mMessagesFragment );*/
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( this );
        preferences.edit().putString( EXTRA_CURRENT_FRAGMENT, lastRecentFrag.getFragment() ).commit();

        if( mRouteStopsFragment != null )
        {
            getFragmentManager().popBackStack();
            ft.remove(mRouteStopsFragment);
        }
        mRouteStopsFragment = null;

        ft.replace( R.id.agency_selector_container, mAgencySelectorFragment );
        ft.commit();
    }

    private void showRouteListFragment()
    {
        if( mRouteListFragment != null && mRouteListFragment.isVisible() )
            return;
        enableNavDrawers(true);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations( android.R.animator.fade_in, android.R.animator.fade_out );
        ft.disallowAddToBackStack();
        findViewById(R.id.clear).setVisibility(View.GONE);
        findViewById(R.id.mapTypeSpinner).setVisibility(View.GONE);
        findViewById(R.id.Favourite).setVisibility(View.GONE);
        if( mRouteListFragment == null )
        {
            mRouteListFragment = RouteListFragment.newInstance();
            ft.add(R.id.content_frame, mRouteListFragment);
        }else
        {
            ft.show( mRouteListFragment );
        }
        if( mMapFragment != null )
        {
            ft.hide( mMapFragment );
        }

        if( mMessagesFragment != null )
        {
            ft.hide( mMessagesFragment );
        }

        if( mRouteStopsFragment != null )
        {
            getFragmentManager().popBackStack();
            ft.remove(mRouteStopsFragment);
        }
        mRouteStopsFragment = null;
        ft.commit();
    }


    private void showMapFragment()
    {
        if( mMapFragment != null && mMapFragment.isVisible() )
            return;
        enableNavDrawers(true);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations( android.R.animator.fade_in, android.R.animator.fade_out );
        ft.disallowAddToBackStack();
        findViewById(R.id.clear).setVisibility(View.VISIBLE);
        findViewById(R.id.Favourite).setVisibility(View.VISIBLE);
        mapTypeSpinner.setVisibility(View.VISIBLE);

        if( mMapFragment == null )
        {
            mMapFragment = EtaMapFragment.newInstance();
            ft.add( R.id.content_frame, mMapFragment );
            mMapFragment.getMapAsync(this);
            View clearbutton = findViewById(R.id.clear);
            clearbutton.setVisibility(View.VISIBLE);
            mapTypeSpinner.setVisibility(View.VISIBLE);
        }else
        {
            ft.show( mMapFragment );
        }

        if( mRouteListFragment != null )
        {
            ft.hide( mRouteListFragment );
        }

        if( mMessagesFragment != null )
        {
            ft.hide( mMessagesFragment );
        }

        if( mRouteStopsFragment != null )
        {
            getFragmentManager().popBackStack();
            ft.remove(mRouteStopsFragment);
        }

        Timer timer = new Timer();
        TimerTask timerTask;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // check if GPS enabled
                UpdateAlerts();
            }
        };
        timer.schedule(timerTask, 0, 25000);
        mRouteStopsFragment = null;
        mMapFragment.setSelectedRoutes( EtaAppController.getInstance().getSelectedRoutes() );
        mMapFragment.SelectedAgency(EtaAppController.getInstance().getSelectedAgency());
        ft.commit();

    }
    private void UpdateAlerts() {

        mAnnouncements = EtaAppController.getInstance().getAnnouncements();
        if( mAnnouncements == null || mAnnouncements.isEmpty() ){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAlerts_Layout.setVisibility(View.GONE);
                }
            });
        }
        else {
            for (AnnouncementSection section : mAnnouncements) {
                if (section != null && section.announcements != null) {
                    for (final Announcement a : section.announcements) {
                        if(AnnouncementSection.TYPE_HIGH.equalsIgnoreCase(section.type)){
                            Log.e("success",a.text.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAlerts_Layout.setVisibility(View.VISIBLE);
                                    mAlert_text.setText(a.text);
                                }
                            });

                        }
                    }
                }
            }
        }
    }

    private void showMessagesFragment()
    {
        if( mMessagesFragment != null && mMessagesFragment.isVisible() )
            return;

        enableNavDrawers(true);
        findViewById(R.id.mapTypeSpinner).setVisibility(View.GONE);
        findViewById(R.id.clear).setVisibility(View.GONE);
        findViewById(R.id.Favourite).setVisibility(View.GONE);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations( android.R.animator.fade_in, android.R.animator.fade_out );
        ft.disallowAddToBackStack();

        if( mMessagesFragment == null )
        {
            mMessagesFragment = MessagesFragment.newInstance();
            ft.add( R.id.content_frame, mMessagesFragment );
        }else
        {
            ft.show( mMessagesFragment );
        }

        if( mRouteListFragment != null )
        {
            ft.hide( mRouteListFragment );
        }

        if( mMapFragment != null )
        {
            ft.hide( mMapFragment );
        }

        if( mRouteStopsFragment != null )
        {
            getFragmentManager().popBackStack();
            ft.remove(mRouteStopsFragment);

        }
        mRouteStopsFragment = null;
        mMessagesFragment.loadMessages();
        ft.commit();

    }

    private void showRouteStopsFragment( int routeId )
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations( android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out );

        mRouteStopsFragment = RouteStopsFragment.newInstance( routeId );

        enableNavDrawers(false);

        findViewById(R.id.mapTypeSpinner).setVisibility(View.GONE);
        findViewById(R.id.clear).setVisibility(View.GONE);
        findViewById(R.id.Favourite).setVisibility(View.GONE);
        ft.add( R.id.route_stops_container, mRouteStopsFragment );
        ft.addToBackStack( null );
        ft.commit();

    }

    private void handleOnDrawerClosed()
    {
        if( mShouldSwitchFragments )
        {
            mShouldSwitchFragments = false;
            showSelectAgencyFragment();
        }
    }

    private void handleOnDrawerOpened()
    {

    }

    private void showSubNavigation()
    {
        mSubNavigation.setVisibility( View.VISIBLE );
        showArrows();
        enableNavDrawers(true);
    }

    private void hideSubNavigation()
    {
        mSubNavigation.setVisibility( View.GONE );
        hideArrows();
    }

    private void showRoutesArrow()
    {
        mRoutesSubNavArrow.setVisibility( View.VISIBLE );
        mMapSubNavArrow.setVisibility( View.INVISIBLE );
        mMessagesSubNavArrow.setVisibility( View.INVISIBLE );
    }

    private void showMapArrow()
    {
        mRoutesSubNavArrow.setVisibility( View.INVISIBLE );
        mMapSubNavArrow.setVisibility( View.VISIBLE );
        mMessagesSubNavArrow.setVisibility( View.INVISIBLE );
    }

    private void showMessagesArrow()
    {
        mRoutesSubNavArrow.setVisibility( View.INVISIBLE );
        mMapSubNavArrow.setVisibility( View.INVISIBLE );
        mMessagesSubNavArrow.setVisibility( View.VISIBLE );
    }

    private void showArrows()
    {
        mSubNavigationArrows.setVisibility( View.VISIBLE );
    }

    private void hideArrows()
    {
        mSubNavigationArrows.setVisibility( View.GONE );
    }

    @Override
    public void onClick( View v )
    {
        int id = v.getId();
        if( id == R.id.sub_navigation_routes )
        {
            mAlerts_Layout.setVisibility(View.GONE);
            showRouteListFragment();
            showRoutesArrow();
        }else if( id == R.id.sub_navigation_map )
        {
            showMapFragment();
            showMapArrow();
        }else if( id == R.id.sub_navigation_messages )
        {
            mAlerts_Layout.setVisibility(View.GONE);
            showMessagesFragment();
            showMessagesArrow();
        }
    }

    @Subscribe
    public void onAgencySelected( AgencySelectedEvent event )
    {
        clearStoredRoutes();
        mSectionId = -1;
        EtaAppController.getInstance().setStops( EtaAppController.getInstance().getStops() );
        EtaAppController.getInstance().setRoutes( EtaAppController.getInstance().getRoutes() );
        showRouteListFragment();
        Agency agency = EtaAppController.getInstance().getSelectedAgency();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( this );
        SharedPreferences.Editor edit = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson( agency );
        edit.putString( EXTRA_AGENCY, json );
        edit.commit();
//        getActionBar().setTitle( agency.name );
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_title_layout);
        ((TextView) findViewById(R.id.action_bar_title)).setText(agency.name);
        mTitle = agency.name;
        mDrawerTitle = agency.name;
        showSubNavigation();
        showRoutesArrow();
        initEtaLoading();
        initVehicleLoading();
        initAnnouncementsLoading();
        initStopImageLoading();
        enableNavDrawers( true );
        if( mAgencySelectorFragment != null )
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations( android.R.animator.fade_in, android.R.animator.fade_out );
            ft.remove( mAgencySelectorFragment );
            ft.commit();
        }
    }

    @Subscribe
    public void onSectionSelected( SectionSelectedEvent event )
    {
        if( event.section == 0 && mSectionId != 0 )
        {
            if( getIsDrawerOpen() )
            {
                mShouldSwitchFragments = true;
                closeDrawer();
            }else
            {
                showSelectAgencyFragment();
                hideSubNavigation();
            }
        }else
        {
            closeDrawer();

            if( event.section == 3 )
            {
                showRateApp();
            }else if(event.section == 2 )
            {
                showRiderAlerts();
            }else if( event.section == 1 )
            {
                showFeedbackDrawer();
            }
            else if( event.section == 4 )
            {
                showHelpDrawer();
            }
        }
    }

    @Subscribe
    public void onSelectedRoutesChanged( SelectedRoutesChangedEvent routesChanged )
    {
        if( mMapFragment != null )
        {
            mMapFragment.setSelectedRoutes( EtaAppController.getInstance().getSelectedRoutes() );
        }
    }

    @Subscribe
    public void onViewStopsForRoute( ViewStopsForRouteEvent event )
    {
        showRouteStopsFragment( event.routeId );
    }

    @Subscribe
    public void onVehiclesLoaded( VehiclesUpdatedEvent event )
    {
        mIsVehicleLoading = false;
        initVehicleLoading();
    }

    @Subscribe
    public void onStopEtasLoaded( StopEtasUpdatedEvent event )
    {
        mIsEtaLoading = false;
        initEtaLoading();
    }

    @Subscribe
    public void onAgencyHasRoutesEvent( AgencyHasRoutesEvent event )
    {
        if( event.hasRoutes )
            showSubNavigation();
        else
            hideSubNavigation();
    }

    @Subscribe
    public void onCloseHelpFragmentEvent( CloseHelpFragmentEvent event )
    {
        closeRightDrawer();
    }

    @Subscribe
    public void onCloseFeedbackFragmentEvent( CloseFeedbackFragmentEvent event )
    {
        closeRightDrawer();
    }

    @Subscribe
    public void onWebUrlClickedEvent( WebUrlClickedEvent event )
    {
        Intent intent = new Intent( this, WebViewActivity.class );
        intent.putExtra( WebViewActivity.EXTRA_URL, event.url );
        startActivity( intent );
    }

    @Subscribe
    public void onCloseSelectorFragment( CloseSelectorEvent event )
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations( android.R.animator.fade_in, android.R.animator.fade_out );
        ft.remove( mAgencySelectorFragment );
        ft.commit();
        showSubNavigation();
    }

    private void initVehicleLoading()
    {
        if( mIsVehicleLoading )
            return;

        mIsVehicleLoading = true;
        mVehicleLoadingHandler.removeCallbacks( mVehicleLoadingHandlerCallback );
        mVehicleLoadingHandler.postDelayed( mVehicleLoadingHandlerCallback, 2500 );
    }

    private void initAnnouncementsLoading()
    {
        if( mIsAnnouncementsLoading )
            return;

        mIsAnnouncementsLoading = true;

        Timer timer = new Timer();
        TimerTask timerTask;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // check if GPS enabled
                EtaAppController.getInstance().loadAnnouncements();
            }
        };
        timer.schedule(timerTask, 0, 20000);
//        mEtaAnnouncementsLoading.removeCallbacks( mAnnouncementsHandlerCallback );
//        mEtaAnnouncementsLoading.postDelayed( mAnnouncementsHandlerCallback, 2500 );
    }
    private void initStopImageLoading()
    {
        if( mIsStopImagesLoading )
            return;

        mIsStopImagesLoading = true;

        Timer timer = new Timer();
        TimerTask timerTask;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // check if GPS enabled
                EtaAppController.getInstance().loadStopImagesForAgency();
            }
        };
        timer.schedule(timerTask, 0, 20000);
//        mEtaAnnouncementsLoading.removeCallbacks( mAnnouncementsHandlerCallback );
//        mEtaAnnouncementsLoading.postDelayed( mAnnouncementsHandlerCallback, 2500 );
    }

    private void initScheduleLoading(final int stopID)
    {
        if( mIsScheduleLoading )
            return;

        mIsScheduleLoading = true;

        Timer timer = new Timer();
        TimerTask timerTask;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // check if GPS enabled
                EtaAppController.getInstance().loadScheduleForAgency(stopID);
            }
        };
        timer.schedule(timerTask, 0, 20000);
    }

    private void initEtaLoading()
    {
        if( mIsEtaLoading ) {
            return;
        }

        mIsEtaLoading = true;
        mEtaLoadingHandler.removeCallbacks( mEtaLoadingHandlerCallback );
        mEtaLoadingHandler.postDelayed( mEtaLoadingHandlerCallback, 2500 );
    }

    private void cancelLoading()
    {
        mVehicleLoadingHandler.removeCallbacks( mVehicleLoadingHandlerCallback );
        mIsVehicleLoading = false;

        mEtaLoadingHandler.removeCallbacks( mEtaLoadingHandlerCallback );
        mIsEtaLoading = false;

//        mEtaAnnouncementsLoading.removeCallbacks( mEtaLoadingHandlerCallback );
//        mIsAnnouncementsLoading = false;
    }

    private Runnable mVehicleLoadingHandlerCallback = new Runnable()
    {
        @Override
        public void run()
        {
            EtaAppController.getInstance().loadVehicles();
        }
    };

    private Runnable mEtaLoadingHandlerCallback = new Runnable()
    {
        @Override
        public void run() {
            EtaAppController.getInstance().loadEtas();
        }
    };

    private void generateLastKnownState()
    {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if( preferences.contains( EXTRA_NETWORK_ID ) )
        {
            mNetworkId = preferences.getInt( EXTRA_NETWORK_ID, -1 );
        }

        if( preferences.contains( EXTRA_AGENCY ) )
        {
            String agencyJson = preferences.getString( EXTRA_AGENCY, "" );
            Gson gson = new Gson();
            Agency agency = gson.fromJson( agencyJson, Agency.class );
            EtaAppController.getInstance().setSelectedAgency( agency );
        }
    }


    private FragmentManager.OnBackStackChangedListener mBackstackListener = new FragmentManager.OnBackStackChangedListener()
    {
        @Override
        public void onBackStackChanged()
        {
            if( getFragmentManager().getBackStackEntryCount() > 0 )
                enableNavDrawers(false);
            else
                enableNavDrawers(true);
        }
    };

    private void clearStoredRoutes(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(RouteListFragment.PREFS_SELECTED_ROUTES);
        editor.remove(EXTRA_AGENCY);
        editor.remove(EXTRA_CURRENT_FRAGMENT);
        editor.commit();
    }

    public class FetchAppVersionFromGooglePlayStore extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                String appVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + "com.etatransit" + "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        .ownText();
                return appVersion;
            }
            catch (IOException e) {
                e.printStackTrace();
                return "";
            }catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
        @Override
        protected void onPostExecute(String onlineVersion) {
            super.onPostExecute(onlineVersion);
            if (onlineVersion != null && !onlineVersion.isEmpty()) {
                if (currentVersion.equals(onlineVersion))
                {
                    Log.d("update", "No Update of App Required");
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder
                            .setTitle("Update Available")
                            .setMessage("A new version of Spot is available.")
                            .setCancelable(false)
                            .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("Update",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/apps/details?id=com.etatransit"));
                                    startActivity(i);
                                    dialog.dismiss();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }

    }
    private String getStopNameByID(int stopID) {
        List<Stop> stopData = EtaAppController.getInstance().getStops();
        for (Stop s:stopData) {
            if (s.id == stopID) {
                return s.name;
            }
        }
        return null;
    }
    private Vehicle getVehicleDataByID(EnRoute enRoute)
    {
        List<Vehicle> tempvehicledata = EtaAppController.getInstance().getVehicles();
        for (Vehicle vehicledata : tempvehicledata) {
            if (enRoute.equipmentID.equals(vehicledata.equipmentID)) {
//                Log.d("getVehicleDataByID",vehicledata.equipmentID+"  :  "+enRoute.equipmentID);
                return vehicledata;
            }
        }
        return null;
    }
    private Route getRouteDataByID(int routeID)
    {
        List<Route> temproutedata = EtaAppController.getInstance().getRoutes();
        for (Route routedata:temproutedata){
            if (routedata!=null && routedata.id == routeID)
            {
                return routedata;
            }
        }
        return null;
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
