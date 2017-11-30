package com.visual.android.locsilence;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by ErikL on 10/19/2017.
 */

public class Graphics extends AppCompatActivity {
    public static String ids = null;
    //On Start drawing the circles for stored locations
    public SQLDatabaseHandler startDraw(GoogleMap map, SQLDatabaseHandler handler){
        List<Location> enslavingall = handler.getAllLocations();
        //iterates through database and draws the circles
        for (int x = 0; x < enslavingall.size(); x++) {
            Location cur = enslavingall.get(x);
            Integer tmp = cur.getRad();
            Log.e("rad",tmp.toString());
            LatLng cent = new LatLng(cur.getLat(),cur.getLng());
            CircleOptions opt = new CircleOptions().center(cent).radius(100).strokeColor(Color.BLACK).fillColor(0x88FF0000).clickable(true);
            final Circle circle = map.addCircle(opt);
           /* map.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
                @Override
                public void onCircleClick(Circle circle) {
                    Log.e("circle clicked", "Circle " + circle.getId() + " was clicked");
                }
            });*/
            cur.setCid(circle.getId());
            //cur.setRad(100);
            handler.updateLocalGame(cur);
        }
        return handler;
    }

    // when a new location is added then newLocDraw needs to be called to add the location(for if we add adding locations on map activity)
  
    public void newLocDraw(GoogleMap map,Location loc){
        Circle circle = map.addCircle(new CircleOptions().center(new LatLng(loc.getLat(),loc.getLng())).radius(100).strokeColor(Color.BLACK).fillColor(0x88FF0000));
        SQLDatabaseHandler handler = new SQLDatabaseHandler(this);
        circle.setClickable(true);
        loc.setCid(circle.getId());
        handler.updateLocalGame(loc);
    }

    //On Click radius changing Method to do so now valid
    public void radChange(GoogleMap map,String id,int nrad){
        SQLDatabaseHandler handler = new SQLDatabaseHandler(this);
        map.clear();
        Location nloc = handler.getLocation(id);
        nloc.setRad(nrad);
        handler.updateLocalGame(nloc);
        startDraw(map, handler);
    }

    //Perimeter outlining (Work in Progress) LatLngBounds promising to help
    public void perimeter(GoogleMap map){
        SQLDatabaseHandler handler = new SQLDatabaseHandler(this);
        List<Location> enslavingall = handler.getAllLocations();
        for (int x = 0; x < enslavingall.size(); x++) {

        }
    }

        //LatLng spot = new LatLng(2.2,2.2);
    }

