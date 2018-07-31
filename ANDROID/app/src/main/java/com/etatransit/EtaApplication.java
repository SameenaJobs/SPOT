package com.etatransit;

import android.app.Application;
import android.util.Log;

/**
 * Created by mark on 10/5/14.
 */
public class EtaApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        EtaAppController controller = EtaAppController.getInstance();
    }
}
