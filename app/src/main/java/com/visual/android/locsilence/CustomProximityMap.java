package com.visual.android.locsilence;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import java.util.ArrayList;


public class CustomProximityMap extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = CustomProximityMap.class.getSimpleName();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private ArrayList<LatLng> boundary = new ArrayList<LatLng>();
    private Graphics draw = new Graphics();
    private GoogleMap mMap;
    private Location selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_proximity_map);

        // Init info
        selectedLocation = getIntent().getParcelableExtra("selectedLocation");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_go_to_selectedLoc);
        Button mBackButton = (Button) findViewById(R.id.button_customProxy_back);
        Button mSetButton = (Button) findViewById(R.id.button_customProxy_set);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_customProxy);
        mapFragment.getMapAsync(this);

        // Init listeners
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    if (checkLocationPermission()) {
                        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            if (mMap != null) {
                                mMap.setMyLocationEnabled(true);
                                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                            }
                        }
                    }

                    double latitude;
                    double longitude;
                    if (selectedLocation != null) {
                        latitude = selectedLocation.getLat();
                        longitude = selectedLocation.getLng();
                    } else {
                        latitude = Constants.DEFAULT_LAT;
                        longitude = Constants.DEFAULT_LONG;
                    }

                    CameraUpdate mCameraLocation = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(latitude, longitude), 14.5f);

                    mMap.animateCamera(mCameraLocation);
                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent customSettingsIntent = new Intent(CustomProximityMap.this, LocSettingsActivity.class);
                customSettingsIntent.putExtra("selectedLocation", selectedLocation);
                startActivity(customSettingsIntent);
                finish();
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });

        mSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(boundary.size() < 3) {
                    Utility.alertToast(CustomProximityMap.this, "Need to set at least 3 points");
                }
                else {
                    String customProximityJSON = new Gson().toJson(boundary);
                    selectedLocation.setCustomProximity(customProximityJSON);
                    selectedLocation.setRadius(-1);
                    Intent customSettingsIntent = new Intent(CustomProximityMap.this, LocSettingsActivity.class);
                    customSettingsIntent.putExtra("selectedLocation", selectedLocation);
                    startActivity(customSettingsIntent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                }
            }
        });


    }

    /**
     * `     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        FloatingActionButton fabRevertPoint = (FloatingActionButton) findViewById(R.id.fab_revert_point);

        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    mMap.getUiSettings().setCompassEnabled(true);
                }
            }
        }

        double latitude;
        double longitude;
        if (selectedLocation != null) {
            latitude = selectedLocation.getLat();
            longitude = selectedLocation.getLng();
        } else {
            latitude = Constants.DEFAULT_LAT;
            longitude = Constants.DEFAULT_LONG;
        }

        LatLng loc = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(loc).title(selectedLocation.getName()));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 14.5f));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                int MAX_POINTS = 8;
                if(boundary.size() < MAX_POINTS){
                    boundary.add(point);
                    Log.i(TAG, "Point added to custom proximity boundary["+(boundary.size()-1)+"]");
                    if(boundary.size() >=3){
                        draw.perimeterDraw(mMap,boundary);
                    }
                    draw.pointDraw(mMap,point);
                }
                else{
                    Utility.alertToast(CustomProximityMap.this, "Maximum 8 points");
                }

            }
        });

        fabRevertPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    boundary.remove(boundary.size()-1);
                    Log.i(TAG, "Point added to custom proximity boundary["+(boundary.size()-1)+"]");
                    mMap.clear();
                    if(boundary.size() >= 3){
                        draw.perimeterDraw(mMap,boundary);
                    }
                    else{
                        boundary.clear();
                    }
                }
            }
        });

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(CustomProximityMap.this,
                                        new String[]{
                                                Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


}
