package com.michalgoly.mapify.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.michalgoly.mapify.R;
import com.michalgoly.mapify.handlers.SpotifyHandler;
import com.michalgoly.mapify.parcels.TrackWrapper;
import com.michalgoly.mapify.utils.ItemClickSupport;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";

    private Toolbar toolbar = null;
    private TextView infoTextView = null;
    private MaterialSearchView materialSearchView = null;
    private RecyclerView recyclerView = null;
    private RecyclerView.Adapter recyclerViewAdaper = null;

    private List<TrackWrapper> searchedTracks = null;
    private TrackWrapper currentTrack = null;
    private List<String> recentSearches = null; // in the future will be persisted in a db
    private String accessToken = null;
    private SpotifyApi spotifyApi = null;
    private SpotifyService spotifyService = null;
    private OnSearchFragmentInteractionListener mainActivityListener = null;

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
            spotifyApi = new SpotifyApi();
            spotifyApi.setAccessToken(accessToken);
            spotifyService = spotifyApi.getService();
        }
        currentTrack = SpotifyHandler.getInstance(null).getCurrentTrack();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(KEY_ACCESS_TOKEN, accessToken);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.tb_search_fragment);
        toolbar.setTitle(getString(R.string.toolbar_title));
        toolbar.setTitleTextColor(Color.WHITE);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        infoTextView = (TextView) view.findViewById(R.id.tv_search_info);
        infoTextView.setVisibility(View.GONE);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_search);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerViewAdaper = new TracksAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recyclerViewAdaper);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {

            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Log.d(TAG, "Item " + position + " with id " + searchedTracks.get(position).getId() + " clicked");
                mainActivityListener.onSearchFragmentInteraction(R.id.bottom_menu_player;
            }
        });

        if (recentSearches == null && searchedTracks == null) {
            recyclerView.setVisibility(View.GONE);
            infoTextView.setText(getString(R.string.no_recent_searches));
            infoTextView.setVisibility(View.VISIBLE);
        }

        materialSearchView = (MaterialSearchView) view.findViewById(R.id.tb_search_view);
        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit called with query: " + query);
                new TracksTask().execute(query.trim());
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSearchFragmentInteractionListener) {
            mainActivityListener = (OnSearchFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSearchFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tb_search_menu, menu);
        MenuItem item = menu.findItem(R.id.tb_action_search);
        materialSearchView.setMenuItem(item);
        super.onCreateOptionsMenu(menu,inflater);
    }

    /**
     * Interaction with the parent Activity
     */
    public interface OnSearchFragmentInteractionListener {
        void onSearchFragmentInteraction(int menuItemId);
    }

    private class TracksAdapter extends RecyclerView.Adapter<TracksView> {

        private final LayoutInflater inflater;

        public TracksAdapter() {
            inflater = LayoutInflater.from(getContext());
        }

        @Override
        public TracksView onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.search_row, parent, false);
            TracksView tracksView = new TracksView(view);
            return tracksView;
        }

        @Override
        public void onBindViewHolder(TracksView holder, int position) {
            holder.setIsRecyclable(false);
            if (searchedTracks != null) {
                holder.title.setText(searchedTracks.get(position).getTitle());
                holder.artists.setText(searchedTracks.get(position).getArtists());
                if (currentTrack != null
                        && searchedTracks.get(position).getId().equals(currentTrack.getId())) {
                    holder.title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                    holder.artists.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                }
            } else {
                Log.d(TAG, "searchedTrack was null in onBindViewHolder");
            }
        }

        @Override
        public int getItemCount() {
            return searchedTracks == null ? 0 : searchedTracks.size();
        }
    }

    private class TracksView extends RecyclerView.ViewHolder {

        private TextView title = null;
        private TextView artists = null;

        public TracksView(View v) {
            super(v);
            this.title = (TextView) v.findViewById(R.id.tv_row_title);
            this.artists = (TextView) v.findViewById(R.id.tv_row_artists);
        }
    }

    private class TracksTask extends AsyncTask<String, Void, TracksPager> {

        @Override
        protected TracksPager doInBackground(String... query) {
            return spotifyService.searchTracks(query[0]);
        }

        @Override
        protected void onPostExecute(TracksPager tracksPager) {
            if (tracksPager != null) {
                searchedTracks = new ArrayList<>();
                infoTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                for (Track t : tracksPager.tracks.items) {
                    if (t.album.images.isEmpty())
                        continue;
                    String artists = "";
                    for (int i = 0; i < t.artists.size(); i++) {
                        if (i + 1 == t.artists.size())
                            artists += t.artists.get(i).name;
                        else
                            artists += t.artists.get(i).name + ", ";
                    }
                    String spotifyTrackPrefix = "spotify:track:";
                    searchedTracks.add(new TrackWrapper(t.name, artists, spotifyTrackPrefix + t.id,
                            t.album.images.get(0).url, t.duration_ms));
                }
                recyclerViewAdaper.notifyDataSetChanged();
                mainActivityListener.onSearchFragmentInteraction(-1);
            } else {
                Log.d(TAG, "tracks was null");
            }
        }

    }

    private class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private Drawable divider;

        public DividerItemDecoration(Context context) {
            divider = context.getResources().getDrawable(R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + divider.getIntrinsicHeight();

                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
    }
}
