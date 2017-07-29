package com.michalgoly.mapify.handlers;

import android.media.session.MediaSession;
import android.util.Log;

import com.michalgoly.mapify.parcels.TrackWrapper;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.LinkedList;
import java.util.List;

public class SpotifyHandler implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private static final String TAG = "SpotifyHandler";
    private static final String PLAYBACK_PLAY = "kSpPlaybackNotifyPlay";
    private static final String PLAYBACK_PAUSE = "kSpPlaybackNotifyPause";
    private static final String PLAYBACK_AUDIO_DELIVERY_DONE = "kSpPlaybackNotifyAudioDeliveryDone";

    private static SpotifyHandler instance = null;

    private SpotifyPlayer player = null;
    private TrackWrapper currentTrack = null;
    private LinkedList<TrackWrapper> previousTracks = null;
    private LinkedList<TrackWrapper> nextTracks = null;
    private PlaybackState currentPlaybackState = null;
    private Metadata metadata = null;
    private MediaSession mediaSession = null;
    private static int headsetClick = 0;

    private SpotifyHandler(Config config) {
        setPlayer(config);
    }

    public static SpotifyHandler getInstance(Config config) {
        if (instance == null)
            return new SpotifyHandler(config);
        else
            return instance;
    }

    public SpotifyPlayer getPlayer() {
        return player;
    }

    public TrackWrapper getCurrentTrack() {
        return currentTrack;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public PlaybackState getCurrentPlaybackState() {
        return currentPlaybackState;
    }

    public void play() {
        if (currentTrack != null && player != null) {
            if (currentPlaybackState != null) {
                player.playUri(null, currentTrack.getId(), 0, (int) currentPlaybackState.positionMs);
            } else {
                player.playUri(null, currentTrack.getId(), 0, 0);
            }
        } else {
            Log.d(TAG, "playSong(): currentTrack or player was null");
        }
    }

    public void pause() {
        if (currentTrack != null && player != null) {
            player.pause(null);
        } else {
            Log.d(TAG, "pauseSong(): currentTrack or player was null");
        }
    }

    public void playNext() {
        if (nextTracks != null && !nextTracks.isEmpty()) {
            previousTracks.offerLast(currentTrack);
            currentTrack = nextTracks.pollFirst();
            metadata = null;
            currentPlaybackState = null;
            play();
        } else {
            Log.d(TAG, "playNextSong(): trackQueue was null or empty");
        }
    }

    public void playPrevious() {
        if (previousTracks != null && !previousTracks.isEmpty()) {
            nextTracks.offerFirst(currentTrack);
            currentTrack = previousTracks.pollLast();
            metadata = null;
            currentPlaybackState = null;
            play();
        } else {
            Log.d(TAG, "playPreviousSong(): previousTrack was null or empty");
        }
    }

    /**
     * Updates the current playlist and sets the current track to the index provided. Tracks before
     * the current track will be treated as previous tracks, tracks after the current track as next
     * tracks in the queue.
     *
     * @param tracks List<TrackWrapper> - The list of the new tracks to play
     * @param index int - The position of the current track in the list provided
     */
    public void updatePlaylist(List<TrackWrapper> tracks, int index) {
        if (tracks == null || index < 0)
            throw new IllegalArgumentException("Parameter tracks was null, or index < 0");
        if (tracks.size() - 1 < index)
            throw new IllegalArgumentException("Parameter index was out of bounds");

        previousTracks = new LinkedList<>();
        nextTracks = new LinkedList<>();
        currentTrack = tracks.get(index);
        boolean found = false;
        for (int i = 0; i < tracks.size(); i++) {
            if (i < index)
                previousTracks.add(tracks.get(i));
            else if (i > index)
                nextTracks.add(tracks.get(i));
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "onLoggedIn");
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "onLoggedOut");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d(TAG, "onLoginFailed");
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "onTemporaryError");
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d(TAG, "onConnectionMessage");
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d(TAG, "Playback event received: " + playerEvent.name());
        currentPlaybackState = player.getPlaybackState();
        metadata = player.getMetadata();
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(TAG, "onPlaybackError: " + error.toString());
    }


    private void setPlayer(Config config) {
        Log.d(TAG, "Inside setPlayer" + config);
        Spotify.getPlayer(config, this, new SpotifyPlayer.InitializationObserver() {

            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                Log.d(TAG, "Setting the player");
                player = spotifyPlayer;
                player.addConnectionStateCallback(SpotifyHandler.this);
                player.addNotificationCallback(SpotifyHandler.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Failed to initialise the Spotify player inside the SpotifyHandler", throwable);
            }
        });
    }

}
