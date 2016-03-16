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

package com.jakob.ecgraph.fragments;

import android.app.Fragment;
import android.os.Bundle;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.Cluster;
import com.jakob.ecgraph.objects.EventRecord;

/**
 * This fragment stores data about the state of EventMapActivity so when a configuration change
 * occurs, such as orientation change, the activity can be reloaded with the same state.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class RetainMapFragment extends Fragment {
    private static final String TAG = "RetainMapFragment";
    private boolean mListFooterDisplayed = false;
    private boolean mRecordFooterDisplayed = false;
    private boolean mEventListDisplayed = false;
    private Cluster<EventRecord> mSelectedCluster;
    private EventRecord mSelectedRecord;
    private CameraPosition mCameraPosition = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The Fragment is retained so it can survive activity restarts
        setRetainInstance(true);
    }

    public boolean isListFooterDisplayed() {
        return mListFooterDisplayed;
    }

    public void setListFooterDisplayed(boolean mListFooterDisplayed) {
        this.mListFooterDisplayed = mListFooterDisplayed;
    }

    public boolean isRecordFooterDisplayed() {
        return mRecordFooterDisplayed;
    }

    public void setRecordFooterDisplayed(boolean mRecordFooterDisplayed) {
        this.mRecordFooterDisplayed = mRecordFooterDisplayed;
    }

    public boolean isEventListDisplayed() {
        return mEventListDisplayed;
    }

    public void setEventListDisplayed(boolean mEventListDisplayed) {
        this.mEventListDisplayed = mEventListDisplayed;
    }

    public Cluster<EventRecord> getSelectedCluster() {
        return mSelectedCluster;
    }

    public void setSelectedCluster(Cluster<EventRecord> mSelectedCluster) {
        this.mSelectedCluster = mSelectedCluster;
    }

    public EventRecord getSelectedRecord() {
        return mSelectedRecord;
    }

    public void setSelectedRecord(EventRecord mSelectedRecord) {
        this.mSelectedRecord = mSelectedRecord;
    }

    public CameraPosition getCameraPosition() {
        return mCameraPosition;
    }

    public void setCameraPosition(CameraPosition mCameraPosition) {
        this.mCameraPosition = mCameraPosition;
    }
}
