/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.jakob.ecgraph.R;
import com.jakob.ecgraph.fragments.DeleteDialogFragment;
import com.jakob.ecgraph.fragments.EcgViewerFragment;
import com.jakob.ecgraph.fragments.EventDataFragment;
import com.jakob.ecgraph.objects.EventRecord;

/**
 * A simple activity which displays an EventRecord displayed in the database. This activity contains
 * an EventDataFragment, displaying data about the mRecord, and an EcgViewerFragment, which displays
 * recorded ECG data which can be scrolled and scaled with touch gestures.
 *
 * @author  Jakob Pennington
 * @version 1.0
 */
public class EventViewerActivity extends AppCompatActivity
        implements DeleteDialogFragment.DeleteDialogListener {

    private static final String TAG = "EventViewerActivity";
    private EventRecord mRecord;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);

        // Retrieve the record to be displayed
        mRecord = (EventRecord) getIntent().getSerializableExtra("record");
        String fileName = mRecord.getFileName();

        // Set up the EventDataFragment
        EventDataFragment eventDataFragment = (EventDataFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_event_data);
        eventDataFragment.setData(mRecord);

        // Set up the EcgViewerFragment
        EcgViewerFragment mEcgViewerFragment = (EcgViewerFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_ecg_viewer);
        mEcgViewerFragment.setECGData(fileName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Delete the record currently being viewed. A dialog confirms before the record is deleted
        if (id == R.id.action_delete) {
            showDeleteDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        // A dialog to confirm whether the user wants to delete a record
        DeleteDialogFragment deleteDialogFragment = DeleteDialogFragment.newInstance(mRecord);
        deleteDialogFragment.show(getSupportFragmentManager(), "delete");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // Only called if user confirms to delete record but delete action unsuccessful
        Snackbar.make(findViewById(R.id.event_viewer_layout), "Delete Record Unsuccessful",
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // Empty method for the DeleteDialogInterface
    }

}
