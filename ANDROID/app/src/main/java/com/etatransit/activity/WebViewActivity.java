package com.etatransit.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.etatransit.R;

/**
 * Created by mark on 1/11/15.
 */
public class WebViewActivity extends Activity
{
    public static final String EXTRA_URL = "extra_url";

    private WebView mWebView;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_webview );

        getActionBar().setHomeButtonEnabled( true );
        getActionBar().setDisplayHomeAsUpEnabled( true );

        mWebView = (WebView) findViewById( R.id.webview );
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled( true );
        mWebView.setWebViewClient( new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading( WebView view, String url )
            {
                if( url.startsWith( "mailto:" ) )
                {
                    MailTo mt = MailTo.parse(url);
                    Intent intent = getEmailIntent( mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc() );
                    startActivity( intent );
                    return true;
                }else if( url.startsWith( "http" ) )
                {
                    view.loadUrl( url );
                    return true;
                }else
                {
                    Intent intent = new Intent( Intent.ACTION_VIEW );
                    intent.setData( Uri.parse( url ) );
                    startActivity( intent );
                    return true;
                }
            }
        });
        mWebView.loadUrl( getIntent().getStringExtra( EXTRA_URL ) );
    }


    public static Intent getEmailIntent( String address, String subject, String body, String cc )
    {
        Intent intent = new Intent( Intent.ACTION_SEND );
        intent.putExtra( Intent.EXTRA_EMAIL, new String[] { address } );
        intent.putExtra( Intent.EXTRA_TEXT, body );
        intent.putExtra( Intent.EXTRA_SUBJECT, subject );
        intent.putExtra( Intent.EXTRA_CC, cc );
        intent.setType( "message/rfc822" );
        return intent;
    }


    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event )
    {
        // Check if the key event was the Back button and if there's history
        if( ( keyCode == KeyEvent.KEYCODE_BACK ) && mWebView.canGoBack() )
        {
            mWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown( keyCode, event );
    }


    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        finish();
        return true;
    }
}
