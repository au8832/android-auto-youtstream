package com.hnxlabs.csnt.youstream;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarToast;
import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.MenuController;
import com.google.android.apps.auto.sdk.MenuItem;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchItem;
import com.google.android.apps.auto.sdk.StatusBarController;
import com.google.android.apps.auto.sdk.notification.CarNotificationExtender;
import com.google.android.apps.auto.sdk.ui.CarLayoutManager;
import com.google.android.apps.auto.sdk.ui.CarRecyclerView;
import com.google.android.apps.auto.sdk.ui.PagedListView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.hnxlabs.csnt.youstream.adapters.CardsAdapter;
import com.hnxlabs.csnt.youstream.data.TrackItem;
import com.hnxlabs.csnt.youstream.listeners.FragmentsLifecyleListener;

import org.mortbay.jetty.Main;

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
    private FragmentsLifecyleListener mFragmentLifycycle = new FragmentsLifeCycleCallback();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main_car);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, searchFragment, FRAGMENT_SEARCH)
                .detach(searchFragment)
                .add(R.id.fragment_container, videoFragment, FRAGMENT_YOUTUBE)
                .detach(videoFragment)
                .commitNow();

        String initialFragmentTag = FRAGMENT_SEARCH;
        if (bundle != null && bundle.containsKey(CURRENT_FRAGMENT_KEY)) {
            initialFragmentTag = bundle.getString(CURRENT_FRAGMENT_KEY);
        }
        switchToFragment(initialFragmentTag);

        getCarUiController().getSearchController().setSearchCallback(new SearchCallback() {
            @Override
            public void onSearchItemSelected(SearchItem searchItem) {
                Log.d("serachcontroll",searchItem.getTitle().toString());
                String title = searchItem.getTitle().toString();
                searchFragment.startSearch(title);
            }

            @Override
            public boolean onSearchSubmitted(String s) {
                Log.d("searchbox", s);
                searchFragment.startSearch(s);
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
        getCarUiController().getStatusBarController().setAppBarAlpha(1.0f);
        getCarUiController().getStatusBarController().setAppBarBackgroundColor(((ColorDrawable)getDrawable(R.drawable.header_title).getCurrent()).getColor());
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
        Fragment newFragment = manager.findFragmentByTag(tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (currentFragment != null) {
            transaction.detach(currentFragment);
        }
        transaction.attach(newFragment);
        transaction.commit();
        mCurrentFragmentTag = tag;
    }

    @Override
    public void onStart() {
        super.onStart();
        // switchToFragment(mCurrentFragmentTag);
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
            ((CarFragment) f).setFragmentsLifecyleListener(mFragmentLifycycle);
        }
    };

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent){

        if(mCurrentFragmentTag.equals(FRAGMENT_YOUTUBE) && keyCode == KeyEvent.KEYCODE_BACK){
            videoFragment.onBackPressed();
            switchToFragment(FRAGMENT_SEARCH);
            return true;
        }
        Log.d("search_activity", "keycode="+keyCode+" videoid="+searchFragment.getSelectedVideoId());
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && mCurrentFragmentTag.equals(FRAGMENT_SEARCH) && searchFragment.getSelectedVideoId() != null) {
            videoFragment.setVideoId(searchFragment.getSelectedVideoId());
            switchToFragment(FRAGMENT_YOUTUBE);
        }
        return false;
    }

    @Override
    public void onBackPressed(){
        Log.d(mCurrentFragmentTag,"Back Pressed");
        if(mCurrentFragmentTag.equals(FRAGMENT_YOUTUBE))
            switchToFragment(FRAGMENT_SEARCH);
    }

    private class FragmentsLifeCycleCallback extends FragmentsLifecyleListener {

        @Override
        public void onReadyToDetach() {
            super.onReadyToDetach();
            videoFragment.onBackPressed();
            onBackPressed();
        }
        @Override
        public void onClickVideo(String videoId) {
            super.onClickVideo(videoId);
            if (mCurrentFragmentTag.equals(FRAGMENT_SEARCH) && searchFragment.getSelectedVideoId() != null) {
                videoFragment.setVideoId(videoId);
                switchToFragment(FRAGMENT_YOUTUBE);
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
