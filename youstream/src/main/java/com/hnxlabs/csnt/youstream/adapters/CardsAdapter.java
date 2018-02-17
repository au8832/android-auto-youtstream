package com.hnxlabs.csnt.youstream.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.apps.auto.sdk.ui.CarRecyclerView;
import com.google.android.apps.auto.sdk.ui.PagedListView;
import com.hnxlabs.csnt.youstream.R;
import com.hnxlabs.csnt.youstream.data.TrackItem;
import com.hnxlabs.csnt.youstream.listeners.FragmentsLifecyleListener;

import org.mortbay.jetty.HttpURI;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed abrar on 2/5/18.
 */

public class CardsAdapter extends CarRecyclerView.Adapter<CardsAdapter.ViewHolder> implements PagedListView.ItemCap {

    private int mItemsMax = -1;
    private int currentPosition = RecyclerView.NO_POSITION;
    private static FragmentsLifecyleListener lifecyleListener;

    @Override
    public void setMaxItems(int i) {
        this.mItemsMax = i;
    }

    public static class ViewHolder extends CarRecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        private TrackItem t;
        private int itemPosition;
        public ViewHolder(View v) {
            super(v);
            this.mView = v;
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(CardsAdapter.lifecyleListener != null)
                        lifecyleListener.onClickVideo(t.getVideoKey(), getItemPosition());
                }
            });
        }

        public void setT(TrackItem t) {
            this.t = t;
        }

        public int getItemPosition() {
            return itemPosition;
        }

        public void setItemPosition(int itemPosition) {
            this.itemPosition = itemPosition;
        }
    }

    public void selectIndex(Integer pos){
        int previousPositon = currentPosition;
        currentPosition = pos;
        notifyItemChanged(previousPositon);
        notifyItemChanged(currentPosition);
    }

    public String getVideoId(int position) {

        if(this.mDataset.size() == 0)
            return null;

        if(position >= this.mDataset.size())
            position = this.mDataset.size()-1;
        if(position < 0)
            position = 0;

        return this.mDataset.get(position).getVideoKey();
    }

    private List<TrackItem> mDataset;
    // Provide a suitable constructor (depends on the kind of dataset)
    public CardsAdapter(List<TrackItem> myDataset) {
        mDataset = myDataset;
    }

    public CardsAdapter(){
        this.mDataset = new ArrayList<>();
    }

    public void setLifecyleListener(FragmentsLifecyleListener lifecyleListener){
        CardsAdapter.lifecyleListener = lifecyleListener;
    }

    public void add(TrackItem t) {
        this.mDataset.add(t);
    }

    public void clearAll () {
        this.mDataset.clear();
        currentPosition = -1;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_card, parent, false);

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        TextView t = (TextView) holder.mView.findViewById(R.id.title);
        TextView t2 = (TextView) holder.mView.findViewById(R.id.duration);
        ImageView iv = (ImageView) holder.mView.findViewById(R.id.thumbnail);
        holder.setItemPosition(position);
        try {
            new DownloadImageFromInternet(iv).execute(mDataset.get(position).getUrl());
        } catch (Exception e) {
            Log.e("async","url error.");
        }
        t.setText(mDataset.get(position).getTitle());
        t2.setText(mDataset.get(position).getDuration());
        holder.setT(mDataset.get(position));
        holder.itemView.setSelected(position == currentPosition);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

}
