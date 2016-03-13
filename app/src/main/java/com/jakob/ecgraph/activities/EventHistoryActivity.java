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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jakob.ecgraph.R;
import com.jakob.ecgraph.adapters.DatabaseAdapter;
import com.jakob.ecgraph.adapters.EventHistoryAdapter;
import com.jakob.ecgraph.decorations.DividerItemDecoration;
import com.jakob.ecgraph.objects.EventRecord;

import java.util.List;


/**
 * A simple activity containing a RecyclerView displaying EventRecords stored in the database.
 * The RecyclerView is handled by the EventHistoryAdapter, which displays a list of EventRecord
 * objects obtained from the database. When an item is clicked in the RecyclerView, the record is
 * displayed in EventViewerActivity.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class EventHistoryActivity extends AppCompatActivity
        implements EventHistoryAdapter.ClickListener {

    private DatabaseAdapter mDatabaseAdapter;
    private RecyclerView mEventHistoryRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);

        // Set up the RecyclerView
        mEventHistoryRecyclerView = (RecyclerView) findViewById(R.id.event_history_recycler_view);
        mEventHistoryRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        mDatabaseAdapter = new DatabaseAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get EventRecords from the database and populate the RecyclerView
        List<EventRecord> eventHistory = mDatabaseAdapter.getEventHistory();
        EventHistoryAdapter mEventHistoryAdapter = new EventHistoryAdapter(this, eventHistory);
        mEventHistoryAdapter.setClickListener(this);
        mEventHistoryRecyclerView.setAdapter(mEventHistoryAdapter);
        mEventHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // If a record was just deleted in EventViewerActivity, display a snackbar
        if (getIntent().getBooleanExtra("deleted", false)) {
            Snackbar.make(mEventHistoryRecyclerView, "Record Successfully Deleted",
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // When an EventRecord is clicked, the object is passed to EventViewerActivity to be presented
    @Override
    public void itemClicked(View view) {
        EventRecord clickedRecord = (EventRecord) view.getTag();
        Intent intent = new Intent(this, EventViewerActivity.class);
        intent.putExtra("record", clickedRecord);
        startActivity(intent);
    }
}
