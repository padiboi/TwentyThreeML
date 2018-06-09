package com.example.hackio.twentythreeml;

import android.app.Application;

import com.hypertrack.lib.HyperTrack;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize HyperTrack SDK with your Publishable Key here.
        // Sign up to get your keys https://www.hypertrack.com/signup
        // Get your keys from https://dashboard.hypertrack.com/settings
        HyperTrack.initialize(this, "pk_57e6c87f1728b831a320c84c66bbcc4b3cf10988");
    }
}
