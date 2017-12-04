package com.visual.android.locsilence;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas Brochard on 12/1/2017.
 */

public class JsonUtils {

    private static final String TAG = CustomProximityMap.class.getSimpleName();

    public static ArrayList<LatLng> customProxToList(String customProximityJson){
        Log.i("Custom Prox JSON", customProximityJson);
        if(customProximityJson == null)
            return null;
        try{
            Type listType = new TypeToken<ArrayList<LatLng>>() {}.getType();
            ArrayList<LatLng> boundary = new Gson().fromJson(customProximityJson, listType);
            Log.i("Boundary Size", " "+boundary.size());
            return boundary;
        }
        catch(JsonParseException e){
            Log.e(TAG, "Error parsing json custom proximity boundary\n" + e.getStackTrace());
            return null;
        }
    }

    public static List<Integer> volumeLevelsToList(String volumesLevelsJson){
        if(volumesLevelsJson == null) return null;
        try{
            Type listType = new TypeToken<List<Integer>>() {}.getType();
            List<Integer> volumeLevels = new Gson().fromJson(volumesLevelsJson, listType);
            return volumeLevels;
        }
        catch(JsonParseException e){
            Log.e(TAG, "Error parsing json custom proximity boundary\n" + e.getStackTrace());
            return null;
        }
    }

}
