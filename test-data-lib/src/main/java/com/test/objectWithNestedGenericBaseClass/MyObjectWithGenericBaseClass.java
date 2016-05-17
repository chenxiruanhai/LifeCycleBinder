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

package com.test.objectWithNestedGenericBaseClass;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.test.MyObject;
import com.test.MyView;

import it.codingjam.lifecyclebinder.LifeCycleAware;
import it.codingjam.lifecyclebinder.ViewLifeCycleAware;

class MyGenericBaseClass<T extends MyView> implements ViewLifeCycleAware<T> {

    @LifeCycleAware
    MyObject myBaseObject;

    @Override
    public void onCreate(T view, Bundle bundle) {

    }

    @Override
    public void onStart(T view) {

    }

    @Override
    public void onResume(T view) {

    }

    @Override
    public boolean hasOptionsMenu() {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public boolean onOptionsItemSelected(T view, MenuItem item) {
        return false;
    }

    @Override
    public void onPause(T view) {

    }

    @Override
    public void onStop(T view) {

    }

    @Override
    public void onSaveInstanceState(T view, Bundle bundle) {

    }

    @Override
    public void onDestroy(T view) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}

public class MyObjectWithGenericBaseClass extends MyGenericBaseClass<MyView> {

    @LifeCycleAware
    MyObject myObject;
}
