package com.hnxlabs.csnt.youstream;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.ui.PagedListView;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PromotedItem;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.hnxlabs.csnt.youstream.adapters.CardsAdapter;
import com.hnxlabs.csnt.youstream.data.TrackItem;
import com.hnxlabs.csnt.youstream.listeners.FragmentsLifecyleListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed abrar on 2/4/18.
 */

public class SearchFragment extends CarFragment {
    private final String TAG = "SearchFragment";
    private CardsAdapter mAdapter;
    private ArrayList<TrackItem> searchList;
    private PagedListView mPagedListView;
    private int selectedPos = 0;
    private YouTube mYoutube;
    private OnMotion onMotion;
    private SpinKitView mProgressBar;
    private String latestSearchQuery = null;
    private String SEARCH_QUERY_KEY = "SEACRH_QUERY_KEY";
    private String SELECTED_POS_KEY = "SELECTED_POS_KEY";
    private String SEARCH_FRAGMENT_KEY = "SEARCH_FRAGMENT_KEY";

    public SearchFragment() {
        // Required empty public constructor
        searchList = new ArrayList<>();
        mAdapter = new CardsAdapter();
    }

    public String getSelectedVideoId(){
        return this.mAdapter.getVideoId(selectedPos);
    }

    private void setFocus(){
        if(mPagedListView != null)
            mPagedListView.requestFocus();
    }

    public void setSelectedPos(int selectedPos) {
        this.selectedPos = selectedPos;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setFocus();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach");
        setTitle(R.string.app_name);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        View v = inflater.inflate(R.layout.searchview_fragment, container, false);
        mProgressBar = v.findViewById(R.id.youtubeLoading);
        mPagedListView = v.findViewById(R.id.pagedView);
        mPagedListView.removeDefaultItemDecoration();
        mPagedListView.setAdapter(mAdapter);
        mPagedListView.setFocusable(true);
        mPagedListView.setEnabled(true);
        onMotion = new OnMotion();
        mPagedListView.setOnGenericMotionListener(onMotion);
        setFocus();
        return v;
    }

    public void startSearch(String query){
        mProgressBar.setVisibility(View.VISIBLE);
        latestSearchQuery = query;
        new Tasker(mAdapter, getFragmentsLifecyleListener(), selectedPos).execute(latestSearchQuery);
    }

    public void startSearch(String query, int position){
        this.selectedPos = position;
        this.startSearch(query);
    }

    private class OnMotion implements View.OnGenericMotionListener {

        @Override
        public boolean onGenericMotion(View view, MotionEvent motionEvent) {
            Log.d(TAG, "setting view selected");
            float direction = motionEvent.getAxisValue(MotionEvent.AXIS_VSCROLL);
            // set the current position within bounds
            scrollWithDirection(direction);
            return true;
        }
    }

    private void scrollWithDirection(float direction){

        if (direction == 1.0f) { //direction down
            if (selectedPos > mAdapter.getItemCount() - 2)
                selectedPos = mAdapter.getItemCount() - 2;
            scrollTo(++selectedPos);
        } else if (direction == -1.0f ) {
            if (selectedPos < 1)
                selectedPos = 1;
            scrollTo(--selectedPos);
        }
    }

    private void scrollTo(Integer index){
        mAdapter.selectIndex(index);
        mPagedListView.scrollToPosition(index);
    }

    @Override
    public void onStart() {
        super.onStart();
        getCarUiController().getSearchController().showSearchBox();
        getCarUiController().getStatusBarController().showAppHeader();
        getCarUiController().getStatusBarController().showConnectivityLevel();
        getCarUiController().getStatusBarController().setAppBarAlpha(0.0f);

        setFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.setLifecyleListener(getFragmentsLifecyleListener());
        int size = mAdapter.getItemCount();
        if(null != latestSearchQuery && !latestSearchQuery.isEmpty() && size < 1) {
            startSearch(latestSearchQuery);
        }
        else {
            scrollTo(selectedPos);
        }
        setFocus();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // call superclass to save any view hierarchy
        outState.putString(SEARCH_QUERY_KEY, latestSearchQuery);
        outState.putInt(SELECTED_POS_KEY, selectedPos);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(null != savedInstanceState) {
            latestSearchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY);
            selectedPos = savedInstanceState.getInt(SELECTED_POS_KEY);
        }
    }

    public class Tasker extends AsyncTask<String, String, CardsAdapter> {

        CardsAdapter adapter;
        private int position;
        public Tasker(CardsAdapter adapter, FragmentsLifecyleListener callback, int pos) {
            this.adapter = adapter;
            this.adapter.setLifecyleListener(callback);
            this.position = pos;
        }
        @Override
        protected CardsAdapter doInBackground(String... strings) {
            try {
                mYoutube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) throws IOException {
                    }
                }).setApplicationName("youtube-39568").build();
                YouTube.Search.List search = mYoutube.search().list("id,snippet");
                search.setKey("AIzaSyAOEG8BIIFYoS8NZ-gUqVertSHj01rE18g");
                search.setQ(strings[0]);
                search.setType("video");
                search.setMaxResults(30l);
                search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url, snippet/channelTitle)");
                SearchListResponse searchResponse = search.execute();
                List<SearchResult> searchResultList = searchResponse.getItems();
                this.adapter.clearAll();
                for(SearchResult s: searchResultList) {
                    this.adapter.add(new TrackItem(s));
                }

            }catch (Exception e) {
                Log.e("searchgrag", e.getMessage());
            }
            return this.adapter;
        }

        protected void onPostExecute(CardsAdapter result) {
            mProgressBar.setVisibility(View.GONE);
            result.notifyDataSetChanged();
            scrollTo(position);
        }
    }


}
