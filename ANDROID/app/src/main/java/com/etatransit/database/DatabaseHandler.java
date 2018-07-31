package com.etatransit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.etatransit.model.RiderAlert;
import com.splunk.mint.Mint;

/**
 * Created by innovator on 8/30/2016.
 */
public class DatabaseHandler {

    private static final int DATABASE_VERSION = 1;

    private Context DmCtx = null;

    private DatabaseHelper SpotDBHelper;
    private static SQLiteDatabase db;

    private DatabaseHandler dbHandler;

    // Database Name
    private static final String DATABASE_NAME = "RiderAlerts";

    // RiderAlerts table name
    private static final String TABLE_ALERTS = "Alerts";

    // RiderAlerts Table Columns names
    public static String ALERT_ID = "_id";
    public static final String ALERT_NAME = "name";
    public static final String SELECTED_AGENCY = "selected_agency";
    public static final String SELECTED_AGENCY_URL = "selected_agency_url";
    public static final String IS_STOPNAME = "is_stopname";
    public static final String STOP_NAME = "stop_name";
    public static final String STOP_ID = "stop_id";
    public static final String START_DATE = "start_date";
    public static final String START_TIME = "start_time";
    public static final String IS_REPEAT = "is_repeat";
    public static final String MINUTES = "minutes";
    public static final String REPEAT_TYPE = "repeat";
    public static final String SELECTED_DAYS = "days";
    public static final String REPEAT_END_DATE = "repeat_end_date";
    public static final String REPEAT_START_DATE = "repeat_start_date";
    public static final String REPEAT_START_TIME = "repeat_start_time";
    public static final String ROUTES = "routes";
    public static final String ALLROUTES = "all_routes";
    public static final String STATUS = "status";
    public static final String END_AFTER_OCCURENCES = "end_after_occurences";
    public static final String ETA_MINS = "eta_mins";
    public static final String REPEAT_END_TYPE = "repeat_end_type";
    public static final String OCCURENCES = "occurences";
    public static final String IS_DIRECTION = "is_direction";
    public static final String DIRECTION = "direction";
    RiderAlert riderAlert;

    private static final String TABLE_RIDER_ALERTS = " CREATE TABLE " + TABLE_ALERTS + " (" +
            ALERT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ALERT_NAME + " TEXT NOT NULL, " +
            SELECTED_AGENCY + " TEXT NOT NULL, " +
            SELECTED_AGENCY_URL + " TEXT NOT NULL, " +
            START_DATE + " TEXT NOT NULL, " +
            IS_STOPNAME + " TEXT NOT NULL, " +
            STOP_NAME + " TEXT NOT NULL, " +
            STOP_ID + " TEXT NOT NULL, " +
            START_TIME + " TEXT NOT NULL, " +
            SELECTED_DAYS + " TEXT , " +
            REPEAT_TYPE + " TEXT , " +
            REPEAT_END_TYPE + " TEXT , " +
            REPEAT_END_DATE + " TEXT , " +
            REPEAT_START_DATE + " TEXT , " +
            ROUTES + " TEXT, " +
            ALLROUTES + " TEXT, " +
            IS_REPEAT + " TEXT, " +
            STATUS + " TEXT, " +
            ETA_MINS + " TEXT, " +
            REPEAT_START_TIME + " TEXT, " +
            OCCURENCES + " TEXT, " +
            IS_DIRECTION + " TEXT, " +
            DIRECTION + " TEXT, " +
            END_AFTER_OCCURENCES + " Text, "+
            MINUTES + " TEXT NOT NULL);";


