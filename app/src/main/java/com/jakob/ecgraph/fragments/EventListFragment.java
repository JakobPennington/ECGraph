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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.maps.android.clustering.Cluster;
import com.jakob.ecgraph.R;
import com.jakob.ecgraph.activities.EventMapActivity;
import com.jakob.ecgraph.adapters.EventHistoryAdapter;
import com.jakob.ecgraph.decorations.DividerItemDecoration;
import com.jakob.ecgraph.objects.EventRecord;

import java.util.List;

/**
 * This fragment displays a list of EventRecords contained in a Cluster on the map in
 * EventMapFragment. Selecting n event from this list returns to EventMapFragment displaying the
 * selected event.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class EventListFragment extends Fragment implements EventHistoryAdapter.ClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        // Update the title in the ActionBar which is shared with EventMapFragment
        ((EventMapActivity)getActivity()).getSupportActionBar().setTitle("Event List");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Cluster<EventRecord> selectedCluster = ((EventMapActivity) getActivity()).getSelectedCluster();

        // Set up the adapter for the RecyclerView
        EventHistoryAdapter mEventHistoryAdapter = new EventHistoryAdapter(getActivity(),
                (List<EventRecord>) selectedCluster.getItems());
        mEventHistoryAdapter.setClickListener(this);

        // Set up the RecyclerView
        RecyclerView mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.event_list_recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));;
        mRecyclerView.setAdapter(mEventHistoryAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    // When an EventRecord is clicked, the object is passed to EventViewerActivity to be presented
    @Override
    public void itemClicked(View view) {
        ((EventMapActivity)getActivity()).selectEvent((EventRecord) view.getTag());
        ((EventMapActivity)getActivity()).showEventMapFragment();
    }
}
