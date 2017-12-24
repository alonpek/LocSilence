package com.visual.android.locsilence;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private Graphics draw = new Graphics();
    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    final SQLDatabaseHandler db = new SQLDatabaseHandler(this);
    PlaceAutocompleteFragment autocompleteFragment;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Map");
        actionBar.setSubtitle("LocSilence");
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        Button mMapButton = (Button) findViewById(R.id.mapButton);
        Button mLocationsButton = (Button) findViewById(R.id.locButton);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        mMapButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        mLocationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, SavedLocActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(db.getSize()<Constants.MAX_DB_SIZE) {
                    addLocationAction();
                }
                else{
                    Utility.alertToast(MapsActivity.this, "saved locations is limited to " + Constants.MAX_DB_SIZE);
                }
            }
        });

    }

    public void addLocationAction(){
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());

                Location selectedLocation = getSelectedLocation(place, db);
                if(selectedLocation != null) {
                    Intent settingsIntent = new Intent(MapsActivity.this, LocSettingsActivity.class);
                    settingsIntent.putExtra("selectedLocation", selectedLocation);
                    startActivity(settingsIntent);
                }
                else{
                    Utility.alertToast(MapsActivity.this, "saved locations is limited to " + Constants.MAX_DB_SIZE);
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.setting_id:
                //Go to settings activity
                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // Return either a location object if 'place' already exists in db, else a new location object
    public Location getSelectedLocation(Place place, SQLDatabaseHandler db){
        // If place is in db already update location info in db
        Location selectedLocation;
        if (db.locationInDB(place.getId())) {
            selectedLocation = db.getLocation(place.getId());
        }
        // If place is new set basic new locations
        else {
            selectedLocation = new Location(
                    place.getId(),
                    place.getName().toString(),
                    place.getAddress().toString(),
                    (float) place.getLatLng().latitude,
                    (float) place.getLatLng().longitude,
                    new Date().toString(),
                    new Date().toString(),
                    "");
        }
        selectedLocation.printLocation();
        return selectedLocation;
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

        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                }
            }
        }

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        android.location.Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));

        double latitude;
        double longitude;
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        else {
            latitude = Constants.DEFAULT_LAT;
            longitude = Constants.DEFAULT_LONG;
        }

        if (!isNotificationPolicyAccessGranted()) {
            requestNotificationPolicyAccess();
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 14.5f));

        SQLDatabaseHandler db = new SQLDatabaseHandler(this);
        if (db.getLocalGamesCount() > 0) {
           db = draw.startDraw(mMap, db);
        }
        // Obtain all locations
        List<Location> locations = db.getAllLocations();

        for (Location Savedlocation : locations) {
            Log.i("Marker Maker", "Hit");
            Log.i("Current Lat", Double.toString(Savedlocation.getLat()));
            Log.i("Current Long", Double.toString(Savedlocation.getLng()));
            LatLng loc = new LatLng(Savedlocation.getLat(), Savedlocation.getLng());
            mMap.addMarker(new MarkerOptions().position(loc).title(Savedlocation.getName()));
        }

        Utility.recursiveSilencePhoneTask = new RecursiveSilencePhoneTask(locationManager, db, this);
        Utility.recursiveSilencePhoneTask.execute(locationManager);
    }


    private boolean isNotificationPolicyAccessGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            NotificationManager notificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager.isNotificationPolicyAccessGranted();
        }
        return true;
    }


    private void requestNotificationPolicyAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.
                    ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
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
                                ActivityCompat.requestPermissions(MapsActivity.this,
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


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    System.exit(1);
                }
                return;
            }
        }
    }
}
