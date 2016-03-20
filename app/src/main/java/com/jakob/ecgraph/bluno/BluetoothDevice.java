package com.jakob.ecgraph.bluno;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.jakob.ecgraph.services.BluetoothLeService;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDevice {
    private final static String TAG = BluetoothDevice.class.getSimpleName();
    public static final int REQUEST_ENABLE_BT = 1;
    private static BluetoothGattCharacteristic mSCharacteristic, mModelNumberCharacteristic, mSerialPortCharacteristic, mCommandCharacteristic;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;
    private Context mContext;
    private Handler mHandler = new Handler();

    private int mBaudrate = 115200;
    private String mPassword = "AT+PASSWOR=DFRobot\r\n";
    private String mBaudrateBuffer = "AT+CURRUART=" + mBaudrate + "\r\n";

    public boolean mConnected = false;

    public static final String SerialPortUUID = "0000dfb1-0000-1000-8000-00805f9b34fb";
    public static final String CommandUUID = "0000dfb2-0000-1000-8000-00805f9b34fb";
    public static final String ModelNumberStringUUID = "00002a24-0000-1000-8000-00805f9b34fb";

    private ConnectionResultListener mConnectionResultListener;
    private DataEventListener mDataEventListener;

    public BluetoothDevice(Context context) {
        mContext = context;
    }

    public void serialBegin(int baud) {
        mBaudrate = baud;
        mBaudrateBuffer = "AT+CURRUART=" + mBaudrate + "\r\n";
    }

    private Runnable mConnectingOverTimeRunnable = new Runnable() {
        @Override
        public void run() {
            mBluetoothLeService.close();
        }
    };

    private Runnable mDisconnectingOverTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothLeService != null) {
                mBluetoothLeService.close();
            }
        }
    };

    public boolean onCreateProcess() {
        // Returns true if BT is supported and false if not.
        if (!initiate()) {
            return false;
        }

        Intent gattServiceIntent = new Intent(mContext, BluetoothLeService.class);
        mContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        return true;
    }

    public void onResumeProcess() {
        mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    public void onPauseProcess() {
        scanLeDevice(false);
        mContext.unregisterReceiver(mGattUpdateReceiver);
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
            mHandler.postDelayed(mDisconnectingOverTimeRunnable, 10000);
            mBluetoothLeService.close();
        }
        mSCharacteristic = null;
    }

    public void onDestroyProcess() {
        mContext.unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    boolean initiate() {
        // Use this check to determine whether BLE is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        // Initializes a Bluetooth adapter. For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        return mBluetoothAdapter != null;
    }

    /**********************************************************
     * Handles various events fired by the Service.
     * * ACTION_GATT_CONNECTED: connected to a GATT server.
     * * ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
     * * ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
     * * ACTION_DATA_AVAILABLE: received data from the device.
     * This can be a result of read or notification operations.
     **********************************************************/
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                mHandler.removeCallbacks(mConnectingOverTimeRunnable);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mHandler.removeCallbacks(mDisconnectingOverTimeRunnable);
                mBluetoothLeService.close();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
                    System.out.println("ACTION_GATT_SERVICES_DISCOVERED  " +
                            gattService.getUuid().toString());
                }
                getGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (mSCharacteristic == mModelNumberCharacteristic) {
                    if (intent.getStringExtra(BluetoothLeService.DF_ROBOT_DATA) != null) {
                        mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, false);
                        mSCharacteristic = mCommandCharacteristic;
                        mSCharacteristic.setValue(mPassword);
                        mBluetoothLeService.writeCharacteristic(mSCharacteristic);
                        mSCharacteristic.setValue(mBaudrateBuffer);
                        mBluetoothLeService.writeCharacteristic(mSCharacteristic);
                        mSCharacteristic = mSerialPortCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
                    }
                } else if (mSCharacteristic == mSerialPortCharacteristic) {
                    mDataEventListener.onDataReceived(intent.getByteArrayExtra(BluetoothLeService.ECG_DATA));
                }
            }
        }
    };

    public void scanForBluno() {
        scanLeDevice(true);
    }

    void scanLeDevice(final boolean enable) {
        if (enable) {
            if (!mScanning) {
                mScanning = true;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        } else {
            if (mScanning) {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }

    // Code to manage Service lifecycle.
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                ((Activity) mContext).finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Device scan callback.
    // Scans for devices and adds them to a list
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final android.bluetooth.BluetoothDevice device, int rssi, byte[] scanRecord) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device.getName().equals("Bluno")) {
                        // Stop scanning and connect to the device
                        scanLeDevice(false);
                        if (mBluetoothLeService.connect(device.getAddress())) {
                            Log.d(TAG, "Connect request success");
                            mHandler.postDelayed(mConnectingOverTimeRunnable, 10000);
                            mConnectionResultListener.onConnectionResult(true);
                        } else {
                            Log.d(TAG, "Connect request fail");
                            mConnectionResultListener.onConnectionResult(false);
                        }
                    }
                }
            });
        }
    };

    private void getGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        mModelNumberCharacteristic = null;
        mSerialPortCharacteristic = null;
        mCommandCharacteristic = null;
        ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            System.out.println("displayGattServices + uuid=" + uuid);

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                switch (uuid) {
                    case ModelNumberStringUUID:
                        mModelNumberCharacteristic = gattCharacteristic;
                        System.out.println("mModelNumberCharacteristic  " + mModelNumberCharacteristic.getUuid().toString());
                        break;
                    case SerialPortUUID:
                        mSerialPortCharacteristic = gattCharacteristic;
                        System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristic.getUuid().toString());
                        break;
                    case CommandUUID:
                        mCommandCharacteristic = gattCharacteristic;
                        System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristic.getUuid().toString());
                        break;
                }
            }
            mGattCharacteristics.add(charas);
        }

        if (mModelNumberCharacteristic == null || mSerialPortCharacteristic == null || mCommandCharacteristic == null) {
            Toast.makeText(mContext, "Please select DFRobot devices", Toast.LENGTH_SHORT).show();
        } else {
            mSCharacteristic = mModelNumberCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
            mBluetoothLeService.readCharacteristic(mSCharacteristic);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void setConnectionResultListener(ConnectionResultListener eventListener) {
        this.mConnectionResultListener = eventListener;
    }

    public void setDataEventListener(DataEventListener dataEventListener){
        this.mDataEventListener = dataEventListener;
    }


    public interface ConnectionResultListener {
        void onConnectionResult(boolean result);
    }

    public interface DataEventListener {
        void onDataReceived(byte[] data);
    }
}