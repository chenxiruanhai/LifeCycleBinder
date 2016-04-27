/*
 *   Copyright 2016 Fabio Collini.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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