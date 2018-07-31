package com.etatransit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.etatransit.R;
import com.splunk.mint.Mint;

/**
 * Created by mark on 9/29/14.
 */
public class SplashActivity extends BaseActivity
{
    private static final String EXTRA_TIME_PASSED = "extra_time_paused";

    private static final int SPLASH_TIMER_MAX = 3; //in seconds;
    private static final int ONE_SECOND = 1000;


    private int mSplashTimePassed = 0;
    private Handler mHandler = new Handler();


    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        com.nullwire.trace.ExceptionHandler.register(this,"http://etaqa2.etaspot.com/");
        if(getResources().getBoolean(R.bool.mint_debugging_enabled)) {
            Mint.enableDebug();
        }
        if(getResources().getBoolean(R.bool.mint_logging_enabled)) {
            Mint.enableLogging(true);
        }
        Mint.initAndStartSession( SplashActivity.this, getString(R.string.mint_key) );
        setContentView(R.layout.activity_splash);

        if( savedInstanceState != null && savedInstanceState.containsKey( EXTRA_TIME_PASSED ) )
        {
            mSplashTimePassed = savedInstanceState.getInt( EXTRA_TIME_PASSED );
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        Mint.startSession( SplashActivity.this );
        validateTimer();
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        mHandler.removeCallbacks( mTimer );
    }

    @Override
    protected void onStop() {
        super.onStop();
        Mint.closeSession( SplashActivity.this );
        Mint.flush();
    }

    @Override
    protected void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState( outState );
        outState.putInt( EXTRA_TIME_PASSED, mSplashTimePassed );
    }


    private void validateTimer()
    {
        if( mSplashTimePassed >= SPLASH_TIMER_MAX )
        {
            startActivity( new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }else
        {
            mHandler.postDelayed( mTimer, ONE_SECOND );
        }
    }


    private Runnable mTimer = new Runnable()
    {
        @Override
        public void run()
        {
            mSplashTimePassed++;
            validateTimer();
        }
    };
}
