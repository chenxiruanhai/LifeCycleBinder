package it.codingjam.lifecyclebinder.demo;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import it.codingjam.lifecyclebinder.DefaultViewLifeCycleAware;


public class ActivityLogger extends DefaultViewLifeCycleAware<AppCompatActivity> {

    private static final String TAG = "ACTIVITY_LOG";

    @Override
    public void onCreate(AppCompatActivity activity, Bundle bundle) {
//        Log.i(TAG, "Creating activity:" + activity);
    }

    @Override
    public void onStart(AppCompatActivity activity) {
//        Log.i(TAG, "Starting activity:" + activity);
    }

    @Override
    public void onResume(AppCompatActivity activity) {
//        Log.i(TAG, "Resuming activity:" + activity);
    }

    @Override
    public void onPause(AppCompatActivity activity) {
//        Log.i(TAG, "Pausing activity:" + activity);
    }

    @Override
    public void onStop(AppCompatActivity activity) {
//        Log.i(TAG, "Stopping activity:" + activity);
    }

    @Override
    public void onDestroy(AppCompatActivity activity) {
//        Log.i(TAG, "Destroying activity:" + activity);
    }
}
