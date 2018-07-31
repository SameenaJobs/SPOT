package com.etatransit.receiver;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.etatransit.activity.MainActivity;

import java.util.List;

/**
 * Created by innovate on 30/3/17.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private android.widget.Toast Toast;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(final Context context, final Intent intent) {
        try {
            boolean isVisible = MyApplication.isActivityVisible();
            Context appContext = context.getApplicationContext();
            if (isVisible == true) {
                if (checkInternet(context)) {

                    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
                        Intent i = new Intent(context, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(i);
                        Toast.makeText(context, "Reconnected.", Toast.LENGTH_LONG).show();
                    } else {

                        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();

                            Intent i = new Intent(context, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(i);
                            Toast.makeText(context, "Reconnected.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

            boolean checkInternet (Context context){
                ServiceManager serviceManager = new ServiceManager(context);
                if (serviceManager.isNetworkAvailable()) {
                    return true;
                } else {
                    Toast.makeText(context,"No Network", Toast.LENGTH_LONG) .show();

                    return false;
                }
            }

            }
