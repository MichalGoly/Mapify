package com.michalgoly.mapify.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class LocationTrackWrapper implements Parcelable {

    private LatLng latLng = null;
    private TrackWrapper trackWrapper = null;

    public LocationTrackWrapper(LatLng latLng, TrackWrapper trackWrapper) {
        this.latLng = latLng;
        this.trackWrapper = trackWrapper;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public TrackWrapper getTrackWrapper() {
        return trackWrapper;
    }

    public void setTrackWrapper(TrackWrapper trackWrapper) {
        this.trackWrapper = trackWrapper;
    }

    protected LocationTrackWrapper(Parcel in) {
        latLng = (LatLng) in.readValue(LatLng.class.getClassLoader());
        trackWrapper = (TrackWrapper) in.readValue(TrackWrapper.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(latLng);
        dest.writeValue(trackWrapper);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<LocationTrackWrapper> CREATOR = new Parcelable.Creator<LocationTrackWrapper>() {
        @Override
        public LocationTrackWrapper createFromParcel(Parcel in) {
            return new LocationTrackWrapper(in);
        }

        @Override
        public LocationTrackWrapper[] newArray(int size) {
            return new LocationTrackWrapper[size];
        }
    };
}
