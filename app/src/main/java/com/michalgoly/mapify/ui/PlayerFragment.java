package com.michalgoly.mapify.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.michalgoly.mapify.R;
import com.michalgoly.mapify.com.michalgoly.mapify.parcels.TrackWrapper;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PlayerFragment extends Fragment implements SpotifyPlayer.NotificationCallback,
        ConnectionStateCallback {

    private static final String TAG = "PlayerFragment";
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    private static final String KEY_CURRENT_TRACK= "KEY_CURRENT_TRACK";

    private String accessToken = null;
    private SpotifyPlayer player = null;
    private TrackWrapper currentTrack = null;
    private PlaybackState currentPlaybackState = null;
    private Metadata metadata = null;

    private OnFragmentInteractionListener mListener = null;

    private Toolbar toolbar = null;
    private TextView titleTextView = null;
    private TextView artistsTextView = null;
    private ImageView playPauseImageView = null;
    private ImageView previousImageView = null;
    private ImageView nextImageView = null;

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance(String accessToken, TrackWrapper currentTrack) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(KEY_ACCESS_TOKEN, accessToken);
        args.putParcelable(KEY_CURRENT_TRACK, currentTrack);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accessToken = getArguments().getString(KEY_ACCESS_TOKEN);
            Log.i(TAG, "Access token inside the PlayerFragment " + accessToken);
            Config config = new Config(getContext(), accessToken, getString(R.string.spotify_client_id));
            setPlayer(config);
            currentTrack = getArguments().getParcelable(KEY_CURRENT_TRACK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.tb_player_fragment);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        titleTextView = (TextView) view.findViewById(R.id.tv_player_track);
        artistsTextView = (TextView) view.findViewById(R.id.tv_player_artists);
        previousImageView = (ImageView) view.findViewById(R.id.iv_previous);
        previousImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "previousImageView clicked");
            }
        });
        nextImageView = (ImageView) view.findViewById(R.id.iv_next);
        nextImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "nextImageView clicked");
            }
        });
        playPauseImageView = (ImageView) view.findViewById(R.id.iv_play_pause);
        playPauseImageView.setTag(R.drawable.ic_play_arrow_black_24dp);
        playPauseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch ((Integer) playPauseImageView.getTag()) {
                    case R.drawable.ic_play_arrow_black_24dp:
                        playPauseImageView.setImageResource(R.drawable.ic_pause_black_24dp);
                        playPauseImageView.setTag(R.drawable.ic_pause_black_24dp);
                        playSong();
                        break;
                    default:
                        playPauseImageView.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        playPauseImageView.setTag(R.drawable.ic_play_arrow_black_24dp);
                        pauseSong();
                        break;
                }
            }
        });
        // start playing the clicked track if a user navigated here through the SearchFragment
        playSong();
        updateUi();
        return view;
    }

    private void setPlayer(Config config) {
        Log.d(TAG, "Inside setPlayer" + config);
        Spotify.getPlayer(config, this, new SpotifyPlayer.InitializationObserver() {

            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                Log.d(TAG, "Setting the player");
                player = spotifyPlayer;
                player.addConnectionStateCallback(PlayerFragment.this);
                player.addNotificationCallback(PlayerFragment.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Failed to initialise the Spotify player inside the PlayerFragment", throwable);
                getActivity().finishAffinity();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(KEY_ACCESS_TOKEN, accessToken);
        bundle.putParcelable(KEY_CURRENT_TRACK, currentTrack);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
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
        // update the UI only on pause and play events
        if (playerEvent.name().equals("kSpPlaybackNotifyPause")
                || playerEvent.name().equals("kSpPlaybackNotifyPlay")) {
            updateUi();
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(TAG, "onPlaybackError: " + error.toString());
    }

    /**
     * Interaction with the parent Activity
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int menuitemId, TrackWrapper currentTrack);
    }

    private void playSong() {
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

    private void pauseSong() {
        if (currentTrack != null && player != null) {
            player.pause(null);
        } else {
            Log.d(TAG, "pauseSong(): currentTrack or player was null");
        }
    }

    private void updateUi() {

        /*
         * 1. Check if there is a current song and update the cover, song title and artist
         * 2. Otherwise, fill the toolbar with the primaryColor, set artist to an empty string and
         * ask the user to search for a song
         * 3. If the current song is playing, show the pause button
         * 4. Otherwise, show the play button
         */
        if (isAdded()) {
            if (currentTrack != null) {
                titleTextView.setText(currentTrack.getTitle());
                artistsTextView.setText(currentTrack.getArtists());
                new CoverTask().execute(currentTrack.getCoverUrl());
            } else {
                titleTextView.setText(getActivity().getString(R.string.ask_user_search));
                artistsTextView.setText("");
                toolbar.setBackground(null);
                toolbar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            }
            if (currentTrack != null && currentPlaybackState != null && currentPlaybackState.isPlaying) {
                playPauseImageView.setImageResource(R.drawable.ic_pause_black_24dp);
                playPauseImageView.setTag(R.drawable.ic_pause_black_24dp);
            } else {
                playPauseImageView.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                playPauseImageView.setTag(R.drawable.ic_play_arrow_black_24dp);
            }
        }
    }

    private class CoverTask extends AsyncTask<String, Void, Drawable> {
        @Override
        protected Drawable doInBackground(String... params) {
            Drawable cover = null;
            try (InputStream in = new BufferedInputStream((InputStream) new URL(
                    currentTrack.getCoverUrl()).getContent())) {
                cover = Drawable.createFromStream(in, "Spotify URL cover");
            } catch (IOException e) {
                Log.w(TAG, "Failed to read the cover url: " + currentTrack.getCoverUrl(), e);
            }
            return cover;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (isAdded()) {
                if (drawable != null) {
                    toolbar.setBackground(drawable);
                } else {
                    toolbar.setBackground(null);
                    toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                }
            }
        }
    }
}
