package com.michalgoly.mapify.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.michalgoly.mapify.R;
import com.michalgoly.mapify.handlers.SpotifyHandler;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.LoginActivity;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class MainActivity extends AppCompatActivity implements SearchFragment.OnSearchFragmentInteractionListener,
        PlayerFragment.OnPlayerFragmentInteractionListener, MapFragment.OnMapFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_INTERNET = 0;
    private static final int REQUEST_LOCATION = 1;
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    private static final String KEY_BOTTOM_MENU_ID = "KEY_BOTTOM_MENU";

    private BottomNavigationView bottomNavigationView = null;
    private int bottomItemId = -1;

    private SpotifyHandler spotifyHandler = null;
    private String accessToken = null;
    private MediaSession mediaSession = null;
    private static int headsetClick = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isLoggedIn() && internetPermissionGranted()) {
            authenticateSpotify();
        } else {
            if (accessToken == null) {
                Log.w(TAG, "User did not login, exiting...");
                finishAffinity();
            }
        }
        initUi();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(KEY_ACCESS_TOKEN, accessToken);
        bundle.putInt(KEY_BOTTOM_MENU_ID, bottomItemId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == LoginActivity.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                case TOKEN:
                    accessToken = response.getAccessToken();
                    final Config playerConfig = new Config(this, accessToken, getString(R.string.spotify_client_id));
                    Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {

                        @Override
                        public void onInitialized(SpotifyPlayer spotifyPlayer) {
                            Log.i(TAG, "onInitialized() called");
                            spotifyHandler = SpotifyHandler.getInstance(playerConfig);
                            registerMediaSession(getApplicationContext());
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
            case REQUEST_LOCATION:
                Log.d(TAG, "REQUEST_LOCATION ignore");
                break;
            default:
                Log.d(TAG, "Should never happen, closing the app");
                finishAffinity();
                break;
        }
    }

    @Override
    public void onSearchFragmentInteraction(int menuItemId) {
        if (menuItemId != -1)
            bottomNavigationView.findViewById(menuItemId).performClick();
    }

    @Override
    public void onMapFragmentInteraction(int menuitemId) {
        if (menuitemId != -1)
            bottomNavigationView.findViewById(menuitemId).performClick();
    }

    @Override
    public void onPlayerFragmentInteraction(int menuitemId) {
        if (menuitemId != -1)
            bottomNavigationView.findViewById(menuitemId).performClick();
    }

    private void initUi() {
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
                fragment = PlayerFragment.newInstance();
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
        Log.d(TAG, "authenticateSpotify() called");
        String clientId = getString(R.string.spotify_client_id);
        String redirectUri = getString(R.string.spotify_auth_callback);
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(clientId,
                AuthenticationResponse.Type.TOKEN, redirectUri);
        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, LoginActivity.REQUEST_CODE, request);
    }

    private void startSearchFragment() {
        Log.i(TAG, "startSearchFragment() called");
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

    // MediaSession handles the headset interactions
    private void registerMediaSession(final Context context) {
        mediaSession = new MediaSession(context, TAG);
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSession.Callback() {

            private long DOUBLE_CLICK_DELAY = 500;

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                spotifyHandler = SpotifyHandler.getInstance(null);
                KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        headsetClick++;
                        Handler handler = new Handler();
                        Runnable r = new Runnable() {

                            @Override
                            public void run() {
                                if (headsetClick == 1) {
                                    // single click
                                    if (spotifyHandler.getCurrentPlaybackState() != null) {
                                        if (spotifyHandler.getCurrentPlaybackState().isPlaying) {
                                            spotifyHandler.pause();
                                        } else {
                                            spotifyHandler.play();
                                        }
                                    }
                                } else if (headsetClick == 2) {
                                    // double click
                                    spotifyHandler.play();
                                }
                                headsetClick = 0;
                            }
                        };
                        if (headsetClick == 1) {
                            handler.postDelayed(r, DOUBLE_CLICK_DELAY);
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onPlay() {
                super.onPlay();
                SpotifyHandler.getInstance(null).play();
            }

            @Override
            public void onPause() {
                super.onPause();
                SpotifyHandler.getInstance(null).pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                SpotifyHandler.getInstance(null).playNext();
            }
        });
        mediaSession.setActive(true);
    }

}
