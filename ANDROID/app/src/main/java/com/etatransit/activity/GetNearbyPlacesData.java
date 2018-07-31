package com.etatransit.activity;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by innovate on 22/8/17.
 */
public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    String googlePlacesData;
    GoogleMap mMap;
    String url;
    int type;
    ArrayList<MarkerOptions> placesList = new ArrayList<MarkerOptions>();

    @Override
    protected String doInBackground(Object... params) {
        try {
            mMap = (GoogleMap) params[0];
            url = (String) params[1];
            type = (int) params[2];
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
        nearbyPlacesList =  dataParser.parse(result);
        ShowNearbyPlaces(nearbyPlacesList);
    }

    private void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {
        clearOverlays();
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + vicinity);
            BitmapDescriptor bm = BitmapDescriptorFactory.fromResource(type);
            markerOptions.icon(bm);
            placesList.add(markerOptions);
            //move map camera
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
        for (MarkerOptions m:placesList)
            mMap.addMarker(m);
    }
    private void clearOverlays(){
            for (MarkerOptions m:placesList) {
                m.visible(false);
//                mMap.addMarker(m);
            }
    }
}
