package com.jakob.ecgraph.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakob.ecgraph.bluno.BluetoothDevice;
import com.jakob.ecgraph.R;

/**
 * A Fragment which establishes a Bluetooth connection with an BTLE ECG device.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class BluetoothFragment extends Fragment implements BluetoothDevice.ConnectionResultListener, BluetoothDevice.DataEventListener {
    private BluetoothDevice mBluetoothDevice;
    private TextView mBluetoothMessage;
    private ImageView mBluetoothIcon;
    private boolean mIsBtleSupported;

    public BluetoothFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        mBluetoothDevice = new BluetoothDevice(getActivity());

        // Initiate Bluetooth via the Bluno Library
        mIsBtleSupported = mBluetoothDevice.onCreateProcess();
        enableBluetooth();

        // Set the Uart Baudrate on BLE chip to 115200
        mBluetoothDevice.serialBegin(115200);
        mBluetoothDevice.setConnectionResultListener(this);
        mBluetoothDevice.setDataEventListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBluetoothDevice.onResumeProcess();

        // Allow the elements on the screen to update and prompt the user.
        mBluetoothMessage = (TextView) getActivity().findViewById(R.id.connect_message);
        mBluetoothIcon = (ImageView) getActivity().findViewById(R.id.device_icon);

        if (!mIsBtleSupported) {
            // Bluetooth unsupported
            setText(getString(R.string.bluetooth_unsupported));
            setIcon(R.drawable.bluetooth_off);
        }
        mBluetoothDevice.scanForBluno();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBluetoothDevice.onPauseProcess();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBluetoothDevice.onDestroyProcess();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == BluetoothDevice.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
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
        if (!mBluetoothDevice.getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothDevice.REQUEST_ENABLE_BT);
        }
    }

    protected void setText(String message) {
        mBluetoothMessage.setText(message);
    }

    protected void setIcon(int id) {
        mBluetoothIcon.setImageResource(id);
    }

    @Override
    public void onConnectionResult(boolean result) {
        if (result){
            setText(getString(R.string.bluetooth_success));
            setIcon(R.drawable.done);
        } else {
            setText(String.valueOf(R.string.bluetooth_failed));
            setIcon(R.drawable.bluetooth_off);
        }
    }

    @Override
    public void onDataReceived(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
            sb.append(' ');
        }
        System.out.println(sb.toString());
    }
}