    public long insert_alerts(String name, String selected_agency, String selected_agency_url, String stop_name, String stop_id, String is_stopname, String start_date, String start_time,
                              String is_repeat, String mins, String repeat_type,
                              String selected_days, String repeat_start_date , String repeat_start_time, String repeat_end_date, String routes, String allroutes, String status,String repeat_end_type,String end_after_occurences,String is_direction,String direction) {




        synchronized (this) {
            if (DmCtx == null)
                return 0;

            dbHandler = new DatabaseHandler(DmCtx);
            dbHandler.open();


            ContentValues cValues = new ContentValues();
            cValues.put(ALERT_NAME, name);
            cValues.put(SELECTED_AGENCY, selected_agency);
            cValues.put(SELECTED_AGENCY_URL, selected_agency_url);
            cValues.put(STOP_NAME, stop_name);
            cValues.put(STOP_ID, stop_id);
            cValues.put(IS_STOPNAME, is_stopname);
            cValues.put(START_DATE, start_date);
            cValues.put(START_TIME, start_time);
            cValues.put(IS_REPEAT, is_repeat);
            cValues.put(MINUTES, mins);
            cValues.put(REPEAT_TYPE, repeat_type);
            cValues.put(SELECTED_DAYS, selected_days);
            cValues.put(REPEAT_START_DATE, repeat_start_date);
            cValues.put(REPEAT_START_TIME, repeat_start_time);
            cValues.put(REPEAT_END_DATE, repeat_end_date);
            cValues.put(ROUTES, routes);
            cValues.put(STATUS, status);
            cValues.put(ALLROUTES, allroutes);
            cValues.put(END_AFTER_OCCURENCES, end_after_occurences);
            cValues.put(ETA_MINS,"NA");
            cValues.put(OCCURENCES,"0");
            cValues.put(REPEAT_END_TYPE,repeat_end_type);
            cValues.put(IS_DIRECTION,is_direction);
            cValues.put(DIRECTION,direction);
            long res = db.insert(TABLE_ALERTS, null, cValues);
            dbHandler.close();
            return res;
        }

    }

    public Cursor fetchAllRiderAlerts() {
        synchronized (this) {
            if (DmCtx == null)
                return null;

            dbHandler = new DatabaseHandler(DmCtx);
            dbHandler.open();

            Cursor mCursor = null;
            try {
                String query = "SELECT * FROM " + TABLE_ALERTS;

//                Log.d("Rider DB", query);
                mCursor = db.rawQuery(query, null);
//                Log.d("**--**", mCursor + "");
                if (mCursor != null) {
                    mCursor.moveToFirst();
                }
//                Log.d("Rider DB", mCursor.getCount() + "");
            } catch (Exception e) {
                return mCursor;
            }finally {
                dbHandler.close();
            }

            return mCursor;
        }
    }



    public Cursor fetchRiderAlertByID(String alertId) {
        synchronized (this) {
            if (DmCtx == null)
                return null;

            dbHandler = new DatabaseHandler(DmCtx);
            dbHandler.open();

            Cursor mCursor = null;
            try {
                String query = "SELECT * FROM " + TABLE_ALERTS + " where " + ALERT_ID + "='" + alertId + "'";

//                Log.d("Rider DB", query);
                mCursor = db.rawQuery(query, null);
                if (mCursor != null) {
                    mCursor.moveToFirst();
                }
//                Log.d("**--**", mCursor.getCount() + "");
            } catch (Exception e) {
                return mCursor;
            }finally {
                dbHandler.close();
            }

            return mCursor;
        }
    }

