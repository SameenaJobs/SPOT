package com.etatransit.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 10/6/14.
 */
public class EtaAppModel implements Parcelable
{

    private List<Agency> mAgencies = new ArrayList<Agency>();
    private Agency mSelectedAgency;
    private String mAgencyPath;
    private List<Route> mRoutes = new ArrayList<Route>();
    private List<Stop> mStops = new ArrayList<Stop>();
    private List<Route> mSelectedRoutes = new ArrayList<Route>();
    private List<AnnouncementSection> mAnnouncments = new ArrayList<AnnouncementSection>();
    private List<Vehicle> mVehicles = new ArrayList<Vehicle>();
    private List<StopEta> mStopEtas = new ArrayList<StopEta>();
    private List<OneStopEta> mOneStopEtas = new ArrayList<OneStopEta>();
    private List<StopImage> mStopImages = new ArrayList<StopImage>();
    private List<Schedule> mSchedule = new ArrayList<Schedule>();
    private LatLng mCurrentLocation = null;


    public EtaAppModel(){}


    public EtaAppModel( Parcel source )
    {
        source.readTypedList( mAgencies, Agency.CREATOR );
        mSelectedAgency = source.readParcelable( Agency.class.getClassLoader() );
        mAgencyPath = source.readString();
        source.readTypedList( mRoutes, Route.CREATOR );
        source.readTypedList( mStops, Stop.CREATOR );
        source.readTypedList( mSelectedRoutes, Route.CREATOR );
        source.readTypedList( mAnnouncments, AnnouncementSection.CREATOR );
        source.readTypedList( mVehicles, Vehicle.CREATOR );
        source.readTypedList( mStopEtas, StopEta.CREATOR );
        source.readTypedList( mStopImages, StopImage.CREATOR );
        source.readTypedList( mSchedule, Schedule.CREATOR );
        mCurrentLocation = source.readParcelable( LatLng.class.getClassLoader() );
    }


    public static final Creator<EtaAppModel> CREATOR = new Creator<EtaAppModel>() {
        @Override
        public EtaAppModel createFromParcel(Parcel in) {
            return new EtaAppModel(in);
        }

        @Override
        public EtaAppModel[] newArray(int size) {
            return new EtaAppModel[size];
        }
    };

    public List<Agency> getAgencies(){ return mAgencies; }
    public void setAgencies( List<Agency> agencies )
    {
        this.mAgencies = agencies;
    }


    public Agency getSelectedAgency(){ return mSelectedAgency; }
    public void setSelectedAgency( Agency agency )
    {
        mSelectedAgency = agency;
        mRoutes = new ArrayList<Route>();
        mStops = new ArrayList<Stop>();
        mSelectedRoutes = new ArrayList<Route>();
        mAnnouncments = new ArrayList<AnnouncementSection>();
        mVehicles = new ArrayList<Vehicle>();
        mStopEtas = new ArrayList<StopEta>();
        mStopImages = new ArrayList<StopImage>();
    }


    public String getAgencyPath(){ return mAgencyPath; };
    public void setAgencyPath( String agencyPath )
    {
        mAgencyPath = agencyPath;
    }


    public List<Route> getRoutes(){ return mRoutes; }
    public void setRoutes( List<Route> routes )
    {
        this.mRoutes = routes;
    }


    public List<Stop> getStops(){ return mStops; }
    public void setStops( List<Stop> stops )
    {
        this.mStops = stops;
    }


    public List<Route> getSelectedRoutes(){ return mSelectedRoutes; }
    public void setSelectedRoutes( List<Route> selectedRoutes )
    {
        this.mSelectedRoutes = selectedRoutes;
    }


    public List<AnnouncementSection> getAnnouncements(){ return mAnnouncments; }
    public void setAnnouncements( List<AnnouncementSection> announcements )
    {
        if( announcements == null )
            this.mAnnouncments = new ArrayList<AnnouncementSection>();
        else
            this.mAnnouncments = announcements;
    }


    public List<Vehicle> getVehicles(){ return mVehicles; }
    public void setVehicles( List<Vehicle> vehicles )
    {
        this.mVehicles = vehicles;
    }


    public List<StopEta> getStopEtas(){
//        List<StopEta> subList = new ArrayList<StopEta>(mStopEtas);
        // mStopEtas.removeAll(subList);
        Log.d("emptyInfo", "mStopEtas.size() in EtaAppModel"+mStopEtas.size());
        return mStopEtas;
    }
    public void setStopEtas( List<StopEta> stopEtas )
    {
        this.mStopEtas = stopEtas;
    }

    public List<OneStopEta> getOneStopEtas(){ return mOneStopEtas; }

    public void setOneStopEtas( List<OneStopEta> onestopEtas )
    {
        this.mOneStopEtas = onestopEtas;
    }

    public List<StopImage> getStopImages(){
        return mStopImages; }
    public void setStopImages( List<StopImage> stopImages )
    {
        this.mStopImages = stopImages;
    }

    public List<Schedule> getSchedule(){
        return mSchedule; }
    public void setSchedule( List<Schedule> schedule )
    {
        this.mSchedule = schedule;
    }

    public LatLng getCurrentLocation(){ return mCurrentLocation; }
    public void setCurrentLocation( LatLng currentLocation )
    {
        mCurrentLocation = currentLocation;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
        dest.writeTypedList( mAgencies );
        dest.writeParcelable( mSelectedAgency, flags );
        dest.writeString( mAgencyPath );
        dest.writeTypedList( mRoutes );
        dest.writeTypedList( mStops );
        dest.writeTypedList( mSelectedRoutes );
        dest.writeTypedList( mAnnouncments );
        dest.writeTypedList( mVehicles );
        dest.writeTypedList( mStopEtas );
        dest.writeTypedList( mStopImages );
        dest.writeParcelable( mCurrentLocation, flags );
    }
}
