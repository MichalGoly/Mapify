package com.michalgoly.mapify.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.michalgoly.mapify.R;
import com.michalgoly.mapify.handlers.LocationHandler;
import com.michalgoly.mapify.model.LocationTrackWrapper;
import com.michalgoly.mapify.model.PolylineWrapper;
import com.michalgoly.mapify.model.TrackWrapper;
import com.michalgoly.mapify.utils.AlertsManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {

    private static final String TAG = "MapFragment";
    private static final int MAP_UPDATE_DELAY_MS = 1000;

    private SupportMapFragment mapFragment = null;
    private GoogleMap googleMap = null;

    private LocationHandler locationHandler = null;
    private OnMapFragmentInteractionListener mainActivityListener = null;
    private ScheduledExecutorService timeUpdateService = Executors.newSingleThreadScheduledExecutor();

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
//        if (points != null)
//            args.putParcelableArrayList(KEY_POINTS, new ArrayList<>(points));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // no-op for now
            // points = getArguments().getParcelableArrayList(KEY_POINTS);
        }

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
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMapFragmentInteractionListener) {
            mainActivityListener = (OnMapFragmentInteractionListener) context;
            bindLocationHandler(context);
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
        try {
            this.googleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.e(TAG, "location was not enabled!", e);
            AlertsManager.alertAndExit(getContext(), "Location was not enabled!");
        }
        startTimer();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    /**
     * Interaction with the parent Activity
     */
    public interface OnMapFragmentInteractionListener {
        void onMapFragmentInteraction(int menuItemId);
    }

    /**
     * Schedules a task to update the map every 1s
     */
    private void startTimer() {
        timeUpdateService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateMap();
            }
        }, 0, MAP_UPDATE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void updateMap() {
        if (isAdded()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*
                     * 1. Retrieve the LocationTrackWrapper map from the LocationHandler
                     * 2. Iterate over the map
                     * 3. Create a list of PolyLineWrappers
                     * 4. Iterate over the list and draw it to the map
                     * 5. TODO cache it to prevent possible sluggishness
                     */
                    List<LocationTrackWrapper> locations = locationHandler.getLocations();
                    List<PolylineWrapper> polylines = extractPolylines(locations);
                    for (PolylineWrapper p : polylines)
                        drawToMap(p);
                }
            });
        }
    }

    private List<PolylineWrapper> extractPolylines(List<LocationTrackWrapper> locations) {
        List<PolylineWrapper> polylines = new ArrayList<>();
        TrackWrapper currentTrack = null;
        int currentIndex = -1;
        for (LocationTrackWrapper ltw : locations) {
            if (currentTrack == null) {
                currentTrack = ltw.getTrackWrapper();
                currentIndex = 0;
                polylines.add(new PolylineWrapper())
            }
        }
        return polylines;
    }

    private void drawToMap(PolylineWrapper wrapper) {

    }

    private void bindLocationHandler(Context context) {
        Log.d(TAG, "bindLocationHandler(context) called");
        Intent intent = new Intent(context, LocationHandler.class);
        boolean isBind = context.bindService(intent, new LocationHandlerConnection(), Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bindService() returned " + isBind);
    }

    private class LocationHandlerConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected() called");
            LocationHandler.ServiceBinder binder = (LocationHandler.ServiceBinder) service;
            locationHandler = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected() called");
            locationHandler = null;
        }
    }

//    private void redrawPath() {
//        if (googleMap != null) {
//            googleMap.clear();
//            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true)
//                    .visible(true);
//            for (LatLng p : points)
//                options.add(p);
//            path = googleMap.addPolyline(options);
//        }
//    }
//
//    private void enableLocation() {
//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission to access the location is missing.
//            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_LOCATION);
//        } else if (googleMap != null) {
//            // Access to the location has been granted to the app.
//            googleMap.setMyLocationEnabled(true);
//        }
//    }
}