    public Cursor fetchStopIdByID(String stopId,String stopName) {

        synchronized (this) {
            if (DmCtx == null)
                return null;

            dbHandler = new DatabaseHandler(DmCtx);
            dbHandler.open();

            Cursor mCursor = null;
            try {
                String query = "SELECT * FROM " + TABLE_ALERTS + " where " + STOP_ID + "='" + stopId + "' And " + STOP_NAME + "='" + stopName + "'" ;

                Log.d("Rider DB:SELECT", query);
                mCursor = db.rawQuery(query, null);
                Log.d("Rider DB:SELECT", mCursor + "");
                if (mCursor != null) {
                    mCursor.moveToFirst();
                }
//                Log.d("Rider DB", mCursor.getCount() + "");
            } catch (Exception e) {
                return mCursor;
            }finally {
                dbHandler.close();
            }

            return mCursor;
        }
    }

//    public RiderAlert fetchRiderAlertModel(int alertId) {
//        Cursor mCursor = null;
//        try {
//            String query ="SELECT * FROM " + TABLE_ALERTS +" where "+ALERT_ID+"='"+alertId+"'" ;
//
//            Log.e("query---",query);
//            mCursor = db.rawQuery(query,null);
//            Log.e("**--**",mCursor+"");
//            if (mCursor != null) {
//                mCursor.moveToFirst();
//            }
//            Log.d("**--**",mCursor.getCount()+"");
//
//            JSONObject allroutes = new JSONObject(mCursor.getString(13));
//            JSONObject routes = new JSONObject(mCursor.getString(14));
//
//
//             riderAlert = new RiderAlert(Integer.parseInt(mCursor.getString(0)), mCursor.getString(1),
//                    mCursor.getString(2), mCursor.getString(3), mCursor.getString(4),
//                    mCursor.getString(5), mCursor.getString(6), mCursor.getString(7),mCursor.getString(8),mCursor.getString(9),
//                     mCursor.getString(10),mCursor.getString(11),mCursor.getString(12),routes,allroutes,
//                     mCursor.getString(15),mCursor.getString(16));
//
//
//
//        } catch (Exception e) {
//            return riderAlert;
//        }
//        return riderAlert;
//    }

    public Boolean deleteRiderAlert(String alertId){
        synchronized (this) {
            if (DmCtx == null)
                return false;

            dbHandler = new DatabaseHandler(DmCtx);
            dbHandler.open();

            Cursor mCursor = null;
            try {
                String query = "DELETE  FROM " + TABLE_ALERTS + " where " + ALERT_ID + "='" + alertId + "'";

                Log.d("Rider DB :delete", query);

                mCursor = db.rawQuery(query, null);
                Log.d("Rider DB :delete", mCursor.getCount() + "");
            } catch (Exception e) {
                return false;
            }finally {
                dbHandler.close();
            }

            return false;
        }
    }

    public Cursor UpdateRiderAlertByID(String name, String selected_agency, String selected_agency_url,
                                       String stop_name, String stop_id, String is_stopname, String start_date,
                                       String start_time, String is_repeat, String mins, String repeat_type,
                                       String selected_days, String repeat_start_date, String repeat_start_time,
                                       String repeat_end_date, String routes, String allroutes,String status,
                                       String repeat_end_type,String end_after_occurences, String alertId,String is_direction,String direction) {
        synchronized (this) {
            if (DmCtx == null)
                return null;

            dbHandler = new DatabaseHandler(DmCtx);
            dbHandler.open();


            Cursor mCursor = null;
            try {
                String query = "UPDATE " + TABLE_ALERTS + " set " + ALERT_NAME
                        + " = '" + name + "'" + "," + SELECTED_AGENCY
                        + " = '" + selected_agency + "'" + "," + SELECTED_AGENCY_URL
                        + " = '" + selected_agency_url + "'" + "," + STOP_NAME
                        + " = '" + stop_name + "'" + "," + STOP_ID
                        + " = '" + stop_id + "'" + "," + IS_STOPNAME
                        + " = '" + is_stopname + "'" + "," + START_DATE
                        + " = '" + start_date + "'" + "," + START_TIME
                        + " = '" + start_time + "'" + "," + IS_REPEAT
                        + " = '" + is_repeat + "'" + "," + MINUTES
                        + " = '" + mins + "'" + "," + REPEAT_TYPE
                        + " = '" + repeat_type + "'" + "," + SELECTED_DAYS
                        + " = '" + selected_days + "'" + "," + REPEAT_START_DATE
                        + " = '" + repeat_start_date + "'" + "," + REPEAT_START_TIME
                        + " = '" + repeat_start_time + "'" + "," + REPEAT_END_DATE
                        + " = '" + repeat_end_date + "'" + "," + ROUTES
                        + " = '" + routes + "'" + "," + ALLROUTES
                        + " = '" + allroutes + "'" + "," + IS_DIRECTION
                        + " = '" + is_direction + "'" + "," + DIRECTION
                        + " = '" + direction + "'" + "," + END_AFTER_OCCURENCES
                        + " = '" + end_after_occurences + "'" + "," + REPEAT_END_TYPE
                        + " = '" + repeat_end_type + "'" + "," + STATUS + " = '" + status + "'" + " where " + ALERT_ID + " = '" + alertId + "'";

//                Log.d("Rider DB", query);
                mCursor = db.rawQuery(query, null);
//                Log.d("Rider DB", mCursor + "");
                if (mCursor != null) {
                    mCursor.moveToFirst();
                }
//                Log.d("Rider DB", mCursor.getCount() + "");
            } catch (Exception e) {
                return mCursor;
            }finally {
                dbHandler.close();
            }

            return mCursor;
        }


    }


