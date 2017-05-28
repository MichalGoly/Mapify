package com.michalgoly.mapify.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.michalgoly.mapify.R;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback,
        ConnectionStateCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {

    }

    @Override
    public void onPlaybackError(Error error) {

    }
}
