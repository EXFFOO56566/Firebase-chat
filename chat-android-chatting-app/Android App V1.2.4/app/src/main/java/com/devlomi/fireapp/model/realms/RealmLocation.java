package com.devlomi.fireapp.model.realms;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.HashMap;

import io.realm.RealmObject;

/**
 * Created by Devlomi on 22/01/2018.
 */


public class RealmLocation extends RealmObject implements Parcelable {
    private double lat;
    private double lng;
    private String address;
    private String name;


    public RealmLocation(double lat, double lng, String address, String name) {
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.name = name;
    }

    public RealmLocation() {
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Exclude
    public LatLng getLatlng() {
        return new LatLng(lat, lng);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
        dest.writeString(this.address);
        dest.writeString(this.name);
    }

    protected RealmLocation(Parcel in) {
        this.lat = in.readDouble();
        this.lng = in.readDouble();
        this.address = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<RealmLocation> CREATOR = new Parcelable.Creator<RealmLocation>() {
        public RealmLocation createFromParcel(Parcel source) {
            return new RealmLocation(source);
        }

        public RealmLocation[] newArray(int size) {
            return new RealmLocation[size];
        }
    };

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> locationMap = new HashMap<>();
        locationMap.put("lat", lat);
        locationMap.put("lng", lng);
        locationMap.put("address", address);
        locationMap.put("name", name);
        return locationMap;
    }
}