    public Cursor UpdateETARiderAlertByID( String alertId,String eta_mins) {

        synchronized (this) {
            if (DmCtx == null)
                return null;

            dbHandler = new DatabaseHandler(DmCtx);
            dbHandler.open();


            Cursor mCursor = null;
            try {
                String query = "UPDATE " + TABLE_ALERTS + " set " + ETA_MINS
                        + " = '" + eta_mins + "'" + " where " + ALERT_ID + " = '" + alertId + "'";

//                Log.d("Rider DB-", query);
                mCursor = db.rawQuery(query, null);
//                Log.d("Rider DB", mCursor + "");
                if (mCursor != null) {
                    mCursor.moveToFirst();
                }
//                Log.d("Rider DB", mCursor.getCount() + "");
            } catch (Exception e) {

                Log.e("exception", e.getMessage());
                return mCursor;
            }finally {
                dbHandler.close();
            }

            return mCursor;
        }

    }

    public Cursor UpdateOccurencesRiderAlertByID( String alertId,String occurences) {

        synchronized (this) {
            if (DmCtx == null)
                return null;

            dbHandler = new DatabaseHandler(DmCtx);
            dbHandler.open();


            Cursor mCursor = null;
            try {
                String query = "UPDATE " + TABLE_ALERTS + " set " + OCCURENCES
                        + " = '" + occurences + "'" + " where " + ALERT_ID + " = '" + alertId + "'";

//                Log.d("Rider DB", query);
                mCursor = db.rawQuery(query, null);
//                Log.d("Rider DB", mCursor + "");
                if (mCursor != null) {
                    mCursor.moveToFirst();
                }
//                Log.d("Rider DB", mCursor.getCount() + "");
            } catch (Exception e) {

                Log.e("exception", e.getMessage());
                return mCursor;
            }finally {
                dbHandler.close();
            }

            return mCursor;
        }



    }

    public Cursor UpdateStatusRiderAlertByID(String status, String alertId) {

        synchronized (this) {
            if (DmCtx == null)
                return null;

            dbHandler = new DatabaseHandler(DmCtx);
            dbHandler.open();
            Cursor mCursor = null;
            try {
                String query = "UPDATE " + TABLE_ALERTS + " set " + STATUS
                        + " = '" + status + "'" + " where " + ALERT_ID + " = '" + alertId + "'";

//                Log.d("Rider DB", query);
                mCursor = db.rawQuery(query, null);
//                Log.d("Rider DB", mCursor + "");
                if (mCursor != null) {
                    mCursor.moveToFirst();
                }
//                Log.d("Rider DB", mCursor.getCount() + "");
            } catch (Exception e) {

                Log.e("exception", e.getMessage());
                return mCursor;
            }finally {
                dbHandler.close();
            }

            return mCursor;
        }



    }


    public DatabaseHandler(Context context) {
        this.DmCtx = context;
        SpotDBHelper = new DatabaseHelper(DmCtx);
    }


    public DatabaseHandler open() throws SQLException {
        SpotDBHelper = new DatabaseHelper(DmCtx);
        db = SpotDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (SpotDBHelper != null) {
            SpotDBHelper.close();
        }
    }



    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
//            Log.d("Rider DB", "DB Created");
        }




        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            sqLiteDatabase.execSQL(TABLE_RIDER_ALERTS);
//            Log.d("Rider DB", "TABLE Created");

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }

    }
}
