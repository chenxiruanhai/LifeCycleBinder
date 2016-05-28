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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.Callable;

import it.codingjam.lifecyclebinder.LifeCycleAware;
import it.codingjam.lifecyclebinder.LifeCycleBinder;
import it.codingjam.lifecyclebinder.RetainedObjectProvider;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainFragment extends Fragment implements MvpView {

    private ProgressBar progress;
    private TextView title;
    private TextView description;

    @LifeCycleAware
    Logger logger = new Logger();

    @RetainedObjectProvider("presenter")
    Callable<Presenter> presenterFactory = new Callable<Presenter>() {
        @Override
        public Presenter call() throws Exception {
            return new Presenter();
        }
    };

    Presenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LifeCycleBinder.bind(this);
    }

    @Nullable
    @Override
    public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.view.View view = inflater.inflate(R.layout.activity_main, container, false);

        progress = (ProgressBar) view.findViewById(R.id.progress);
        title = (TextView) view.findViewById(R.id.title);
        description = (TextView) view.findViewById(R.id.description);

        view.findViewById(R.id.share).setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                presenter.share();
            }
        });

        return view;
    }

    @Override
    public void update(Model model) {
        progress.setVisibility(GONE);
        title.setText(model.getNote().getTitle());
        description.setText(model.getNote().getDescription());
    }

    @Override
    public void showLoading() {
        progress.setVisibility(VISIBLE);
    }

    @Override
    public void share(String message, int requestCode) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        LifeCycleBinder.startActivityForResult(getActivity(), Intent.createChooser(sendIntent, getResources().getText(R.string.share)), requestCode);
    }
}
