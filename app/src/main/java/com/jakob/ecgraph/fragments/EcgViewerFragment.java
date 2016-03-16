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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakob.ecgraph.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * A fragment containing a GraphView displaying static ECG data. The database stores the file name
 * of the txt file which contains the ECG data. This fragment is located in the EventViewerActivity.
 *
 * @author  Jakob Pennington
 * @version 1.0
 */
public class EcgViewerFragment extends Fragment {
    private static final String TAG = "EcgViewerFragment";
    private LineGraphSeries<DataPoint> mEcgSeries;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ecg_viewer, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        generateECGView();
    }

    // Set up the GraphView to present stored ECG data
    private void generateECGView() {
        GraphView ecgGraph = (GraphView) getActivity().findViewById(R.id.ecg_graph_viewer);
        Viewport viewport = ecgGraph.getViewport();
        GridLabelRenderer gridLabel = ecgGraph.getGridLabelRenderer();

        // Set the graph to the appropriate scale
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(500);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(-1.5);
        viewport.setMaxY(2);
        viewport.setScrollable(true);
        viewport.setScalable(true);

        // Remove Grid axis labels for cleaner look
        gridLabel.setHorizontalLabelsVisible(false);
        gridLabel.setVerticalLabelsVisible(false);
        gridLabel.setHighlightZeroLines(false);

        mEcgSeries.setColor(getResources().getColor(R.color.accent_color));
        mEcgSeries.setThickness(10);

        ecgGraph.addSeries(mEcgSeries);
    }

    /* Read the text file storing ECG data, create DataPoints for each value and create a mEcgSeries
     * in the GraphView fom these DataPoints
     */
    public void setECGData(String fileName) {
        // Read each line from the text file storing ECG data
        int index = 0;
        mEcgSeries = new LineGraphSeries<>();
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        try {
            FileInputStream fileInputStream = getActivity().openFileInput(fileName);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(fileInputStream));
            String newLine;

            while ((newLine = buffer.readLine()) != null) {
                Double value = Double.valueOf(newLine);
                index++;
                dataPoints.add(new DataPoint(index, value));
            }

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        mEcgSeries.resetData(dataPoints);
    }
}
