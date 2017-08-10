package com.michalgoly.mapify.model;

import android.graphics.Color;

import com.google.android.gms.maps.model.Polyline;

import java.util.Date;

public class PolylineWrapper {

    private Polyline polyline = null;
    private Color color = null;
    private TrackWrapper trackWrapper = null;
    private Date startDate = null;
    private Date endDate = null;

    public PolylineWrapper(Polyline polyline, Color color, TrackWrapper trackWrapper, Date startDate, Date endDate) {
        this.polyline = polyline;
        this.color = color;
        this.trackWrapper = trackWrapper;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
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
