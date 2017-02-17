package com.danielkim.soundrecorder;

import android.app.Application;

import com.zhy.autolayout.config.AutoLayoutConifg;


/**
 * Created by IVY on 2017/2/9.
 */

public class RecorderApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AutoLayoutConifg.getInstance().useDeviceSize();
    }
}
