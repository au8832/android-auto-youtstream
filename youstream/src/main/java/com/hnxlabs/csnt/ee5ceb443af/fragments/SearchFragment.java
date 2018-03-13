package com.hnxlabs.csnt.ee5ceb443af.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.apps.auto.sdk.ui.PagedListView;
import com.hnxlabs.csnt.ee5ceb443af.R;
import com.hnxlabs.csnt.ee5ceb443af.adapters.CardsAdapter;
import com.hnxlabs.csnt.ee5ceb443af.asynctasks.YoutubeSearchTask;
import com.hnxlabs.csnt.ee5ceb443af.data.TrackItem;
import com.hnxlabs.csnt.ee5ceb443af.listeners.FragmentCustomEvents;

import java.util.List;

/**
 * Created by ahmed abrar on 2/4/18.
 */

public class SearchFragment extends CarFragment {
    private final String TAG = "SearchFragment";
    private CardsAdapter mAdapter;
    private PagedListView mPagedListView;
    private int selectedPos = 0;
    private OnMotion onMotion;
    private SpinKitView mProgressBar;
    private String latestSearchQuery = null;
    private String SEARCH_QUERY_KEY = "SEARCH_QUERY_KEY";
    private String SELECTED_POS_KEY = "SELECTED_POS_KEY";

    public SearchFragment(){
        this.mAdapter = new CardsAdapter();
    }

    private void setFocus(){
        if(mPagedListView != null)
            mPagedListView.requestFocus();
    }

    private void setData(List<TrackItem> items){
        this.mAdapter = new CardsAdapter(items);
        this.selectedPos = 0;
        this.scrollTo(0);
        mPagedListView.setAdapter(this.mAdapter);
    }

    @Override
    public void setFragmentCustomEvents(FragmentCustomEvents fragmentCustomEvents){
        this.mAdapter.setLifecyleListener(fragmentCustomEvents);
    }

    public void search(String s){
        try {
            latestSearchQuery = s;
            mProgressBar.setVisibility(View.VISIBLE);
            List<TrackItem> items = new YoutubeSearchTask(this).execute(s).get();
            this.setData(items);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void setPosition(Integer pos){
        this.selectedPos = pos;
        this.scrollTo(pos);
    }

    public String getSelectedVideoId(){
        return this.mAdapter.getVideoId(selectedPos);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        View v = inflater.inflate(R.layout.searchview_fragment, container, false);
        mProgressBar = v.findViewById(R.id.youtubeLoading);
        mPagedListView = v.findViewById(R.id.pagedView);
        mPagedListView.removeDefaultItemDecoration();
        mPagedListView.setFocusable(true);
        mPagedListView.setEnabled(true);
        mPagedListView.setAdapter(this.mAdapter);
        onMotion = new OnMotion();
        mPagedListView.setOnGenericMotionListener(onMotion);
        setFocus();
        return v;
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
    public void onSaveInstanceState(Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_QUERY_KEY, latestSearchQuery);
        outState.putInt(SELECTED_POS_KEY, selectedPos);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(null != savedInstanceState) {
            latestSearchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY);
            Integer tempselectedPos = savedInstanceState.getInt(SELECTED_POS_KEY);
            if( latestSearchQuery != null) {
                this.search(latestSearchQuery);
                this.scrollTo(tempselectedPos);
                this.selectedPos = tempselectedPos;
            }
        }
    }
}
