package com.etatransit.http;

import com.etatransit.model.AgencyList;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by mark on 11/25/14.
 */
public interface EtaAgencyService
{

    String SERVICE_ENDPOINT = "http://api.etaspot.com/etaphi";


    @GET( "/GetAgencies.php?token=8873b08bf45e6a38cd5fafce218f95bd32b5a003" )
    void getAllAgencies( Callback<AgencyList> callback );

}
