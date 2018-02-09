package com.hnxlabs.csnt.youstream;

import android.os.AsyncTask;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;

/**
 * Created by ahmed abrar on 2/4/18.
 */

public class CarFragment extends Fragment {
    private String mTitle;

    public CarFragment() {
        super();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setTitle(@StringRes int resId) {
        this.mTitle = getContext().getString(resId);
    }


}
