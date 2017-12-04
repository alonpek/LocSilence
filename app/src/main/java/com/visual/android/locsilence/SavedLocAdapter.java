package com.visual.android.locsilence;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by RamiK on 10/23/2017.
 */

public class SavedLocAdapter extends ArrayAdapter<Location> {

    private List<Location> locations;
    private Context context;
    private SQLDatabaseHandler db;

    public SavedLocAdapter(Context context, List<Location> locations, SQLDatabaseHandler db) {
        super(context, 0, locations);
        this.locations = locations;
        this.context = context;
        this.db = db;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_location, parent, false);
        }

        final Location location = locations.get(position);

        if (location != null) {
            // Init info
            final TextView locationName = (TextView) convertView.findViewById(R.id.name);
            final TextView locationAddress = (TextView) convertView.findViewById(R.id.address);
            final Button mEditButton = (Button) convertView.findViewById(R.id.edit_button);
            final Button mDeleteButton = (Button) convertView.findViewById(R.id.delete_button);

            // Set basic ui
            locationName.setText(location.getName());
            locationAddress.setText(location.getAddress());

            mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent settingsIntent = new Intent(context, LocSettingsActivity.class);
                    settingsIntent.putExtra("editing", true);
                    settingsIntent.putExtra("selectedLocation", location);
                    context.startActivity(settingsIntent);
                }
            });

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (db.getLocation(location.getId()) != null) {
                        db.deleteLocalGame(location.getId());
                    }
                    locations.remove(position);
                    notifyDataSetChanged();
                }
            });
        }

        return convertView;
    }


    public Location getItem(int position) {
        return locations.get(position);
    }


    public void updateLocations(List<Location> locations) {
        this.locations.clear();
        this.locations.addAll(locations);
        notifyDataSetChanged();
    }
}
