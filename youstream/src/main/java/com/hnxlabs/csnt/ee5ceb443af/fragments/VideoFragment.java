package com.hnxlabs.csnt.ee5ceb443af.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import com.hnxlabs.csnt.ee5ceb443af.R;
import com.hnxlabs.csnt.ee5ceb443af.listeners.FragmentCustomEvents;
import com.hnxlabs.csnt.ee5ceb443af.ui.VideoEnabledWebChromeClient;
import com.hnxlabs.csnt.ee5ceb443af.ui.VideoWebView;

/**
 * Created by ahmed abrar on 2/8/18.
 */

public class VideoFragment extends CarFragment {

    private String TAG ="videoFragment";
    private VideoWebView webView;
    private String videoId = null;
    private VideoEnabledWebChromeClient webChromeClient;
    private Button backBtn;
    private FragmentCustomEvents fragmentCustomEvents;
    private String VIDEO_ID_KEY = "VIDEO_ID_KEY";
    private String SAVE_VIDEO_PLAY_TIME = "SAVE_VIDEO_PLAY_TIME";
    private String MENU_VISIBLE_KEY = "MENU_VISIBLE_KEY";
    private Long timeStart = 0l;
    private Integer videoPlayTime = 0;
    private Boolean menuVisible = true;


    // leave this default construcotr as is for fragments
    public VideoFragment(){
        this.setCustomTag("YOUTUBE_VIDEO_FRAGMENT");
        this.setTitle("Video Player");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    public void setFragmentCustomEvents(FragmentCustomEvents lifecyleListener){
         this.fragmentCustomEvents = lifecyleListener;
    }

    public void setVideoId(String videoId){
        this.videoId = videoId;
        this.videoPlayTime = 0;
    }

    public void onBackPressed() {
        if(webChromeClient != null)
            webChromeClient.onBackPressed();
    }

    private void toggleMenubar(){
        if(menuVisible){
            getCarUiController().getSearchController().hideSearchBox();
            getCarUiController().getStatusBarController().hideAppHeader();
            getCarUiController().getStatusBarController().hideConnectivityLevel();
        } else {
            getCarUiController().getSearchController().showSearchBox();
            getCarUiController().getStatusBarController().showAppHeader();
            getCarUiController().getStatusBarController().showConnectivityLevel();
        }
        menuVisible = !menuVisible;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        View v = inflater.inflate(R.layout.videoplayer_fragment, container, false);
        backBtn = (Button) v.findViewById(R.id.backBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fragmentCustomEvents != null)
                    fragmentCustomEvents.onReadyToDetach();
            }
        });

        View nonVideoLayout = v.findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = (ViewGroup)v.findViewById(R.id.videoLayout); // Your own view, read class comments

        webView = (VideoWebView) v.findViewById(R.id.webview);

        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, new ProgressBar(getContext()), webView);
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
        {
            @Override
            public void toggledFullscreen(boolean fullscreen)
            {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if(!fullscreen) {
                    if(fragmentCustomEvents != null)
                        fragmentCustomEvents.onReadyToDetach();
                }

            }
        });

        webView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "webview on click");
            }
        });

        webView.setWebChromeClient(webChromeClient);
        // Call private class InsideWebViewClient
        webView.setWebViewClient(new InsideWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setLoadWithOverviewMode(true);


        // Navigate anywhere you want, but consider that this classes have only been tested on YouTube's mobile site
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString(VIDEO_ID_KEY, videoId);
        bundle.putLong(SAVE_VIDEO_PLAY_TIME, videoPlayTime);
        bundle.putBoolean(MENU_VISIBLE_KEY, menuVisible);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(null != savedInstanceState) {
            videoId = savedInstanceState.getString(VIDEO_ID_KEY);
            videoPlayTime = new Long(savedInstanceState.getLong(SAVE_VIDEO_PLAY_TIME)).intValue();
            menuVisible = savedInstanceState.getBoolean(MENU_VISIBLE_KEY);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        videoPlayTime = new Long(System.currentTimeMillis() - timeStart).intValue()/1000 + videoPlayTime;
        toggleMenubar();
    }

    @Override
    public void onResume(){
        super.onResume();
        webView.loadUrl("https://m.youtube.com/watch?v="+this.videoId);
        timeStart = System.currentTimeMillis();
        toggleMenubar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy();
        }
    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            webView.unMute();
            webView.requestFullScreen();
            webView.seekBySeconds(videoPlayTime);
        }

        @Override
        public void onUnhandledKeyEvent (WebView view, KeyEvent event) {
            if(event.getAction() == KeyEvent.ACTION_UP) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        videoPlayTime += 60;
                        webView.seekBySeconds(60);
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        videoPlayTime -= 20;
                        webView.seekBySeconds((-20));
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                            webView.playOrPause();
                        break;
                }
            }
        }
    }
}
