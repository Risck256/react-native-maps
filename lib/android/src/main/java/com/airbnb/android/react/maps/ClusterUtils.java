package com.airbnb.android.react.maps;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterUtils implements ClusterItem {
    final LatLng latLng;
    final String title;
    final String id;

    public ClusterUtils(LatLng latLng, String id, @Nullable String title) {
        this.latLng = latLng;
        this.id = id;
        this.title = title;
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
