package com.hnxlabs.csnt.youstream.data;

import com.google.api.services.youtube.model.SearchResult;

/**
 * Created by ahmed abrar on 2/4/18.
 */

public class TrackItem {

    private String title;
    private String duration;
    private String videoKey;
    private String url;

    public TrackItem (String title, String duration) {
        this.title = title;
        this.duration = duration;
    }

    public TrackItem(SearchResult sr){
        this.title = sr.getSnippet().getTitle();
        this.videoKey = sr.getId().getVideoId();
        this.duration = sr.getSnippet().getDescription();
        this.url = sr.getSnippet().getThumbnails().getDefault().getUrl();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getVideoKey() {
        return videoKey;
    }

    public void setVideoKey(String videoKey) {
        this.videoKey = videoKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
