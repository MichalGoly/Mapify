package com.michalgoly.mapify.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.spotify.sdk.android.player.Metadata;

public class TrackWrapper implements Parcelable {

    private String title = null;
    private String artists = null;
    private String id = null;
    private String coverUrl = null;
    private Long duration = null;

    public TrackWrapper() {
        // empty constructor
    }

    public TrackWrapper(String title, String artists, String id, String coverUrl, Long duration) {
        this.title = title;
        this.artists = artists;
        this.id = id;
        this.coverUrl = coverUrl;
        this.duration = duration;
    }

    public static TrackWrapper fromTrack(Metadata.Track track) {
        return new TrackWrapper(track.name, track.artistName, track.uri, track.albumCoverWebUrl, track.durationMs);
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

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    protected TrackWrapper(Parcel in) {
        title = in.readString();
        artists = in.readString();
        id = in.readString();
        coverUrl = in.readString();
        duration = in.readLong();
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
        dest.writeLong(duration);
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