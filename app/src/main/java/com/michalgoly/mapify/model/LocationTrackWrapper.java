package com.michalgoly.mapify.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class LocationTrackWrapper implements Parcelable{

    private LatLng latLng = null;
    private TrackWrapper trackWrapper = null;
    private Date date = null;

    public LocationTrackWrapper(LatLng latLng, TrackWrapper trackWrapper, Date date) {
        this.latLng = latLng;
        this.trackWrapper = trackWrapper;
        this.date = date;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    protected LocationTrackWrapper(Parcel in) {
        latLng = (LatLng) in.readValue(LatLng.class.getClassLoader());
        trackWrapper = (TrackWrapper) in.readValue(TrackWrapper.class.getClassLoader());
        long tmpDate = in.readLong();
        date = tmpDate != -1 ? new Date(tmpDate) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(latLng);
        dest.writeValue(trackWrapper);
        dest.writeLong(date != null ? date.getTime() : -1L);
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
