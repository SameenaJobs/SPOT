package com.etatransit.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.etatransit.EtaAppController;
import com.etatransit.R;
import com.etatransit.adapter.MessageAdapter;
import com.etatransit.event.AgencySelectedEvent;
import com.etatransit.event.BusProvider;
import com.etatransit.event.MessagesUpdatedEvent;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

/**
 * Created by mark on 10/13/14.
 */
public class MessagesFragment extends Fragment
{

    public static MessagesFragment newInstance()
    {
        return new MessagesFragment();
    }


    private ListView mListView;
    private MessageAdapter mAdapter;

    private TextView mEmptyText;

    private FrameLayout mListContainer;
    private FrameLayout mProgressContainer;

    private boolean mListShown = true;


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.fragment_messages, container, false );
    }


    @Override
    public void onViewCreated( View view, Bundle savedInstanceState )
    {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById( android.R.id.list );
        mAdapter = new MessageAdapter();
        mListView.setAdapter( mAdapter );

        mEmptyText = (TextView) view.findViewById( R.id.empty_text );

        mListContainer = (FrameLayout) view.findViewById( R.id.list_container );
        mProgressContainer = (FrameLayout) view.findViewById( R.id.progress_container );
        setListShown( false, false );
    }


    @Override
    public void onResume()
    {
        super.onResume();
        BusProvider.getInstance().register( this );
        loadMessages();
    }


    @Override
    public void onPause()
    {
        super.onPause();
        BusProvider.getInstance().unregister( this );
    }


    @Subscribe
    public void onMessagesUpdated( MessagesUpdatedEvent event )
    {
        if( !isAdded() )
            return;

        mAdapter.setData( EtaAppController.getInstance().getAnnouncements() );
        setNoDataAvailable( ( mAdapter.getCount() <= 0 ) );
        setListShown( true, true );
    }


    private void setListShown( boolean shown, boolean animate )
    {
        Mint.leaveBreadcrumb("MessagesFragment setListShown animate" );
        if( mListShown == shown )
            return;

        mListShown = shown;

        if( shown )
        {
            if( animate )
            {
                mProgressContainer.startAnimation( AnimationUtils.loadAnimation( getActivity(), android.R.anim.fade_out ) );
                mListContainer.startAnimation( AnimationUtils.loadAnimation( getActivity(), android.R.anim.fade_in ) );
            }else
            {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility( View.GONE );
            mListContainer.setVisibility( View.VISIBLE );
        }else
        {
            if( animate )
            {
                mProgressContainer.startAnimation( AnimationUtils.loadAnimation( getActivity(), android.R.anim.fade_in ) );
                mListContainer.startAnimation( AnimationUtils.loadAnimation( getActivity(), android.R.anim.fade_out ) );
            }else
            {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility( View.VISIBLE );
            mListContainer.setVisibility( View.GONE );
        }
    }


    public void setNoDataAvailable( boolean noDataAvailable )
    {
        if( noDataAvailable )
        {
            mListView.setVisibility( View.GONE );
            mEmptyText.setVisibility( View.VISIBLE );
        }else
        {
            mListView.setVisibility( View.VISIBLE );
            mEmptyText.setVisibility( View.GONE );
        }
    }


    @Subscribe
    public void onAgencySelectedEvent( AgencySelectedEvent event )
    {
        loadMessages();
    }


    public void loadMessages()
    {
        EtaAppController.getInstance().loadAnnouncements();
    }
}
