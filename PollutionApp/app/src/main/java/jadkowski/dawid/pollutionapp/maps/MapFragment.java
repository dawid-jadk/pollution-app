package jadkowski.dawid.pollutionapp.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;

import jadkowski.dawid.pollutionapp.helpers.Constants;

/**
 * Created by dawid on 28/01/2016.
 */
public class MapFragment extends SupportMapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static ArrayList<WeightedLatLng> locations = new ArrayList<>();
    public static HeatmapTileProvider mProvider;
    public static TileOverlay mOverlay;
    private GoogleApiClient mGoogleApiClient;
    private Gradient gradient = new Gradient(Constants.GRADIENT_COLOURS, Constants.GRADIENT_STARTPOINTS);

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        initMapSettings();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //No permission
            Toast.makeText(getContext(), "No location permission.", Toast.LENGTH_SHORT).show();
            return;
        }
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        initCamera(mCurrentLocation);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    protected void initMapSettings() {
        mProvider = new HeatmapTileProvider.Builder().weightedData(locations).gradient(gradient).build();
        mProvider.setRadius(HeatmapTileProvider.DEFAULT_RADIUS);
        mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    private void initCamera(Location location) {
        if (location != null) {
            CameraPosition position = CameraPosition.builder()
                    .target(new LatLng(location.getLatitude(),
                            location.getLongitude()))
                    .zoom(11f)
                    .bearing(0.0f)
                    .tilt(0.0f)
                    .build();
            getMap().animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), null);
            getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
            getMap().setTrafficEnabled(false);
            getMap().getUiSettings().setZoomControlsEnabled(true);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //No permission
                Toast.makeText(getContext(), "No location permission.", Toast.LENGTH_SHORT).show();
                return;
            }
            getMap().setMyLocationEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}