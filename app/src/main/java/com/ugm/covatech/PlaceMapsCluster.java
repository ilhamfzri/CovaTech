package com.ugm.covatech;

import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class PlaceMapsCluster implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final String address;
    private final Float rating;
    private final String photoURL;


    public PlaceMapsCluster(double lat, double lng, String title, String snippet, String address, Float rating, String photoURL) {

        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
        this.address = address;
        this.rating = rating;
        this.photoURL = photoURL;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public String getAddress() {
        return address;
    }

    public Float getRating(){
        return rating;
    }

    public String getPhotoURL(){
        return photoURL;
    }
}
