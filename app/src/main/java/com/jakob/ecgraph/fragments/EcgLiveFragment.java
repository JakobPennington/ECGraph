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

import com.jakob.ecgraph.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * A fragment which contains a GraphView displaying live ECG data broadcast by the EcgDataService.
 * This fragment is located in and controlled by the MainActivity class.
 *
 * @author  Jakob Pennington
 * @version 1.0
 */
public class EcgLiveFragment extends Fragment {
    private static final String TAG = "EcgLiveFragment";
    private static final int DISPLAY_BUFFER_SIZE = 5;
    private LineGraphSeries<DataPoint> mECGSeries;
    private double mGraphTime;
    private DataPoint[] mDataPoints;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ecg_live, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mGraphTime = 0;
        mDataPoints = new DataPoint[DISPLAY_BUFFER_SIZE];
        generateECGView();
    }

    // Set up the GraphView to display the live ECG trace
    private void generateECGView() {
        GraphView ecgGraph = (GraphView) getActivity().findViewById(R.id.ecg_graph_live);
        Viewport viewport = ecgGraph.getViewport();
        GridLabelRenderer gridLabel = ecgGraph.getGridLabelRenderer();

        // Set the graph to the appropriate scale
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(750);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(-1.5);
        viewport.setMaxY(2);

        // Remove Grid axis labels for cleaner look
        gridLabel.setHorizontalLabelsVisible(false);
        gridLabel.setVerticalLabelsVisible(false);
        gridLabel.setHighlightZeroLines(false);

        // Prepare data and create ECG trace
        mECGSeries = new LineGraphSeries<>();
        mECGSeries.setColor(getResources().getColor(R.color.accent_color));
        mECGSeries.setThickness(10);
        ecgGraph.addSeries(mECGSeries);
    }

    // Data added to the GraphView from EcgDataService via the Main Activity
    public void addNewData(double[] ECGData){
        for (int i = 0; i < DISPLAY_BUFFER_SIZE; i++) {
            mDataPoints[i] = new DataPoint(mGraphTime, ECGData[i]);
            mGraphTime += 4;
        }
        mECGSeries.appendData(mDataPoints, true, 750);
    }

    public void clear(){
        mECGSeries.resetData();
    }
}
