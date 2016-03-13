/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jakob.ecgraph.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.Cluster;
import com.jakob.ecgraph.R;
import com.jakob.ecgraph.fragments.EventListFragment;
import com.jakob.ecgraph.fragments.EventMapFragment;
import com.jakob.ecgraph.fragments.RetainMapFragment;
import com.jakob.ecgraph.objects.EventRecord;

/**
 * A Google Maps Activity which display event records based on location data. The activity switches
 * between EventMapFragment, which displays events on a map, and EventListFragment, which displays a
 * list of events in a cluster n the map.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class EventMapActivity extends AppCompatActivity {
    private static final String TAG = "EventMapActivity";
    private TextView mFooterTitle;
    private TextView mFooterSubtitle;
    private ObjectAnimator mRecordFooterHide;
    private ObjectAnimator mRecordFooterShow;
    private ObjectAnimator mListFooterHide;
    private ObjectAnimator mListFooterShow;
    private boolean mListFooterDisplayed = false;
    private boolean mRecordFooterDisplayed = false;
    private boolean mEventListDisplayed = false;
    private CameraPosition mCameraPosition = null;
    private EventRecord mSelectedRecord;
    private Cluster<EventRecord> mSelectedCluster;
    private RetainMapFragment mRetainMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_map);

        setUpBottomSheets();

        // find the retained fragment on activity restarts
        FragmentManager fragmentManager = getFragmentManager();
        mRetainMapFragment = (RetainMapFragment) fragmentManager.findFragmentByTag("data");

        if (mRetainMapFragment == null) {
            // If this is the first onCreate(), create the RetainMapFragment
            mRetainMapFragment = new RetainMapFragment();
            fragmentManager.beginTransaction().add(mRetainMapFragment, "data").commit();

            // Only force show EventMapFragment on first onCreate()
            showEventMapFragment();
        } else {
            // If this is not the first onCreate(), load data from mRetainMapFragment
            mCameraPosition = mRetainMapFragment.getCameraPosition();
            mSelectedRecord = mRetainMapFragment.getSelectedRecord();
            mSelectedCluster = mRetainMapFragment.getSelectedCluster();
            mEventListDisplayed = mRetainMapFragment.isEventListDisplayed();
            mListFooterDisplayed = mRetainMapFragment.isListFooterDisplayed();
            mRecordFooterDisplayed = mRetainMapFragment.isRecordFooterDisplayed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Store the state of the Activity in mRetainMapFragment to be restored in onCreate
        mRetainMapFragment.setCameraPosition(mCameraPosition);
        mRetainMapFragment.setSelectedRecord(mSelectedRecord);
        mRetainMapFragment.setSelectedCluster(mSelectedCluster);
        mRetainMapFragment.setEventListDisplayed(mEventListDisplayed);
        mRetainMapFragment.setListFooterDisplayed(mListFooterDisplayed);
        mRetainMapFragment.setRecordFooterDisplayed(mRecordFooterDisplayed);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Show fragments/footers baased on data retained by mRetainMapFragment
        if(mEventListDisplayed){
            showEventListFragment();
        } else if(mListFooterDisplayed){
            showListFooter();
        } else if(mRecordFooterDisplayed){
            showRecordFooter();
        }

        // If a record has been selected, set the text in RecordFooter accordingly
        if (mSelectedRecord != null){
            setRecordFooterText(mSelectedRecord.getDate(), mSelectedRecord.getTime());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                /* If EventListFragment is displayed, switch to EventMapFragment rather than
                 * finishing the activity
                 */
                if (mEventListDisplayed) {
                    showEventMapFragment();
                } else {
                    onBackPressed();
                }
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        /* If EventListFragment is displayed, switch to EventMapFragment rather than finishing the
         * activity
         */
        if (mEventListDisplayed) {
            showEventMapFragment();
        } else {
            super.onBackPressed();
        }
    }

    private void setUpBottomSheets() {
        // Set up the mRecordFooter which displays EventRecord information when a marker is selected
        View mRecordFooter = findViewById(R.id.fragment_map_record_footer);
        mFooterTitle = (TextView) findViewById(R.id.map_footer_title);
        mFooterSubtitle = (TextView) findViewById(R.id.map_footer_subtitle);
        Button mFooterViewButton = (Button) findViewById(R.id.view_record_button);
        mFooterViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EventViewerActivity.class);
                intent.putExtra("record", mSelectedRecord);
                startActivity(intent);
            }
        });

        // Set up the mClusterFooter which displays a prompt to show the mListFooter
        final View mClusterFooter = findViewById(R.id.fragment_map_cluster_footer);
        mClusterFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the ListFooter
                mListFooterHide.start();
                mListFooterDisplayed = false;
                showEventListFragment();
            }
        });

        // Set up ObjectAnimators which animate the transitions between footers
        mRecordFooterShow = ObjectAnimator
                .ofFloat(mRecordFooter, View.TRANSLATION_Y, -80 * getResources().getDisplayMetrics().density)
                .setDuration(250);
        mRecordFooterHide = ObjectAnimator
                .ofFloat(mRecordFooter, View.TRANSLATION_Y, 80 * getResources().getDisplayMetrics().density)
                .setDuration(250);
        mListFooterShow = ObjectAnimator
                .ofFloat(mClusterFooter, View.TRANSLATION_Y, -60 * getResources().getDisplayMetrics().density)
                .setDuration(250);
        mListFooterHide = ObjectAnimator
                .ofFloat(mClusterFooter, View.TRANSLATION_Y, 60 * getResources().getDisplayMetrics().density)
                .setDuration(250);
    }

    public void setRecordFooterText(String title, String subtitle) {
        mFooterTitle.setText(title);
        mFooterSubtitle.setText(subtitle);
    }

    public boolean getListFooterDisplayed() {
        return mListFooterDisplayed;
    }

    public boolean getRecordFooterDisplayed() {
        return mRecordFooterDisplayed;
    }

    public void showRecordFooter() {
        mRecordFooterShow.start();
        mRecordFooterDisplayed = true;
    }

    public void hideRecordFooter() {
        mRecordFooterHide.start();
        mRecordFooterDisplayed = false;
    }

    public void showListFooter() {
        mListFooterShow.start();
        mListFooterDisplayed = true;
    }

    public void hideListFooter() {
        mListFooterHide.start();
        mListFooterDisplayed = false;
    }

    public void swapFooters() {
        if (mListFooterDisplayed) {
            AnimatorSet set = new AnimatorSet();
            set.play(mListFooterHide).before(mRecordFooterShow);
            set.start();
            mRecordFooterDisplayed = true;
            mListFooterDisplayed = false;
        } else if (mRecordFooterDisplayed) {
            AnimatorSet set = new AnimatorSet();
            set.play(mRecordFooterHide).before(mListFooterShow);
            set.start();
            mListFooterDisplayed = true;
            mRecordFooterDisplayed = false;
        }
    }

    public void setSelectedRecord(EventRecord record) {
        mSelectedRecord = record;
    }

    public void setSelectedCluster(Cluster<EventRecord> cluster) {
        mSelectedCluster = cluster;
    }

    public Cluster<EventRecord> getSelectedCluster() {
        return mSelectedCluster;
    }

    public void showEventMapFragment() {
        // Transition the EventMapFragment into view
        EventMapFragment eventMapFragment = new EventMapFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder, eventMapFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
        mEventListDisplayed = false;
    }

    public void showEventListFragment() {
        // Transition the EventListFragment into view
        EventListFragment eventListFragment = new EventListFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder, eventListFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
        mEventListDisplayed = true;
    }

    public void selectEvent(EventRecord record) {
        setSelectedRecord(record);
        showRecordFooter();
        setRecordFooterText(record.getDate(), record.getTime());
    }

    public void setCameraPosition(CameraPosition cameraPosition) {
        mCameraPosition = cameraPosition;
    }

    public CameraPosition getCameraPosition() {
        return mCameraPosition;
    }
}
