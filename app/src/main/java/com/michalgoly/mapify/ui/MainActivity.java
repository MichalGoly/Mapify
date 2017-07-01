package com.michalgoly.mapify.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.widget.FrameLayout;

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

public class MainActivity extends AppCompatActivity implements SearchFragment.OnFragmentInteractionListener,
        PlayerFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_INTERNET = 0;
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    private static final String KEY_BOTTOM_MENU_ID = "KEY_BOTTOM_MENU";

    private BottomNavigationView bottomNavigationView = null;
    private int bottomItemId = -1;

//    private SpotifyPlayer player = null;
//    private PlaybackState playbackState = null;
//    private Metadata metadata = null;
    private String accessToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            Log.i(TAG, "Bundle was not null");
            accessToken = savedInstanceState.getString(KEY_ACCESS_TOKEN);
//            if (accessToken != null) {
//                Config config = new Config(this, accessToken, getString(R.string.spotify_client_id));
//                setPlayer(config);
//            }
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
//            player.playUri(null, metadata.currentTrack.uri, 0, (int) metadata.currentTrack.durationMs);
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
                    startSearchFragment();
//                    Config playerConfig = new Config(this, accessToken, getString(R.string.spotify_client_id));
//                    Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
//
//                        @Override
//                        public void onInitialized(SpotifyPlayer spotifyPlayer) {
//                            player = spotifyPlayer;
//                            player.addConnectionStateCallback(MainActivity.this);
//                            player.addNotificationCallback(MainActivity.this);
//                            startSearchFragment();
//                        }
//
//                        @Override
//                        public void onError(Throwable throwable) {
//                            Log.e(TAG, "Failed to initialise the Spotify player", throwable);
//                            finishAffinity();
//                        }
//                    });
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
                 fragment = SearchFragment.newInstance(accessToken);
                else
                 Log.e(TAG, "selectFragment() the accessToken was null!");
                break;
            case R.id.bottom_menu_player:
                fragment = PlayerFragment.newInstance(accessToken, null); // TODO change
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

//    private void setPlayer(Config config) {
//        Spotify.getPlayer(config, this, new SpotifyPlayer.InitializationObserver() {
//
//            @Override
//            public void onInitialized(SpotifyPlayer spotifyPlayer) {
//                player = spotifyPlayer;
//                player.addConnectionStateCallback(MainActivity.this);
//                player.addNotificationCallback(MainActivity.this);
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                Log.e(TAG, "Failed to initialise the Spotify player", throwable);
//                finishAffinity();
//            }
//        });
//    }

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
            fragment = SearchFragment.newInstance(accessToken);
        } catch (Exception e) {
            Log.e(TAG, "Failed to instantiate the SearchFragment", e);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragment).commit();
    }

    private boolean isLoggedIn() {
        return accessToken != null;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // no-op for now
    }
}
