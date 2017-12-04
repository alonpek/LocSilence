package com.visual.android.locsilence;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.google.android.gms.location.places.Place;

import java.util.Date;
import java.util.List;


public class SavedLocActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_loc);

        // Init info
        final SQLDatabaseHandler db = new SQLDatabaseHandler(this);
        final List<Location> locations = db.getAllLocations();
        ListView listView = (ListView) findViewById(R.id.listview_savedLocs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.list_toolbar);
        Button mMapButton = (Button) findViewById(R.id.mapButton);

        // Set Basic ui
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Saved Locations");
        toolbar.setSubtitle("LocSilence");

        final SavedLocAdapter savedLocAdapter = new SavedLocAdapter(this, locations, db);
        listView.setAdapter(savedLocAdapter);

        savedLocAdapter.notifyDataSetChanged();

        EditText searchBar = (EditText)findViewById(R.id.search);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //TODO: Auto-generated stub
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                savedLocAdapter.updateLocations(db.getAllLocations(charSequence.toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //TODO: Auto-generated stub
            }
        });

        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SavedLocActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);

            }
        });
    }

    // Return either a location object if 'place' already exists in db, else a new location object
    public Location getSelectedLocation(Place place, SQLDatabaseHandler db) {        // If place is in db already update location info in db
        Location selectedLocation = null;
        if (db.locationInDB(place.getId())) {
            selectedLocation = db.getLocation(place.getId());
        }
        // If place is new set basic new locations
        else if(db.getSize()<Constants.MAX_DB_SIZE){
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
        return selectedLocation;
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
                Intent intent = new Intent(SavedLocActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void next_page(View v) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }


}



