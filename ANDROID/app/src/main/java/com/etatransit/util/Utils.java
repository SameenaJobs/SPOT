package com.etatransit.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.VectorDrawable;
import android.util.Log;
import android.util.TypedValue;

import com.etatransit.EtaAppController;
import com.etatransit.R;
import com.etatransit.model.Route;
import com.etatransit.model.Stop;
import com.etatransit.model.Vehicle;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by mark on 10/5/14.
 */
public class Utils
{

    private static final String TAG = Utils.class.getSimpleName();


    public static int convertColorStringToHex( String color )
    {
        color = color.replace( "#", "" );
        return Integer.parseInt( color, 16 ) + 0xFF000000;
    }
    public static int converColorStringToInt(String color)
    {
        color = color.replace( "#", "" );
        return Integer.parseInt( color, 16 );
    }

    public static int getDarkerColor(int color, float factor) {
//        Log.d("isColorDark", "getDarkerColor StopFav");
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
        hsv[2] = hsv[2] * factor;
        return  Color.HSVToColor(hsv);
    }
    public static int getLightColor(int color, float factor) {
//        Log.d("isColorDark", "getDarkerColor StopFav");
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
        hsv[2] = 0.2f * (1.0f - hsv[2]);
        return  Color.HSVToColor(hsv);
    }

    public static MarkerOptions drawArrowHead( Context context, int color, LatLng from, LatLng to )
    {
        // obtain the bearing between the last two points
        double bearing = getBearing( from, to );
        bearing -= 90;

        float anchorX = 0.5f;
        float anchorY = 0.5f;

        LayerDrawable ld = (LayerDrawable) context.getResources().getDrawable( R.drawable.direction_arrow_marker );
        Bitmap b = Bitmap.createBitmap( ld.getIntrinsicWidth(), ld.getIntrinsicHeight(), Bitmap.Config.ARGB_8888 );

        ld.setBounds( 0, 0, ld.getIntrinsicWidth(), ld.getIntrinsicHeight() );
        ld.getDrawable( 0 ).setColorFilter( color, PorterDuff.Mode.SRC_ATOP );
        ld.draw( new Canvas( b ) );

        Matrix matrix = new Matrix();
        matrix.postRotate( (float) bearing );
        Bitmap rotatedImage = Bitmap.createBitmap( b, 0, 0, b.getWidth(), b.getHeight(), matrix, true );

        return new MarkerOptions()
                        .position( from )
                        .icon( BitmapDescriptorFactory.fromBitmap( rotatedImage ) )
                        .anchor( anchorX, anchorY )
                        .flat( true );
    }


    public static MarkerOptions drawStop( Context context, int color, Stop stop )
    {
        float size = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 12, context.getResources().getDisplayMetrics() );
        Bitmap b = Bitmap.createBitmap( (int) size, (int) size, Bitmap.Config.ARGB_8888 );
        GradientDrawable gd = (GradientDrawable) context.getResources().getDrawable( R.drawable.stop_marker );
        gd.setBounds( 0, 0, (int) size, (int) size );
        gd.setColor( color );
        gd.draw( new Canvas( b ) );

        return new MarkerOptions()
                .position( new LatLng( stop.lat, stop.lng ) )
                .icon( BitmapDescriptorFactory.fromBitmap( b ) )
                .flat( true )
                .title( stop.name )
                .snippet( "Lorem Ipsum" );
    }
    public static Bitmap updateStopIcon(Route r,Context context){
        float size = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 12, context.getResources().getDisplayMetrics() );
        Bitmap b = Bitmap.createBitmap( (int) size, (int) size, Bitmap.Config.ARGB_8888 );
        GradientDrawable gd = (GradientDrawable) context.getResources().getDrawable( R.drawable.stop_marker );
        gd.setBounds( 0, 0, (int) size, (int) size );
        gd.setColor(Utils.convertColorStringToHex(r.color));
        gd.draw( new Canvas( b ) );
        return b;
    }
    public static Bitmap drawFavStop(Context context){

        float size = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 26, context.getResources().getDisplayMetrics() );
        Bitmap mDotMarkerBitmap = Bitmap.createBitmap( (int) size, (int) size, Bitmap.Config.ARGB_8888 );
        Canvas canvas = new Canvas(mDotMarkerBitmap);
        Drawable shape = context.getResources().getDrawable(R.drawable.favoritestar);
        shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
        shape.draw(canvas);
        return mDotMarkerBitmap ;
    }
    public static MarkerOptions drawFavStop(Context context, Stop stop){
        float size = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 12, context.getResources().getDisplayMetrics() );
        Bitmap b = Bitmap.createBitmap( (int) size, (int) size, Bitmap.Config.ARGB_8888 );
        VectorDrawable gd = (VectorDrawable) context.getResources().getDrawable( R.drawable.favoritestar );
        gd.setBounds( 0, 0, (int) size, (int) size );
