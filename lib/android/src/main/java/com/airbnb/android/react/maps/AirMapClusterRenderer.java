package com.airbnb.android.react.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class AirMapClusterRenderer extends DefaultClusterRenderer<ClusterUtils> {
    private Context context;
    private ReadableMap pathImages = null;

    public AirMapClusterRenderer(Context context, GoogleMap map, ClusterManager<ClusterUtils> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    public void setPathImage(ReadableMap pathImage) {
        this.pathImages = pathImage;
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<ClusterUtils> cluster) {
        return cluster.getSize() > 20;
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<ClusterUtils> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
        IconGenerator mClusterIconGenerator = new IconGenerator(this.context.getApplicationContext());
        LayoutInflater mLayoutInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mLayoutInflater.inflate(R.layout.layout_cluster, null, false);
        mClusterIconGenerator.setContentView(view);
        mClusterIconGenerator.setBackground(new ColorDrawable(Color.TRANSPARENT));
//        mClusterIconGenerator.setTextAppearance(R.style.AppTheme_WhiteTextAppearance);
        final Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
//        Bitmap bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), id);
//        Bitmap bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), resourceDrawableIdHelper.getResourceDrawableId(this.context, uri))
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterUtils item, final MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        if (this.pathImages != null && this.pathImages.getString(item.id) != null) {
            String path = this.pathImages.getString(item.id);
            if (path.startsWith("http")) {
                Glide.with(this.context)
                        .asBitmap()
                        .load(path)
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resource));
                            }
                        });
                return;
            }
            int id = this.context.getResources().getIdentifier(path, "drawable", this.context.getPackageName());
            Bitmap bitmap = BitmapFactory.decodeResource(this.context.getResources(), id);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    }
}
