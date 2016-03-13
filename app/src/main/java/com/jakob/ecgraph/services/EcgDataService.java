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

package com.jakob.ecgraph.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A service which emulates an ECG sending live ECG data to the application. Data is read from
 * a text file containing ECG data sources from the American Heart Association database. Data
 * is broadcast to the main thread for processing and display.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class EcgDataService extends IntentService {
    public final static String NEW_DATA = "NEW_DATA";
    private static final String TAG = "EcgDataService";
    private static final int RECORD_BUFFER_SIZE = 3750;
    private static final int DISPLAY_BUFFER_SIZE = 5;
    private List<Double> mRecordBuffer = new ArrayList<>();
    private List<Double> mRecordedData = new ArrayList<>();
    private boolean mRecording = false;
    private IBinder mBinder = new LocalBinder();

    public EcgDataService() {
        super("EcgDataService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /* Everything in this method happens in its own thread. Data is continuously read and sent to
     * the MainActivity without interrupting the MainActivity
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String newLine;
        double newValue = 0;
        double[] mDisplayBuffer = new double[DISPLAY_BUFFER_SIZE];

        // Continuously loop through ECG data stored in data.txt
        try {
            InputStream inputStream = this.getAssets().open("data.txt");
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
            while (true) {
                // Only every third value is stored in the mDisplayBuffer
                for (int i = 0; i < (DISPLAY_BUFFER_SIZE * 3); i++) {
                    // Read a line of the text file
                    newLine = bufferReader.readLine();

                    // If the end of the file is reached, loop back to the start.
                    if (newLine == null) {
                        inputStream = this.getAssets().open("data.txt");
                        bufferReader = new BufferedReader(new InputStreamReader(inputStream));
                        newLine = bufferReader.readLine();
                    }

                    // Parse the value as a double
                    try {
                        newValue = Double.parseDouble(newLine);
                    } catch (NumberFormatException nfe) {
                        Log.e(TAG, "Error parsing string to double");
                    }

                    // Store data value in mRecordedData if recording. If not, add to buffer.
                    if (mRecording) {
                        mRecordedData.add(newValue);
                    } else {
                        // Limit the size of the buffer. If the limit is reached, remove the first value
                        if (mRecordBuffer.size() == RECORD_BUFFER_SIZE) {
                            mRecordBuffer.remove(0);
                        }
                        mRecordBuffer.add(newValue);
                    }

                    /* Store data in buffer so display is only updated periodically. Every third
                     * value is stored for performance improvement
                     */
                    mDisplayBuffer[i / 3] = newValue;
                }

                // Send the data to MainActivity
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(NEW_DATA);
                broadcastIntent.putExtra("ECGData", mDisplayBuffer);
                sendBroadcast(broadcastIntent);

                // Sleep for 60 milliseconds to emulate 250Hz ECG signals
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error sleeping thread");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading text file. " + e);
        }
    }

    // Called when the user presses the record button
    public void recordStart() {
        mRecordedData.clear();
        mRecording = true;
    }

    // Called when the user presses the stop button
    public ArrayList<Double> recordStop() {
        // Create a new ArrayList and add the buffered data, then the recorded data.
        ArrayList<Double> recordedData = new ArrayList<>();
        recordedData.addAll(mRecordBuffer);
        recordedData.addAll(mRecordedData);

        // Reset data structures for future recordings
        mRecordBuffer.clear();
        mRecordedData.clear();

        mRecording = false;

        return recordedData;
    }

    public class LocalBinder extends Binder {
        public EcgDataService getService() {
            return EcgDataService.this;
        }
    }
}
