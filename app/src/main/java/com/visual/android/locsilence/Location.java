package com.visual.android.locsilence;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by RamiK on 10/14/2017.
 */

public class Location implements Parcelable{

    private String id;
    private String name;
    private String address;
    private double lat;
    private double lng;
    private String createdAt;
    private String updatedAt;
    private String circleId;
    private int radius;
    private List<LatLng> customProximity;
    private List<Integer> volumes;

    public Location(){}

    public Location(String id, String name, String address, double lat, double lng,
                    String createdAt, String updatedAt, String circleId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.volumes = new ArrayList<Integer>(Arrays.asList(0,0,0));
        this.circleId = circleId;
        this.radius = 100;
        this.customProximity = new ArrayList<LatLng>();
    }

    // default parcel constructor
    public Location(Parcel parcel) {
        this.id = parcel.readString();
        this.name = parcel.readString();
        this.address = parcel.readString();
        this.lat = parcel.readDouble();
        this.lng = parcel.readDouble();
        this.createdAt = parcel.readString();
        this.updatedAt = parcel.readString();
        this.circleId = parcel.readString();
        this.radius = parcel.readInt();
        this.customProximity = parcel.createTypedArrayList(LatLng.CREATOR);
        this.volumes = (List<Integer>) parcel.readSerializable();
    }


    @Override
    public void writeToParcel(Parcel parcel, int args){
        parcel.writeString(this.id);
        parcel.writeString(this.name);
        parcel.writeString(this.address);
        parcel.writeDouble(this.lat);
        parcel.writeDouble(this.lng);
        parcel.writeString(this.createdAt);
        parcel.writeString(this.updatedAt);
        parcel.writeString(this.circleId);
        parcel.writeInt(this.radius);
        parcel.writeTypedList(this.customProximity);
        parcel.writeSerializable((Serializable) this.volumes);
    }


    public static final Creator<Location> CREATOR=new Creator<Location>(){
        @Override
        public Location createFromParcel(Parcel parcel){
            return new Location(parcel);
        }
        @Override
        public Location[] newArray(int i) {
            return new Location[i];
        }
    };
    @Override
    public int describeContents(){
        return 0;
    }

    // GET methods
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress(){ return address; }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public List<Integer> getVolumes(){ return  this.volumes; }

    public String getCircleId() { return circleId; }

    public int getRadius() { return radius; }

    public List<LatLng> getCustomProximity(){ return customProximity; }

    // SET methods

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address){ this.address = address; }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setVolumes(List<Integer> volumes){ this.volumes = volumes; };

    public void setCircleId(String circleId) { this.circleId = circleId; }

    public void setRadius(int radius) { this.radius = radius; }

    public void setCustomProximity(List<LatLng> customProximity){ this.customProximity = customProximity; }

    public void printLocation(){
        Log.i("logDB", "Location: (name: " + this.getName() + ") | " +
                "(Address: " + this.getAddress() + ") | " +
                "(LatLong: " + this.getLat() + ":" + this.getLng() + ") |" +
                "(Volumes: " + this.getVolumes().toString() + ") |" +
                "(ID: " + this.getId() + ") | " + "(Cid: " + this.getCircleId() + ") |" +
                "(Radius: " + this.getRadius() + ") |" + "(customProx: " + this.getCustomProximity() + ") |"
        );
    }
}
