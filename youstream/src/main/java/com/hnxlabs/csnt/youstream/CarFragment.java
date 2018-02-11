package com.hnxlabs.csnt.youstream;

import android.os.AsyncTask;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.apps.auto.sdk.CarUiController;
import com.hnxlabs.csnt.youstream.listeners.FragmentsLifecyleListener;

/**
 * Created by ahmed abrar on 2/4/18.
 */

public class CarFragment extends Fragment {
    private String mTitle;
    private CarUiController carUiController;
    private FragmentsLifecyleListener fragmentsLifecyleListener;

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

    public CarUiController getCarUiController() {
        return carUiController;
    }

    public void setCarUiController(CarUiController carUiController) {
        this.carUiController = carUiController;
    }

    public FragmentsLifecyleListener getFragmentsLifecyleListener() {
        return fragmentsLifecyleListener;
    }

    public void setFragmentsLifecyleListener(FragmentsLifecyleListener fragmentsLifecyleListener) {
        this.fragmentsLifecyleListener = fragmentsLifecyleListener;
    }
}
