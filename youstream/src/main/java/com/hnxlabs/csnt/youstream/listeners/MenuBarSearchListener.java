package com.hnxlabs.csnt.youstream.listeners;

import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchItem;
import com.hnxlabs.csnt.youstream.asynctasks.YoutubeSearchTask;
import com.hnxlabs.csnt.youstream.data.TrackItem;
import com.hnxlabs.csnt.youstream.fragments.SearchFragment;
import com.hnxlabs.csnt.youstream.asynctasks.SearchLookAheadTask;

import java.util.List;

/**
 * Created by ahmed abrar on 2/19/18.
 */

public class MenuBarSearchListener extends SearchCallback {

    private CarUiController carUiController;
    private SearchFragment searchFragment;

    public MenuBarSearchListener(CarUiController uiController, SearchFragment searchFragment){
        this.carUiController = uiController;
        this.searchFragment = searchFragment;
        this.carUiController.getSearchController().setSearchCallback(this);
    }

    @Override
    public void onSearchItemSelected(SearchItem searchItem) {
       this.searchFragment.search(searchItem.getTitle().toString());
    }

    @Override
    public boolean onSearchSubmitted(String s) {
        this.searchFragment.search(s);
        return true;
    }

    @Override
    public void onSearchTextChanged(String s) {
        if( s.length() > 1 )
            new SearchLookAheadTask(this.carUiController).execute(s);
    }
}
