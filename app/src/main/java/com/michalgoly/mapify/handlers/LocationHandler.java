package com.michalgoly.mapify.handlers;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.michalgoly.mapify.model.LocationTrackWrapper;
import com.michalgoly.mapify.model.TrackWrapper;
import com.michalgoly.mapify.utils.AlertsManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LocationHandler extends Service {

    private static final String TAG = "LocationHandler";
    private static final long UPDATE_INTERVAL_MS = 10_000;
    private static final long FASTEST_UPDATE_INTERVAL_MS = UPDATE_INTERVAL_MS / 2;
    private static final float SMALLEST_DISPLACEMENT_M = 1;

    private IBinder binder = new ServiceBinder();

    private FusedLocationProviderClient fusedLocationProviderClient = null;
    private SettingsClient settingsClient = null;
    private LocationSettingsRequest locationSettingsRequest = null;
    private LocationCallback locationCallback = null;
    private LocationRequest locationRequest = null;
    private Location currentLocation = null;
    private SpotifyHandler spotifyHandler = null;
    private List<LocationTrackWrapper> locations = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        init();
        return binder;
    }

    public class ServiceBinder extends Binder {
        public LocationHandler getService() {
            return LocationHandler.this;
        }
    }

    public List<LocationTrackWrapper> getLocations() {
        return locations;
    }

    /**
     * @return Location - The current location, or null if not available yet
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    private void init() {
        Log.d(TAG, "init() called");
        locations = new ArrayList<>();
        bindSpotifyHandler();
        initLocationServices();
    }

    private void initLocationServices() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                /*
                 * 1. Every 10s
                 * 2. If song is playing, create a LocationTrackWrapper and put it into a TreeMap
                 *    sorted by Date
                 * 3. Otherwise, do nothing
                 */
                currentLocation = locationResult.getLastLocation();
                Log.d(TAG, "onLocationResult(locationResult): " + currentLocation.toString());
                if (spotifyHandler.getCurrentPlaybackState() != null
                        && spotifyHandler.getCurrentPlaybackState().isPlaying) {
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    TrackWrapper currentTrack = spotifyHandler.getCurrentTrack();
                    locations.add(new LocationTrackWrapper(latLng, currentTrack, new Date()));
                }
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

    private void startLocationUpdates() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
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
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                AlertsManager.alertAndExit(LocationHandler.this, "Location settings are not satisfied." + e.toString());
                                // TODO Attempt to upgrate location settings, rather than killing the app
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                AlertsManager.alertAndExit(LocationHandler.this, errorMessage);
                        }
                    }
                });
    }

    private void bindSpotifyHandler() {
        Log.d(TAG, "bindSpotifyHandler() called");
        Intent intent = new Intent(this, SpotifyHandler.class);
        boolean isBind = bindService(intent, new SpotifyHandlerConnection(), Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bindService() returned " + isBind);
    }

    private class SpotifyHandlerConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected() called");
            SpotifyHandler.ServiceBinder binder = (SpotifyHandler.ServiceBinder) service;
            spotifyHandler = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected() called");
            spotifyHandler = null;
        }
    }


}
