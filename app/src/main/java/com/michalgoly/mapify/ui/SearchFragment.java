package com.michalgoly.mapify.ui;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.michalgoly.mapify.R;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;

public class SearchFragment extends Fragment implements SpotifyPlayer.NotificationCallback,
        ConnectionStateCallback {

    private static final String TAG = "SearchFragment";
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";

    private Toolbar toolbar = null;
    private MaterialSearchView materialSearchView = null;

    private SpotifyPlayer player = null;
    private String accessToken = null;
    private SpotifyApi spotifyApi = null;
    private SpotifyService spotifyService = null;
    private OnFragmentInteractionListener mListener = null;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static SearchFragment newInstance(String accessToken) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(KEY_ACCESS_TOKEN, accessToken);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accessToken = getArguments().getString(KEY_ACCESS_TOKEN);
            Log.i(TAG, "Access token inside the SearchFragment " + accessToken);
            Config config = new Config(getContext(), accessToken, getString(R.string.spotify_client_id));
            setPlayer(config);

            spotifyApi = new SpotifyApi();
            spotifyApi.setAccessToken(accessToken);
            spotifyService = spotifyApi.getService();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(KEY_ACCESS_TOKEN, accessToken);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.tb_search_fragment);
        toolbar.setTitle(getString(R.string.toolbar_title));
        toolbar.setTitleTextColor(Color.WHITE);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        materialSearchView = (MaterialSearchView) view.findViewById(R.id.tb_search_view);
//        materialSearchView.setSuggestions(getResources().getStringArray(R.array.search_suggestions));
//     TODO   materialSearchView.setVoiceSearch(true);
        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit called with query: " + query);
                new TracksTask().execute(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange called with newText: " + newText);
                return false;
            }
        });
        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {

            @Override
            public void onSearchViewShown() {
                Log.d(TAG, "onSearchViewShown called");
            }

            @Override
            public void onSearchViewClosed() {
                Log.d(TAG, "onSearchViewClosed");
            }
        });
        setHasOptionsMenu(true);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tb_search_menu, menu);
        MenuItem item = menu.findItem(R.id.tb_action_search);
        materialSearchView.setMenuItem(item);
        super.onCreateOptionsMenu(menu,inflater);
    }

    private void setPlayer(Config config) {
        Spotify.getPlayer(config, this, new SpotifyPlayer.InitializationObserver() {

            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                player = spotifyPlayer;
                player.addConnectionStateCallback(SearchFragment.this);
                player.addNotificationCallback(SearchFragment.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Failed to initialise the Spotify player inside the SearchFragment", throwable);
                getActivity().finishAffinity();
            }
        });
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
        Log.d(TAG, "onPlaybackEvent");
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(TAG, "onPlaybackError: " + error.toString());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class TracksTask extends AsyncTask<String, Void, TracksPager> {

        @Override
        protected TracksPager doInBackground(String... query) {
            return spotifyService.searchTracks(query[0]);
        }

        @Override
        protected void onPostExecute(TracksPager tracksPager) {
            if (tracksPager != null) {
                for (Track t : tracksPager.tracks.items)
                    Log.d(TAG, "id: " + t.id + ", name: " + t.name);
            } else {
                Log.d(TAG, "tracks was null");
            }
        }

    }
}
