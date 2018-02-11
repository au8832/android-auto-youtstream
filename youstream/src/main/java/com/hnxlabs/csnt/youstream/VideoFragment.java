package com.hnxlabs.csnt.youstream;

import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.apps.auto.sdk.CarUiController;
import com.hnxlabs.csnt.youstream.adapters.CardsAdapter;
import com.hnxlabs.csnt.youstream.listeners.FragmentsLifecyleListener;
import com.hnxlabs.csnt.youstream.ui.VideoEnabledWebChromeClient;
import com.hnxlabs.csnt.youstream.ui.VideoEnabledWebView;
import com.hnxlabs.csnt.youstream.ui.VideoWebView;

import java.util.ArrayList;

/**
 * Created by ahmed abrar on 2/8/18.
 */

public class VideoFragment extends CarFragment {

    private String TAG ="videoFrgmet";
    private VideoWebView webView;
    private String videoId;
    private CarUiController carUiController;
    private VideoEnabledWebChromeClient webChromeClient;
    private Button backBtn;
    private FragmentsLifecyleListener fragmentsLifecyleListener;


    public VideoFragment(){

    }
     public void setCarUiController(CarUiController uic){
        this.carUiController = uic;
     }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    public void setFragmentsLifecyleListener(FragmentsLifecyleListener lifecyleListener){
         this.fragmentsLifecyleListener = lifecyleListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach");

        setTitle("Video Player");

    }

    public void setVideoId(String videoId){
        this.videoId = videoId;
    }

    public void onBackPressed() {
        if(webChromeClient != null)
            webChromeClient.onBackPressed();
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
                if(fragmentsLifecyleListener != null)
                    fragmentsLifecyleListener.onReadyToDetach();
            }
        });
        carUiController.getSearchController().hideSearchBox();
        carUiController.getStatusBarController().hideAppHeader();
        carUiController.getStatusBarController().hideConnectivityLevel();
        carUiController.getStatusBarController().setAppBarAlpha(0f);

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
                    if(fragmentsLifecyleListener != null)
                        fragmentsLifecyleListener.onReadyToDetach();
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
        webView.loadUrl("https://m.youtube.com/watch?v="+this.videoId);
        return v;
    }

    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }

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
        }

        @Override
        public void onUnhandledKeyEvent (WebView view, KeyEvent event) {
            if(event.getAction() == KeyEvent.ACTION_UP) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        webView.seekBySeconds(60);
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
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
