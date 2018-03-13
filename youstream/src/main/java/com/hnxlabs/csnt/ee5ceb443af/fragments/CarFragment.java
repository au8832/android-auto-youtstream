package com.hnxlabs.csnt.ee5ceb443af.fragments;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.google.android.apps.auto.sdk.CarUiController;
import com.hnxlabs.csnt.ee5ceb443af.listeners.FragmentCustomEvents;

/**
 * Created by ahmed abrar on 2/4/18.
 */

public class CarFragment extends Fragment {
    private String mTitle;
    private CarUiController carUiController;
    private FragmentCustomEvents fragmentCustomEvents;
    private String customTag;

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

    public FragmentCustomEvents getFragmentCustomEvents() {
        return fragmentCustomEvents;
    }

    public void setFragmentCustomEvents(FragmentCustomEvents fragmentCustomEvents) {
        this.fragmentCustomEvents = fragmentCustomEvents;
    }

    public String getCustomTag() {
        return customTag;
    }

    public void setCustomTag(String customTag) {
        this.customTag = customTag;
    }
}
