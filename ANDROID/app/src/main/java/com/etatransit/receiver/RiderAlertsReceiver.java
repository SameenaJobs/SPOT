package com.etatransit.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.etatransit.activity.CreateRiderAlert;
import com.etatransit.database.DatabaseHandler;
import com.etatransit.model.RiderAlert;
import com.etatransit.service.AlertsService;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by innovator on 8/29/2016.
 */
public class RiderAlertsReceiver extends WakefulBroadcastReceiver {

    private int alertId;
    HashMap<String,Integer> routesList;
    AlarmManager mAlarmManager;
    PendingIntent mPendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras == null) {
            Log.d("Service","null");
        } else {

            alertId= extras.getInt("alertId");
        }

        Log.e("alertID",alertId+"");
        Log.e("In receiver","receiver");

       Intent intent1 = new Intent(context,AlertsService.class);
        intent1.putExtra("alertId", alertId);
        context.startService(intent1);


    }

    public void setAlarm(Context context, Calendar calendar, int ID) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, RiderAlertsReceiver.class);
        intent.putExtra("alertId", ID);
        mPendingIntent = PendingIntent.getBroadcast(context, ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);


        // Calculate notification time
        Calendar c = Calendar.getInstance();
        long currentTime = c.getTimeInMillis();
        long diffTime = calendar.getTimeInMillis() - currentTime;

        // Start alarm using notification time
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + diffTime,
                mPendingIntent);

        // Restart alarm if device is rebooted
//        ComponentName receiver = new ComponentName(context, BootReceiver.class);
//        PackageManager pm = context.getPackageManager();
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP);
    }

    public void setRepeatAlarm(Context context, Calendar calendar, int ID, long RepeatTime) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.e("asetRepeat",ID+"");
        Intent intent = new Intent(context, RiderAlertsReceiver.class);
        intent.putExtra("alertId", ID);
        mPendingIntent = PendingIntent.getBroadcast(context, ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Calculate notification timein
        Calendar c = Calendar.getInstance();
        long currentTime = c.getTimeInMillis();
        long diffTime = calendar.getTimeInMillis() - currentTime;

        // Start alarm using initial notification time and repeat interval time
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + diffTime,
                RepeatTime , mPendingIntent);

        // Restart alarm if device is rebooted
//        ComponentName receiver = new ComponentName(context, BootReceiver.class);
//        PackageManager pm = context.getPackageManager();
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context, int ID) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        Toast.makeText(context,"Cancelled"+ID,Toast.LENGTH_LONG).show();
        // Cancel Alarm using Reminder ID
        mPendingIntent = PendingIntent.getBroadcast(context, ID, new Intent(context, RiderAlertsReceiver.class), 0);
        mAlarmManager.cancel(mPendingIntent);

        // Disable alarm
//        ComponentName receiver = new ComponentName(context, BootReceiver.class);
//        PackageManager pm = context.getPackageManager();
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                PackageManager.DONT_KILL_APP);
    }
}
