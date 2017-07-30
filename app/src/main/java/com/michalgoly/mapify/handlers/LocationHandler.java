package com.michalgoly.mapify.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationHandler {

    private static final String TAG = "LocationHandler";
    private static final long UPDATE_INTERVAL_MS = 10_000;
    private static final long FASTEST_UPDATE_INTERVAL_MS = UPDATE_INTERVAL_MS / 2;
    private static final float SMALLEST_DISPLACEMENT_M = 10;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static LocationHandler instance = null;

    private Context context = null;
    private FusedLocationProviderClient fusedLocationProviderClient = null;
    private SettingsClient settingsClient = null;
    private LocationSettingsRequest locationSettingsRequest = null;
    private LocationCallback locationCallback = null;
    private LocationRequest locationRequest = null;
    private Location currentLocation = null;

    private LocationHandler(Context context) {
        this.context = context;
//        initLocationServices();
    }

    public static LocationHandler getInstance(Context context) {
        if (instance == null)
            instance = new LocationHandler(context);
        return instance;
    }

//    private void initLocationServices() {
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient();
//        settingsClient = LocationServices.getSettingsClient(context);
//        locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                /*
//                 * 1. Add the locationResult to the List of locations
//                 * 2. Update the UI to draw the path
//                 */
//                Log.d(TAG, "onLocationResult: " + locationResult.getLastLocation().toString());
//                currentLocation = locationResult.getLastLocation();
//                points.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
//                if (mainActivityListener != null)
//                    mainActivityListener.onMapFragmentInteraction(-1);
//                redrawPath();
//            }
//        };
//        locationRequest = new LocationRequest();
//        locationRequest.setInterval(UPDATE_INTERVAL_MS);
//        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
//        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_M);
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationSettingsRequest = new LocationSettingsRequest.Builder()
//                .addLocationRequest(locationRequest).build();
//        startLocationUpdates();
//    }

//    private void startLocationUpdates() {
//        settingsClient.checkLocationSettings(locationSettingsRequest)
//                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
//                    @Override
//                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                        Log.i(TAG, "All location settings are satisfied.");
//                        try {
//                            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
//                                    locationCallback, Looper.myLooper());
//                        } catch (SecurityException e) {
//                            Log.wtf("Should never happen", e);
//                        }
//                    }
//                })
//                .addOnFailureListener(getActivity(), new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        int statusCode = ((ApiException) e).getStatusCode();
//                        switch (statusCode) {
//                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
//                                        "location settings ");
//                                try {
//                                    // Show the dialog by calling startResolutionForResult(), and check the
//                                    // result in onActivityResult().
//                                    ResolvableApiException rae = (ResolvableApiException) e;
//                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
//                                } catch (IntentSender.SendIntentException sie) {
//                                    Log.i(TAG, "PendingIntent unable to execute request.");
//                                }
//                                break;
//                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                                String errorMessage = "Location settings are inadequate, and cannot be " +
//                                        "fixed here. Fix in Settings.";
//                                Log.e(TAG, errorMessage);
//                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
//    }


}
