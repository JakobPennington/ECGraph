package com.jakob.ecgraph.activities;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.jakob.ecgraph.R;
import com.jakob.ecgraph.fragments.BluetoothFragment;
import com.jakob.ecgraph.fragments.NfcDialogFragment;
import com.jakob.ecgraph.fragments.NfcFragment;

/**
 * An activity which allows the user to connect to an ECG device. An NFC tag is written with data to
 * allow the tag to be scanned to begin recording, and a bluetooth connection is constructed to
 * allow the transfer of data between smartphone and ECG device.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class ConnectDeviceActivity extends AppCompatActivity implements NfcDialogFragment.NfcDialogListener {
    private static final String TAG = "ConnectDeviceActivity";
    private NfcFragment mNfcFragment;
    private BluetoothFragment mBluetoothFragment;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        mNfcFragment = new NfcFragment();
        mBluetoothFragment = new BluetoothFragment();

        // Transition the NfcFragment into view
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder_connect, mNfcFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Process intents created when a NFC tag is scanned.
        super.onNewIntent(intent);
        mNfcFragment.processIntent(intent);
    }

    public void showNFCDialog() {
        // A dialog to prompt the user to turn on NFC in the device's settings
        NfcDialogFragment nfcDialogFragment = new NfcDialogFragment();
        nfcDialogFragment.show(getSupportFragmentManager(), "nfc");
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

    public void waitBluetooth() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectBluetooth();
            }
        }, 1000);
    }

    private void connectBluetooth() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder_connect, mBluetoothFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
