package com.visual.android.locsilence;

/**
 * Created by RamiK on 10/13/2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SQLDatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 11;

    // Database Name
    private static final String DATABASE_NAME = "Locations.db";

    // Contacts table name
    private static final String TABLE_LOCATIONS = "Locations";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LONG = "long";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";
    private static final String KEY_VOL = "volumes";
    private static final String KEY_CID = "circle_id";
    private static final String KEY_RAD = "radius";
    private static final String KEY_CUST_PROX = "custom_proximity";

    private Type typeListInteger;
    private Type typeListLatLng;

    public SQLDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.typeListInteger = new TypeToken<List<Integer>>() {}.getType();
        this.typeListLatLng = new TypeToken<ArrayList<LatLng>>() {}.getType();
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("
                + KEY_ID + " VARCHAR(255) PRIMARY KEY," + KEY_NAME + " VARCHAR(255),"
                + KEY_ADDRESS + " VARCHAR(255)," + KEY_LAT + " FLOAT(255, 255)," + KEY_LONG + " FLOAT(255, 255),"
                + KEY_CREATED_AT + " DATETIME," + KEY_UPDATED_AT + " DATETIME," +
                KEY_VOL + " VARCHAR(255)," + KEY_CID + " VARCHAR(255)," +
                KEY_RAD + " TINY(255)," + KEY_CUST_PROX + " VARCHAR(255))";
        Log.i("Table", CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    public int getSize(){
        return this.getAllLocations().size();
    }

    // Adding new contact
    public boolean addLocation(Location location) {
        boolean responseCode = true;
        SQLiteDatabase db = this.getWritableDatabase();
        Gson gson = new Gson();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, location.getId());
        values.put(KEY_NAME, location.getName());
        values.put(KEY_ADDRESS, location.getAddress());
        values.put(KEY_LAT, location.getLat());
        values.put(KEY_LONG, location.getLng());
        values.put(KEY_CREATED_AT, location.getCreatedAt());
        values.put(KEY_UPDATED_AT, location.getUpdatedAt());
        values.put(KEY_VOL, gson.toJson(location.getVolumes()));
        values.put(KEY_CID, location.getCircleId());
        values.put(KEY_RAD, location.getRadius());
        values.put(KEY_CUST_PROX, gson.toJson(location.getCustomProximity()));
        // Inserting Row
        try {
            db.insertOrThrow(TABLE_LOCATIONS, null, values);
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            responseCode = false;
        }

        db.close(); // Closing database connection
        return responseCode;
    }

    // Getting single contact
    public Location getLocation(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Gson gson = new Gson();
        Cursor cursor = db.query(TABLE_LOCATIONS, new String[]{KEY_ID,
                        KEY_NAME, KEY_ADDRESS, KEY_LAT, KEY_LONG, KEY_CREATED_AT, KEY_UPDATED_AT,
                        KEY_VOL, KEY_CID, KEY_RAD, KEY_CUST_PROX}, KEY_ID + "=?",
                new String[]{id}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        if (cursor != null && cursor.getCount() > 0) {
            Location location = new Location();
            location.setId(cursor.getString(0));
            location.setName(cursor.getString(1));
            location.setAddress(cursor.getString(2));
            location.setLat(cursor.getFloat(3));
            location.setLng(cursor.getFloat(4));
            location.setCreatedAt(cursor.getString(5));
            location.setUpdatedAt(cursor.getString(6));
            location.setVolumes((List<Integer>)gson.fromJson(cursor.getString(7), typeListInteger));
            location.setCircleId(cursor.getString(8));
            location.setRadius(cursor.getInt(9));
            location.setCustomProximity((List<LatLng>)gson.fromJson(cursor.getString(10), typeListLatLng));

            cursor.close();
            return location;

        }

        cursor.close();

        return null;
    }

    public List<Location> getAllLocations() {
        return getAllLocations("");
    }

    // Getting All Contacts
    public List<Location> getAllLocations(String query) {
        List<Location> allLocations = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATIONS + " WHERE name LIKE " +
                DatabaseUtils.sqlEscapeString("%" + query + "%");

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Gson gson = new Gson();

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Location location = new Location();
                location.setId(cursor.getString(0));
                location.setName(cursor.getString(1));
                location.setAddress(cursor.getString(2));
                location.setLat(cursor.getFloat(3));
                location.setLng(cursor.getFloat(4));
                location.setCreatedAt(cursor.getString(5));
                location.setUpdatedAt(cursor.getString(6));
                location.setVolumes((List<Integer>)gson.fromJson(cursor.getString(7), typeListInteger));
                location.setCircleId(cursor.getString(8));
                location.setRadius(cursor.getInt(9));
                location.setCustomProximity((List<LatLng>)gson.fromJson(cursor.getString(10), typeListLatLng));
                // Adding contact to list
                allLocations.add(location);
            } while (cursor.moveToNext());
        }

        cursor.close();

        Collections.sort(allLocations, new Comparator<Location>() {
            public int compare(Location o1, Location o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        // return contact list
        return allLocations;
    }

    // Updating single contact
    public int updateLocalGame(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();
        Gson gson = new Gson();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, location.getId());
        values.put(KEY_NAME, location.getName());
        values.put(KEY_ADDRESS, location.getAddress());
        values.put(KEY_LAT, location.getLat());
        values.put(KEY_LONG, location.getLng());
        values.put(KEY_CREATED_AT, location.getCreatedAt());
        values.put(KEY_UPDATED_AT, location.getUpdatedAt());
        values.put(KEY_VOL, gson.toJson(location.getVolumes()));
        values.put(KEY_CID, location.getCircleId());
        values.put(KEY_RAD, location.getRadius());
        values.put(KEY_CUST_PROX, gson.toJson(location.getCustomProximity()));
        // updating row
        return db.update(TABLE_LOCATIONS, values, KEY_ID + "='" + location.getId() + "'", null);
    }

    // Deleting single contact
    public void deleteLocalGame(String uniqueID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATIONS, KEY_ID + " = ?",
                new String[]{uniqueID});
        db.close();
    }


    // Getting contacts Count
    public int getLocalGamesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOCATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cursorCount = cursor.getCount();
        cursor.close();

        // return count
        return cursorCount;
    }

    // Checks if Location already exists in db
    public boolean locationInDB(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "Select * from " + TABLE_LOCATIONS + " where " + KEY_ID + " = '" + id + "'";
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    // prints formatted DB for testing
    public void printDB() {
        List<Location> locations = getAllLocations();
        for (Location location : locations) {
            Log.i("logDB", "DB: (name: " + location.getName() + ") | " +
                    "(Address: " + location.getAddress() + ") | " +
                    "(LatLong: " + location.getLat() + ":" + location.getLng() + ") |" +
                    "(vol: " + location.getVolumes() + ") |" +
                    "(ID: " + location.getId() + ") | " + "(Cid: " + location.getCircleId() + ") |" +
                    "(Radius: " + location.getRadius() + ") |" +
                    "(Custom_proximity "+ location.getCustomProximity() + ") |"
            );
        }

    }

}

