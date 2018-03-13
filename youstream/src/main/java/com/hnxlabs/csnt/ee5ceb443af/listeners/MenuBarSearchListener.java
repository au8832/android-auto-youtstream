package com.hnxlabs.csnt.ee5ceb443af.listeners;

import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchItem;
import com.hnxlabs.csnt.ee5ceb443af.fragments.SearchFragment;
import com.hnxlabs.csnt.ee5ceb443af.asynctasks.SearchLookAheadTask;

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
