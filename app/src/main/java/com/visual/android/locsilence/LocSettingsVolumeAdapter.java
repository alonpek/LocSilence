package com.visual.android.locsilence;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Thomas Brochard on 10/27/2017.
 */

public class LocSettingsVolumeAdapter extends ArrayAdapter<String> {

    private String[] volumeTypes;
    private List<Integer> volumeLevels;
    private int maxVolume;

    public LocSettingsVolumeAdapter(Context context, String[] volumeTypes, List<Integer> defaultVolumes, int maxVolume) {
        super(context, R.layout.item_loc_settings_volumes, volumeTypes);
        this.volumeTypes = volumeTypes;
        this.maxVolume = maxVolume;
        this.volumeLevels = defaultVolumes;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.item_loc_settings_volumes, parent, false);

        // Init info
        final TextView mVolumeTitle = (TextView) customView.findViewById(R.id.title_volumeType);
        mVolumeTitle.setText(getItem(position));
        final SeekBar mVolumeSeekBar = (SeekBar) customView.findViewById(R.id.seekBar_volume);
        final CheckBox mDefaultCheckBox = (CheckBox) customView.findViewById(R.id.check_default);

        // Set seekbar to default value
        mVolumeSeekBar.setMax(this.maxVolume);
        if (volumeLevels.get(position) >= 0) {
            mVolumeSeekBar.setProgress(volumeLevels.get(position));
        } else {
            mDefaultCheckBox.setChecked(true);
            mVolumeSeekBar.setEnabled(false);
        }

        // Init Listeners
        mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeLevels.set(position, progress);
                mDefaultCheckBox.setChecked(false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //TODO: Auto-generated stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //TODO: Auto-generated stub
            }
        });

        // Set checkbox and listener
        mDefaultCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDefaultCheckBox.isChecked()) {
                    volumeLevels.set(position, -1);
                    mVolumeSeekBar.setEnabled(false);
                } else {
                    mVolumeSeekBar.setEnabled(true);
                    volumeLevels.set(position, mVolumeSeekBar.getProgress());
                }
            }
        });
        return customView;
    }


    // returns an array representation of the current volume settings
    public List<Integer> getVolumeLevels() {
        return volumeLevels;
    }

    @Override
    public String getItem(int position) {
        return volumeTypes[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
