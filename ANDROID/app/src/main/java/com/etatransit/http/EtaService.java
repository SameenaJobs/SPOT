package com.etatransit.http;

import com.etatransit.model.AnnouncementList;
import com.etatransit.model.OneStopEtaList;
import com.etatransit.model.RouteList;
import com.etatransit.model.ScheduleList;
import com.etatransit.model.StopEtaList;
import com.etatransit.model.StopImageList;
import com.etatransit.model.StopList;
import com.etatransit.model.VehicleList;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by mark on 10/5/14.
 */
public interface EtaService
{


    @GET( "/{path}?service=get_routes&token=8873b08bf45e6a38cd5fafce218f95bd32b5a003&includeExt=1" )
    void getAllRoutes( @Path("path") String path, Callback<RouteList> callback );

    @GET( "/{path}?service=get_stops&token=8873b08bf45e6a38cd5fafce218f95bd32b5a003" )
    void getAllStops( @Path("path") String path, Callback<StopList> callback );

    @GET( "/{path}?service=get_stops&token=8873b08bf45e6a38cd5fafce218f95bd32b5a003" )
    void getStopsForRoute( @Path("path") String path, @Query( "routeID" ) int routeID, Callback<StopList> callback );

    @GET( "/{path}?service=get_stop_etas&statusData=1&token=8873b08bf45e6a38cd5fafce218f95bd32b5a003" )
    void getStopEtas( @Path("path") String path, Callback<StopEtaList> callback );

    @GET( "/{path}?service=get_stop_etas&token=8873b08bf45e6a38cd5fafce218f95bd32b5a003" )
    void getStopEtasForRoute( @Path("path") String path, @Query( "routeID" ) int routeID, Callback<StopEtaList> callback );

    @GET( "/{path}?service=get_stop_etas&statusData=1&token=8873b08bf45e6a38cd5fafce218f95bd32b5a003" )
    void getStopEtasForStop( @Path("path") String path, @Query( "stopID" ) int stopID, Callback<OneStopEtaList> callback );

    @Headers("Cache-Control: max-age=100")
    @GET( "/{path}?service=get_vehiclesStat&includeETAData=1&orderedETAArray=1&includeStatusData=1&token=8873b08bf45e6a38cd5fafce218f95bd32b5a003" )
    void getVehicles( @Path("path") String path, Callback<VehicleList> callback );

    @GET( "/{path}?service=get_service_announcements&token=8873b08bf45e6a38cd5fafce218f95bd32b5a003" )
    void getAnnouncements( @Path("path") String path, Callback<AnnouncementList> callback );

    @GET( "/{path}?service=get_stopimages&token=8873b08bf45e6a38cd5fafce218f95bd32b5a003" )
    void getStopImages( @Path("path") String path, Callback<StopImageList> callback );

    @GET( "/{path}?service=get_schedules&token=8873b08bf45e6a38cd5fafce218f95bd32b5a003" )
    void get_schedules( @Path("path") String path,@Query( "stopID" ) int stopID, Callback<ScheduleList> callback );

}
