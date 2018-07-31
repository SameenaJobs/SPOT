package com.etatransit.activity;

import android.app.Activity;
import android.os.Bundle;

import com.etatransit.R;
import com.splunk.mint.Mint;

/**
 * Created by mark on 11/25/14.
 */
public class ErrorActivity extends Activity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Mint.leaveBreadcrumb("ErrorActivity onCreate " );
        setContentView( R.layout.activity_error );
    }
}
