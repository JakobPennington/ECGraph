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

import java.util.Random;

/**
 * A fragment located within the MainActivity which will display live data related to the heart
 * activity of the user. Currently, this data is simulated by generating random numbers within a
 * typical heart range.
 *
 * TODO: Implement algorithms to calculate heart rate from ECG data
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class OverviewFragment extends Fragment {
    private static final String TAG = "OverviewFragment";
    private TextView mHeartRateTV;
    private TextView mMinHeartRateTV;
    private TextView mMaxHeartRateTV;
    private TextView mAvgHeartRateTV;
    private int mMinHeartRate;
    private int mMaxHeartRate;
    private int mAvgHeartRate;
    private int counter;
    private int total;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        reset();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHeartRateTV = (TextView) getActivity().findViewById(R.id.heartrate);
        mMaxHeartRateTV = (TextView) getActivity().findViewById(R.id.max_value);
        mMinHeartRateTV = (TextView) getActivity().findViewById(R.id.min_value);
        mAvgHeartRateTV = (TextView) getActivity().findViewById(R.id.avg_value);
    }

    // Generate a simulated heart rate and update the min, max and average values
    public void update(){
        // Generate a random number between 65 and 75 to emulate a typical heart rate
        Random random = new Random();
        int heartRate = random.nextInt(75 - 65) + 65;

        // Test if smaller than minimum
        if (mMinHeartRate == 0){
            mMinHeartRate = heartRate;
        }else if (heartRate < mMinHeartRate){
            mMinHeartRate = heartRate;
        }

        // Test if greater than maximum
        if (heartRate > mMaxHeartRate) {
            mMaxHeartRate = heartRate;
        }

        // Calculate the average
        total += heartRate;
        counter++;
        mAvgHeartRate = total/counter;

        mHeartRateTV.setText(String.valueOf(heartRate));
        mMaxHeartRateTV.setText(String.format("%d bpm", mMaxHeartRate));
        mMinHeartRateTV.setText(String.format("%d bpm", mMinHeartRate));
        mAvgHeartRateTV.setText(String.format("%d bpm", mAvgHeartRate));
    }

    // Reset variables storing heart rate data
    public void reset(){
        mAvgHeartRate = 0;
        mMaxHeartRate = 0;
        mMinHeartRate = 0;
        counter = 0;
        total = 0;
    }

}

