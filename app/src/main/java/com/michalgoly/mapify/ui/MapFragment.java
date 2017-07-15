package com.michalgoly.mapify.ui;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.michalgoly.mapify.R;
import com.michalgoly.mapify.com.michalgoly.mapify.parcels.TrackWrapper;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {

    private static final String TAG = "MapFragment";

    private static final int REQUEST_LOCATION = 1;
    private static final long UPDATE_INTERVAL_MS = 10_000;
    private static final long FASTEST_UPDATE_INTERVAL_MS = UPDATE_INTERVAL_MS / 2;
    private static final float SMALLEST_DISPLACEMENT_M = 10;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static final String KEY_POINTS = "KEY_POINTS";

    private SupportMapFragment mapFragment = null;
    private GoogleMap googleMap = null;
    private FusedLocationProviderClient fusedLocationProviderClient = null;
    private SettingsClient settingsClient = null;
    private LocationSettingsRequest locationSettingsRequest = null;
    private LocationCallback locationCallback = null;
    private LocationRequest locationRequest = null;
    private Location currentLocation = null;
    private List<LatLng> points = null;
    private Polyline path = null;

    private OnMapFragmentInteractionListener mainActivityListener = null;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(List<LatLng> points) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        if (points != null)
            args.putParcelableArrayList(KEY_POINTS, new ArrayList<>(points));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            points = getArguments().getParcelableArrayList(KEY_POINTS);
        }
        if (points == null)
            points = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
        else
            Log.d(TAG, "mapFragment was null");
        redrawPath();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelableArrayList(KEY_POINTS, new ArrayList<>(points));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMapFragmentInteractionListener) {
            mainActivityListener = (OnMapFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMapFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMyLocationButtonClickListener(this);
        enableLocation();
        initLocationServices();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != REQUEST_LOCATION)
            return;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocation();
        } else {
            Log.i(TAG, "No location permission, closing the app...");
            getActivity().finishAffinity();
        }
    }

    public void initLocationServices() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        settingsClient = LocationServices.getSettingsClient(getContext());
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                /*
                 * 1. Add the locationResult to the List of locations
                 * 2. Update the UI to draw the path
                 */
                Log.d(TAG, "onLocationResult: " + locationResult.getLastLocation().toString());
                currentLocation = locationResult.getLastLocation();
                points.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                mainActivityListener.onMapFragmentInteraction(-1, null, points);
                redrawPath();
            }
        };
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_M); 
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        startLocationUpdates();
    }

    public void startLocationUpdates() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");
                        try {
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                                    locationCallback, Looper.myLooper());
                        } catch (SecurityException e) {
                            Log.wtf("Should never happen", e);
                        }
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Interaction with the parent Activity
     */
    public interface OnMapFragmentInteractionListener {
        void onMapFragmentInteraction(int menuitemId, TrackWrapper currentTrack, List<LatLng> points);
    }

    private void redrawPath() {
        if (googleMap != null) {
            googleMap.clear();
            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true)
                    .visible(true);
            for (LatLng p : points)
                options.add(p);
            path = googleMap.addPolyline(options);
        }
    }

    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else if (googleMap != null) {
            // Access to the location has been granted to the app.
            googleMap.setMyLocationEnabled(true);
        }
    }
}
