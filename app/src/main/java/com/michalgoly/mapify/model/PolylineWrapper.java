package com.michalgoly.mapify.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.Date;
import java.util.List;

public class PolylineWrapper {

    private List<LatLng> points = null;
    private int color = -1;
    private TrackWrapper trackWrapper = null;
    private Date startDate = null;
    private Date endDate = null;

    public PolylineWrapper() {
        // empty constructor
    }

    public PolylineWrapper(List<LatLng> points, int color, TrackWrapper trackWrapper, Date startDate, Date endDate) {
        this.points = points;
        this.color = color;
        this.trackWrapper = trackWrapper;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public TrackWrapper getTrackWrapper() {
        return trackWrapper;
    }

    public void setTrackWrapper(TrackWrapper trackWrapper) {
        this.trackWrapper = trackWrapper;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
