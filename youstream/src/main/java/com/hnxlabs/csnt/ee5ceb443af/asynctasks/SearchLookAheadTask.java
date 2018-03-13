package com.hnxlabs.csnt.ee5ceb443af.asynctasks;

import android.os.AsyncTask;

import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.SearchItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ahmed abrar on 2/19/18.
 */

public class SearchLookAheadTask extends AsyncTask<String, String, List<SearchItem>> {

    private CarUiController uiController;

    public SearchLookAheadTask(CarUiController carUiController){
        this.uiController = carUiController;
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
            this.uiController.getSearchController().setSearchItems(searchItemList);
    }
}
