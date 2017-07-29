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
import android.widget.SeekBar;
import android.widget.TextView;

import com.michalgoly.mapify.R;
import com.michalgoly.mapify.handlers.SpotifyHandler;
import com.michalgoly.mapify.parcels.TrackWrapper;
import com.spotify.sdk.android.player.PlaybackState;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerFragment extends Fragment {

    private static final String TAG = "PlayerFragment";
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";

    private SpotifyHandler spotifyHandler = null;

    private OnPlayerFragmentInteractionListener mainActivityListener = null;
    private ScheduledExecutorService timeUpdateService = Executors.newSingleThreadScheduledExecutor();

    private Toolbar toolbar = null;
    private SeekBar trackProgressBar = null;
    private TextView titleTextView = null;
    private TextView artistsTextView = null;
    private ImageView playPauseImageView = null;
    private ImageView previousImageView = null;
    private ImageView nextImageView = null;

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance() {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
//        args.putString(KEY_ACCESS_TOKEN, accessToken);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // no-op for now
        }
        spotifyHandler = SpotifyHandler.getInstance(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.tb_player_fragment);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        trackProgressBar = (SeekBar) view.findViewById(R.id.sb_player);
        trackProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // no-op
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // no-op
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch called");
                // update the currentSong to the current progress
                if (spotifyHandler.getCurrentTrack() != null) {
                    spotifyHandler.getPlayer().seekToPosition(null, seekBar.getProgress());
                }
            }
        });
        titleTextView = (TextView) view.findViewById(R.id.tv_player_track);
        artistsTextView = (TextView) view.findViewById(R.id.tv_player_artists);
        previousImageView = (ImageView) view.findViewById(R.id.iv_previous);
        previousImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "previousImageView clicked");
                spotifyHandler.playPrevious();
            }
        });
        nextImageView = (ImageView) view.findViewById(R.id.iv_next);
        nextImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "nextImageView clicked");
                spotifyHandler.playNext();
            }
        });
        playPauseImageView = (ImageView) view.findViewById(R.id.iv_play_pause);
        playPauseImageView.setTag(R.drawable.ic_play_arrow_black_24dp);
        playPauseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch ((Integer) playPauseImageView.getTag()) {
                    case R.drawable.ic_play_arrow_black_24dp:
                        spotifyHandler.play();
                        break;
                    default:
                        spotifyHandler.pause();
                        break;
                }
            }
        });
        startTimer();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlayerFragmentInteractionListener) {
            mainActivityListener = (OnPlayerFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlayerFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Interaction with the parent Activity
     */
    public interface OnPlayerFragmentInteractionListener {
        void onPlayerFragmentInteraction(int menuitemId);
    }

    private void updateUi() {
        /*
         * 1. Check if there is a current song and update the cover, song title and artist
         * 2. Otherwise, fill the toolbar with the primaryColor, set artist to an empty string and
         * ask the user to search for a song
         * 3. If the current song is playing, show the pause button
         * 4. Otherwise, show the play button
         * 5. Update the song progress bar
         */
        try {
            if (isAdded()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TrackWrapper currentTrack = spotifyHandler.getCurrentTrack();
                        PlaybackState currentPlaybackState = spotifyHandler.getCurrentPlaybackState();
                        if (currentTrack != null) {
                            titleTextView.setText(currentTrack.getTitle());
                            artistsTextView.setText(currentTrack.getArtists());
                            trackProgressBar.setMax(currentTrack.getDuration().intValue());
                            new CoverTask().execute(currentTrack.getCoverUrl()); // TODO this should be cached!
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
                        if (currentPlaybackState != null) {
                            trackProgressBar.setProgress((int) currentPlaybackState.positionMs);
                        } else {
                            trackProgressBar.setProgress(0);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Schedules a task to update the UI every 0.1s
     */
    private void startTimer() {
        timeUpdateService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateUi();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private class CoverTask extends AsyncTask<String, Void, Drawable> {
        @Override
        protected Drawable doInBackground(String... params) {
            Drawable cover = null;
            TrackWrapper currentTrack = spotifyHandler.getCurrentTrack();
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
