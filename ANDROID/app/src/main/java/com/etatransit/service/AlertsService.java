package com.etatransit.service;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.etatransit.R;
import com.etatransit.activity.MainActivity;
import com.etatransit.database.DatabaseHandler;
import com.etatransit.event.BusProvider;
import com.etatransit.event.StopEtasUpdatedEvent;
import com.etatransit.http.EtaService;
import com.etatransit.model.EtaAppModel;
import com.etatransit.model.OneStopEta;
import com.etatransit.model.OneStopEtaList;
import com.splunk.mint.Mint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by innovator on 8/25/2016.
 */
public class AlertsService extends Service {



    ArrayList<Integer> stopIdArray;
    private EtaAppModel mAppModel;
    private RestAdapter mRestAdapter;
    private EtaService mEtaService;
    HashMap<String,Integer> routesList;
    int startId;
    public static final long NOTIFY_INTERVAL = 60000; // 10 seconds
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    private int randomPIN;
    private DatabaseHandler db;
    private Cursor cursor;

    ArrayList<String> mins = new ArrayList<>();
    @Override
    public void onCreate() {

        super.onCreate();

    }

    public AlertsService() {
        super();
    }


    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(),"Destroyed",Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.startId = startId;
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = new Timer();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        mTimer.scheduleAtFixedRate(new LoadDBValues(), 0, NOTIFY_INTERVAL);


