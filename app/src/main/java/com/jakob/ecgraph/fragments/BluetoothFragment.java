package com.jakob.ecgraph.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakob.ecgraph.bluno.BlunoLibrary;
import com.jakob.ecgraph.R;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * [Activity Description]
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class BluetoothFragment extends BlunoLibrary {
    private TextView mBluetoothMessage;
    private ImageView mBluetoothIcon;
    private boolean mIsBtleSupported;

    private int[] mBuffer = new int[17];
    private int mIndex = 0;

    public BluetoothFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        // Initiate Bluetooth via the Bluno Library
        mIsBtleSupported = onCreateProcess();
        enableBluetooth();

        // Set the Uart Baudrate on BLE chip to 115200
        serialBegin(115200);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        onResumeProcess();

        // Allow the elements on the screen to update and prompt the user.
        mBluetoothMessage = (TextView) getActivity().findViewById(R.id.connect_message);
        mBluetoothIcon = (ImageView) getActivity().findViewById(R.id.device_icon);

        if (!mIsBtleSupported) {
            // Bluetooth unsupported
            setText(getString(R.string.bluetooth_unsupported));
            setIcon(R.drawable.bluetooth_off);
        }

        scanForBluno();
    }

    @Override
    public void onPause() {
        super.onPause();
        onPauseProcess();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onDestroyProcess();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            setText(getString(R.string.bluetooth_off));
            setIcon(R.drawable.bluetooth_off);
        } else {
            setText(getString(R.string.bluetooth_connect));
            setIcon(R.drawable.bluetooth);
        }
    }

    public void enableBluetooth() {
        // Ensures Bluetooth is enabled on the device. If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onSerialReceived(byte[] data) {


        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
            sb.append(' ');
        }
        System.out.println(sb.toString());
    }

    private int byteToInt(String highByte, String lowByte){
        return 0;
    }

   /* byteArrayToLong = function(*//*byte[]*//*byteArray) {
        var value = 0;
        for ( var i = byteArray.length - 1; i >= 0; i--) {
            value = (value * 256) + byteArray[i];
        }

        return value;
    };*/

    protected void setText(String message) {
        mBluetoothMessage.setText(message);
    }

    protected void setIcon(int id) {
        mBluetoothIcon.setImageResource(id);
    }
}
