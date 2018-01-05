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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
    private LatLng selectedLatLng;
    private boolean customizingBoundary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loc_settings);
        Log.i(TAG, "LocSettingsActivity created");

        // Init info
        final SQLDatabaseHandler db = new SQLDatabaseHandler(this);
        selectedLocation = (Location) getIntent().getParcelableExtra("selectedLocation");
        final LatLng locCenter = new LatLng(selectedLocation.getLat(), selectedLocation.getLng());
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        LinearLayout volSettingsLayout = (LinearLayout) findViewById(R.id.volumeSettings_layout);
        final FloatingActionButton mGoToLocFab = (FloatingActionButton) findViewById(R.id.fab_go_to_selectedLoc);
        final Button mSetButton = (Button) findViewById(R.id.set_button);
        final Button mDeleteButton = (Button) findViewById(R.id.delete_button);
        final EditText mRadiusEditText = (EditText) findViewById(R.id.radius_editText);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_customProxy);
        mapFragment.getMapAsync(this);

        // Set basic ui
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(selectedLocation.getAddress());
        mToolbar.setSubtitle("LocSilence");

        // Create and set custom adapter of different volume type settings
        final LocSettingsVolumeAdapter locSettingsVolumeAdapter = new LocSettingsVolumeAdapter(this,
                selectedLocation.getVolumes(), audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        AdaptorToLinearLayout(volSettingsLayout, locSettingsVolumeAdapter);

        // Init Listeners
        mRadiusEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (customizingBoundary == false) {
                    mMap.clear();
                    if (boundary.isEmpty() == false) {
                        boundary.clear();
                    }
                    if (s.toString().equals("")) {
                        draw.drawCircle(mMap, locCenter, Constants.DEFAULT_RADIUS);
                        mMap.addMarker(new MarkerOptions().position(selectedLatLng).title(selectedLocation.getName()));
                    } else {
                        draw.drawCircle(mMap, locCenter, Integer.parseInt(s.toString()));
                        mMap.addMarker(new MarkerOptions().position(selectedLatLng).title(selectedLocation.getName()));
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //TODO: Auto-generated stub
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //TODO: Auto-generated stub
            }
        });

        mGoToLocFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraUpdate mCameraLocation = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(locCenter.latitude, locCenter.longitude), 14.5f);
                mMap.animateCamera(mCameraLocation);
            }
        });

        mSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> volumeLevels = locSettingsVolumeAdapter.getVolumeLevels();
                selectedLocation.setVolumes(volumeLevels);

                if(boundary.isEmpty() == false){
                    selectedLocation.setCustomProximity(boundary);
                }
                else if ((mRadiusEditText.getText().toString()).equals("")){
                    selectedLocation.setRadius(Constants.DEFAULT_RADIUS);
                }
                else{
                    selectedLocation.setRadius(Integer.parseInt(mRadiusEditText.getText().toString()));
                }

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
        this.mMap = googleMap;
        FloatingActionButton fabRevertPoint = (FloatingActionButton) findViewById(R.id.fab_revert_point);
        final EditText mRadiusEditText = (EditText) findViewById(R.id.radius_editText);

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

        double latitude = selectedLocation.getLat();
        double longitude = selectedLocation.getLng();

        this.selectedLatLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(selectedLatLng).title(selectedLocation.getName()));

        if(selectedLocation.getCustomProximity().isEmpty()) {
            draw.drawCircle(mMap, new LatLng(selectedLocation.getLat(), selectedLocation.getLng()), selectedLocation.getRadius());
        }
        else{
            draw.perimDraw(mMap, (ArrayList<LatLng>) selectedLocation.getCustomProximity());
        }
        customizingBoundary = false;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 14.5f));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                customizingBoundary = true;
                mRadiusEditText.setText("");
                if(boundary.isEmpty()){
                    mMap.clear();
                }
                if (boundary.size() < Constants.MAX_BOUNDARY_POINTS) {
                    boundary.add(point);
                    Log.i("TAG", "Point added to custom boundary. Boundary size: " + boundary.size());
                    if (boundary.size() >= 3) {
                        Log.i("TAG", "Drawing new perimeter");
                        mMap.clear();
                        draw.perimeterUpdate(mMap, boundary);
                    }
                    draw.pointDraw(mMap, point);
                } else {
                    Utility.alertToast(LocSettingsActivity.this, "Maximum 8 points");
                }
                customizingBoundary = false;
            }
        });

        fabRevertPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Point reverted from custom boundary settings. Current boundary points: " + boundary.size());
                if (mMap != null && boundary.isEmpty() == false) {
                    boundary.remove(boundary.size() - 1);
                    mMap.clear();
                    if (boundary.size() >= 3) {
                        draw.perimeterUpdate(mMap, boundary);
                    } else {
                        boundary.clear();
                        draw.drawCircle(mMap, new LatLng(selectedLocation.getLat(), selectedLocation.getLng()), Constants.DEFAULT_RADIUS);
                        mMap.addMarker(new MarkerOptions().position(selectedLatLng).title(selectedLocation.getName()));
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

    private void AdaptorToLinearLayout(LinearLayout layout, LocSettingsVolumeAdapter adapter) {
        LinearLayout vols = new LinearLayout(this);
        vols.setOrientation(LinearLayout.VERTICAL);
        for (int pos = 0; pos < adapter.getSize(); pos++) {
            vols.addView(adapter.getView(pos, null, null));
        }
        layout.addView(vols, 0);
    }
}