        return START_STICKY;
    }

    public void loadOneStopEtas(final int stopId, final int alertID, final HashMap<String, Integer> routesList)
    {

        mEtaService.getStopEtasForStop(mAppModel.getAgencyPath(),stopId, new Callback<OneStopEtaList>() {
            @Override
            public void success(OneStopEtaList stopEtaList, Response response) {

                if (stopEtaList != null && stopEtaList.get_stop_etas != null) {
                    final List<Integer> list= new ArrayList<Integer>(routesList.values());

                    if(String.valueOf(stopEtaList.get_stop_etas.get(0).id) != null){
                        setOneStopEtas(stopEtaList.get_stop_etas);
                        mins = new ArrayList<>();
                        for(int i=0;i< stopEtaList.get_stop_etas.size();i++){

                            for(int j =0;j< stopEtaList.get_stop_etas.get(i).enRoute.size();j++){
                                for(int k = 0;k< list.size();k++){
                                    if(stopEtaList.get_stop_etas.get(i).enRoute.get(j).routeID == list.get(k)){
                                        String minutes = "NA-NA";

                                        if(stopEtaList.get_stop_etas.get(i).enRoute.get(j).direction.equals("") || stopEtaList.get_stop_etas.get(i).enRoute.get(j).direction.equals("NA")){
                                            if(stopEtaList.get_stop_etas.get(i).enRoute.get(j).equipmentID.equals("-")){
                                                minutes =  "NA"+"-"+stopEtaList.get_stop_etas.get(i).enRoute.get(j).minutes;

                                            } else{
                                                minutes =  stopEtaList.get_stop_etas.get(i).enRoute.get(j).equipmentID+"-"+stopEtaList.get_stop_etas.get(i).enRoute.get(j).minutes;

                                            }

                                        }else{
                                            if(stopEtaList.get_stop_etas.get(i).enRoute.get(j).equipmentID.equals("-")){
                                                minutes =  "NA"+":"+stopEtaList.get_stop_etas.get(i).enRoute.get(j).direction+"-"+stopEtaList.get_stop_etas.get(i).enRoute.get(j).minutes;

                                            } else{
                                                minutes =  stopEtaList.get_stop_etas.get(i).enRoute.get(j).equipmentID+":"+stopEtaList.get_stop_etas.get(i).enRoute.get(j).direction+"-"+stopEtaList.get_stop_etas.get(i).enRoute.get(j).minutes;

                                            }

                                        }
                                        mins.add(minutes);
                                    }
                                }

                            }
                        }


                    }
                    String mins_string = "NA";
                    for(int z=0;z<mins.size();z++){
                        if(mins_string.equals("NA")){
                            mins_string = mins.get(z);
                        }else{
                            mins_string = mins_string+","+mins.get(z);
                        }


                    }
                    db.UpdateETARiderAlertByID( String.valueOf(alertID),mins_string);

                } else {
                    String mins_string = "NA";
                    db.UpdateETARiderAlertByID( String.valueOf(alertID),mins_string);
                    BusProvider.getInstance().post(new StopEtasUpdatedEvent());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                setOneStopEtas(new ArrayList<OneStopEta>());
                String mins_string = "NA";
                db.UpdateETARiderAlertByID( String.valueOf(alertID),mins_string);
                if (error != null && error.getResponse() != null) {

                    Mint.leaveBreadcrumb("Error Loading Stop ETA's For Agency : Server Status : " + error.getResponse() + " : URL : " + error.getResponse().getUrl() + "errorrrr" + error);
                }
            }
        });



    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void scheduleNotification(int alertID) {
        Cursor details = db.fetchRiderAlertByID(String.valueOf(alertID));

        if(details.getCount() != 0){

            String mins_string = details.getString(cursor.getColumnIndex(DatabaseHandler.ETA_MINS));
            String stopname = details.getString(cursor.getColumnIndex(DatabaseHandler.STOP_NAME));
            String minutes = details.getString(cursor.getColumnIndex(DatabaseHandler.MINUTES));
            int mintime = Integer.parseInt(minutes.split("-")[0]);
            int maxtime = Integer.parseInt(minutes.split("-")[1]);
            String isRepeat = details.getString(cursor.getColumnIndex(DatabaseHandler.IS_REPEAT));
            String direction = details.getString(cursor.getColumnIndex(DatabaseHandler.DIRECTION));
            String is_direction = details.getString(cursor.getColumnIndex(DatabaseHandler.IS_DIRECTION));
            int occurences = Integer.parseInt(details.getString(cursor.getColumnIndex(DatabaseHandler.OCCURENCES)));

            randomPIN = (int)(Math.random()*9000)+1000;

            boolean status = false;
            List<String> items = Arrays.asList(mins_string.split("\\s*,\\s*"));

            NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this);
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setContentTitle("Rider Alerts");
            mBuilder.setContentText("New arrivals to "+stopname);
            mBuilder.setTicker("New Message Alert!");
            mBuilder.setSmallIcon(R.drawable.ic_logo_eta);
            mBuilder.setPriority(2);
            mBuilder.setSound(soundUri);

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            inboxStyle.setBigContentTitle("Rider Alerts");
            inboxStyle.setSummaryText("Stop: "+stopname);

            for(int j=0;j<items.size();j++){

                String equipmentId_direction,eta,equipmentId,direction_from_service = "NA";

                equipmentId_direction = items.get(j).split("-")[0].trim();

                if(equipmentId_direction.contains(":")){
                    equipmentId = equipmentId_direction.split(":")[0].trim();
                    direction_from_service = equipmentId_direction.split(":")[1].trim();
                }else{
                    equipmentId = equipmentId_direction;
                }


                eta = items.get(j).split("-")[1].trim();



                if(is_direction.equals("true")){
                    if(direction.equals(direction_from_service)) {
                        if (Integer.parseInt(eta) < maxtime) {
                            status = true;
                            inboxStyle.addLine("Vehicle No " + equipmentId + " will arrive in " + eta + " minutes");

                        }
                    }
                }else{
                    if(Integer.parseInt(eta) < maxtime){
                        status = true;
                        inboxStyle.addLine("Bus No "+equipmentId+" will arrive in "+eta+" minutes");

                    }
                }
            }
            if(status){
                mBuilder.setStyle(inboxStyle);
                mBuilder.setAutoCancel(true);

                Intent resultIntent = new Intent(this, MainActivity.class);
                resultIntent.putExtra("alertId",alertID);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(randomPIN, mBuilder.build());
            }


            if(isRepeat.equals("false")){
                if(status){

                    try {
                        db.UpdateStatusRiderAlertByID("expired", String.valueOf(alertID));
                    }catch (Exception e){
                        Log.e("Exception",e.getMessage());
                    }finally {

                    }


                }
            }else{
                if(status){
                    occurences = occurences +1;
                    try {

                        db.UpdateOccurencesRiderAlertByID(String.valueOf(alertID),String.valueOf(occurences));
                        db.UpdateStatusRiderAlertByID("active",String.valueOf(alertID));
                    }catch (Exception e){

                    }finally {

                    }
                }
            }
        }

        // builder.setDeleteIntent(NotificationEventReceiver.getDeleteIntent(this));

    }

    public List<OneStopEta> getOneStopEtas()
    {
        return mAppModel.getOneStopEtas();
    }

    public void setOneStopEtas( List<OneStopEta> onestopEtas )
    {
        mAppModel.setOneStopEtas( onestopEtas );
        BusProvider.getInstance().post( new StopEtasUpdatedEvent() );
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private class LoadDBValues extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // display toast

                    try {
                        db = new DatabaseHandler(getApplicationContext());
                        cursor = db.fetchAllRiderAlerts();
                        if (cursor.moveToFirst()) {
                            stopIdArray = new ArrayList<>();

                            while (cursor.isAfterLast() == false) {
                                String status = cursor.getString(cursor.getColumnIndex(DatabaseHandler.STATUS));
                                Log.e("status",status);
                                if(status.equals("active") || status.equals("started")){
                                    String stopName = cursor.getString(cursor.getColumnIndex(DatabaseHandler.STOP_NAME));
                                    Log.e("STOPNAME",stopName);
                                    int alertId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHandler.ALERT_ID)));
                                    int stopId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHandler.STOP_ID)));
                                    String startDate = cursor.getString(cursor.getColumnIndex(DatabaseHandler.START_DATE));
                                    String startTime = cursor.getString(cursor.getColumnIndex(DatabaseHandler.START_TIME));
                                    String is_repeat = cursor.getString(cursor.getColumnIndex(DatabaseHandler.IS_REPEAT));
                                    String selectedAgency = cursor.getString(cursor.getColumnIndex(DatabaseHandler.SELECTED_AGENCY));
                                    String selectedAgencyUrl = cursor.getString(cursor.getColumnIndex(DatabaseHandler.SELECTED_AGENCY_URL));
                                    int occurences = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHandler.OCCURENCES)));

                                    JSONObject selectedDays = new JSONObject(cursor.getString(cursor.getColumnIndex(DatabaseHandler.SELECTED_DAYS)));
                                    JSONArray days = selectedDays.optJSONArray("selected_days");
                                    ArrayList<String> dayslist = new ArrayList<String>();

                                    if (days != null) {
                                        for (int i=0;i<days.length();i++){
                                            dayslist.add(days.get(i).toString());
                                        }
                                    }
                                    routesList = new HashMap<>();
                                    JSONObject allroutes = new JSONObject(cursor.getString(cursor.getColumnIndex(DatabaseHandler.ROUTES)));
                                    Iterator<String> iter = allroutes.keys();
                                    while (iter.hasNext()) {
                                        String key = iter.next();
                                        try {

                                            Object value = allroutes.get(key);
                                            routesList.put(key, (Integer) value);
                                        } catch (JSONException e) {
                                            // Something went wrong!
                                        }
                                    }

                                    mAppModel = new EtaAppModel();
                                    String serviceUrl = selectedAgencyUrl;
                                    String endpointUrl = null;
                                    if (null != serviceUrl && serviceUrl.length() > 0) {
                                        int endIndex = serviceUrl.lastIndexOf("/");
                                        if (endIndex != -1) {
                                            endpointUrl = serviceUrl.substring(0, endIndex);
                                            mAppModel.setAgencyPath("service.php");
                                        }
                                    }
                                    mRestAdapter = new RestAdapter.Builder().setEndpoint(endpointUrl).build();
                                    mEtaService = mRestAdapter.create(EtaService.class);

                                    loadOneStopEtas(stopId,alertId,routesList);

                                    String day = startDate.split("/")[0].trim();
                                    String month = startDate.split("/")[1].trim();
                                    String year = startDate.split("/")[2].trim();
                                    String hourOfday = startTime.split(":")[0].trim();
                                    String minute = startTime.split(":")[1].trim();


                                    Calendar current_time= Calendar.getInstance();
                                    int day_of_week = current_time.get(Calendar.DAY_OF_WEEK);
                                    String today = null;
                                    if (day_of_week == 2) {
                                        today = "Monday";
                                    } else if (day_of_week == 3) {
                                        today = "Tuesday";
                                    } else if (day_of_week == 4) {
                                        today = "Wednesday";
                                    } else if (day_of_week == 5) {
                                        today = "Thursday";
                                    } else if (day_of_week == 6) {
                                        today = "Friday";
                                    } else if (day_of_week == 7) {
                                        today = "Saturday";
                                    } else if (day_of_week == 1) {
                                        today = "Sunday";
                                    }

                                    String minutes_array = cursor.getString(cursor.getColumnIndex(DatabaseHandler.ETA_MINS));
                                    List<String> list = new ArrayList<String>(Arrays.asList(minutes_array.split(" , ")));

                                    if(is_repeat.equals("true")){

                                        String repeat_start_time = cursor.getString(cursor.getColumnIndex(DatabaseHandler.REPEAT_START_TIME));
                                        String repeat_start_date = cursor.getString(cursor.getColumnIndex(DatabaseHandler.REPEAT_START_DATE));
                                        String repeat_type = cursor.getString(cursor.getColumnIndex(DatabaseHandler.REPEAT_TYPE));
                                        String end_type = cursor.getString(cursor.getColumnIndex(DatabaseHandler.REPEAT_END_TYPE));
                                        String repeat_end_date = cursor.getString(cursor.getColumnIndex(DatabaseHandler.REPEAT_END_DATE));
                                        int end_of_occurences = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHandler.END_AFTER_OCCURENCES)));
                                        String repeat_start_day = repeat_start_date.split("/")[0].trim();
                                        String repeat_start_month = repeat_start_date.split("/")[1].trim();
                                        String repeat_start_year = repeat_start_date.split("/")[2].trim();
                                        String repeat_start_hourOfday = repeat_start_time.split(":")[0].trim();
                                        String repeat_start_minute = repeat_start_time.split(":")[1].trim();

                                        Calendar mCalendar = Calendar.getInstance();
                                        mCalendar.set(Calendar.MONTH, Integer.parseInt(repeat_start_month));
                                        mCalendar.set(Calendar.YEAR, Integer.parseInt(repeat_start_year));
                                        mCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(repeat_start_day));
                                        mCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(repeat_start_hourOfday));
                                        mCalendar.set(Calendar.MINUTE, Integer.parseInt(repeat_start_minute));

                                        if(repeat_type.equals("Daily")){
                                            if(occurences == 0 && status.equals("active")) {
                                                int month_number = current_time.get(Calendar.MONTH)+1;
                                                if ((current_time.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(repeat_start_day) &&
                                                        current_time.get(Calendar.YEAR) == Integer.parseInt(repeat_start_year)
                                                        && month_number == Integer.parseInt(repeat_start_month)
                                                        && current_time.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(hourOfday))) {
                                                    // schedule task
                                                    int diff = Integer.parseInt(minute) - current_time.get(Calendar.MINUTE);
                                                    if (diff > -2 && diff <= 0) {

                                                        db.UpdateStatusRiderAlertByID("started", String.valueOf(alertId));
                                                        if (list.size() == 1 && list.get(0).equals("NA")) {

                                                        } else {
                                                            scheduleNotification(alertId);
                                                        }

                                                    }

                                                }
                                            }else if(occurences == 0 && status.equals("started")){
                                                if(current_time.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(hourOfday) || status.equals("started") ){

                                                    // schedule task
                                                    int diff = Integer.parseInt(minute)- current_time.get(Calendar.MINUTE);
                                                    if( diff > -2 && diff <= 0 || status.equals("started") ){

                                                        db.UpdateStatusRiderAlertByID("started", String.valueOf(alertId));
                                                        if(list.size() == 1 && list.get(0).equals("NA") ){

                                                        }else{
                                                            scheduleNotification(alertId);
                                                        }

                                                    }


                                                }
                                            }else if(occurences != 0 && status.equals("active")){
                                                if(current_time.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(hourOfday) ){

                                                    // schedule task
                                                    int diff = Integer.parseInt(minute)- current_time.get(Calendar.MINUTE);
                                                    if( diff > -2 && diff <= 0  ){

                                                        db.UpdateStatusRiderAlertByID("started", String.valueOf(alertId));
                                                        if(list.size() == 1 && list.get(0).equals("NA") ){

                                                        }else{
                                                            scheduleNotification(alertId);
                                                        }

                                                    }


                                                }
                                            }else if(occurences != 0 && status.equals("started")){
                                                if(current_time.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(hourOfday) || status.equals("started") ){

                                                    // schedule task
                                                    int diff = Integer.parseInt(minute)- current_time.get(Calendar.MINUTE);
                                                    if( diff >= -2 && diff <= 0 || status.equals("started") ){

                                                        db.UpdateStatusRiderAlertByID("started", String.valueOf(alertId));
                                                        if(list.size() == 1 && list.get(0).equals("NA") ){

                                                        }else{
                                                            scheduleNotification(alertId);
                                                        }

                                                    }


                                                }
                                            }

                                        }else if(repeat_type.equals("Weekly")){
                                            if(dayslist.contains(today)){
                                                if(occurences == 0 && status.equals("active")) {
                                                    int month_number = current_time.get(Calendar.MONTH)+1;
                                                    if ((current_time.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(repeat_start_day) &&
                                                            current_time.get(Calendar.YEAR) == Integer.parseInt(repeat_start_year)
                                                            && month_number == Integer.parseInt(repeat_start_month)
                                                            && current_time.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(hourOfday))) {
                                                        // schedule task
                                                        int diff = Integer.parseInt(minute) - current_time.get(Calendar.MINUTE);
                                                        if (diff > -4 && diff <= 0) {

                                                            db.UpdateStatusRiderAlertByID("started", String.valueOf(alertId));
                                                            if (list.size() == 1 && list.get(0).equals("NA")) {

                                                            } else {
                                                                scheduleNotification(alertId);
                                                            }

                                                        }

                                                    }
                                                }else if(occurences == 0 && status.equals("started")){
                                                    if(current_time.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(hourOfday) || status.equals("started") ){

                                                        // schedule task
                                                        int diff = Integer.parseInt(minute)- current_time.get(Calendar.MINUTE);
                                                        if( diff > -2 && diff <= 0 || status.equals("started") ){

                                                            db.UpdateStatusRiderAlertByID("started", String.valueOf(alertId));
                                                            if(list.size() == 1 && list.get(0).equals("NA") ){

                                                            }else{
                                                                scheduleNotification(alertId);
                                                            }

                                                        }


                                                    }
                                                }else if(occurences != 0 && status.equals("active")){
                                                    if(current_time.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(hourOfday) ){

                                                        // schedule task
                                                        int diff = Integer.parseInt(minute)- current_time.get(Calendar.MINUTE);
                                                        if( diff > -2 && diff <= 0  ){

                                                            db.UpdateStatusRiderAlertByID("started", String.valueOf(alertId));
                                                            if(list.size() == 1 && list.get(0).equals("NA") ){

                                                            }else{
                                                                scheduleNotification(alertId);
                                                            }

                                                        }


                                                    }
                                                }else if(occurences != 0 && status.equals("started")){
                                                    if(current_time.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(hourOfday) || status.equals("started") ){

                                                        // schedule task
                                                        int diff = Integer.parseInt(minute)- current_time.get(Calendar.MINUTE);
                                                        if( diff > -2 && diff <= 0 || status.equals("started") ){

                                                            db.UpdateStatusRiderAlertByID("started", String.valueOf(alertId));
                                                            if(list.size() == 1 && list.get(0).equals("NA") ){

                                                            }else{
                                                                scheduleNotification(alertId);
                                                            }

                                                        }


                                                    }
                                                }
                                            }

                                        }

                                        if(end_type.equals("on")){
                                            String repeat_end_day = repeat_end_date.split("/")[0].trim();
                                            String repeat_end_month = repeat_end_date.split("/")[1].trim();
                                            String repeat_end_year = repeat_end_date.split("/")[2].trim();
                                            Calendar currentTime= Calendar.getInstance();
                                            int month_number = current_time.get(Calendar.MONTH)+1;
                                            if((current_time.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(repeat_end_day) &&
                                                    current_time.get(Calendar.YEAR) == Integer.parseInt(repeat_end_year)
                                                    && month_number == Integer.parseInt(repeat_end_month))){

                                                db.UpdateStatusRiderAlertByID("expired",String.valueOf(alertId));
                                            }
                                        }else if(end_type.equals("after")){
                                            if(end_of_occurences == occurences){

                                                db.UpdateStatusRiderAlertByID("expired",String.valueOf(alertId));
                                            }
                                        }


                                    }else{
                                        Calendar mCalendar = Calendar.getInstance();
                                        mCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourOfday));
                                        mCalendar.set(Calendar.MINUTE, Integer.parseInt(minute));
                                        if(current_time.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(hourOfday) || status == "started"){

                                            // schedule task
                                            int diff = Integer.parseInt(minute)- current_time.get(Calendar.MINUTE);
                                            if( (diff > -2 && diff <= 0) || status.equals("started") ){

                                                db.UpdateStatusRiderAlertByID("started", String.valueOf(alertId));
                                                if(list.size() == 1 && list.get(0).equals("NA") ){

                                                }else{
                                                    scheduleNotification(alertId);
                                                }

                                            }


                                        }
                                    }

                                    stopIdArray.add(stopId);
                                }



                                cursor.moveToNext();
                            }
                        }


                    } catch (Exception e) {
                        Log.e("Catch  in service",e.getMessage());

                    } finally {

                    }
                }
            });
        }
    }

}
