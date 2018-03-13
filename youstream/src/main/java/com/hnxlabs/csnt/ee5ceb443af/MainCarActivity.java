package com.hnxlabs.csnt.ee5ceb443af;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.google.android.apps.auto.sdk.CarActivity;
import com.hnxlabs.csnt.ee5ceb443af.fragments.CarFragment;
import com.hnxlabs.csnt.ee5ceb443af.fragments.SearchFragment;
import com.hnxlabs.csnt.ee5ceb443af.fragments.VideoFragment;
import com.hnxlabs.csnt.ee5ceb443af.listeners.FragmentCustomEvents;
import com.hnxlabs.csnt.ee5ceb443af.listeners.MenuBarSearchListener;

/**
 * Created by ahmed abrar on 2/4/18.
 */

public class MainCarActivity extends CarActivity {

    private static final String SEARCH_FRAGMENT_KEY = "SEARCH_FRAGMENT";
    private static final String CURRENT_FRAGMENT_TAG_KEY = "app_current_fragment_tag";
    private static final String FRAGMENT_SEARCH = "SEARCH_VIDEO_FRAGMENT";
    private static final String FRAGMENT_YOUTUBE = "YOUTUBE_VIDEO_FRAGMENT";

    private String mCurrentFragmentTag;
    private SearchFragment searchFragment = new SearchFragment();
    private VideoFragment videoFragment;
    private FragmentCustomEvents mCustomFragmentCallback = new CustomFragmentCallback();

    public MainCarActivity(){

    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main_car);
        FragmentManager fragmentManager = getSupportFragmentManager();
         if( null == bundle) {
            mCurrentFragmentTag = FRAGMENT_SEARCH;
            searchFragment = new SearchFragment();
            searchFragment.setCustomTag(FRAGMENT_SEARCH);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, searchFragment, searchFragment.getCustomTag())
                    .commit();
        } else {
             mCurrentFragmentTag = bundle.getString(CURRENT_FRAGMENT_TAG_KEY);
             searchFragment = (SearchFragment) fragmentManager.getFragment(bundle, SEARCH_FRAGMENT_KEY);
         }

        new MenuBarSearchListener(getCarUiController(), searchFragment);
        fragmentManager.registerFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks,false);
    }

    @Override
    public void onStart(){
        super.onStart();
        getCarUiController().getSearchController().showSearchBox();
        getCarUiController().getStatusBarController().showTitle();
        getCarUiController().getStatusBarController().setAppBarAlpha(0.0f);
        getCarUiController().getStatusBarController().setTitle(getString(R.string.app_name));
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(CURRENT_FRAGMENT_TAG_KEY, mCurrentFragmentTag);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.putFragment(bundle, SEARCH_FRAGMENT_KEY, searchFragment);
    }

    /*private void switchToFragment(String tag) {
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
    }*/

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

        }
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState){
            ((CarFragment) f).setCarUiController(getCarUiController());
            ((CarFragment) f).setFragmentCustomEvents(mCustomFragmentCallback);
        }
    };

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent){

        if(mCurrentFragmentTag.equals(FRAGMENT_YOUTUBE) && keyCode == KeyEvent.KEYCODE_BACK){
            videoFragment.onBackPressed();
            return true;
        }
        String videoId = searchFragment.getSelectedVideoId();
        Log.d("search_activity", "keycode="+keyCode+" videoid="+videoId);
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && mCurrentFragmentTag.equals(FRAGMENT_SEARCH) &&  videoId != null) {
            playVideo(videoId);
        }
        return false;
    }

    private void playVideo(String videoId){
        videoFragment = new VideoFragment();
        videoFragment.setVideoId(videoId);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .detach(searchFragment)
                .add(R.id.fragment_container, videoFragment, videoFragment.getCustomTag())
                .commit();
    }

    private void stopVideo(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction t = fragmentManager.beginTransaction();
        videoFragment = (VideoFragment) fragmentManager.findFragmentByTag(FRAGMENT_YOUTUBE);
        if(null != videoFragment) {
            t.remove(videoFragment);
        }
        t.attach(searchFragment)
            .commit();
    }

    private class CustomFragmentCallback extends FragmentCustomEvents {

        @Override
        public void onReadyToDetach() {
            super.onReadyToDetach();
            stopVideo();
        }
        @Override
        public void onClickVideo(String videoId, Integer pos) {
            super.onClickVideo(videoId, pos);
            if (mCurrentFragmentTag.equals(FRAGMENT_SEARCH) && videoId != null) {
                playVideo(videoId);
                searchFragment.setPosition(pos);
            }
        }
    }
}
