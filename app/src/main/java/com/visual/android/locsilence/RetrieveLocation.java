package com.visual.android.locsilence;

import android.location.Criteria;
import android.location.LocationManager;
import android.os.AsyncTask;

/**
 * Created by RamiK on 11/1/2017.
 */


public abstract class RetrieveLocation extends AsyncTask<LocationManager, Void, android.location.Location> {


    @Override
    protected android.location.Location doInBackground(LocationManager... locationManagers) {
        System.out.println("DO IN BACKGROUND");

        Criteria criteria = new Criteria();
        android.location.Location location = null;
        try {
            location = locationManagers[0].getLastKnownLocation(locationManagers[0]
                    .getBestProvider(criteria, false));
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        //double latitude = location.getLatitude();
        //double longitude = location.getLongitude();

        if (!Utility.firstRecursiveExecution) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Utility.firstRecursiveExecution = false;
        }


        return location;
    }
}
