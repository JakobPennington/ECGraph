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

package com.jakob.ecgraph.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakob.ecgraph.R;
import com.jakob.ecgraph.objects.EventRecord;

/**
 * A fragment which displays information about a single EventRecord in EventViewerActivity
 *
 * @author  Jakob Pennington
 * @version 1.0
 */
public class EventDataFragment extends Fragment {
    private static final String TAG = "EcgViewerFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_data, container, false);
    }

    public void setData(EventRecord record) {
        // Get reverences to the appropriate TextViews
        TextView date = (TextView) getActivity().findViewById(R.id.date);
        TextView time = (TextView) getActivity().findViewById(R.id.time);
        TextView duration = (TextView) getActivity().findViewById(R.id.duration);

        // Update the TextViews based on data in EventRecord
        date.setText(record.getDate());
        time.setText(String.format("Time: %s", record.getTime()));
        duration.setText(String.format("Duration: %s", record.getDuration()));
    }
}
