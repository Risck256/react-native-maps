package com.airbnb.android.react.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class CustomClusterRenderer extends DefaultClusterRenderer<Cluster> {

    private final Context mContext;
//    private String path = null;
    private String firstImage = null;
    private String secondImage = null;
    private IconGenerator mClusterIconGenerator;

    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<Cluster> clusterManager, ReadableMap path) {
        super(context, map, clusterManager);
        mContext = context;
        this.firstImage = path.getString("first");
        this.secondImage = path.getString("second");
    }


    @Override
    protected void onBeforeClusterRendered(final com.google.maps.android.clustering.Cluster<Cluster> cluster, final MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
        mClusterIconGenerator = new IconGenerator(this.mContext.getApplicationContext());
        LayoutInflater mLayoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    protected void onBeforeClusterItemRendered(Cluster item, final MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        String path = null;
        if (item.active_deals) {
            path = this.firstImage;
        } else {
            path = this.secondImage;
        }
        if (path.startsWith("http")) {
            Glide.with(mContext)
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
        int id = this.mContext.getResources().getIdentifier(path, "drawable", this.mContext.getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), id);
//        Bitmap bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), resourceDrawableIdHelper.getResourceDrawableId(this.context, uri))
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));

//        Bitmap b = loadBitmapByExternalURL("http://localhost:8081/assets/src/assets/images/icPinOfferte@3x.png?platform=android&hash=ad0302cccc3efc4ad4e2f82e88888be3");
//        final BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.fromBitmap(b);
//        markerOptions.icon(markerDescriptor).snippet(item.title);
//        Log.w("ciao", item.toString());
    }

    private Bitmap loadBitmapByExternalURL(String uri) {
        Bitmap bitmap = null;

        try {
            InputStream in = new URL(uri).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
