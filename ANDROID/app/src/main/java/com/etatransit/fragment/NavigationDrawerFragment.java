package com.etatransit.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.etatransit.R;
import com.etatransit.adapter.NavigationAdapter;
import com.etatransit.event.BusProvider;
import com.etatransit.event.SectionSelectedEvent;

/**
 * Created by mark on 9/29/14.
 */
public class NavigationDrawerFragment extends Fragment implements AdapterView.OnItemClickListener
{

    private static final String TAG = NavigationDrawerFragment.class.getSimpleName();


    private ListView mListView;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.fragment_navigation, container, false );
    }


    @Override
    public void onViewCreated( View view, Bundle savedInstanceState )
    {
        super.onViewCreated( view, savedInstanceState );
        mListView = (ListView) view.findViewById( android.R.id.list );
        mListView.setOnItemClickListener( this );
    }


    @Override
    public void onActivityCreated( Bundle savedInstanceState )
    {
        super.onActivityCreated( savedInstanceState );
        mListView.setAdapter( new NavigationAdapter() );
    }

    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
    {
        BusProvider.getInstance().post( new SectionSelectedEvent( position ));
    }
}