//        gd.draw( new Canvas( b ) );
        Log.d("newMarker", "Utils: "+stop.id);

        return new MarkerOptions()
                .position( new LatLng( stop.lat, stop.lng ) )
                .icon( BitmapDescriptorFactory.fromBitmap( b ) )
                .flat( true )
                .title( stop.name )
                .snippet( "Lorem Ipsum" );
    }
    public static Bitmap updateVehicleMarker(Context context,Vehicle v){
        float anchorX = 0.5f;
        float anchorY = 1f;
        Route route = EtaAppController.getInstance().getRouteForId( v.routeID );
        int routeColor,busColor;
        Bitmap b = null;
        String route_type = "bus";
        if (route!=null || route.color!=null) {
            routeColor = Utils.convertColorStringToHex(route.color);
        }
        else
            routeColor = Utils.convertColorStringToHex("#2481b7");
        try {
            route_type = route.vType;
        } catch (Exception e) {
            route_type = "bus";
        }
        busColor = Utils.convertColorStringToHex("#FFFFFF");
        if (route_type.equals("Train")) {
            LayerDrawable ld = (LayerDrawable) context.getResources().getDrawable(R.drawable.train_vehicle_marker);
            b = Bitmap.createBitmap(ld.getIntrinsicWidth(), ld.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            ld.setBounds(0, 0, ld.getIntrinsicWidth(), ld.getIntrinsicHeight());
            ld.getDrawable(0).setColorFilter(routeColor, PorterDuff.Mode.SRC_IN);
//            ld.getDrawable(1).setColorFilter(busColor, PorterDuff.Mode.SRC_IN);
            ld.draw(new Canvas(b));
        } else {
            LayerDrawable ld = (LayerDrawable) context.getResources().getDrawable(R.drawable.vehicle_marker);
            b = Bitmap.createBitmap(ld.getIntrinsicWidth(), ld.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            ld.setBounds(0, 0, ld.getIntrinsicWidth(), ld.getIntrinsicHeight());
            ld.getDrawable(0).setColorFilter(routeColor, PorterDuff.Mode.SRC_IN);
//            ld.getDrawable(1).setColorFilter(busColor, PorterDuff.Mode.SRC_IN);

            ld.draw(new Canvas(b));
        }
        return b;

    }
    public static MarkerOptions drawVehicleMarker( Context context, int color, Vehicle vehicle)
    {

        float anchorX = 0.5f;
        float anchorY = 1f;
        Route route = EtaAppController.getInstance().getRouteForId( vehicle.routeID );
        int routeColor;
        Bitmap b = null;
        String route_type = "bus";
        if (route!=null || route.color!=null) {
            routeColor = Utils.convertColorStringToHex(route.color);
        }
        else
            routeColor = Utils.convertColorStringToHex("#2481b7");
        try {
            route_type = route.vType;
        } catch (Exception e) {
            route_type = "bus";
        }

        if (route_type.equals("Train")) {
            LayerDrawable ld = (LayerDrawable) context.getResources().getDrawable(R.drawable.train_vehicle_marker);
            b = Bitmap.createBitmap(ld.getIntrinsicWidth(), ld.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            ld.setBounds(0, 0, ld.getIntrinsicWidth(), ld.getIntrinsicHeight());
            ld.getDrawable(0).setColorFilter(routeColor, PorterDuff.Mode.SRC_ATOP);
            ld.draw(new Canvas(b));
        } else {
            LayerDrawable ld = (LayerDrawable) context.getResources().getDrawable(R.drawable.vehicle_marker);
            b = Bitmap.createBitmap(ld.getIntrinsicWidth(), ld.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            ld.setBounds(0, 0, ld.getIntrinsicWidth(), ld.getIntrinsicHeight());
            ld.getDrawable(0).setColorFilter(routeColor, PorterDuff.Mode.SRC_ATOP);
            ld.draw(new Canvas(b));
        }
        return new MarkerOptions()
                .position(new LatLng(vehicle.lat, vehicle.lng))
                .icon(BitmapDescriptorFactory.fromBitmap(b))
                .anchor(anchorX, anchorY)
                .flat(false)
                .infoWindowAnchor(0.5f, 0);

    }


    private static final double degreesPerRadian = 180.0 / Math.PI;

    private static double getBearing( LatLng from, LatLng to )
    {
        double lat1 = from.latitude * Math.PI / 180.0;
        double lon1 = from.longitude * Math.PI / 180.0;
        double lat2 = to.latitude * Math.PI / 180.0;
        double lon2 = to.longitude * Math.PI / 180.0;

        // Compute the angle.
        double angle = - Math.atan2( Math.sin( lon1 - lon2 ) * Math.cos( lat2 ), Math.cos( lat1 ) * Math.sin( lat2 ) - Math.sin( lat1 ) * Math.cos( lat2 ) * Math.cos( lon1 - lon2 ) );

        if (angle < 0.0)
            angle += Math.PI * 2.0;

        // And convert result to degrees.
        angle = angle * degreesPerRadian;

        return angle;
    }


    public static double distance( LatLng latLng1, LatLng latLng2, char unit )
    {
        return distance( latLng1.latitude, latLng1.longitude, latLng2.latitude, latLng2.longitude, unit );
    }


    public static double distance( double lat1, double lon1, double lat2, double lon2, char unit )
    {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public static double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public static double rad2deg(double rad)
    {
        return (rad * 180.0 / Math.PI);
    }

}
