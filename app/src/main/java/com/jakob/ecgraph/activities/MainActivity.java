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

import android.Manifest;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.jakob.ecgraph.R;
import com.jakob.ecgraph.adapters.DatabaseAdapter;
import com.jakob.ecgraph.fragments.EcgLiveFragment;
import com.jakob.ecgraph.fragments.NavigationDrawerFragment;
import com.jakob.ecgraph.fragments.OverviewFragment;
import com.jakob.ecgraph.fragments.RetainMainFragment;
import com.jakob.ecgraph.objects.EventRecord;
import com.jakob.ecgraph.services.EcgDataService;
import com.software.shell.fab.ActionButton;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * A launcher activity containing an OverviewFragment, displaying live heart data, and a
 * EcgLiveFragment, displaying a live ECG trace. A NavigationDrawerFragment implements a
 * Navigation Drawer and Hamburger Menu for navigation. This activity also handles connection to the
 * Google API, allowing location based services.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final float RECORD_BUTTON_DISTANCE = 120f;
    private static final String TAG = "MainActivity";
    private static final String DIALOG_ERROR = "dialog_error";
    private DataReceiver mDataReceiver;
    private EcgLiveFragment mEcgLiveFragment;
    private OverviewFragment mOverviewFragment;
    private ViewGroup mTimerSheet;
    private TextView mTimer;
    private ActionButton mRecordButton;
    private DrawerLayout mDrawerLayout;
    private EcgDataService mService;
    private boolean mBound = false;
    private boolean mRecording = false;
    private boolean mStartFromNFC = false;
    private boolean mButtonMovedRight = false;
    private boolean mResolvingError = false;
    private boolean mResumingRecording = false;
    private Handler mHandler = new Handler();
    private long mTimeStart = 0L;
    private String mTimerString;
    private Calendar mCalendar;
    private DatabaseAdapter mDatabaseAdapter;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private FragmentManager fragmentManager;
    private RetainMainFragment mRetainMainfragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up custom toolbar and navigation drawer
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setUpNavigationDrawer(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get references to fragments and views
        mEcgLiveFragment = (EcgLiveFragment) getFragmentManager().findFragmentById(R.id.fragment_ecg_live);
        mOverviewFragment = (OverviewFragment) getFragmentManager().findFragmentById(R.id.fragment_overview);
        mTimerSheet = (ViewGroup) findViewById(R.id.bottom_sheet);
        mTimer = (TextView) findViewById(R.id.timer);

        // Start EcgDataService
        Intent intent = new Intent(this, EcgDataService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);

        // Make DataReceiver receive broadcast from EcgDataService
        mDataReceiver = new DataReceiver();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EcgDataService.NEW_DATA);
        registerReceiver(mDataReceiver, intentFilter);

        // Create DatabaseAdapter
        mDatabaseAdapter = new DatabaseAdapter(this);

        // Manage longer setting up tasks in methods
        setUpRecordButton();
        buildGoogleApiClient();

        // Find the retained fragment on activity restarts
        fragmentManager = getFragmentManager();
        mRetainMainfragment = (RetainMainFragment) fragmentManager.findFragmentByTag("data");

        if (mRetainMainfragment == null) {
            // If this is the first onCreate(), create the RetainMainFragment
            mRetainMainfragment = new RetainMainFragment();
            fragmentManager.beginTransaction().add(mRetainMainfragment, "data").commit();
        } else {
            // Handle restoring the state of the activity from mRetainMainFragment
            mRecording = mRetainMainfragment.isRecording();
            mTimeStart = mRetainMainfragment.getTimeStart();
            mCalendar = mRetainMainfragment.getCalendar();
        }

        if (mRecording) {
            // Resume the recording process
            mResumingRecording = true;
            resumeRecording();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up connections to EcgDataService
        unregisterReceiver(mDataReceiver);
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }

        // Handle storing the state of the activity in mRetainMainFragment
        mRetainMainfragment.setRecording(mRecording);
        mRetainMainfragment.setTimeStart(mTimeStart);
        mRetainMainfragment.setCalendar(mCalendar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connect to the Google API to access location services
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Clean up Google API connection
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Remove the existing ECG trace from EcgLiveFragment
        mEcgLiveFragment.clear();

        // If the app was launched from a NFC tag
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            mStartFromNFC = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Reset ready for new recording
        resetTimerSheet();
        if (mButtonMovedRight) {
            resetRecordButton();
            mButtonMovedRight = false;
        }

        if (!mRecording) {
            mOverviewFragment.reset();
        }

        if (!isChangingConfigurations()) {
            cancelRecording();
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

    public void onBackPressed() {
        // If the Navigation Drawer is open, close it rather than finishing the activity
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setUpNavigationDrawer(Toolbar toolbar) {
        // Set up the Navigation Drawer
        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
        navigationDrawerFragment.setUp(mDrawerLayout, toolbar);

        // Randomly generate a heart rate in EcgOverviewFragment
        emulateHeartData();
    }

    private void setUpRecordButton() {
        //Set up the record Floating Action Button
        mRecordButton = (ActionButton) findViewById(R.id.button_record);
        mRecordButton.setButtonColor(getResources().getColor(R.color.accent_color));
        mRecordButton.setButtonColorPressed(getResources().getColor(R.color.accent_color_dark));
        mRecordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record));
        mRecordButton.removeShadow();
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRecording) {
                    startRecording();
                } else {
                    mHandler.removeCallbacks(timer);
                    stopRecording();
                }
            }
        });
    }

    private synchronized void buildGoogleApiClient() {
        // Connect to the Google API to access location services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void startRecording() {
        // Start recording ECG data
        mRecording = true;
        mService.recordStart();

        // Animate the record button and timer
        mHandler.postDelayed(animateTimerSheet, 500);
        mTimeStart = SystemClock.uptimeMillis();
        mHandler.postDelayed(timer, 0);
        mRecordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.stop));
        mRecordButton.setImageSize(40);

        /* If the record button is pressed, animate the button to the right. If recording is started
         * with NFC, the animation is activated in onWindowFocusChanged after appropriate views are
         * inflated.
         */
        if (!mStartFromNFC) {
            mRecordButton.moveRight(120.0f);
            mButtonMovedRight = true;
        }

        // Get formatted date and time at the time the recording starts
        mCalendar = Calendar.getInstance();
    }

    private void stopRecording() {
        // Stop recording and get recorded data from EcgDataService
        mRecording = false;
        ArrayList<Double> recordedData = mService.recordStop();

        // Clear the mRetainMainFragment
        fragmentManager.beginTransaction().remove(mRetainMainfragment).commit();

        String time = new SimpleDateFormat("K:mm a", Locale.getDefault()).format(mCalendar.getTime());
        String date = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(mCalendar.getTime());
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(mCalendar.getTime()) + ".txt";

        // Write ECG data to a text file, with a value on each line
        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            for (Double data : recordedData) {
                String value = String.valueOf(data);
                fos.write(value.getBytes());
                fos.write("\n".getBytes());
            }
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        // Create the EventRecord object representing the event just recorded
        EventRecord record = new EventRecord(time, date, mTimerString, fileName,
                mLastLocation.getLatitude(), mLastLocation.getLongitude());

        // Store the recorded event in the database
        long id = mDatabaseAdapter.insertRecord(record);
        if (id < 0) {
            Log.e(TAG, "SQL insert unsuccessful");
        } else {
            Log.e(TAG, "SQL insert successful");
        }

        // Launch EventViewerActivity to display the data just recorded
        Intent intent = new Intent(this, EventViewerActivity.class);
        intent.putExtra("record", record);
        startActivity(intent);
    }

    private void cancelRecording() {
        // Stop recording, ignoring the data returned from the EcgDataService
        mRecording = false;
        mService.recordStop();
    }

    private void resumeRecording() {
        // Animate the record button and timer
        mHandler.postDelayed(animateTimerSheet, 0);
        mHandler.postDelayed(timer, 0);
        mRecordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.stop));
        mRecordButton.setImageSize(40);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();

        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    public void onDialogDismissed() {
        mResolvingError = false;
    }

    // Handle result from error resolution regarding connection to the Google API
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    // Receives data from the EcgDataService and updates data in the EcgLiveFragment
    public class DataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double[] data = intent.getDoubleArrayExtra("ECGData");
            mEcgLiveFragment.addNewData(data);
        }
    }

    // Binds the main thread to the EcgDataService
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            EcgDataService.LocalBinder binder = (EcgDataService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            /* If the app is launched via NFC tag, recording can only begin once the EcgDataService
             * is bound to the main thread.
             */
            if (mStartFromNFC) {
                startRecording();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            Log.d(TAG, "Service Disconnected!");
        }
    };

    // Item clicked from the static items at the bottom of the navigation drawer
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navigation_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
    }

    // A separate thread to keep track of recording length and update the timer.
    private Runnable timer = new Runnable() {
        @Override
        public void run() {
            int mTimeElapsed = (int) (SystemClock.uptimeMillis() - mTimeStart);
            int milliseconds = mTimeElapsed % 1000;
            if (milliseconds > 99) {
                milliseconds /= 10;
            }
            int seconds = (mTimeElapsed / 1000) % 60;
            int minutes = mTimeElapsed / 1000 / 60;
            mTimerString = minutes + ":" + String.format("%02d", seconds) + "." + String.format("%02d", milliseconds);
            mTimer.setText(mTimerString);
            mHandler.post(this);
        }
    };

    // Animation for the timer sheet to slide up when the record button is pressed.
    private Runnable animateTimerSheet = new Runnable() {
        // TODO: Move to ObjectAnimator for animation.
        @Override
        public void run() {
            TransitionManager.beginDelayedTransition(mTimerSheet);
            RelativeLayout.LayoutParams timerSheetRules = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            timerSheetRules.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                timerSheetRules.addRule(RelativeLayout.RIGHT_OF, R.id.fragment_overview);
            }

            mTimerSheet.setLayoutParams(timerSheetRules);
        }
    };

    /* If the app starts from a NFC tag, or if the app is recording when the orientation changes,
     * the record button can only be animated after onResume is finished and views are inflated.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mStartFromNFC) {
            mRecordButton.moveRight(RECORD_BUTTON_DISTANCE);
            mButtonMovedRight = true;
            mStartFromNFC = false;
        } else if (mResumingRecording) {
            mRecordButton.moveRight(RECORD_BUTTON_DISTANCE);
            mButtonMovedRight = true;
            mResumingRecording = false;
        }
    }

    private void emulateHeartData() {
        // Update the heart rate in OverviewFragment every second
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mOverviewFragment.update();
                mHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void resetTimerSheet() {
        // Get screen density to convert px into dp
        float density = getResources().getDisplayMetrics().density;

        // ToDo: Move to ObjectAnimator for animation
        // Set layoutparams to move the sheet below the screen
        RelativeLayout.LayoutParams timerSheetRules = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        timerSheetRules.setMargins(0, 0, 0, (int) (-115 * density));
        timerSheetRules.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mTimerSheet.setLayoutParams(timerSheetRules);
    }

    private void resetRecordButton() {
        // Move back to original location
        mRecordButton.moveLeft(RECORD_BUTTON_DISTANCE);

        // Reset image to record button
        mRecordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record));
        mRecordButton.setImageSize(25);
    }

}