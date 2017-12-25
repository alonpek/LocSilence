package com.visual.android.locsilence;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class LocSettingsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = LocSettingsActivity.class.getSimpleName();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private Graphics draw = new Graphics();
    private GoogleMap mMap;
    private ArrayList<LatLng> boundary = new ArrayList<LatLng>();
    private String[] volumeTypes = {"Ringtone", "Notifications", "Alarms"};
    private Location selectedLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loc_settings);

        // Init info
        final SQLDatabaseHandler db = new SQLDatabaseHandler(this);
        selectedLocation = (Location) getIntent().getParcelableExtra("selectedLocation");
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        final Button mSetButton = (Button) findViewById(R.id.set_button);
        final Button mDeleteButton = (Button) findViewById(R.id.delete_button);
        //final EditText mGeneralProximity = (EditText) findViewById(R.id.genericProxy_editText);
        //final CheckBox mCustomProximity = (CheckBox) findViewById(R.id.customProx_checkBox);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_customProxy);
        mapFragment.getMapAsync(this);
*/
        // Set basic ui
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(selectedLocation.getAddress());
        mToolbar.setSubtitle("LocSilence");

        // Create and set custom adapter of different volume type settings
        final LocSettingsVolumeAdapter locSettingsVolumeAdapter = new LocSettingsVolumeAdapter(this,
                selectedLocation.getVolumes(), audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        LinearLayout volSettingsListView = (LinearLayout) findViewById(R.id.volumeSettings_listview);
        LinearLayout vols = new LinearLayout(this);
        vols.setOrientation(LinearLayout.VERTICAL);
        for(int pos=0; pos<locSettingsVolumeAdapter.getSize(); pos++){
            Log.i("halp", "getting view: " + locSettingsVolumeAdapter.getItem(pos));
            vols.addView(locSettingsVolumeAdapter.getView(pos, null, null));
        }
        volSettingsListView.addView(vols, 0);
/*
        // Init Listeners
        mGeneralProximity.addTextChangedListener(new TextWatcher() {
            // editingText flag used for preventing infinite recursive loop
            boolean editingText = false;
            public void afterTextChanged(Editable s) {
                String proximityString = mGeneralProximity.getText().toString();
                if (!proximityString.equals("") && editingText == false) {
                    int proximity = Integer.parseInt(proximityString);
                    editingText = true;
                    if (proximity > 300) {
                        mGeneralProximity.setText("");
                        mGeneralProximity.setHint(" 300 max");
                    } else if (proximity < 1) {
                        s.replace(0, s.length(), "1", 0, 1);
                    }
                    editingText = false;
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //TODO: Auto-generated stub
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mCustomProximity.isChecked()) {
                    mCustomProximity.setChecked(false);
                }
            }
        });
*/

        mSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> volumeLevels = locSettingsVolumeAdapter.getVolumeLevels();
                selectedLocation.setVolumes(volumeLevels);

                if (db.getLocation(selectedLocation.getId()) == null) {
                    db.addLocation(selectedLocation);
                } else {
                    db.updateLocalGame(selectedLocation);
                }
                db.close();
                startActivity(new Intent(LocSettingsActivity.this, MapsActivity.class));
                finish();
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (db.getLocation(selectedLocation.getId()) != null) {
                    db.deleteLocalGame(selectedLocation.getId());
                }
                db.close();
                startActivity(new Intent(LocSettingsActivity.this, MapsActivity.class));
                finish();
            }
        });
    }

    // Manipulates the map once available.
    // This callback is triggered when the map is ready to be used.
    // This is where we can add markers or lines, add listeners or move the camera. In this case,
    // we just add a marker near Sydney, Australia.
    // If Google Play services is not installed on the device, the user will be prompted to install
    // it inside the SupportMapFragment. This method will only be triggered once the user has
    // installed Google Play services and returned to the app.
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
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mMap.getUiSettings().setCompassEnabled(true);
                }
            }
        }

/*        double latitude;
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
                    Utility.alertToast(LocSettingsActivity.this, "Maximum 8 points");
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
        });*/
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
                                ActivityCompat.requestPermissions(LocSettingsActivity.this,
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