package com.airbnb.android.react.maps;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class Cluster implements ClusterItem {

    final String title;
    final LatLng latLng;
    final Boolean active_deals;

    public Cluster(LatLng latLng, String title, Boolean active_deals) {
        this.title = title;
        this.latLng = latLng;
        this.active_deals = active_deals;
    }

    @Override
    public LatLng getPosition() {
        return this.latLng;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    @Override
    public String getTitle() {
        return this.title;
    }


}
