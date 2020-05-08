package cordova.mapboxplugin;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.FillManager;
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.mapboxsdk.utils.ColorUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.squareup.picasso.Picasso;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import app.rechargeit.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * This class echoes a string called from JavaScript.
 */
public class Mapboxplugin extends CordovaPlugin {
    private static final String TAG = "Mapboxplugin";
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_REQ_CODE = 0;

    public static final int PERMISSION_DENIED_ERROR = 20;

    private static final String MAPBOX_ACCESSTOKEN_RESOURCE_KEY = "mapbox_access_token";

    private static final String ACTION_SHOW = "show";
    private static final String ACTION_REMOVE = "remove";
    private static final String ACTION_ADD_MARKERS = "addMarkers";
    private static final String ACTION_REMOVE_ALL_MARKERS = "removeAllMarkers";
    private static final String ACTION_ADD_MARKER_CALLBACK = "addMarkerCallback";
    // TODO:
    // private static final String ACTION_REMOVE_MARKER_CALLBACK = "removeMarkerCallback";
    private static final String ACTION_ADD_POLYGON = "addPolygon";
    private static final String ACTION_ADD_GEOJSON = "addGeoJSON";
    private static final String ACTION_GET_CENTER = "getCenter";
    private static final String ACTION_SET_CENTER = "setCenter";
    private static final String ACTION_GET_ZOOMLEVEL = "getZoomLevel";
    private static final String ACTION_SET_ZOOMLEVEL = "setZoomLevel";
    private static final String ACTION_GET_BOUNDS = "getBounds";
    private static final String ACTION_SET_BOUNDS = "setBounds";
    private static final String ACTION_GET_TILT = "getTilt";
    private static final String ACTION_SET_TILT = "setTilt";
    private static final String ACTION_ANIMATE_CAMERA = "animateCamera";
    private static final String ACTION_ON_REGION_WILL_CHANGE = "onRegionWillChange";
    private static final String ACTION_ON_REGION_IS_CHANGING = "onRegionIsChanging";
    private static final String ACTION_ON_REGION_DID_CHANGE = "onRegionDidChange";
    private static final String ACTION_CONVERT_COORDINATES = "convertCoordinate";
    private static final String ACTION_CONVERT_POINTS = "convertPoint";
    private static final String ADD_CUSTOM_BUTTON = "addCustomButton";
    private static final String EDIT_CUSTOM_BUTTON = "editCustomButton";
    private static final String REOMOVE_CUSTOM_BUTTON = "removeCustomButton";
    private static final String ADD_CUSTOM_BUTTON_CALL_BACK = "addCustomButtonCallback";
    private static float retinaFactor;
    private String accessToken;
    private CallbackContext callback;
    private CallbackContext markerCallbackContext;
    private FrameLayout layout;
    private View topLayer;
    private View bottomLayer;
    private static final String ID_ICON_1 = "friends";
    private static final String ID_ICON_2 = "kiosk";
    private static final String ID_ICON_3 = "mylocation";
    private static final String ID_ICON_4 = "store";
    private static final String ID_ICON_5 = "Kiosk-green";
    private static final String ID_ICON_6 = "store1";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    public MapView mapView;
    public MapboxMap mMapboxMap;
    private Style mStyle;
    private boolean showUserLocation;
    private ImageButton carImageButton;
    private ImageButton cycleImageButton;
    private ImageButton walkImageButton;
    private TextView distanceTextView;
    private TextView timeTextView;
    private TextView addressTextView;
    private TextView titleAddressTextView;
    private TextView startNavigationTextView;
    private ImageView logoImageView;
    private ProgressBar loadProgressBar;
    private NavigationMapRoute navigationMapRoute;
    private SymbolManager symbolManager;
    private FillManager fillManager;
    private Symbol symbol;
    Point start;
    Point destination;
    private String selectedProfile = DirectionsCriteria.PROFILE_DRIVING;
    private String imageUrlLogo = "";
    private DirectionsRoute currentRoute;
    private int left = 0;
    private int right = 0;
    private int top = 0;
    private int bottom = 0;
    private boolean isFriends = false;


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        DisplayMetrics metrics = new DisplayMetrics();
        cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        retinaFactor = metrics.density;


