package com.michalgoly.mapify.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.michalgoly.mapify.R;
import com.michalgoly.mapify.com.michalgoly.mapify.parcels.TrackWrapper;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.LoginActivity;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class MainActivity extends AppCompatActivity implements SearchFragment.OnSearchFragmentInteractionListener,
        PlayerFragment.OnPlayerFragmentInteractionListener,
        MapFragment.OnMapFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_INTERNET = 0;
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    private static final String KEY_BOTTOM_MENU_ID = "KEY_BOTTOM_MENU";

    private BottomNavigationView bottomNavigationView = null;
    private int bottomItemId = -1;

    private String accessToken = null;
    private TrackWrapper currentTrack = null;
    private List<TrackWrapper> searchedTracks = null;
    private PlaybackState currentPlaybackState = null;
    private Metadata metadata = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            Log.i(TAG, "Bundle was not null");
            accessToken = savedInstanceState.getString(KEY_ACCESS_TOKEN);
            bottomItemId = savedInstanceState.getInt(KEY_BOTTOM_MENU_ID);
            if (bottomItemId != -1) {
                selectFragment(bottomItemId);
            }
        }

        if (!isLoggedIn() && internetPermissionGranted()) {
            authenticateSpotify();
        } else {
            if (accessToken == null) {
                Log.w(TAG, "User did not login, exiting...");
                finishAffinity();
            }
        }

        initUi(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onSaveInstanceState saving accessToken: " + accessToken);
        savedInstanceState.putString(KEY_ACCESS_TOKEN, accessToken);
        savedInstanceState.putInt(KEY_BOTTOM_MENU_ID, bottomItemId);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == LoginActivity.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                case TOKEN:
                    accessToken = response.getAccessToken();
                    Config playerConfig = new Config(this, accessToken, getString(R.string.spotify_client_id));
                    Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {

                        @Override
                        public void onInitialized(SpotifyPlayer spotifyPlayer) {
                            startSearchFragment();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Log.e(TAG, "Failed to initialise the Spotify player", throwable);
                            finishAffinity();
                        }
                    });
                    break;

                case ERROR:
                    Log.e(TAG, "The Spotify auth flow returned an error");
                    break;

                default:
                    Log.e(TAG, "Failed to authenticate with Spotify, most likely the auth flow "
                            + "was cancelled");
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_INTERNET:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    authenticateSpotify();
                } else {
                    Log.i(TAG, "No internet permission, closing the app...");
                    finishAffinity();
                }
                break;
            default:
                Log.d(TAG, "Should never happen, closing the app");
                finishAffinity();
                break;
        }
    }

    private void initUi(Bundle savedInstanceState) {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item.getItemId());
                return true;
            }
        });
    }

    private void selectFragment(int menuItemId) {
        Fragment fragment = null;
        switch (menuItemId) {
            case R.id.bottom_menu_search:
                if (accessToken != null)
                 fragment = SearchFragment.newInstance(accessToken, searchedTracks);
                else
                 Log.e(TAG, "selectFragment() the accessToken was null!");
                break;
            case R.id.bottom_menu_player:
                fragment = PlayerFragment.newInstance(accessToken, currentTrack, currentPlaybackState,
                        metadata);
                break;
            case R.id.bottom_menu_map:
                fragment = MapFragment.newInstance();
                break;
            default:
                Log.wtf(TAG, "Should never happen");
                break;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fl_content, fragment).commit();
    }

    private boolean internetPermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        if (checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
            return true;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.INSTALL_LOCATION_PROVIDER}, REQUEST_INTERNET);
        return false;
    }

    private void authenticateSpotify() {
        String clientId = getString(R.string.spotify_client_id);
        String redirectUri = getString(R.string.spotify_auth_callback);
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(clientId,
                AuthenticationResponse.Type.TOKEN, redirectUri);
        builder.setScopes(new String[] {"streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, LoginActivity.REQUEST_CODE, request);
    }

    private void startSearchFragment() {
        Fragment fragment = null;
        try {
            fragment = SearchFragment.newInstance(accessToken, searchedTracks);
        } catch (Exception e) {
            Log.e(TAG, "Failed to instantiate the SearchFragment", e);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragment).commit();
    }

    private boolean isLoggedIn() {
        return accessToken != null;
    }

    @Override
    public void onSearchFragmentInteraction(int menuItemId, TrackWrapper currentTrack,
                                            List<TrackWrapper> searchedTracks) {
        /*
         * 1. Update the currentTrack
         * 2. Update the searchedTracks
         * 3. Make sure the old currentPlaybackState and metadata are wiped
         * 2. Grab the menuItemId and select the appropriate bottom bar menu item
         */
        if (currentTrack != null)
           this.currentTrack = currentTrack;
        if (searchedTracks != null)
           this.searchedTracks = searchedTracks;
        this.currentPlaybackState = null;
        this.metadata = null;
        if (menuItemId != -1)
           bottomNavigationView.findViewById(menuItemId).performClick();
    }

    @Override
    public void onMapFragmentInteraction(int menuItemId, TrackWrapper currentTrack) {
        // currently no-op
    }

    @Override
    public void onPlayerFragmentInteraction(int menuItemId, TrackWrapper currentTrack,
                                            PlaybackState currentPlaybackState, Metadata metadata) {
        if (currentTrack != null)
            this.currentTrack = currentTrack;
        if (currentPlaybackState != null)
            this.currentPlaybackState = currentPlaybackState;
        if (metadata != null)
            this.metadata = metadata;
        if (menuItemId != -1)
            bottomNavigationView.findViewById(menuItemId).performClick();
    }


}
