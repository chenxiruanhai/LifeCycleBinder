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

package it.codingjam.lifecyclebinder.mvp;

import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import it.codingjam.lifecyclebinder.DefaultViewLifeCycleAware;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class Presenter extends DefaultViewLifeCycleAware<View> {

    public static final String MODEL = "MODEL";

    private View view;

    private Model model;

    private boolean loading;

    @Override
    public void onCreate(View view, Bundle bundle) {
        if (model == null) {
            if (bundle == null) {
                model = new Model();
            } else {
                bundle.getParcelable(MODEL);
            }
        }
    }

    @Override
    public void onSaveInstanceState(View view, Bundle bundle) {
        bundle.putParcelable(MODEL, model);
    }

    @Override
    public void onResume(View view) {
        this.view = view;
        if (loading) {
            view.showLoading();
        } else {
            if (model.getNote() == null) {
                reloadData();
            } else {
                view.update(model);
            }
        }
    }

    @Override
    public void onPause(View view) {
        this.view = null;
    }

    private void reloadData() {
        loading = true;
        view.showLoading();
        Observable.just(new Note("title", "description"))
                .delay(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Note>() {
                    @Override
                    public void call(Note note) {
                        loading = false;
                        model.setNote(note);
                        if (view != null) {
                            view.update(model);
                        }
                    }
                });
    }
}