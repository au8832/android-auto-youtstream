package com.hnxlabs.csnt.youstream;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchItem;
import com.hnxlabs.csnt.youstream.listeners.FragmentsLifecyleListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by ahmed abrar on 2/4/18.
 */

public class MainCarActivity extends CarActivity {

    private static final String CURRENT_FRAGMENT_KEY = "app_current_fragment";
    private static final String FRAGMENT_SEARCH = "search";
    private static final String FRAGMENT_YOUTUBE = "YOUTUBE_FRAGMENT";

    private String mCurrentFragmentTag;
    private SearchFragment searchFragment = new SearchFragment();
    private VideoFragment videoFragment = new VideoFragment();
    private FragmentsLifecyleListener mCustomFragmentCallback = new CustomFragmentCallback();

    public MainCarActivity(){

    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main_car);

        if( null == bundle ) { //init fragment manager only once
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, searchFragment, FRAGMENT_SEARCH)
                    //.detach(searchFragment)
                    //.add(R.id.fragment_container, videoFragment, FRAGMENT_YOUTUBE)
                    //.detach(videoFragment)
                    .commitNow();
            mCurrentFragmentTag = FRAGMENT_SEARCH;
        }

        // String initialFragmentTag = FRAGMENT_SEARCH;
        if (bundle != null && bundle.containsKey(CURRENT_FRAGMENT_KEY)) {
            mCurrentFragmentTag = bundle.getString(CURRENT_FRAGMENT_KEY);
        }
        //switchToFragment(initialFragmentTag);

        getCarUiController().getSearchController().setSearchCallback(new SearchCallback() {
            @Override
            public void onSearchItemSelected(SearchItem searchItem) {
                Log.d("serachcontroll",searchItem.getTitle().toString());
                String title = searchItem.getTitle().toString();
                searchFragment.startSearch(title, 0);
            }

            @Override
            public boolean onSearchSubmitted(String s) {
                Log.d("searchbox", s);
                searchFragment.startSearch(s, 0);
                return true;
            }

            @Override
            public void onSearchTextChanged(String s) {
                if( s.length() > 1 )
                    new SearchTasker().execute(s);
            }
        });
        getCarUiController().getSearchController().showSearchBox();
        getCarUiController().getStatusBarController().showTitle();
        getCarUiController().getStatusBarController().setAppBarAlpha(0.0f);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks,false);

        /*ListMenuAdapter mainMenu = new ListMenuAdapter();
        mainMenu.setCallbacks(mMenuCallbacks);
        mainMenu.addMenuItem(MENU_HOME, new MenuItem.Builder()
                .setTitle(getString(R.string.demo_title))
                .setType(MenuItem.Type.ITEM)
                .build());
        mainMenu.addMenuItem(MENU_DEBUG, new MenuItem.Builder()
                .setTitle(getString(R.string.menu_debug_title))
                .setType(MenuItem.Type.SUBMENU)
                .build());

        ListMenuAdapter debugMenu = new ListMenuAdapter();
        debugMenu.setCallbacks(mMenuCallbacks);
        debugMenu.addMenuItem(MENU_DEBUG_LOG, new MenuItem.Builder()
                .setTitle(getString(R.string.menu_exlap_stats_log_title))
                .setType(MenuItem.Type.ITEM)
                .build());
        debugMenu.addMenuItem(MENU_DEBUG_TEST_NOTIFICATION, new MenuItem.Builder()
                .setTitle(getString(R.string.menu_test_notification_title))
                .setType(MenuItem.Type.ITEM)
                .build());
        mainMenu.addSubmenu(MENU_DEBUG, debugMenu);

        MenuController menuController = carUiController.getMenuController();
        menuController.setRootMenuAdapter(mainMenu);
        menuController.showMenuButton();
*/


    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString(CURRENT_FRAGMENT_KEY, mCurrentFragmentTag);
        super.onSaveInstanceState(bundle);
    }

    private void switchToFragment(String tag) {
        if (tag.equals(mCurrentFragmentTag)) {
            return;
        }
        FragmentManager manager = getSupportFragmentManager();
        Fragment currentFragment = mCurrentFragmentTag == null ? null : manager.findFragmentByTag(mCurrentFragmentTag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment newFragment = manager.findFragmentByTag(tag);

        if (currentFragment != null) {
            if(mCurrentFragmentTag.equals(FRAGMENT_SEARCH))
                transaction.replace(R.id.fragment_container, currentFragment, mCurrentFragmentTag);
            transaction.detach(currentFragment);
        }
        transaction.attach(newFragment);
        transaction.commit();
        mCurrentFragmentTag = tag;
    }

    private void playVideo(String videoId){
        VideoFragment v = new VideoFragment();
        v.setVideoId(videoId);
        FragmentManager manager = getSupportFragmentManager();
        Fragment searchFragment = manager.findFragmentByTag(FRAGMENT_SEARCH);
        manager.beginTransaction()
                .replace(R.id.fragment_container, searchFragment, FRAGMENT_SEARCH)
                .detach(searchFragment)
                .add(R.id.fragment_container, v, FRAGMENT_YOUTUBE)
                .commit();
        mCurrentFragmentTag = FRAGMENT_YOUTUBE;
    }

    private void playVideo(String videoId, int pos){
        FragmentManager manager = getSupportFragmentManager();
        SearchFragment searchFragment = (SearchFragment) manager.findFragmentByTag(FRAGMENT_SEARCH);
        searchFragment.setSelectedPos(pos);
        manager.beginTransaction()
                .replace(R.id.fragment_container, searchFragment, FRAGMENT_SEARCH)
                .commit();
        playVideo(videoId);
    }

    private void stopVideo(){
        FragmentManager manager = getSupportFragmentManager();
        Fragment videoFragement = manager.findFragmentByTag(FRAGMENT_YOUTUBE);
        Fragment searchFragment = manager.findFragmentByTag(FRAGMENT_SEARCH);
        manager.beginTransaction()
                .remove(videoFragement)
                .attach(searchFragment)
                .commit();
        mCurrentFragmentTag = FRAGMENT_SEARCH;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private final FragmentManager.FragmentLifecycleCallbacks mFragmentLifecycleCallbacks
            = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentStarted(FragmentManager fm, Fragment f) {
            switch (f.getTag()){
                case FRAGMENT_SEARCH:
                    searchFragment = (SearchFragment) f;
                    break;
                case FRAGMENT_YOUTUBE:
                    videoFragment = (VideoFragment) f;
                    break;
            }
            getCarUiController().getStatusBarController().setTitle(((CarFragment) f).getTitle());
        }
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState){
            ((CarFragment) f).setCarUiController(getCarUiController());
            ((CarFragment) f).setFragmentsLifecyleListener(mCustomFragmentCallback);
        }
    };

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent){

        if(mCurrentFragmentTag.equals(FRAGMENT_YOUTUBE) && keyCode == KeyEvent.KEYCODE_BACK){
            videoFragment.onBackPressed();
            //switchToFragment(FRAGMENT_SEARCH);
            stopVideo();
            return true;
        }
        String videoId = searchFragment.getSelectedVideoId();
        Log.d("search_activity", "keycode="+keyCode+" videoid="+videoId);
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && mCurrentFragmentTag.equals(FRAGMENT_SEARCH) &&  videoId != null) {
            //videoFragment.setVideoId(searchFragment.getSelectedVideoId());
            //switchToFragment(FRAGMENT_YOUTUBE);
            playVideo(videoId);
        }
        return false;
    }

    @Override
    public void onBackPressed(){
        Log.d(mCurrentFragmentTag,"Back Pressed");
        if(mCurrentFragmentTag.equals(FRAGMENT_YOUTUBE)){
            stopVideo();
        }
            //switchToFragment(FRAGMENT_SEARCH);
    }

    private class CustomFragmentCallback extends FragmentsLifecyleListener {

        @Override
        public void onReadyToDetach() {
            super.onReadyToDetach();
            videoFragment.onBackPressed();
            onBackPressed();
        }
        @Override
        public void onClickVideo(String videoId, Integer pos) {
            super.onClickVideo(videoId, pos);
            if (mCurrentFragmentTag.equals(FRAGMENT_SEARCH) && videoId != null) {
                //videoFragment.setVideoId(videoId);
                //switchToFragment(FRAGMENT_YOUTUBE);
                playVideo(videoId, pos);
            }
        }
    }
    public class SearchTasker extends AsyncTask<String, String, List<SearchItem>> {

        public SearchTasker() {

        }

        @Override
        protected List<SearchItem> doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            String url = "http://suggestqueries.google.com/complete/search?hl=en&ds=yt&q="+strings[0]+"&client=firefox";
            try {
                Response response = client.newCall(new Request.Builder().url(url).build()).execute();
                String r = response.body().string();
                r = r.replace("[","");
                r = r.replace("]", "");
                r = r.replace("\"","");
                String[] fin = r.split(",");
                List<SearchItem> searchItemList = new ArrayList<>();
                for( String se : fin)
                    searchItemList.add(new SearchItem.Builder().setTitle(se).build());
                return searchItemList;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(List<SearchItem> searchItemList) {
            if(searchItemList != null && searchItemList.size() > 0)
                getCarUiController().getSearchController().setSearchItems(searchItemList);
        }
    }

}
