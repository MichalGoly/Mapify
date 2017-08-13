package com.michalgoly.mapify.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.michalgoly.mapify.R;
import com.michalgoly.mapify.handlers.LocationHandler;
import com.michalgoly.mapify.model.LocationTrackWrapper;
import com.michalgoly.mapify.model.PolylineWrapper;
import com.michalgoly.mapify.model.TrackWrapper;
import com.michalgoly.mapify.utils.AlertsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnPolylineClickListener {

    private static final String TAG = "MapFragment";
    private static final int MAP_UPDATE_DELAY_MS = 1000;
    private static final int[] polylineColors = new int[]{R.color.materialAmber, R.color.materialBlue,
            R.color.materialBlueGrey, R.color.materialBrown, R.color.materialCyan, R.color.materialDeepOrange,
            R.color.materialDeepPurple, R.color.materialGreen, R.color.materialIndygo, R.color.materialGrey,
            R.color.materialLightBlue, R.color.materialLightGreen, R.color.materialRed, R.color.materialOrange,
            R.color.materialTeal, R.color.materialLime, R.color.materialPink, R.color.materialPurple};
    private static final float POLYLINE_WIDTH = 5;

    private SupportMapFragment mapFragment = null;
    private GoogleMap googleMap = null;

    private LocationHandler locationHandler = null;
    private OnMapFragmentInteractionListener mainActivityListener = null;
    private ScheduledExecutorService timeUpdateService = Executors.newSingleThreadScheduledExecutor();
    private Random random = new Random();

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
        this.googleMap.setOnPolylineClickListener(this);
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

    @Override
    public void onPolylineClick(Polyline polyline) {
        polyline.setWidth(POLYLINE_WIDTH * 2);
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
                    drawToMap(polylines);
                }
            });
        }
    }

    private List<PolylineWrapper> extractPolylines(List<LocationTrackWrapper> locations) {
        List<PolylineWrapper> polylines = new ArrayList<>();
        if (locations != null) {
            Collections.sort(locations, new Comparator<LocationTrackWrapper>() {
                @Override
                public int compare(LocationTrackWrapper o1, LocationTrackWrapper o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
            PolylineWrapper wrapper = null;
            TrackWrapper currentTrack = null;
            List<LatLng> points = null;
            for (int i = 0; i < locations.size(); i++) {
                if (currentTrack == null) {
                    // new
                    currentTrack = locations.get(i).getTrackWrapper();
                    points = new ArrayList<>();
                    points.add(locations.get(i).getLatLng());
                    wrapper = new PolylineWrapper();
                    wrapper.setColor(ContextCompat.getColor(getContext(), getColor(currentTrack)));
                    wrapper.setStartDate(locations.get(i).getDate());
                    wrapper.setTrackWrapper(locations.get(i).getTrackWrapper());
                } else {
                    if (currentTrack != locations.get(i).getTrackWrapper()) {
                        // next
                        currentTrack = locations.get(i).getTrackWrapper();
                        points = new ArrayList<>();
                        points.add(locations.get(i).getLatLng());
                        wrapper = new PolylineWrapper();
                        wrapper.setColor(ContextCompat.getColor(getContext(), getColor(currentTrack)));
                        wrapper.setStartDate(locations.get(i).getDate());
                        wrapper.setTrackWrapper(locations.get(i).getTrackWrapper());
                    } else {
                        // continuation of the current track
                        points.add(locations.get(i).getLatLng());
                    }
                }
                // end date check
                if (i + 1 < locations.size()) {
                    if (currentTrack != locations.get(i + 1).getTrackWrapper()) {
                        wrapper.setEndDate(locations.get(i).getDate());
                        wrapper.setPoints(points);
                        polylines.add(wrapper);
                    }
                } else {
                    wrapper.setEndDate(locations.get(i).getDate());
                    wrapper.setPoints(points);
                    polylines.add(wrapper);
                }
            }
        }
        return polylines;
    }

    private void drawToMap(List<PolylineWrapper> wrappers) {
        for (PolylineWrapper pw : wrappers) {
            googleMap.addPolyline(new PolylineOptions()
                    .addAll(pw.getPoints())
                    .color(pw.getColor())
                    .clickable(true)
                    .width(POLYLINE_WIDTH));
        }

    }

    private void bindLocationHandler(Context context) {
        Log.d(TAG, "bindLocationHandler(context) called");
        Intent intent = new Intent(context, LocationHandler.class);
        boolean isBind = context.bindService(intent, new LocationHandlerConnection(), Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bindService() returned " + isBind);
    }

    /**
     * Generates a colour based on the track's unique id
     *
     * @param track TrackWrapper - The track containing the id to generate the colour from
     * @return int - The resource id of the colour
     */
    private int getColor(TrackWrapper track) {
        int hash = track.getId().hashCode();
        if (hash < 0)
            hash *= -1;
        return polylineColors[hash % polylineColors.length];
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
}
