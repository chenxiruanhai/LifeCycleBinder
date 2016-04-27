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

package com.test;

import android.os.Bundle;
import android.view.MenuItem;

import it.codingjam.lifecyclebinder.ViewLifeCycleAware;

public class MyObject implements ViewLifeCycleAware<MyView> {
    @Override
    public void onCreate(MyView view, Bundle bundle) {

    }

    @Override
    public void onStart(MyView view) {

    }

    @Override
    public void onResume(MyView view) {

    }

    @Override
    public boolean onOptionsItemSelected(MyView view, MenuItem item) {
        return false;
    }

    @Override
    public void onPause(MyView view) {

    }

    @Override
    public void onStop(MyView view) {

    }

    @Override
    public void onSaveInstanceState(MyView view, Bundle bundle) {

    }

    @Override
    public void onDestroy(MyView view) {

    }
}