package com.airbnb.android.react.maps;

import android.content.Context;
import android.util.Log;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nullable;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AirMapWMSTile extends AirMapFeature {
  private static final double[] mapBound = {-20037508.34789244, 20037508.34789244};
  private static final double FULL = 20037508.34789244 * 2;

    class AIRMapGSUrlTileProvider extends UrlTileProvider {
    private String urlTemplate;
    private int width;
    private int height;
    public AIRMapGSUrlTileProvider(int width, int height, String urlTemplate) {
      super(width, height);
      this.urlTemplate = urlTemplate;
      this.width = width;
      this.height = height;
    }

    @Override
    public synchronized URL getTileUrl(int x, int y, int zoom) {
      if(AirMapWMSTile.this.maximumZ > 0 && zoom > maximumZ) {
          return null;
      }
      if(AirMapWMSTile.this.minimumZ > 0 && zoom < minimumZ) {
          return null;
      }
      double[] bb = getBoundingBox(x, y, zoom);
      String s = this.urlTemplate
          .replace("{minX}", Double.toString(bb[0]))
          .replace("{minY}", Double.toString(bb[1]))
          .replace("{maxX}", Double.toString(bb[2]))
          .replace("{maxY}", Double.toString(bb[3]))
          .replace("{width}", Integer.toString(width))
          .replace("{height}", Integer.toString(height));
      URL url = null;
      try {
        url = new URL(s);
      } catch (MalformedURLException e) {
        throw new AssertionError(e);
      }
      return url;
    }

    private double[] getBoundingBox(int x, int y, int zoom) {
      double tile = FULL / Math.pow(2, zoom);
      return new double[]{
              mapBound[0] + x * tile,
              mapBound[1] - (y + 1) * tile,
              mapBound[0] + (x + 1) * tile,
              mapBound[1] - y * tile
      };
    }

    public void setUrlTemplate(String urlTemplate) {
      this.urlTemplate = urlTemplate;
    }
  }

  public class AIRMapGSUrlTileProviderHeaders implements TileProvider {
    private String urlTemplate;
    private int width;
    private int height;
    private OkHttpClient httpClient;
    private Headers headers;

    public AIRMapGSUrlTileProviderHeaders(int width, int height, String urlTemplate, ReadableMap h) {
      this.urlTemplate = urlTemplate;
      this.width = width;
      this.height = height;
      Headers.Builder headersBuilder = new Headers.Builder();
      ReadableMapKeySetIterator iterator = h.keySetIterator();
      while (iterator.hasNextKey()) {
        String key = iterator.nextKey();
        if (h.getType(key) == ReadableType.String) {
          headersBuilder.add(key, h.getString(key));
        }
      }
      headers = headersBuilder.build();
      this.httpClient = new OkHttpClient();
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
      double[] bb = getBoundingBox(x, y, zoom);
      String s = this.urlTemplate
              .replace("{minX}", Double.toString(bb[0]))
              .replace("{minY}", Double.toString(bb[1]))
              .replace("{maxX}", Double.toString(bb[2]))
              .replace("{maxY}", Double.toString(bb[3]))
              .replace("{width}", Integer.toString(width))
              .replace("{height}", Integer.toString(height));
      URL url;
      try {
        url = new URL(s);
        final byte[] data = downloadData(url);
        if (data != null) {
          return new Tile(this.width, this.height, data);
        }
      } catch (MalformedURLException e) {
        return null;
      }
      return null;
    }

    private byte[] downloadData(URL url) {
      Request request = new Request.Builder()
              .headers(this.headers)
              .url(url)
              .build();
      try {
        Response response = httpClient.newCall(request).execute();
        return response.body().bytes();
      } catch (IOException e) {
        return null;
      }
    }

    private double[] getBoundingBox(int x, int y, int zoom) {
      double tile = FULL / Math.pow(2, zoom);
      return new double[]{
              mapBound[0] + x * tile,
              mapBound[1] - (y + 1) * tile,
              mapBound[0] + (x + 1) * tile,
              mapBound[1] - y * tile
      };
    }

    public void setUrlTemplate(String urlTemplate) {
      this.urlTemplate = urlTemplate;
    }
  }

  private TileOverlayOptions tileOverlayOptions;
  private TileOverlay tileOverlay;
  private AIRMapGSUrlTileProvider tileProvider;

  private String urlTemplate;
  private float zIndex;
  private float maximumZ;
  private float minimumZ;
  private int tileSize;
  private float opacity;

  public AirMapWMSTile(Context context) {
    super(context);
  }

  public void setUrlTemplate(String urlTemplate) {
    this.urlTemplate = urlTemplate;
    if (tileProvider != null) {
      tileProvider.setUrlTemplate(urlTemplate);
    }
    if (tileOverlay != null) {
      tileOverlay.clearTileCache();
    }
  }

  public void setHeaders(ReadableMap headers) {
    this.headers = headers;
  }

  public void setZIndex(float zIndex) {
    this.zIndex = zIndex;
    if (tileOverlay != null) {
      tileOverlay.setZIndex(zIndex);
    }
  }

  public void setMaximumZ(float maximumZ) {
    this.maximumZ = maximumZ;
    if (tileOverlay != null) {
      tileOverlay.clearTileCache();
    }
  }

  public void setMinimumZ(float minimumZ) {
    this.minimumZ = minimumZ;
    if (tileOverlay != null) {
      tileOverlay.clearTileCache();
    }
  }
  public void setTileSize(int tileSize) {
    this.tileSize = tileSize;
    if (tileOverlay != null) {
      tileOverlay.clearTileCache();
    }
  }
  public void setOpacity(float opacity) {
    this.opacity = opacity;
    if (tileOverlay != null) {
        tileOverlay.setTransparency(1-opacity);
    }
  }

  public TileOverlayOptions getTileOverlayOptions() {
    if (tileOverlayOptions == null) {
      tileOverlayOptions = createTileOverlayOptions();
    }
    return tileOverlayOptions;
  }

  private TileOverlayOptions createTileOverlayOptions() {
    TileOverlayOptions options = new TileOverlayOptions();
    options.zIndex(zIndex);
    options.transparency(1-opacity);
    this.tileProvider = new AIRMapGSUrlTileProvider(this.tileSize, this.tileSize, this.urlTemplate);
    options.tileProvider(this.tileProvider);
    return options;
  }

  @Override
  public Object getFeature() {
    return tileOverlay;
  }

  @Override
  public void addToMap(GoogleMap map) {
    this.tileOverlay = map.addTileOverlay(getTileOverlayOptions());
  }

  @Override
  public void removeFromMap(GoogleMap map) {
    tileOverlay.remove();
  }
}
