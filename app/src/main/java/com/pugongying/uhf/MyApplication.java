package com.pugongying.uhf;

import android.support.multidex.MultiDex;

import com.uhf.uhf.UHF1.UHF1Application;

public class MyApplication  extends UHF1Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);
    }
}
