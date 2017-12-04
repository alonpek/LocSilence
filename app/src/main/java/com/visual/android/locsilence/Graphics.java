package com.visual.android.locsilence;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ErikL on 10/19/2017.
 */

public class Graphics extends AppCompatActivity {
    public static boolean setRadius = false;
    private static double PI = 3.14159265;
    //On Start drawing the circles for stored locations
    public SQLDatabaseHandler startDraw(GoogleMap map, SQLDatabaseHandler handler) {
        List<Location> allLocations = handler.getAllLocations();
        //iterates through database and draws the circles
        for (Location location : allLocations) {
            LatLng center = new LatLng(location.getLat(), location.getLng());
            if(location.getRadius() < 1){
                Log.e("GSON","The Gson is the problem");
                perimDraw(map,JsonUtils.customProxToList(location.getCustomProximity()));
            }
            else {
                CircleOptions opt = new CircleOptions().center(center).radius(location.getRadius()).strokeColor(Color.BLACK).fillColor(0x88FF6800).clickable(true);
                final Circle circle = map.addCircle(opt);
                map.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
                    @Override
                    public void onCircleClick(Circle circle) {
                        setRadius = true;
                        Log.e("circle clicked", "Circle " + circle.getId() + " was clicked");
                    }
                });
                location.setCircleId(circle.getId());
            }
            handler.updateLocalGame(location);
        }
        return handler;
    }

    public static boolean getIfClicked() {
        return setRadius;
    }

    public static void setIfClicked() {
        setRadius = false;
    }

    public static boolean customInLocation(Location current , LatLng cur){
        ArrayList<LatLng> intermediary = JsonUtils.customProxToList(current.getCustomProximity());
        double target = 360;
        double latmax = intermediary.get(0).latitude;
        double lngmax = intermediary.get(0).longitude;
        double latmin = intermediary.get(0).latitude;
        double lngmin = intermediary.get(0).longitude;
        boolean decision = true;
        for(int x =1; x<intermediary.size();x++){
            if(intermediary.get(x).longitude > lngmax){
                lngmax=intermediary.get(x).longitude;
            }
            if(intermediary.get(x).longitude < lngmin){
                lngmin=intermediary.get(x).longitude;
            }
            if(intermediary.get(x).latitude > latmax){
                latmax=intermediary.get(x).latitude;
            }
            if(intermediary.get(x).latitude < latmin){
                latmin=intermediary.get(x).latitude;
            }
        }

        if(cur.latitude > latmax || cur.latitude < latmin || cur.longitude > lngmax || cur.longitude < lngmin ){
            return false;
        }
       /* else {
            double angletotal = 0;
            for (int x = 0; x < intermediary.size(); x++) {
                LatLng tmp = intermediary.get(x);
                if (x == intermediary.size() - 1) {
                    LatLng tmp2 = intermediary.get(0);
                    angletotal += ((Math.atan2((tmp.longitude - cur.longitude), (tmp.latitude - cur.latitude))) -
                            (Math.atan2((tmp2.longitude - cur.longitude), (tmp2.latitude - cur.latitude)))*(180/(PI)) + 180) % 180;
                } else {
                    LatLng tmp2 = intermediary.get(x + 1);
                    angletotal += ((Math.atan2((tmp.longitude - cur.longitude), (tmp.latitude - cur.latitude))) -
                            (Math.atan2((tmp2.longitude - cur.longitude), (tmp2.latitude - cur.latitude)))*(180/(PI)) + 180) % 180;
                }
            }
            if (angletotal == target) {
                decision = true;
            }
        }*/
        return decision;
    }

    public void pointDraw(GoogleMap map, LatLng center) {
        CircleOptions circleOptions = new CircleOptions().center(center).radius(15).strokeColor(Color.BLACK).fillColor(Color.BLACK);
        final Circle circle = map.addCircle(circleOptions);
    }

    public void perimeterDraw(GoogleMap map, ArrayList<LatLng> points) {
        PolygonOptions polygonOptions = new PolygonOptions().strokeColor(Color.BLACK).fillColor(0x88FF6800);
        for (LatLng point : points) {
            polygonOptions.add(point);
        }
        map.clear();
        final Polygon polygon = map.addPolygon(polygonOptions);
    }
    public static void perimDraw(GoogleMap map, ArrayList<LatLng> points) {
        PolygonOptions polygonOptions = new PolygonOptions().strokeColor(Color.BLACK).fillColor(0x88FF6800);
        for (LatLng point : points) {
            polygonOptions.add(point);
        }
        final Polygon polygon = map.addPolygon(polygonOptions);
    }
}
