package com.michalgoly.mapify.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.michalgoly.mapify.R;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.LoginActivity;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback,
        ConnectionStateCallback {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_INTERNET = 0;

    private BottomNavigationView bottomNavigationView = null;
    private int selectedItem = -1;

    private static SpotifyPlayer player = null;
    private static PlaybackState playbackState = null;
    private static Metadata metadata = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isLoggedIn() && internetPermissionGranted()) {
            authenticateSpotify();
        } else {
            player.playUri(null, metadata.currentTrack.uri, 0, (int) metadata.currentTrack.durationMs);
        }

        initUi(savedInstanceState);
    }

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "User logged in to Spotify");
//        player.playUri(null, "spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "User logged out of Spotify");
    }

    @Override
    public void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d(TAG, "User failed to log into Spotify");
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "Spotify temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(TAG, "Spotify connection message received: " + message);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d(TAG, "Playback event received: " + playerEvent.name());
        playbackState = player.getPlaybackState();
        metadata = player.getMetadata();
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(TAG, "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == LoginActivity.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                case TOKEN:
                    Config playerConfig = new Config(this, response.getAccessToken(),
                            getString(R.string.spotify_client_id));
                    Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {

                        @Override
                        public void onInitialized(SpotifyPlayer spotifyPlayer) {
                            player = spotifyPlayer;
                            player.addConnectionStateCallback(MainActivity.this);
                            player.addNotificationCallback(MainActivity.this);
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
                selectFragment(item);
                return true;
            }
        });
        FrameLayout content = (FrameLayout) findViewById(R.id.fl_content);
        content.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }

    private void selectFragment(MenuItem menuItem) {
        Toast.makeText(this, "Menu item: " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
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

    private boolean isLoggedIn() {
        return player != null && player.isLoggedIn();
    }
}
