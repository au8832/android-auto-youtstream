package com.hnxlabs.csnt.ee5ceb443af;

import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarActivityService;

public class CarService extends CarActivityService {
    public Class<? extends CarActivity> getCarActivity() {
        return MainCarActivity.class;
    }
}
