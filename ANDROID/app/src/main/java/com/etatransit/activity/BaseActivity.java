
package com.etatransit.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

import com.etatransit.EtaAppController;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.splunk.mint.Mint;

/**
 * Created by mark on 11/25/14.
 */
public class BaseActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        if( isGooglePlayServicesAvailable() )
        {
            Mint.leaveBreadcrumb("BaseActivity onCreate PLAY ");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }else
        {
            Mint.leaveBreadcrumb("BaseActivity playService_NOT_AVAILABLE");
            startActivity( new Intent( this, ErrorActivity.class ) );
            finish();
        }

    }


    @Override
    protected void onStart()
    {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        mGoogleApiClient.disconnect();

    }


    private boolean isGooglePlayServicesAvailable()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if( ConnectionResult.SUCCESS == resultCode )
        {
            return true;

            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        }else
        {
            showErrorDialog( resultCode );
            return false;
        }


    }


    private void showErrorDialog( int code )
    {
        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog( code, this, CONNECTION_FAILURE_RESOLUTION_REQUEST );

        // If Google Play services can provide an error dialog
        if( errorDialog != null )
        {
            // Create a new DialogFragment for the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            // Set the dialog in the DialogFragment
            errorFragment.setDialog( errorDialog );
            // Show the error dialog in the DialogFragment
            errorFragment.show( getFragmentManager(), "Location Updates" );
        }
    }


    @Override
    public void onConnected( Bundle bundle )
    {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if( location == null )
        {
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority( LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY )
                    .setFastestInterval( 5000L )
                    .setInterval( 10000L )
                    .setSmallestDisplacement( 75.0F );
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }else {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            EtaAppController.getInstance().setCurrentLocation( latLng );
        }

    }


    @Override
    public void onConnectionSuspended(int i)
    {

    }


    @Override
    public void onConnectionFailed( ConnectionResult connectionResult )
    {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if( connectionResult.hasResolution() )
        {
            try
            {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult( this, CONNECTION_FAILURE_RESOLUTION_REQUEST );
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            }catch( IntentSender.SendIntentException e )
            {
                e.printStackTrace();
                // Log the error
            }
        }else
        {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog( connectionResult.getErrorCode() );
        }
    }


    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {

        // Decide what to do based on the original request code
        switch( requestCode )
        {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch( resultCode )
                {
                    case Activity.RESULT_OK:
                        break;
                }
            default:
                break;
        }
    }


    @Override
    public void onLocationChanged( Location location )
    {
        if( location != null )
        {
            LocationServices.FusedLocationApi.removeLocationUpdates( mGoogleApiClient, this );
            LatLng latLng = new LatLng( location.getLatitude(), location.getLongitude() );
            EtaAppController.getInstance().setCurrentLocation( latLng );
        }
    }


    public static class ErrorDialogFragment extends DialogFragment
    {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment()
        {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog( Dialog dialog )
        {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog( Bundle savedInstanceState )
        {
            return mDialog;
        }
    }
}
