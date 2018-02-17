package com.hnxlabs.csnt.youstream.listeners;

/**
 * Created by ahmed abrar on 2/8/18.
 */

public abstract class FragmentsLifecyleListener {

    public void onReadyToDetach(){}
    public void onClickVideo(String videoId, Integer position){}
}
