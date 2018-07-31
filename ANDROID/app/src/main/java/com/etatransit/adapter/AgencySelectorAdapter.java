package com.etatransit.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etatransit.R;
import com.etatransit.activity.MainActivity;
import com.etatransit.model.Agency;
import com.etatransit.util.Utils;
import com.splunk.mint.Mint;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mark on 9/29/14.
 */
public class AgencySelectorAdapter extends BaseAdapter
{

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_AGENCY = 1;
    private static final int VIEW_TYPE_COUNT = 2;


    private List<Agency> mLocalAgencies = new ArrayList<Agency>();
    private List<Agency> mAllAgencies = new ArrayList<Agency>();

    Context context;
    public AgencySelectorAdapter(Context context){
        this.context= context;
    }

    @Override
    public int getCount()
    {

        int count = 2; //headers
        if( mLocalAgencies != null && !mLocalAgencies.isEmpty() )
        {
            count += mLocalAgencies.size();
        }else
        {
            count++; //add an item for no local agencies
        }

        if( mAllAgencies != null && !mAllAgencies.isEmpty() )
        {
            count += mAllAgencies.size();
        }else
        {
            count++; //add an item for no agencies found.
        }

        return count;
    }


    @Override
    public Object getItem( int position )
    {
        int localAgencyHeader = 0;
        int localAgencyStart = localAgencyHeader + 1;
        int localAgencyEnd = localAgencyStart + ( ( mLocalAgencies != null && !mLocalAgencies.isEmpty() ) ? mLocalAgencies.size() - 1 : 0 );
        int allAgencyHeader = localAgencyEnd + 1;
        int allAgencyStart = allAgencyHeader + 1;
        int allAgencyEnd = allAgencyStart + ( ( mAllAgencies != null && !mAllAgencies.isEmpty() ) ? mAllAgencies.size() - 1 : 0 );

        if( position == localAgencyHeader || position == allAgencyHeader )
            return null;

        if( position > allAgencyHeader )
        {
            int pos = position - allAgencyStart;

        }
        if( isLocalAgency( position ) )
        {
            int pos = position - localAgencyStart;
            return ( mLocalAgencies != null && !mLocalAgencies.isEmpty() && pos < mLocalAgencies.size() ) ? mLocalAgencies.get( pos ) : null;
        }else if( isAllAgency( position ) )
        {
            int pos = position - allAgencyStart;
            return ( mAllAgencies != null && !mAllAgencies.isEmpty() && pos < mAllAgencies.size() ) ? mAllAgencies.get( pos ) : null ;
        }

        return null;
    }


    @Override
    public int getItemViewType( int position )
    {
        int localAgencyHeader = 0;
        int localAgencyStart = localAgencyHeader + 1;
        int localAgencyEnd = localAgencyStart + ( ( mLocalAgencies != null && !mLocalAgencies.isEmpty() ) ? mLocalAgencies.size() - 1 : 0 );
        int allAgencyHeader = localAgencyEnd + 1;
        int allAgencyStart = allAgencyHeader + 1;
        int allAgencyEnd = allAgencyStart + ( ( mAllAgencies != null && !mAllAgencies.isEmpty() ) ? mAllAgencies.size() - 1 : 0 );


        if( position == localAgencyHeader || position == allAgencyHeader )
        {
            return VIEW_TYPE_HEADER;
        }else
        {
            return VIEW_TYPE_AGENCY;
        }
    }


    private boolean isLocalAgency( int position )
    {

        int localAgencyHeader = 0;
        int localAgencyStart = localAgencyHeader + 1;
        int localAgencyEnd = localAgencyStart + ( ( mLocalAgencies != null && !mLocalAgencies.isEmpty() ) ? mLocalAgencies.size() - 1 : 0 );
        int allAgencyHeader = localAgencyEnd + 1;
        int allAgencyStart = allAgencyHeader + 1;
        int allAgencyEnd = allAgencyStart + ( ( mAllAgencies != null && !mAllAgencies.isEmpty() ) ? mAllAgencies.size() - 1 : 0 );

        return ( position > localAgencyHeader && position < allAgencyHeader );
    }


    private boolean isAllAgency( int position )
    {
        int localAgencyHeader = 0;
        int localAgencyStart = localAgencyHeader + 1;
        int localAgencyEnd = localAgencyStart + ( ( mLocalAgencies != null && !mLocalAgencies.isEmpty() ) ? mLocalAgencies.size() - 1 : 0 );
        int allAgencyHeader = localAgencyEnd + 1;
        int allAgencyStart = allAgencyHeader + 1;
        int allAgencyEnd = allAgencyStart + ( ( mAllAgencies != null && !mAllAgencies.isEmpty() ) ? mAllAgencies.size() - 1 : 0 );

        return ( position > allAgencyHeader && position <= allAgencyEnd );
    }


    @Override
    public int getViewTypeCount()
    {
        return VIEW_TYPE_COUNT;
    }


    @Override
    public long getItemId( int position )
    {
        return 0;
    }


    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        int itemType = getItemViewType( position );
        if( convertView == null )
        {
            if( itemType == VIEW_TYPE_HEADER )
            {
                convertView = LayoutInflater.from( parent.getContext() ).inflate( R.layout.view_agency_selector_header, parent, false );
            }else
            {
                convertView = LayoutInflater.from( parent.getContext() ).inflate( R.layout.view_agency_selector_agency, parent, false );
            }
        }

        if( itemType == VIEW_TYPE_HEADER )
        {
            TextView tv = (TextView) convertView.findViewById( R.id.label );
            if( position == 0 )
            {
                tv.setText( parent.getContext().getString( R.string.text_near_your_location ) );
            }else
            {
                tv.setText( parent.getContext().getString( R.string.text_all_agencies ) );
            }
        }else if( itemType == VIEW_TYPE_AGENCY )
        {
            Agency a = (Agency) getItem( position );
            ImageView iv = (ImageView) convertView.findViewById( R.id.image_view );
            TextView tv = (TextView) convertView.findViewById( R.id.label );
            if(haveNetworkConnection()) {

                if (a != null) {
                    tv.setText("Loading..");
                    Picasso.with(iv.getContext()).load(a.logoUrl).into(iv);
                    iv.setBackgroundColor(Utils.convertColorStringToHex(a.bgColor));
                    iv.setVisibility(View.VISIBLE);
                    tv.setText(a.name);
                } else {
                    iv.setImageDrawable(null);
                    iv.setVisibility(View.GONE);
                    tv.setText("No Agencies!");
                }
            }
            else{
                tv.setText("Not connected to internet!");
                Intent i = new Intent(context,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            }
        }

        return convertView;
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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



    public void addAllAgencies( List<Agency> agencyList )
    {

        mAllAgencies = new ArrayList<Agency>();
        for( Agency a : agencyList )
        {
            mAllAgencies.add( a );
        }
        notifyDataSetChanged();
    }


    public void addLocalAgencies( List<Agency> agencyList )
    {

        mLocalAgencies = new ArrayList<Agency>();

        for( Agency a : agencyList )
        {
            mLocalAgencies.add( a );
        }
        notifyDataSetChanged();
    }
}
