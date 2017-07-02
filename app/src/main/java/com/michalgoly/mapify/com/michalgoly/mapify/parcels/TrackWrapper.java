package com.michalgoly.mapify.com.michalgoly.mapify.parcels;

import android.os.Parcel;
import android.os.Parcelable;

public class TrackWrapper implements Parcelable {

    private String title = null;
    private String artists = null;
    private String id = null;
    private String coverUrl = null;

    public TrackWrapper(String title, String artists, String id, String coverUrl) {
        this.title = title;
        this.artists = artists;
        this.id = id;
        this.coverUrl = coverUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    protected TrackWrapper(Parcel in) {
        title = in.readString();
        artists = in.readString();
        id = in.readString();
        coverUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artists);
        dest.writeString(id);
        dest.writeString(coverUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TrackWrapper> CREATOR = new Parcelable.Creator<TrackWrapper>() {
        @Override
        public TrackWrapper createFromParcel(Parcel in) {
            return new TrackWrapper(in);
        }

        @Override
        public TrackWrapper[] newArray(int size) {
            return new TrackWrapper[size];
        }
    };
}