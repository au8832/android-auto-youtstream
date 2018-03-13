package com.hnxlabs.csnt.ee5ceb443af.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.hnxlabs.csnt.ee5ceb443af.data.TrackItem;
import com.hnxlabs.csnt.ee5ceb443af.fragments.SearchFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed abrar on 2/25/18.
 */

public class YoutubeSearchTask extends AsyncTask <String, String, List<TrackItem>> {

    private YouTube mYoutube;
    private SearchFragment searchFragment;

    public YoutubeSearchTask(SearchFragment searchFragment){
        this.searchFragment = searchFragment;
    }
    @Override
    protected List<TrackItem> doInBackground(String... strings) {
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
            List<TrackItem> results = new ArrayList<>();
            for( SearchResult s : searchResultList){
                results.add(new TrackItem(s));
            }
            return results;

        }catch (Exception e) {
            Log.e("searchgrag", e.getMessage());
        }
        return null;
    }
}