        try {
            int mapboxAccesstokenResourceId = cordova.getActivity().getResources().getIdentifier(MAPBOX_ACCESSTOKEN_RESOURCE_KEY, "string", cordova.getActivity().getPackageName());
            accessToken = cordova.getActivity().getString(mapboxAccesstokenResourceId);
        } catch (Resources.NotFoundException e) {
            // we'll deal with this when the accessToken property is read, but for now let's dump the error:
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {

        this.callback = callbackContext;

        try {
            if (ACTION_SHOW.equals(action)) {
                Log.e(TAG,"DENSITY=="+retinaFactor);
                final JSONObject options = args.getJSONObject(0);
                final String style = getStyle(options.optString("style"));
                final JSONObject margins = options.isNull("margins") ? null : options.getJSONObject("margins");
                left = applyRetinaFactor(margins == null || margins.isNull("left") ? 0 : margins.getInt("left"));
                right = applyRetinaFactor(margins == null || margins.isNull("right") ? 0 : margins.getInt("right"));
                top = applyRetinaFactor(margins == null || margins.isNull("top") ? 0 : margins.getInt("top"));
                bottom = applyRetinaFactor(margins == null || margins.isNull("bottom") ? 0 : margins.getInt("bottom"));
                Log.e(TAG, "left=" + left + "\n right=" + right + "\ntop=" + top + "\nbottom=" + bottom);
                final JSONObject center = options.isNull("center") ? null : options.getJSONObject("center");

                this.showUserLocation = !options.isNull("showUserLocation") && options.getBoolean("showUserLocation");

                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (accessToken == null) {
                            callbackContext.error(MAPBOX_ACCESSTOKEN_RESOURCE_KEY + " not set in strings.xml");
                            return;
                        }
                        Mapbox.getInstance(cordova.getContext(), accessToken);

                        mapView = new MapView(webView.getContext());

                        // need to do this to register a receiver which onPause later needs
                        mapView.onResume();
                        mapView.onCreate(null);

                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                                mMapboxMap = mapboxMap;
                                HashMap<String, String> iconSet = new HashMap<>();
                                if (options.has("markers")) {

                                    try {

                                        for (int i = 0; i < options.getJSONArray("markers").length(); i++) {
                                            JSONObject markers = options.getJSONArray("markers").getJSONObject(i);
                                            iconSet.put(markers.getString("image"), markers.getString("image"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Style.Builder styleBuilder = new Style.Builder();
                                styleBuilder.fromUri(style);
                                for (Map.Entry<String, String> entry : iconSet.entrySet()) {
                                    styleBuilder.withImage(entry.getKey(), getBitmapFromAsset(entry.getValue()), false);
                                }
                               /* mapboxMap.setStyle(new Style.Builder()
                                                .fromUri(style)
                                                .withImage(ID_ICON_1, BitmapUtils.getBitmapFromDrawable(
                                                        ContextCompat.getDrawable(cordova.getContext(), cordova.getContext().getResources().getIdentifier("map_location", "drawable", cordova.getContext().getPackageName()))), false)
                                                .withImage(ID_ICON_2, getBitmapFromAsset("www/assets/imgs/icon-kiosk_40x48.png"), false)
                                                .withImage(ID_ICON_3, BitmapUtils.getBitmapFromDrawable(
                                                        ContextCompat.getDrawable(cordova.getContext(), cordova.getContext().getResources().getIdentifier("map_location_blue", "drawable", cordova.getContext().getPackageName()))), false)
                                                .withImage(ID_ICON_4, getBitmapFromAsset("www/assets/imgs/new_icons/building.png"), false)
                                                .withImage(ID_ICON_5, getBitmapFromAsset("www/assets/imgs/new_icons/icon-use_green_40x48.png"), false)
                                                .withImage(ID_ICON_6, getBitmapFromAsset("www/assets/imgs/icon-store_40x48.png"), false)*/


                                mapboxMap.setStyle(styleBuilder
                                        , style -> {
                                            symbolManager = new SymbolManager(mapView, mapboxMap, style);
                                            symbolManager.setIconAllowOverlap(true);
                                            symbolManager.setTextAllowOverlap(true);
                                            mStyle = style;
                                            symbolManager.addClickListener(symbol -> {
                                                destination = Point.fromLngLat(symbol.getLatLng().getLongitude(), symbol.getLatLng().getLatitude());
                                                if (!isFriends) {
                                                    imageUrlLogo = symbol.getTextTransform();
                                                    getRoute(start, destination, selectedProfile, cordova.getContext(), imageUrlLogo);
                                                } else {
                                                    if (bottomLayer != null) {
                                                        bottomLayer.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                                addressTextView.setText(symbol.getTextJustify());
                                                titleAddressTextView.setText(symbol.getTextAnchor());
                                            });
                                            fillManager = new FillManager(mapView, mapboxMap, style);
                                            try {
                                                mMapboxMap.getUiSettings().setCompassEnabled(options.isNull("hideCompass") || !options.getBoolean("hideCompass"));
                                                mMapboxMap.getUiSettings().setRotateGesturesEnabled(options.isNull("disableRotation") || !options.getBoolean("disableRotation"));
                                                mMapboxMap.getUiSettings().setScrollGesturesEnabled(options.isNull("disableScroll") || !options.getBoolean("disableScroll"));
                                                mMapboxMap.getUiSettings().setZoomGesturesEnabled(options.isNull("disableZoom") || !options.getBoolean("disableZoom"));
                                                mMapboxMap.getUiSettings().setTiltGesturesEnabled(options.isNull("disableTilt") || !options.getBoolean("disableTilt"));

                                                // placing these offscreen in case the user wants to hide them
                                                if (!options.isNull("hideAttribution") && options.getBoolean("hideAttribution")) {
                                                    mMapboxMap.getUiSettings().setAttributionMargins(-300, 0, 0, 0);
                                                }
                                                if (!options.isNull("hideLogo") && options.getBoolean("hideLogo")) {
                                                    mMapboxMap.getUiSettings().setLogoMargins(-300, 0, 0, 0);
                                                }

                                                if (showUserLocation) {
                                                    showUserLocation();
                                                }

                                                Double zoom = options.isNull("zoomLevel") ? 10 : options.getDouble("zoomLevel");
                                                float zoomLevel = zoom.floatValue();
                                                final CameraPosition.Builder builder =
                                                        new CameraPosition.Builder();

                                                if (center != null) {
                                                    final double lat = center.getDouble("lat");
                                                    final double lng = center.getDouble("lng");
                                                    builder.target(new LatLng(lat, lng));
                                                } else {
                                                    if (zoomLevel > 18.0) {
                                                        zoomLevel = 18.0f;
                                                    }

                                                }
                                                builder.zoom(zoomLevel);
                                                mMapboxMap.animateCamera(
                                                        CameraUpdateFactory.newCameraPosition(builder.build()));


                                                if (options.has("markers")) {
                                                    addMarkers(options.getJSONArray("markers"));
                                                }
                                            } catch (JSONException e) {
                                                callbackContext.error(e.getMessage());
                                                return;
                                            }
                                        });


                            }

                        });
                        // position the mapView overlay
                        int webViewWidth = webView.getView().getWidth();
                        int webViewHeight = webView.getView().getHeight();
                        layout = (FrameLayout) webView.getView().getParent();
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(webViewWidth - left - right, webViewHeight - top - bottom);
                        params.setMargins(left, top, right, bottom);
                        mapView.setLayoutParams(params);
                        layout.addView(mapView);
                        addCustomView();
                        callbackContext.success();
                    }
                });
                callbackContext.success();
            } else if (ACTION_REMOVE.equals(action)) {
                isFriends = false;
                if (mapView != null) {
                    // Remove marker callback handler
                    this.markerCallbackContext = null;
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup vg = (ViewGroup) mapView.getParent();

                            if (vg != null) {
                                vg.removeView(mapView);
                            }

                            if (layout != null && topLayer != null && bottomLayer != null) {
                                layout.removeView(topLayer);
                                layout.removeView(bottomLayer);
                                topLayer = null;
                                bottomLayer = null;
                            }
                            if (mapView != null) {
                                mapView.onDestroy();
                                mMapboxMap = null;
                                mapView = null;
                                navigationMapRoute = null;
                                currentRoute = null;
                                selectedProfile = DirectionsCriteria.PROFILE_DRIVING;
                            }


                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
                            callbackContext.sendPluginResult(pluginResult);
                            callbackContext.success();
                        }
                    });
                }

            } else if (ACTION_GET_CENTER.equals(action)) {
                if (mMapboxMap != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LocationComponent locationComponent = mMapboxMap.getLocationComponent();
                            Map<String, Double> result = new HashMap<String, Double>();
                            result.put("lat", locationComponent.getLastKnownLocation().getLatitude());
                            result.put("lng", locationComponent.getLastKnownLocation().getLongitude());
                            callbackContext.success(new JSONObject(result));
                        }
                    });
                }

            } else if (ACTION_SET_CENTER.equals(action)) {
                if (mMapboxMap != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final JSONObject options = args.getJSONObject(0);
                                final boolean animated = !options.isNull("animated") && options.getBoolean("animated");
                                final double lat = options.getDouble("lat");
                                final double lng = options.getDouble("lng");
                                LocationComponent locationComponent = mMapboxMap.getLocationComponent();
                                locationComponent.getLastKnownLocation().setLatitude(lat);
                                locationComponent.getLastKnownLocation().setLongitude(lng);
                                CameraPosition position = new CameraPosition.Builder()
                                        .target(new LatLng(lat, lng))
                                        .zoom(15)
                                        .build();
                                mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500);
                                callbackContext.success();
                            } catch (JSONException e) {
                                callbackContext.error(e.getMessage());
                            }
                        }
                    });
                }
            } else if (ACTION_GET_ZOOMLEVEL.equals(action)) {

                if (mMapboxMap != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final double zoomLevel = mMapboxMap.getCameraPosition().zoom;
                            callbackContext.success("" + zoomLevel);
                        }
                    });
                }


            } else if (ACTION_SET_ZOOMLEVEL.equals(action)) {
                if (mMapboxMap != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final JSONObject options = args.getJSONObject(0);
                                final double zoom = options.getDouble("level");
                                if (zoom >= 0 && zoom <= 20) {
                                    final boolean animated = !options.isNull("animated") && options.getBoolean("animated");
                                    LocationComponent locationComponent = mMapboxMap.getLocationComponent();
                                    if (locationComponent != null) {
                                        CameraPosition position = new CameraPosition.Builder()
                                                .target(new LatLng(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude()))
                                                .zoom(zoom)
                                                .build();
                                        mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500);
                                    }
                                    callbackContext.success();
                                } else {
                                    callbackContext.error("invalid zoomlevel, use any double value from 0 to 20 (like 8.3)");
                                }
                            } catch (JSONException e) {
                                callbackContext.error(e.getMessage());
                            }
                        }
                    });
                }


            } else if (ACTION_GET_BOUNDS.equals(action)) {
                if (mMapboxMap != null && mapView != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // NOTE: need to change to this on a future release
                            // final CoordinateBounds bounds = mapView.getVisibleCoordinateBounds();
                            // final LatLng sw = bounds.getSouthWest();
                            // final LatLng ne = bounds.getNorthEast();

                            // NOTE: Workaround, see: https://github.com/mapbox/mapbox-gl-native/issues/3863#issuecomment-181825298
                            int viewportWidth = mapView.getWidth();
                            int viewportHeight = mapView.getHeight();
                            LatLng sw = mMapboxMap.getProjection().fromScreenLocation(new PointF(0, viewportHeight));
                            LatLng ne = mMapboxMap.getProjection().fromScreenLocation(new PointF(viewportWidth, 0));
                            Map<String, Double> result = new HashMap<String, Double>();
                            result.put("sw_lat", sw.getLatitude());
                            result.put("sw_lng", sw.getLongitude());
                            result.put("ne_lat", ne.getLatitude());
                            result.put("ne_lng", ne.getLongitude());
                            callbackContext.success(new JSONObject(result));
                        }
                    });
                }

            } else if (ACTION_SET_BOUNDS.equals(action)) {
                if (mMapboxMap != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final JSONObject options = args.getJSONObject(0);
                                final boolean animated = !options.isNull("animated") && options.getBoolean("animated");
                                final double sw_lat = options.getDouble("sw_lat");
                                final double sw_lng = options.getDouble("sw_lng");
                                final double ne_lat = options.getDouble("ne_lat");
                                final double ne_lng = options.getDouble("ne_lng");
                                final LatLng sw = new LatLng(sw_lat, sw_lng);
                                final LatLng ne = new LatLng(ne_lat, ne_lng);
                                LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                        .include(ne) // Northeast
                                        .include(sw) // Southwest
                                        .build();
                                mMapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50), 5000, animated);
                                callbackContext.success();
                            } catch (JSONException e) {
                                callbackContext.error(e.getMessage());
                            }
                        }
                    });
                }

            } else if (ACTION_GET_TILT.equals(action)) {
                if (mMapboxMap != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final double tilt = mMapboxMap.getCameraPosition().tilt;
                            callbackContext.success("" + tilt);
                        }
                    });
                }

            } else if (ACTION_SET_TILT.equals(action)) {
                if (mapView != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final JSONObject options = args.getJSONObject(0);
                                CameraPosition position = new CameraPosition.Builder()
                                        .target(new LatLng(mMapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(), mMapboxMap.getLocationComponent().getLastKnownLocation().getLongitude()))
                                        .tilt(options.optDouble("pitch", 20))
                                        .build();
                                mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), options.optInt("duration", 5000));
                                callbackContext.success();
                            } catch (JSONException e) {
                                callbackContext.error(e.getMessage());
                            }
                        }
                    });
                }


            } else if (ACTION_ANIMATE_CAMERA.equals(action)) {
                if (mMapboxMap != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // TODO check mandatory elements
                                final JSONObject options = args.getJSONObject(0);

                                final JSONObject target = options.getJSONObject("target");
                                final double lat = target.getDouble("lat");
                                final double lng = target.getDouble("lng");
                                final CameraPosition.Builder builder =
                                        new CameraPosition.Builder()
                                                .target(new LatLng(lat, lng));
                                if (options.has("bearing")) {
                                    builder.bearing(((Double) options.getDouble("bearing")).floatValue());
                                }
                                if (options.has("tilt")) {
                                    builder.tilt(((Double) options.getDouble("tilt")).floatValue());
                                }
                                if (options.has("zoomLevel")) {
                                    builder.zoom(((Double) options.getDouble("zoomLevel")).floatValue());
                                }
                                mMapboxMap.animateCamera(
                                        CameraUpdateFactory.newCameraPosition(builder.build()),
                                        (options.optInt("duration", 15)) * 1000, // default 15 seconds
                                        null);
                                callbackContext.success();
                            } catch (JSONException e) {
                                callbackContext.error(e.getMessage());
                            }
                        }
                    });
                }

            } else if (ACTION_ADD_POLYGON.equals(action)) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final JSONObject options = args.getJSONObject(0);
                            final JSONArray points = options.getJSONArray("points");
                            List<LatLng> innerLatLngs = new ArrayList<>();
                            for (int i = 0; i < points.length(); i++) {
                                final JSONObject marker = points.getJSONObject(i);
                                final double lat = marker.getDouble("lat");
                                final double lng = marker.getDouble("lng");
                                innerLatLngs.add(new LatLng(lat, lng));
                            }
                            List<List<LatLng>> latLngs = new ArrayList<>();
                            latLngs.add(innerLatLngs);
                            FillOptions fillOptions = new FillOptions()
                                    .withLatLngs(latLngs)
                                    .withFillColor(ColorUtils.colorToRgbaString(Color.BLUE));
                            fillManager.create(fillOptions);
                            callbackContext.success();
                        } catch (JSONException e) {
                            callbackContext.error(e.getMessage());
                        }
                    }
                });
            } else if (ACTION_ADD_GEOJSON.equals(action)) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final JSONObject options;
                        try {
                            options = args.getJSONObject(0);
                            new LoadGeoJson(cordova.getActivity(), options.getString("url"), callbackContext).execute();
                        } catch (JSONException e) {
                            callbackContext.error(e.getMessage());
                        }

                    }
                });

            } else if (ACTION_ADD_MARKERS.equals(action)) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            addMarkers(args.getJSONArray(0));
                            callbackContext.success();
                        } catch (JSONException e) {
                            callbackContext.error(e.getMessage());
                        }
                    }
                });


            } else if (ACTION_REMOVE_ALL_MARKERS.equals(action)) {
                if (mMapboxMap != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            removeAllAnnotations();
                            callbackContext.success();
                        }
                    });
                }

            } else if (ACTION_ADD_MARKER_CALLBACK.equals(action)) {
                this.markerCallbackContext = callbackContext;
                /*   mapView.setOnInfoWindowClickListener(new MarkerClickListener());*/
             /*   symbolManager.addClickListener(new OnSymbolClickListener() {
                    @Override
                    public void onAnnotationClick(Symbol symbol) {
                        if (markerCallbackContext != null) {
                            final JSONObject json = new JSONObject();
                            try {
                                json.put("title", symbol.getTextAnchor());
                                json.put("subtitle", symbol.getTextJustify());
                                json.put("lat", symbol.getLatLng().getLatitude());
                                json.put("lng", symbol.getLatLng().getLongitude());
                            } catch (JSONException e) {
                                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
                                        "Error in callback of " + ACTION_ADD_MARKER_CALLBACK + ": " + e.getMessage());
                                pluginResult.setKeepCallback(true);
                                markerCallbackContext.sendPluginResult(pluginResult);
                            }
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                            pluginResult.setKeepCallback(true);
                            markerCallbackContext.sendPluginResult(pluginResult);
                        }
                    }
                });*/

            } else if (ACTION_ON_REGION_WILL_CHANGE.equals(action)) {
                if (mapView != null) {
                    mapView.addOnCameraWillChangeListener(new RegionWillChangeListener(callbackContext));
                }


            } else if (ACTION_ON_REGION_IS_CHANGING.equals(action)) {
                if (mapView != null) {
                    mapView.addOnCameraIsChangingListener(new RegionIsChangingListener(callbackContext));
                }

            } else if (ACTION_ON_REGION_DID_CHANGE.equals(action)) {
                if (mapView != null) {
                    mapView.addOnCameraDidChangeListener(new RegionDidChangeListener(callbackContext));
                }

            } else if (ACTION_CONVERT_COORDINATES.equals(action)) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final JSONObject options = args.getJSONObject(0);
                            double lat = options.getDouble("lat");
                            double lng = options.getDouble("lng");
                            if (Math.abs(lat) > 90 || Math.abs(lng) > 180) {
                                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
                                        "Incorrect Leaflet.Point point coordinates.");
                                pluginResult.setKeepCallback(true);
                                markerCallbackContext.sendPluginResult(pluginResult);
                            }
                            Projection pr = mMapboxMap.getProjection();
                            PointF pointF = pr.toScreenLocation(new LatLng(lat, lng));
                            JSONObject result = new JSONObject();
                            result.put("x", pointF.x);
                            result.put("y", pointF.y);
                            callbackContext.success(result);
                        } catch (JSONException e) {
                            callbackContext.error(e.getMessage());
                        }
                    }
                });


            } else if (ACTION_CONVERT_POINTS.equals(action)) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final JSONObject options = args.getJSONObject(0);
                            float x = Float.parseFloat(options.getString("x"));
                            float y = Float.parseFloat(options.getString("y"));
                            if (x < 0 || y < 0) {
                                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
                                        "Incorrect Leaflet.Point point coordinates.");
                                pluginResult.setKeepCallback(true);
                                markerCallbackContext.sendPluginResult(pluginResult);
                            }
                            JSONObject result = new JSONObject();
                            Projection pr = mMapboxMap.getProjection();
                            LatLng latLng = pr.fromScreenLocation(new PointF(x, y));
                            result.put("lat", latLng.getLatitude());
                            result.put("lng", latLng.getLongitude());
                            callbackContext.success(result);

                        } catch (JSONException e) {
                            callbackContext.error(e.getMessage());
                        }
                    }
                });


            } else if (ADD_CUSTOM_BUTTON.equals(action)) {
                isFriends = true;
                if (bottomLayer != null) {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
                            markerCallbackContext.sendPluginResult(pluginResult);
                        }
                    });

                }

            } else if (EDIT_CUSTOM_BUTTON.equals(action)) {
               /* final JSONObject options = args.getJSONObject(0);
                String title= options.getString("title");
                JSONObject colorObject =options.getJSONObject("color");
                int red= colorObject.getInt("red");
                int green= colorObject.getInt("green");
                int blue= colorObject.getInt("blue");
                int alpha= colorObject.getInt("alpha");
                double radius = options.getDouble("radius");*/

            } else if (REOMOVE_CUSTOM_BUTTON.equals(action)) {
                cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (topLayer != null) {
                            topLayer.setVisibility(View.GONE);
                        }
                        if (bottomLayer != null) {
                            bottomLayer.setVisibility(View.GONE);
                        }
                        callbackContext.success();
                    }
                });


            } else if (ADD_CUSTOM_BUTTON_CALL_BACK.equals(action)) {
                callback.success();

            } else {
                return false;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            callbackContext.error(t.getMessage());
        }
        return true;
    }

    private void addMarkers(JSONArray markers) throws JSONException {
        boolean isRouteShow = false;
        List<SymbolOptions> options = new ArrayList<>();
        for (int i = 0; i < markers.length(); i++) {
            final JSONObject marker;
            marker = markers.getJSONObject(i);
            SymbolOptions symbolOptions = new SymbolOptions()
                    .withLatLng(new LatLng(marker.getDouble("lat"), marker.getDouble("lng")))
                    .withTextTransform(marker.has("storeIcon") ? marker.getString("storeIcon") : "")
                    .withTextAnchor(marker.getString("title"))
                    .withTextJustify(marker.getString("subtitle"));
            imageUrlLogo = marker.has("storeIcon") ? marker.getString("storeIcon") : "";
            symbolOptions.withIconImage(marker.getString("image"));
            if (marker.getString("image").contains("current_location")) {
                start = Point.fromLngLat(marker.getDouble("lng"), marker.getDouble("lat"));
                //symbolOptions.withIconImage(ID_ICON_3);
            } else if (marker.getString("image").contains("icon-kiosk")) {
                //symbolOptions.withIconImage(ID_ICON_2);
            } else if (marker.getString("image").contains("icon-store")) {
                //symbolOptions.withIconImage(ID_ICON_4);
            } else {
                //symbolOptions.withIconImage(ID_ICON_1);
            }
            if (marker.has("showRoute")) {
                isRouteShow = true;
                destination = Point.fromLngLat(marker.getDouble("lng"), marker.getDouble("lat"));
            }
            options.add(symbolOptions);
        }
        if (symbolManager != null) {
            symbolManager.create(options);
        }
        if (isRouteShow && !isFriends) {
            getRoute(start, destination, selectedProfile, cordova.getContext(), imageUrlLogo);
        }
    }

    private void removeAllAnnotations() {
        List<Symbol> symbols = new ArrayList<>();
        LongSparseArray<Symbol> symbolArray = symbolManager.getAnnotations();
        for (int i = 0; i < symbolArray.size(); i++) {
            symbols.add(symbolArray.get(i));
        }
        if (!symbols.isEmpty())
            symbolManager.delete(symbols);
    }

    private class RegionWillChangeListener implements MapView.OnCameraWillChangeListener {
        private CallbackContext callback;

        public RegionWillChangeListener(CallbackContext providedCallback) {
            this.callback = providedCallback;
        }

        @Override
        public void onCameraWillChange(boolean animated) {
            if (animated) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
                pluginResult.setKeepCallback(true);
                callback.sendPluginResult(pluginResult);
            }


        }
    }

    private class RegionIsChangingListener implements MapView.OnCameraIsChangingListener {
        private CallbackContext callback;

        public RegionIsChangingListener(CallbackContext providedCallback) {
            this.callback = providedCallback;
        }

        @Override
        public void onCameraIsChanging() {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            callback.sendPluginResult(pluginResult);
        }
    }

    private class RegionDidChangeListener implements MapView.OnCameraDidChangeListener {
        private CallbackContext callback;

        public RegionDidChangeListener(CallbackContext providedCallback) {
            this.callback = providedCallback;
        }

        @Override
        public void onCameraDidChange(boolean animated) {
            if (animated) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
                pluginResult.setKeepCallback(true);
                callback.sendPluginResult(pluginResult);
            }

        }
    }

    private static int applyRetinaFactor(int i) {
        return (int) (i * retinaFactor);
    }

    private static String getStyle(final String requested) {
        if ("light".equalsIgnoreCase(requested)) {
            return Style.LIGHT;
        } else if ("dark".equalsIgnoreCase(requested)) {
            return Style.DARK;
        } else if ("satellite".equalsIgnoreCase(requested)) {
            return Style.SATELLITE;
        } else if ("outdoors".equalsIgnoreCase(requested)) {
            return Style.OUTDOORS;
        } else if ("streets".equalsIgnoreCase(requested)) {
            return Style.MAPBOX_STREETS;
        } else {
            return Style.MAPBOX_STREETS;
        }
    }

    private boolean permissionGranted(String... types) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        for (final String type : types) {
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this.cordova.getActivity(), type)) {
                return false;
            }
        }
        return true;
    }

    protected void showUserLocation() {
        if (permissionGranted(COARSE_LOCATION, FINE_LOCATION)) {
            //noinspection MissingPermission
            // Get an instance of the component
            LocationComponent locationComponent = mMapboxMap.getLocationComponent();

            // Activate with a built LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(cordova.getContext(), mStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            start = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(), locationComponent.getLastKnownLocation().getLatitude());
            SymbolOptions SymbolOptions = new SymbolOptions()
                    .withLatLng(new LatLng(locationComponent.getLastKnownLocation().getLatitude(), locationComponent.getLastKnownLocation().getLongitude()))
                    .withIconImage(ID_ICON_3);
            symbolManager.create(SymbolOptions);
        } else {
            requestPermission(COARSE_LOCATION, FINE_LOCATION);
        }
    }


    private void requestPermission(String... types) {
        ActivityCompat.requestPermissions(
                this.cordova.getActivity(),
                types,
                LOCATION_REQ_CODE);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        switch (requestCode) {
            case LOCATION_REQ_CODE:
                showUserLocation();
                break;
        }
    }

    public void onPause(boolean multitasking) {
        if (mapView != null) {
            mapView.onPause();
        }
    }

    public void onResume(boolean multitasking) {
        if (mapView != null) {
            mapView.onResume();
        }
    }

    public void onDestroy() {
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    private void addCustomView() {
        Runnable runnable = new Runnable() {
            public void run() {
                Context context = cordova.getActivity().getApplicationContext();
                Resources resources = context.getResources();
                String packageName = context.getPackageName();
                //top layer
                topLayer = getLayoutInflater(context).inflate(resources.getIdentifier("top_layer", "layout", packageName), null);
                //views
                carImageButton = (ImageButton) topLayer.findViewById(resources.getIdentifier("img_car", "id", packageName));
                cycleImageButton = (ImageButton) topLayer.findViewById(resources.getIdentifier("img_cycle", "id", packageName));
                walkImageButton = (ImageButton) topLayer.findViewById(resources.getIdentifier("img_walk", "id", packageName));
                //clicks on different Direction Criteria
                carImageButton.setOnClickListener(view -> {
                    selectedProfile = DirectionsCriteria.PROFILE_DRIVING;
                    getRoute(start, destination, DirectionsCriteria.PROFILE_DRIVING, context, imageUrlLogo);
                    onProfileSelection(true, false, false, context);
                });
                cycleImageButton.setOnClickListener(view -> {
                    selectedProfile = DirectionsCriteria.PROFILE_CYCLING;
                    getRoute(start, destination, DirectionsCriteria.PROFILE_CYCLING, context, imageUrlLogo);
                    onProfileSelection(false, true, false, context);
                });
                walkImageButton.setOnClickListener(view -> {
                    selectedProfile = DirectionsCriteria.PROFILE_WALKING;
                    getRoute(start, destination, DirectionsCriteria.PROFILE_WALKING, context, imageUrlLogo);
                    onProfileSelection(false, false, true, context);
                });
                layout.addView(topLayer);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.TOP;
                params.setMargins(left, top, right, 0);
                topLayer.setLayoutParams(params);
                topLayer.setVisibility(View.GONE);

                //bottom layer
                bottomLayer = getLayoutInflater(context).inflate(resources.getIdentifier("bottom_layer", "layout", packageName), null);
                //views
                addressTextView = (TextView) bottomLayer.findViewById(resources.getIdentifier("tv_address", "id", packageName));
                titleAddressTextView = (TextView) bottomLayer.findViewById(resources.getIdentifier("tv_address_title", "id", packageName));
                timeTextView = (TextView) bottomLayer.findViewById(resources.getIdentifier("tv_time", "id", packageName));
                distanceTextView = (TextView) bottomLayer.findViewById(resources.getIdentifier("tv_distance", "id", packageName));
                logoImageView = bottomLayer.findViewById(resources.getIdentifier("image_logo", "id", packageName));
                loadProgressBar = bottomLayer.findViewById(resources.getIdentifier("loadProgress", "id", packageName));
                startNavigationTextView = (TextView) bottomLayer.findViewById(resources.getIdentifier("tv_start_navigation", "id", packageName));
                layout.addView(bottomLayer);
                FrameLayout.LayoutParams paramsBottom = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                paramsBottom.gravity = Gravity.BOTTOM;
                paramsBottom.setMargins(0, 0, 0, webView.getView().getHeight() - (mapView.getLayoutParams().height + top));
                bottomLayer.setLayoutParams(paramsBottom);
                bottomLayer.setVisibility(View.GONE);
                layout.invalidate();
                // start turn by turn navigation
                startNavigationTextView.setOnClickListener(view -> {
                    if (currentRoute != null) {
                        // Call this method with Context from within an Activity
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(false)
                                .build();
                        NavigationLauncher.startNavigation(cordova.getActivity(), options);
                    }
                });


            }
        };
        this.cordova.getActivity().runOnUiThread(runnable);
        callback.success();

    }

    private LayoutInflater getLayoutInflater(Context context) {
        return LayoutInflater.from(context);
    }

    private void onProfileSelection(boolean isDriving, boolean isCycle, boolean isWalk, Context context) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Resources resources = context.getResources();
                String packageName = context.getPackageName();
                if (isDriving) {
                    carImageButton.setImageDrawable(ContextCompat.getDrawable(context, resources.getIdentifier("car", "drawable", packageName)));
                    cycleImageButton.setImageDrawable(ContextCompat.getDrawable(context, resources.getIdentifier("cycle_white", "drawable", packageName)));
                    walkImageButton.setImageDrawable(ContextCompat.getDrawable(context, resources.getIdentifier("walk_white", "drawable", packageName)));
                    carImageButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                    cycleImageButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    walkImageButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                } else if (isCycle) {
                    carImageButton.setImageDrawable(ContextCompat.getDrawable(context, resources.getIdentifier("car_white", "drawable", packageName)));
                    cycleImageButton.setImageDrawable(ContextCompat.getDrawable(context, resources.getIdentifier("cycle", "drawable", packageName)));
                    walkImageButton.setImageDrawable(ContextCompat.getDrawable(context, resources.getIdentifier("walk_white", "drawable", packageName)));
                    carImageButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    cycleImageButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                    walkImageButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));

                } else if (isWalk) {
                    carImageButton.setImageDrawable(ContextCompat.getDrawable(context, resources.getIdentifier("car_white", "drawable", packageName)));
                    cycleImageButton.setImageDrawable(ContextCompat.getDrawable(context, resources.getIdentifier("cycle_white", "drawable", packageName)));
                    walkImageButton.setImageDrawable(ContextCompat.getDrawable(context, resources.getIdentifier("walk", "drawable", packageName)));
                    carImageButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    cycleImageButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    walkImageButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));

                }

            }
        });

    }

    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     *
     * @param origin      the starting point of the route
     * @param destination the desired finish point of the route
     */
    private void getRoute(Point origin, Point destination, String profile, Context context, String urlLogo) {
        if (!isFriends) {
            topLayer.setVisibility(View.VISIBLE);
            GeoJsonSource source = mMapboxMap.getStyle().getSourceAs("destination-source-id");
            if (source != null) {
                source.setGeoJson(Feature.fromGeometry(destination));
            }
        }
        if (start != null && destination != null) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Resources resources = context.getResources();
                    String packageName = context.getPackageName();
                    NavigationRoute.builder(context)
                            .accessToken(resources.getString(resources.getIdentifier("mapbox_access_token", "string", packageName)))
                            .origin(origin)
                            .destination(destination)
                            .profile(profile)
                            .radiuses()
                            .build().getRoute(new Callback<DirectionsResponse>() {
                        @Override
                        public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                            if (bottomLayer != null) {
                                bottomLayer.setVisibility(View.VISIBLE);
                                if (!isFriends) {
                                    distanceTextView.setVisibility(View.VISIBLE);
                                    timeTextView.setVisibility(View.VISIBLE);
                                    startNavigationTextView.setVisibility(View.VISIBLE);
                                    if (urlLogo != null && !urlLogo.isEmpty()) {
                                        logoImageView.setVisibility(View.VISIBLE);
                                        loadLogo(urlLogo);
                                    }


                                }
                            }

                            // You can get the generic HTTP info about the response
                            Log.d(TAG, "Response code: " + response.code());
                            if (response.body() == null) {
                                Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                                return;
                            } else if (response.body().routes().size() < 1) {
                                Log.e(TAG, "No routes found");
                                return;
                            }
                            currentRoute = response.body().routes().get(0);
                            double distanceKm = currentRoute.distance() != null ? (currentRoute.distance() / 1000) : 0.0;
                            convertKmsToMiles(distanceKm);
                            double timeMin = currentRoute.duration() != null ? currentRoute.duration() : 0.0;
                            calculateTime(Math.round(timeMin));
                            // Draw the route on the map
                            if (navigationMapRoute != null) {
                                navigationMapRoute.removeRoute();
                            } else {
                                navigationMapRoute = new NavigationMapRoute(null, mapView, mMapboxMap, resources.getIdentifier("NavigationMapRoute", "style", packageName));
                            }

                            navigationMapRoute.addRoute(currentRoute);
                        }

                        @Override
                        public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                            callback.error(throwable.getMessage());
                        }
                    });
                }
            });

        }
    }

    private void calculateTime(long seconds) {
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) -
                TimeUnit.DAYS.toHours(day);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) -
                TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds));
        long second = TimeUnit.SECONDS.toSeconds(seconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds));
        System.out.println("Day " + day + " Hour " + hours + " Minute " + minute + " Seconds " + second);
        StringBuilder stringBuilder = new StringBuilder();
        if (day > 0) {
            stringBuilder.append(day > 1 ? day + " days" : day + " day");
        }
        if (hours > 0) {
            if (day > 0) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(formattedString("" + hours) + " hr");
        }
        if (minute > 0) {
            if (hours > 0) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(formattedString("" + minute) + " min");
        }
        if (second > 0) {
            if (minute > 0) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(formattedString("" + second) + " sec");
        }
        timeTextView.setText(stringBuilder.toString());
    }

    private String formattedString(String value) {
        String finalValue;
        if (value.length() > 2 && value.startsWith("0")) {
            finalValue = value.replaceFirst("0", "");
        } else if (value.length() == 1 && !value.startsWith("0")) {
            finalValue = "0" + value;
        } else {
            finalValue = value;
        }
        return finalValue;

    }

    private void convertKmsToMiles(double kms) {
        double miles = 0.621371 * kms;
        distanceTextView.setText(String.format("%.2f", miles) + " mi");
    }

    private class LoadGeoJson extends AsyncTask<Void, Void, FeatureCollection> {

        private WeakReference<Activity> weakReference;
        private String geoJsonUrl;
        private CallbackContext callback;

        LoadGeoJson(Activity activity, String geoJsonUrl, CallbackContext providedCallback) {
            this.weakReference = new WeakReference<>(activity);
            this.geoJsonUrl = geoJsonUrl;
            this.callback = providedCallback;

        }

        @Override
        protected FeatureCollection doInBackground(Void... voids) {
            try {
                Activity activity = weakReference.get();
                if (activity != null) {
                    InputStream inputStream = new URL(geoJsonUrl).openStream();
                    return FeatureCollection.fromJson(convertStreamToString(inputStream));
                }
            } catch (Exception exception) {
                Log.e(TAG, "Exception Loading GeoJSON: %s=" + exception.toString());
            }
            return null;
        }

        String convertStreamToString(InputStream is) {
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }

        @Override
        protected void onPostExecute(@Nullable FeatureCollection featureCollection) {
            super.onPostExecute(featureCollection);
            Activity activity = weakReference.get();
            if (activity != null && featureCollection != null) {
                drawLines(featureCollection);
                callback.success();
            }
        }
    }

    private void drawLines(@NonNull FeatureCollection featureCollection) {
        if (mMapboxMap != null) {
            mMapboxMap.getStyle(style -> {
                if (featureCollection.features() != null) {
                    if (featureCollection.features().size() > 0) {
                        style.addSource(new GeoJsonSource("line-source", featureCollection));

// The layer properties for our line. This is where we make the line dotted, set the
// color, etc.
                        style.addLayer(new LineLayer("linelayer", "line-source")
                                .withProperties(lineCap(Property.LINE_CAP_SQUARE),
                                        lineJoin(Property.LINE_JOIN_MITER),
                                        PropertyFactory.lineOpacity(.7f),
                                        lineWidth(7f),
                                        lineColor(Color.parseColor("#3bb2d0"))));
                    }
                }
            });
        }
    }

    public Bitmap getBitmapFromAsset(String filePath) {
        AssetManager assetManager = cordova.getActivity().getApplication().getAssets();
        InputStream istr;
        Bitmap bitmap = null;
        try {
            Log.e(TAG, filePath);
            istr = assetManager.open(filePath);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inDensity = DisplayMetrics.DENSITY_DEFAULT;
            Drawable drawable = Drawable.createFromResourceStream(cordova.getContext().getResources(), null, istr, null, opts);
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }

    private void loadLogo(String valueUrl) {
        Picasso.get()
                .load(valueUrl)
                .into(logoImageView); /*new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        loadProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        loadProgressBar.setVisibility(View.GONE);
                    }
                });*/
    }

}
