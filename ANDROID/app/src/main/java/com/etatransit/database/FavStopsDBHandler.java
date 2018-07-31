package com.etatransit.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.splunk.mint.Mint;

/**
 * Created by Innovate on 9/20/2017.
 */
public class FavStopsDBHandler {
    private static final int DATABASE_VERSION = 1;

    private Context DmCtx = null;

    private DatabaseHelper SpotDBHelper;
    private static SQLiteDatabase db;

    private FavStopsDBHandler dbHandler;
    // Database Name
    private static final String DATABASE_NAME = "Favorites";

    // Favorite Stops table name
    private static final String TABLE_STOPS = "FavoriteStops";
    // Favorite Stops Table Columns names
    public static String FAV_AGENCY = "agency";
    public static final String STOP_ID = "stopID";

    private static final String TABLE_FAV_STOPS = " CREATE TABLE " + TABLE_STOPS + " (" +
            FAV_AGENCY + " TEXT NOT NULL, "+
            STOP_ID + " TEXT NOT NULL,"+"PRIMARY KEY("+FAV_AGENCY+","+STOP_ID+"));";

    public long insert_fav_stops(String agency,String stopID){
        synchronized (this) {
            if (DmCtx == null)
                return 0;

            dbHandler = new FavStopsDBHandler(DmCtx);
            dbHandler.open();

            ContentValues cValues = new ContentValues();
            cValues.put(FAV_AGENCY, agency);
            cValues.put(STOP_ID, stopID);
            long res = db.insert(TABLE_STOPS, null, cValues);
            Log.d("insert", "insert_fav_stops: "+res);
            dbHandler.close();
            return res;
        }

    }
    public Cursor fetchAllFavStops(String agency) {
        synchronized (this) {
            if (DmCtx == null)
                return null;

            dbHandler = new FavStopsDBHandler(DmCtx);
            dbHandler.open();

            Cursor mCursor = null;
            try {
                String query = "SELECT * FROM " + TABLE_STOPS+ " where " + FAV_AGENCY + "='" + agency + "'";

//                Log.d("FavSELECT query---", query);
                mCursor = db.rawQuery(query, null);
                if (mCursor != null) {
                    mCursor.moveToFirst();
                }
            } catch (Exception e) {
                return mCursor;
            }finally {
                dbHandler.close();
            }

            return mCursor;
        }
    }

    public Boolean deleteFavStop(String agency,String stopID){
        synchronized (this) {
            if (DmCtx == null)
                return false;

            dbHandler = new FavStopsDBHandler(DmCtx);
            dbHandler.open();
            Cursor mCursor = null;
            try {
                String query = "DELETE  FROM " + TABLE_STOPS + " where " + STOP_ID + "='" + stopID + "'AND "+ FAV_AGENCY + "='"+agency+"'";
//                Log.d("newMarker","FavDELETEquery---"+ query);
                mCursor = db.rawQuery(query, null);
                Log.d("newMarker","Favdeletionnn"+ mCursor.getCount() + "");
                return true;
            } catch (Exception e) {
                Log.d("newMarker","Exception---"+ e);
                return false;
            }finally {
                dbHandler.close();
            }
        }
    }

    public Boolean dropTable(){
        synchronized (this){
            if (DmCtx == null)
                return false;
            dbHandler = new FavStopsDBHandler(DmCtx);
            dbHandler.open();
            try {
                String query = "DROP TABLE IF EXISTS " + TABLE_STOPS ;
                Log.d("FavDROPquery for delete", query);
                db.rawQuery(query, null);
            }
            catch (Exception e){
                return false;
            }
            finally {
                dbHandler.close();
            }
            return false;
        }
    }

    public FavStopsDBHandler(Context context) {
        this.DmCtx = context;
        SpotDBHelper = new DatabaseHelper(DmCtx);
    }


    public FavStopsDBHandler open() throws SQLException {
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
//            Log.d("Fav DB", "DB Created");
        }




        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            try {

                sqLiteDatabase.execSQL(TABLE_FAV_STOPS);
                Log.d("newMarker", "FavonCreate: "+TABLE_FAV_STOPS);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }

    }
}
