package com.etatransit.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.etatransit.R;
import com.etatransit.database.DatabaseHandler;
import com.etatransit.model.RiderAlert;
import com.guna.libmultispinner.MultiSelectionSpinner;
import com.splunk.mint.Mint;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by innovator on 8/5/2016.
 */
public class CreateRiderAlert extends Activity implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener,MultiSelectionSpinner.OnMultipleItemsSelectedListener
{


    EditText edit_alertDate,edit_alertTime;
    TextView repeatSettings;
    Dialog repeatDialog;
    Button create_alert;
    String hourString,minuteString,secondString,selected_mins;
    public String source,stopName,selectedAgency,selectedAgencyUrl,alertId;
    int stopId;
    CheckBox check_use_stopName,check_repeat;
    ImageView alarmImage;
    EditText edit_alert_name;
    Calendar now;
    HashMap<String,Integer> routesList;
    List<String> routeIdList = new ArrayList<>();
    HashMap<String,Integer> selectedroutesList = new HashMap<>();
    DatabaseHandler spotDB;
    Spinner minutes;
    String is_repeat="false",is_stopName="false";
    MultiSelectionSpinner simpleSpinner;
    List<String> db_selectedArray_routes = new ArrayList<>();
    RiderAlert riderAlert;
    public long p_id = 0;



    private LinearLayout layout_repeatevery,layout_repeaton1,layout_repeaton2,directionLayout;
    Spinner spinner_repeat_type,spinner_repeat_weekNo,spinner_endAfter_occurence,spinner_direction;
    EditText edit_repeat_startDate,edit_repeat_endDate,edit_repeat_startTime;
    RadioGroup ends_radio_group;
    RadioButton radio_never,radio_after,radio_on;
    ArrayList<String> selectedDays = new ArrayList<>();
    String end_type="NA",repeat_type,repeat_startDate="NA",repeat_endDate,occurences,is_direction="false",direction="NA";
    ImageButton tick,cross;
    CheckBox mon,tue,wed,thur,fri,sat,sun;


    @Override
    protected void onPause() {
        super.onPause();
        repeatDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //com.nullwire.trace.ExceptionHandler.register(this,"http://etaqa2.etaspot.com/");
        setContentView(R.layout.create_rider_alert);
        repeatDialog = new Dialog(CreateRiderAlert.this);
        repeatDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        spotDB = new DatabaseHandler(getApplicationContext());
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        edit_alertTime = (EditText)findViewById(R.id.alert_time);
        minutes = (Spinner)findViewById(R.id.minutes);
        spinner_direction = (Spinner)findViewById(R.id.spinner_direction);
        edit_alert_name = (EditText)findViewById(R.id.alert_name);
        alarmImage = (ImageView)findViewById(R.id.alarmImage);
        repeatSettings = (TextView)findViewById(R.id.repeat_settings);
        check_use_stopName = (CheckBox)findViewById(R.id.user_alert_name);
        check_repeat = (CheckBox)findViewById(R.id.is_repeat);
        simpleSpinner = (MultiSelectionSpinner) findViewById(R.id.simpleMultiSpinner);
        edit_alertDate = (EditText)findViewById(R.id.alert_date);
        ImageView createrider_close_btn = (ImageView)findViewById(R.id.createrider_close_btn);
        directionLayout = (LinearLayout)findViewById(R.id.directionLayout);
        createrider_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Intent intent = getIntent();
        if(intent.hasExtra("stopName")){
            stopName = intent.getStringExtra("stopName");
        }if(intent.hasExtra("stopId")){
            stopId = intent.getIntExtra("stopId",0);
        }if(intent.hasExtra("selectedAgency")){
            selectedAgency = intent.getStringExtra("selectedAgency");
        }if(intent.hasExtra("selectedAgencyUrl")){
            selectedAgencyUrl = intent.getStringExtra("selectedAgencyUrl");
        }if(intent.hasExtra("direction")){
            is_direction = intent.getStringExtra("direction");

        }
        if(intent.hasExtra("routesList")){
            routesList = (HashMap<String, Integer>) intent.getSerializableExtra("routesList");

        }if(intent.hasExtra("source")){
            source = intent.getStringExtra("source");
            if(source.equals("edit")){
                alertId = intent.getStringExtra("alertId");

                try {
                    openAndQueryDatabase();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        if(is_direction.equals("true")){
            directionLayout.setVisibility(View.VISIBLE);
        }

        now = Calendar.getInstance();

        final List<String> list= new ArrayList<String>(routesList.keySet());

        minutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                selected_mins = minutes.getSelectedItem().toString();
                Mint.leaveBreadcrumb("CreateRiderAlert selected_mins ");

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });


        spinner_direction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                direction = spinner_direction.getSelectedItem().toString();
                Mint.leaveBreadcrumb("CreateRiderAlert direction ");
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });


        check_use_stopName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(check_use_stopName.isChecked()){
                    edit_alert_name.setText(stopName);
                    is_stopName= "true";
                    Mint.leaveBreadcrumb("CreateRiderAlert is_stopName ");
                }else{

                    is_stopName ="false";
                }

            }
        });


        check_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(check_repeat.isChecked()){
                    alarmImage.setVisibility(View.VISIBLE);
                    repeatSettings.setVisibility(View.VISIBLE);
                    is_repeat = "true";
                    Mint.leaveBreadcrumb("CreateRiderAlert check_repeat " );

                }else{
                    alarmImage.setVisibility(View.INVISIBLE);
                    repeatSettings.setVisibility(View.INVISIBLE);
                    is_repeat ="false";
                }

            }

        });

        TreeMap<String, Boolean> items = new TreeMap<>();
        for(String item : list) {
            items.put(item, Boolean.FALSE);
        }

        /**
         * Simple MultiSelection Spinner (Without Search/Filter Functionlity)
         *
         *  Using MultiSpinner class
         */


        simpleSpinner.setItems(list);
        if(source.equals("edit")){

            HashMap<String,Integer> db_list = new HashMap<>();
            Iterator<String> routesiter = riderAlert.getRoutes().keys();
            while (routesiter.hasNext()) {
                String key = routesiter.next();
                try {

                    Object value =  riderAlert.getRoutes().get(key);
                    db_list.put(key, (Integer) value);
                    db_selectedArray_routes.add(key);


                } catch (JSONException e) {
                    e.printStackTrace();
                    // Something went wrong!
                }
            }

            simpleSpinner.setSelection(db_selectedArray_routes);
        }else{
            simpleSpinner.setSelection(new int[]{0});
        }

        simpleSpinner.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        });


        create_alert = (Button)findViewById(R.id.create_alert);
        create_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(edit_alert_name.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"Please fill all the details", Toast.LENGTH_LONG).show();

                }else if(is_direction.equals("true") && spinner_direction.getSelectedItem().equals("Select Direction")){
                    Toast.makeText(getApplicationContext(),"Please Select Direction", Toast.LENGTH_LONG).show();
                }else{

                    routeIdList = simpleSpinner.getSelectedStrings();

                    for (int i=0;i<routeIdList.size();i++){
                        selectedroutesList.put(routeIdList.get(i),routesList.get(routeIdList.get(i)));
                    }

                    String status = "active";

                    JSONObject jsonroutes = new JSONObject(selectedroutesList);
                    String selectedRoutes = jsonroutes.toString();

                    JSONObject jsonallroutes = new JSONObject(routesList);
                    String allRoutes = jsonallroutes.toString();
                    String hourOfday = edit_alertTime.getText().toString().split(":")[0].trim();
                    String minute = edit_alertTime.getText().toString().split(":")[1].trim();
                    Calendar mCalendar = Calendar.getInstance();
                    mCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourOfday));
                    mCalendar.set(Calendar.MINUTE, Integer.parseInt(minute));


                    if(check_repeat.isChecked()){
                        if(repeat_startDate.equals("NA")){
                            Toast.makeText(CreateRiderAlert.this,"Please Select Repeat Settings",Toast.LENGTH_LONG).show();


                        }else{

                          /*  Log.e("selecteddays",selectedDays+"");
                            Log.e("occur",occurences);
                            Log.e("end_type",end_type);
                            Log.e("repeat",repeat_type);
                            Log.e("end_date",repeat_endDate);
                            Log.e("start_date",repeat_startDate);*/

                            JSONObject jsonobj_days = new JSONObject();
                            try {
                                jsonobj_days.put("selected_days", new JSONArray(selectedDays));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if(source.equals("edit")){
                                spotDB.UpdateRiderAlertByID(edit_alert_name.getText().toString(),selectedAgency,selectedAgencyUrl,stopName,stopId+"",is_stopName, edit_alertDate.getText().toString(), edit_alertTime.getText().toString(),is_repeat,selected_mins,repeat_type,jsonobj_days.toString(),repeat_startDate,edit_repeat_startTime.getText().toString(),repeat_endDate,selectedRoutes,allRoutes,status,end_type,occurences,alertId,is_direction,direction);
                            }else{
                                spotDB.insert_alerts(edit_alert_name.getText().toString(),selectedAgency,selectedAgencyUrl,stopName,stopId+"",is_stopName, edit_alertDate.getText().toString(), edit_alertTime.getText().toString(),is_repeat,selected_mins,repeat_type,jsonobj_days.toString(),repeat_startDate,edit_repeat_startTime.getText().toString(),repeat_endDate,selectedRoutes,allRoutes,status,end_type,occurences,is_direction,direction);
                            }
                            Toast.makeText(getApplicationContext(), "Rider Alert Saved",
                                    Toast.LENGTH_SHORT).show();

                            onBackPressed();

                        }
                    }else{
                        selectedDays.add("NA");
                        JSONObject jsonobj_days = new JSONObject();
                        try {
                            jsonobj_days.put("selected_days", new JSONArray(selectedDays));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(source.equals("edit")){
                            spotDB.UpdateRiderAlertByID(edit_alert_name.getText().toString(),selectedAgency,selectedAgencyUrl,stopName,stopId+"",is_stopName, edit_alertDate.getText().toString(), edit_alertTime.getText().toString(),is_repeat,selected_mins,"NA",jsonobj_days.toString(),"NA","NA","NA",selectedRoutes,allRoutes,status,"NA","0",alertId,is_direction,direction);
                        }else{
                            p_id = spotDB.insert_alerts(edit_alert_name.getText().toString(),selectedAgency,selectedAgencyUrl,stopName,stopId+"",is_stopName, edit_alertDate.getText().toString(), edit_alertTime.getText().toString(),is_repeat,selected_mins,"NA",jsonobj_days.toString(),"NA","NA","NA",selectedRoutes,allRoutes,status,"NA","0",is_direction,direction);
                        }
                        Toast.makeText(getApplicationContext(), "Rider Alert Saved",
                                Toast.LENGTH_SHORT).show();

                        onBackPressed();
                    }

                }


            }
        });

        int month = now.get(Calendar.MONTH)+1;

        String date = now.get(Calendar.DAY_OF_MONTH)+"/"+month+"/"+now.get(Calendar.YEAR);
        edit_alertDate.setText(date);

        repeatSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                Mint.leaveBreadcrumb("CreateRiderAlert repeatSettings ");
                repeatDialog.setContentView(R.layout.repeat_settings);
                ImageView recurrence_close_btn = (ImageView)repeatDialog.findViewById(R.id.recurrence_close_btn);
                recurrence_close_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        repeatDialog.dismiss();
                    }
                });
                mon = (CheckBox)repeatDialog.findViewById(R.id.mon);
                tue = (CheckBox)repeatDialog.findViewById(R.id.tue);
                wed = (CheckBox)repeatDialog.findViewById(R.id.wed);
                thur = (CheckBox)repeatDialog.findViewById(R.id.thur);
                fri = (CheckBox)repeatDialog.findViewById(R.id.fri);
                sat = (CheckBox)repeatDialog.findViewById(R.id.sat);
                sun = (CheckBox)repeatDialog.findViewById(R.id.sun);
                edit_repeat_startDate = (EditText)repeatDialog.findViewById(R.id.repeat_start_date);
                edit_repeat_startTime = (EditText)repeatDialog.findViewById(R.id.repeat_start_time);
                edit_repeat_startTime.setText(edit_alertTime.getText().toString());
                end_type = "never";
                edit_repeat_startDate.setText(edit_alertDate.getText().toString());
                repeat_startDate = edit_repeat_startDate.getText().toString();
                edit_repeat_startDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DatePickerDialog dpd = DatePickerDialog.newInstance(
                                CreateRiderAlert.this,
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        );
                        dpd.show(getFragmentManager(), "Datepickerdialog");
                        dpd.setMinDate(now);
                        dpd.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                String date = dayOfMonth+"/"+(++monthOfYear)+"/"+year;
                                edit_repeat_startDate.setText(date);
                            }
                        });

                    }
                });

                edit_repeat_endDate = (EditText)repeatDialog.findViewById(R.id.repeat_end_date);
                edit_repeat_endDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatePickerDialog dpd1 = DatePickerDialog.newInstance(
                                CreateRiderAlert.this,
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        );

                        dpd1.show(getFragmentManager(), "Datepickerdialog");
                        dpd1.setMinDate(now);
                        dpd1.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                String date = dayOfMonth+"/"+(++monthOfYear)+"/"+year;
                                edit_repeat_endDate.setText(date);
                            }
                        });

                    }
                });

                edit_repeat_startTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar now = Calendar.getInstance();
                        TimePickerDialog tpd = TimePickerDialog.newInstance(
                                CreateRiderAlert.this,
                                now.get(Calendar.HOUR_OF_DAY),
                                now.get(Calendar.MINUTE),
                                false
                        );


                        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                Log.d("TimePicker", "Dialog was cancelled");
                            }
                        });
                        tpd.show(getFragmentManager(), "Timepickerdialog");
                        tpd.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
                                String hour = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
                                String minutes = minute < 10 ? "0"+minute : ""+minute;
                                String seconds = second < 10 ? "0"+second : ""+second;
                                String time = hour+" :"+minutes;
                                edit_repeat_startTime.setText(time);
                            }
                        });
                    }
                });
                layout_repeatevery = (LinearLayout)repeatDialog.findViewById(R.id.layout_repeatevery);
                layout_repeaton1 = (LinearLayout)repeatDialog.findViewById(R.id.layout_repeaton1);
                layout_repeaton2 = (LinearLayout)repeatDialog.findViewById(R.id.layout_repeaton2);
                spinner_repeat_type = (Spinner)repeatDialog.findViewById(R.id.repeat_type);
                spinner_repeat_weekNo = (Spinner)repeatDialog.findViewById(R.id.repeat_week);
                spinner_endAfter_occurence = (Spinner)repeatDialog.findViewById(R.id.after_occurence);
                spinner_repeat_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                        String items = spinner_repeat_type.getSelectedItem().toString();
                        if(items.equals("Weekly")){
                            layout_repeaton1.setVisibility(View.VISIBLE);
                            layout_repeaton2.setVisibility(View.VISIBLE);
                        }else if(items.equals("Daily")){
                            layout_repeaton1.setVisibility(View.GONE);
                            layout_repeaton2.setVisibility(View.GONE);
                        }
                        Mint.leaveBreadcrumb("CreateRiderAlert spinner_repeat_type ");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });
                ends_radio_group = (RadioGroup)repeatDialog.findViewById(R.id.ends_radio_group);
                radio_never = (RadioButton)repeatDialog.findViewById(R.id.radio_never);
                radio_after = (RadioButton)repeatDialog.findViewById(R.id.radio_after);
                radio_on = (RadioButton)repeatDialog.findViewById(R.id.radio_on);
                ends_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        Mint.leaveBreadcrumb("CreateRiderAlert ends_radio_group ");
                        if(checkedId == R.id.radio_never){
                            end_type = "never";
                            spinner_endAfter_occurence.setVisibility(View.INVISIBLE);
                            edit_repeat_endDate.setVisibility(View.INVISIBLE);
                        }else if(checkedId == R.id.radio_after){
                            end_type = "after";
                            spinner_endAfter_occurence.setVisibility(View.VISIBLE);
                            edit_repeat_endDate.setVisibility(View.INVISIBLE);
                        }else if(checkedId == R.id.radio_on){
                            end_type = "on";
                            spinner_endAfter_occurence.setVisibility(View.INVISIBLE);
                            edit_repeat_endDate.setVisibility(View.VISIBLE);
                        }
                    }
                });

                tick = (ImageButton)repeatDialog.findViewById(R.id.tick);
                cross = (ImageButton)repeatDialog.findViewById(R.id.cross);
                tick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Mint.leaveBreadcrumb("CreateRiderAlert tick ");
                        repeat_type = spinner_repeat_type.getSelectedItem().toString();
                        selectedDays = new ArrayList<String>();
                        repeat_startDate = edit_repeat_startDate.getText().toString();
                        if(end_type.equals("after")){
                            occurences = spinner_endAfter_occurence.getSelectedItem().toString();
                            repeat_endDate = "NA";
                        }
                        else if(end_type.equals("on")){
                            repeat_endDate = edit_repeat_endDate.getText().toString();
                            occurences = "0";
                        }else{
                            repeat_endDate = "NA";
                            occurences = "0";
                        }

                        if(end_type.equals("on") && edit_repeat_endDate.getText().toString().equals("")){
                            Toast.makeText(CreateRiderAlert.this, "Please select end date", Toast.LENGTH_LONG).show();
                        }
                        else{

                            if (repeat_type.equals("Weekly") && (!mon.isChecked() && !tue.isChecked() && !wed.isChecked() && !thur.isChecked()
                                    && !sat.isChecked() && !fri.isChecked() && !sun.isChecked())) {
                                Toast.makeText(CreateRiderAlert.this, "Please select repeat day", Toast.LENGTH_LONG).show();


                            } else {
                                if (repeat_type.equals("Weekly")) {


                                    if (mon.isChecked()) {
                                        selectedDays.add("Monday");
                                    }
                                    if (tue.isChecked()) {
                                        selectedDays.add("Tuesday");
                                    }
                                    if (wed.isChecked()) {
                                        selectedDays.add("Wednesday");
                                    }
                                    if (thur.isChecked()) {
                                        selectedDays.add("Thursday");
                                    }
                                    if (fri.isChecked()) {
                                        selectedDays.add("Friday");
                                    }
                                    if (sat.isChecked()) {
                                        selectedDays.add("Saturday");
                                    }
                                    if (sun.isChecked()) {
                                        selectedDays.add("Sunday");
                                    }

                                }else{
                                    selectedDays.add("NA");
                                }


                                repeatDialog.dismiss();

                            }
                        }

                    }
                });

                cross.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Mint.leaveBreadcrumb("CreateRiderAlert cross");
                        repeat_startDate = "NA";
                        repeatDialog.dismiss();
                    }
                });
                repeatDialog.show();
            }

        });
        edit_alertDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mint.leaveBreadcrumb("CreateRiderAlert edit_alertDate");
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        CreateRiderAlert.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );


                dpd.show(getFragmentManager(), "Datepickerdialog");

                dpd.setMinDate(now);

            }
        });



        String time =   now.get(Calendar.HOUR_OF_DAY)+" :"+now.get(Calendar.MINUTE);
        edit_alertTime.setText(time);
        edit_alertTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mint.leaveBreadcrumb("CreateRiderAlert edit_alertTime ");
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        CreateRiderAlert.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false
                );


                tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

                    }
                });
                tpd.show(getFragmentManager(), "Timepickerdialog");
            }
        });
    }

    private void openAndQueryDatabase() throws JSONException {
        try {
            Cursor details = spotDB.fetchRiderAlertByID(alertId);
            if(details.getCount() != 0){

                riderAlert = new RiderAlert();
                riderAlert.setSelectedAgency(details.getString(details.getColumnIndex(DatabaseHandler.SELECTED_AGENCY)));
                selectedAgency = riderAlert.getSelectedAgency();
                riderAlert.setSelectedAgencyUrl(details.getString(details.getColumnIndex(DatabaseHandler.SELECTED_AGENCY_URL)));
                selectedAgencyUrl = riderAlert.getSelectedAgencyUrl();
                riderAlert.setAlertName(details.getString(details.getColumnIndex(DatabaseHandler.ALERT_NAME)));
                riderAlert.setIs_direction(details.getString(details.getColumnIndex(DatabaseHandler.IS_DIRECTION)));
                is_direction = riderAlert.getIs_direction();

                riderAlert.setDirection(details.getString(details.getColumnIndex(DatabaseHandler.DIRECTION)));
                direction = riderAlert.getDirection();
                int direction_index = getIndex(spinner_direction,direction);
                spinner_direction.setSelection(direction_index);
                riderAlert.setAlertStartDate(details.getString(details.getColumnIndex(DatabaseHandler.START_DATE)));
                edit_alertDate.setText(riderAlert.getAlertStartDate());
                riderAlert.setAlertStartTime(details.getString(details.getColumnIndex(DatabaseHandler.START_TIME)));
                edit_alertTime.setText(riderAlert.getAlertStartTime());
                riderAlert.setIs_repeat(details.getString(details.getColumnIndex(DatabaseHandler.IS_REPEAT)));
                if(riderAlert.getIs_repeat().equals("true")){
                    check_repeat.setChecked(true);
                    alarmImage.setVisibility(View.VISIBLE);
                    repeatSettings.setVisibility(View.VISIBLE);
                }
                riderAlert.setIs_stopName(details.getString(details.getColumnIndex(DatabaseHandler.IS_STOPNAME)));
                riderAlert.setStopName(details.getString(details.getColumnIndex(DatabaseHandler.STOP_NAME)));
                stopName = riderAlert.getStopName();
                if(riderAlert.getIs_stopName().equals("true")){
                    check_use_stopName.setChecked(true);
                    edit_alert_name.setText(riderAlert.getStopName());
                }else{
                    edit_alert_name.setText(riderAlert.getAlertName());
                }
                riderAlert.setStopId(details.getString(details.getColumnIndex(DatabaseHandler.STOP_ID)));
                stopId = Integer.parseInt(riderAlert.getStopId());
                try {
                    JSONObject allroutes = new JSONObject(details.getString(details.getColumnIndex(DatabaseHandler.ALLROUTES)));
                    riderAlert.setAllroutes(allroutes);
                    routesList = new HashMap<>();
                    Iterator<String> iter = allroutes.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        try {

                            Object value =  allroutes.get(key);
                            routesList.put(key, (Integer) value);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Something went wrong!
                        }
                    }
                    JSONObject routes = new JSONObject(details.getString(details.getColumnIndex(DatabaseHandler.ROUTES)));
                    riderAlert.setRoutes(routes);



                } catch (JSONException e) {
                    e.printStackTrace();
                }

                riderAlert.setMinutes(details.getString(details.getColumnIndex(DatabaseHandler.MINUTES)));

                minutes.setSelection(getIndex(minutes, riderAlert.getMinutes()));

            }

            else{
                Toast.makeText(CreateRiderAlert.this, "Invalid Alert ", Toast.LENGTH_LONG).show();
            }


        } catch (SQLiteException se ) {
            se.printStackTrace();
        } finally {

        }

    }

    static int[] toIntArray(List<Integer> integerList) {
        int[] intArray = new int[integerList.size()];
        for (int i = 0; i < integerList.size(); i++) {
            intArray[i] = integerList.get(i);
        }
        return intArray;
    }


    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }


    private static long getTriggerAt(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        //calendar.add(Calendar.HOUR, NOTIFICATIONS_INTERVAL_IN_HOURS);
        return calendar.getTimeInMillis();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = dayOfMonth+"/"+(++monthOfYear)+"/"+year;
        edit_alertDate.setText(date);

    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
        minuteString = minute < 10 ? "0"+minute : ""+minute;
        secondString = second < 10 ? "0"+second : ""+second;
        String time = hourString+" :"+minuteString;
        edit_alertTime.setText(time);

    }

    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {

    }
}
