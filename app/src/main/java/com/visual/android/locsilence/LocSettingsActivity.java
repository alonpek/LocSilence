package com.visual.android.locsilence;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;

import java.util.List;

public class LocSettingsActivity extends AppCompatActivity {

    private static final String TAG = LocSettingsActivity.class.getSimpleName();
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
        final EditText mGeneralProximity = (EditText) findViewById(R.id.genericProxy_editText);
        final CheckBox mCustomProximity = (CheckBox) findViewById(R.id.customProx_checkBox);

        // Set basic ui
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(selectedLocation.getAddress());
        mToolbar.setSubtitle("LocSilence");
        if (!selectedLocation.getCustomProximity().equals(Constants.JSON_NULL)) {
            mCustomProximity.setChecked(true);
        }


        // Create and set custom adapter of different volume type settings
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final LocSettingsVolumeAdapter locSettingsVolumeAdapter = new LocSettingsVolumeAdapter(this, volumeTypes,
                JsonUtils.volumeLevelsToList(selectedLocation.getVolumes()),
                audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        ListView settingsListView = (ListView) findViewById(R.id.settings_listview);
        settingsListView.setAdapter(locSettingsVolumeAdapter);


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
                        //s.replace(0, s.length(), "300", 0, 3);
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

        mCustomProximity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedLocation.setCustomProximity(new Gson().toJson(null));
                if (mCustomProximity.isChecked()) {
                    mGeneralProximity.setText("");
                    Intent customProxIntent = new Intent(LocSettingsActivity.this, CustomProximityMap.class);
                    customProxIntent.putExtra("selectedLocation", selectedLocation);
                    startActivity(customProxIntent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
                }
                else{
                    selectedLocation.setCustomProximity(Constants.JSON_NULL);
                }
            }
        });


        mSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> volumeLevels = locSettingsVolumeAdapter.getVolumeLevels();
                selectedLocation.setVolumes(new Gson().toJson(volumeLevels));
                if (mCustomProximity.isChecked()) {
                    // temporary value until we fix the radius/customProx in the recursive task and can set it to -1
                    selectedLocation.setRadius(1);
                } else if ((mGeneralProximity.getText().toString()).equals("")) {
                    selectedLocation.setRadius(Constants.DEFAULT_RADIUS);
                } else {
                    selectedLocation.setRadius(Integer.parseInt(mGeneralProximity.getText().toString()));
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
}