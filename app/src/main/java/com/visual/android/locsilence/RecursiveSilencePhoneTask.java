package com.visual.android.locsilence;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.visual.android.locsilence.R.mipmap.ic_launcher;

/**
 * Created by RamiK on 11/1/2017.
 */

public class RecursiveSilencePhoneTask extends RetrieveLocation {

    private LocationManager locationManager;
    private SQLDatabaseHandler db;
    private Context context;
    private AudioManager audio;
    private static List<Integer> savedVolumes;
    private static Location currentlySilencedLocation;

    private static boolean recentlySilenced = false;

    NotificationCompat.Builder notification;
    private static final int notifID = 12345;


    public RecursiveSilencePhoneTask(LocationManager locationManager, SQLDatabaseHandler db, Context context) {
        this.locationManager = locationManager;
        this.db = db;
        this.context = context;
        if (savedVolumes == null) {
            savedVolumes = new ArrayList<>();
        }
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    protected void onPostExecute(android.location.Location current_location) {
        Log.i("POST", "===============================================");


        super.onPostExecute(current_location);

        //List<Location> locations = db.getAllLocations();
        List<Integer> streamTypes = Arrays.asList(AudioManager.STREAM_RING,
                AudioManager.STREAM_NOTIFICATION, AudioManager.STREAM_ALARM);

        //super.onPostExecute(current_location);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        Boolean workerOn = prefs.getBoolean("operation_switch", true);
        boolean flag = false;

        List<Location> locations = db.getAllLocations();

        if (workerOn) {
            double currentLat = current_location.getLatitude();
            double currentLon = current_location.getLongitude();
            double currentAccuracy = current_location.getAccuracy();

            Log.i("Worker Status", "Is On");
            //List<Location> locations = db.getAllLocations();


            if (locations.size() == 0) {
                removeNotification();
            }

            flag = false;
            for (Location location : locations) {
                Log.i(".", "---------------------------------------------");
                Log.i("Location", location.getAddress());
                float[] results = new float[5];
                int radius = location.getRadius();
                Log.i("Current radius", Integer.toString(radius));
                Log.i("Current accuracy", Double.toString(currentAccuracy));
                double lat = location.getLat();
                double lng = location.getLng();
                android.location.Location.distanceBetween(lat, lng, currentLat, currentLon, results);

                // The computed distance is stored in results[0].
                // If results has length 2 or greater, the initial bearing is stored in results[1].
                // If results has length 3 or greater, the final bearing is stored in results[2]
                Log.i("Current distance", Float.toString(results[0]));

                if (results[0] < radius + currentAccuracy) {
                    Log.i("User", "In Circle!");
                    try {
                        //audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        if (!recentlySilenced) {
                            Log.i("Silencing", "Activated");
                            currentlySilencedLocation = location;
                            modifyPhoneVolume(streamTypes, JsonUtils.volumeLevelsToList(location.getVolumes()));
                            sendNotification("activated", location.getName(), true);
                        }
                        recentlySilenced = true;
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    flag = true;
                    break;
                } else {
                    Log.i("User", "Not In Circle!");
                    removeNotification();
                }
            }
        } else {
            Log.i("Worker Status", "Is Off");
            removeNotification();
        }

        if (!flag) {
            Log.i("User", "Not in any locations");
            try {
                //audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                revertPhoneVolume(streamTypes);
                if (recentlySilenced) {
                    Log.i("Silencing", "Deactivated");
                    if (locations.size() == 0) {
                        Log.i("User", "removing notification");
                        removeNotification();
                    } else {
                        Log.i("SENDING", "DEACTIVATION NOTIFICATION");
                        sendNotification("deactivated", "", false);
                    }
                    recentlySilenced = false;
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        Utility.recursiveSilencePhoneTask = new RecursiveSilencePhoneTask(locationManager, db, context);
        Utility.recursiveSilencePhoneTask.execute(this.locationManager);

    }

    public void sendNotification(String activated, String location, Boolean permanent) {
        NotificationManager nm = (NotificationManager) this.context.getSystemService(NOTIFICATION_SERVICE);

        if (isNotificationVisible()) {
            nm.cancel(notifID);
        }

        notification = new NotificationCompat.Builder(this.context);
        notification.setAutoCancel(true);

        notification.setSmallIcon(ic_launcher);
        notification.setTicker("");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("LocSilence " + activated);
        notification.setContentText(location);

        notification.setOngoing(permanent);
        notification.setAutoCancel(!permanent);

        Intent intent = new Intent(this.context, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        //issues notification
        nm.notify(notifID, notification.build());
    }

    public void removeNotification() {
        NotificationManager nm = (NotificationManager) this.context.getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(notifID);

    }

    private boolean isNotificationVisible() {
        Intent notificationIntent = new Intent(context, RecursiveSilencePhoneTask.class);
        PendingIntent test = PendingIntent.getActivity(context, notifID, notificationIntent, PendingIntent.FLAG_NO_CREATE);
        return test != null;
    }

    private void modifyPhoneVolume(List<Integer> streamTypes, List<Integer> volumeLevels) {
        Log.i("VOLUME", "MODIFIED");
        for (int i = 0; i < streamTypes.size(); i++) {
            if (volumeLevels.get(i) != -1) {
                Log.i("CURRENT VOL", Integer.toString(audio.getStreamVolume(streamTypes.get(i))));
                savedVolumes.add(audio.getStreamVolume(streamTypes.get(i)));
                audio.setStreamVolume(streamTypes.get(i), volumeLevels.get(i), 0);
            } else {
                savedVolumes.add(-1);
            }
        }
    }

    private void revertPhoneVolume(List<Integer> streamTypes) {
        Log.i("Volume", "Reverted");
        List<Integer> volumeLevels = new ArrayList<>();
        if (currentlySilencedLocation != null) {
            volumeLevels = JsonUtils.volumeLevelsToList(currentlySilencedLocation.getVolumes());
        }
        for (int i = 0; i < streamTypes.size(); i++) {
            Log.i("Stream Type", Integer.toString(streamTypes.get(i)));
            Log.i("Audio", Integer.toString(audio.getStreamVolume(streamTypes.get(i))));
            if (savedVolumes.size() > 0 && savedVolumes.get(i) != -1 && i < volumeLevels.size() &&
                    volumeLevels.get(i) == audio.getStreamVolume(streamTypes.get(i))) {
                Log.i("Saved Volume ", Integer.toString(savedVolumes.get(i)));
                audio.setStreamVolume(streamTypes.get(i), savedVolumes.get(i), 0);
            }
        }
        savedVolumes.clear();
    }

}
