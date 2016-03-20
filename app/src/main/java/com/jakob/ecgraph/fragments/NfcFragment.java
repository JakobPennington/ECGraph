package com.jakob.ecgraph.fragments;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakob.ecgraph.R;
import com.jakob.ecgraph.activities.ConnectDeviceActivity;

/**
 * A fragment which handles connecting and writing to the ECG device's NFC, which facilitates
 * the connection via bluetooth.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class NfcFragment extends Fragment {
    private static final String TAG = "NfcFragment";
    private TextView mNfcMessage;
    private ImageView mNfcIcon;
    private NfcAdapter mNfcAdapter;
    private Boolean mDisplayRecentlyUpdated = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nfc, container, false);

        // Get the default NFC adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Allow the elements on the screen to update and prompt the user.
        mNfcMessage = (TextView) getActivity().findViewById(R.id.connect_message);
        mNfcIcon = (ImageView) getActivity().findViewById(R.id.device_icon);

        // Gives this activity priority of NFC events
        enableForegroundDispatchSystem();

        // onResume called after NFC tag is written. This statement allows success/errors to remain
        // and not be immediately overwritten.
        if (!mDisplayRecentlyUpdated) {
            // If NFC is available and on, prompt the user to touch tag. If not, prompt the user to
            // turn on NFC in settings.
            if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
                mNfcMessage.setText(R.string.device_connect);
                mNfcIcon.setImageResource(R.drawable.watch);
            } else {
                mNfcMessage.setText(R.string.nfc_unavailable);
                mNfcIcon.setImageResource(R.drawable.error);
                ((ConnectDeviceActivity)getActivity()).showNFCDialog();
            }
        }
        mDisplayRecentlyUpdated = false;
    }

    @Override
    public void onPause() {
        super.onPause();

        // Releases NFC event priority to other applications
        disableForegroundDispatchSystem();
    }

    public void processIntent(Intent intent) {
        /* Process intent from reading an NFC tag. Two records are added to the NDEF message:
         * 1) A message which triggers the NDEF_DISCOVERED action, allowing MainActivity to
         * interpret the NFC tag and begin recording data
         * 2) An Android ApplicationRecord (AAR) which ensures ECGraph is launched to handle the
         * intent, and not another application.
         */
        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] payload = "Shia LaBeouf".getBytes();
            NdefRecord record1 = NdefRecord.createExternal("com.jakob.ecgraph", "externaltype", payload);
            NdefRecord record2 = NdefRecord.createApplicationRecord("com.jakob.ecgraph");
            NdefMessage msg = new NdefMessage(new NdefRecord[]{
                    record1, record2
            });
            writeNdefMessage(tag, msg);
        }
    }

    private void writeNdefMessage(Tag tag, NdefMessage msg) {
        //Write the NDEF formatted message to the NFC tag
        try {
            if (tag == null) {
                Log.e(TAG, "Tag object cannot be null");
                return;
            }

            Ndef ndef = Ndef.get(tag);

            if (ndef == null) {
                // If the tag is not in NDEF format, reformat the tag and write the message
                formatTag(tag, msg);
            } else {
                // If the NFC tag is NDEF formatted or unformatted, write to the tag
                ndef.connect();

                // Tags can be read-only, which is handled here
                if (!ndef.isWritable()) {
                    Log.e(TAG, "NFC tag is not writeable.");
                    mNfcMessage.setText(R.string.nfc_not_writeable);
                    mNfcIcon.setImageResource(R.drawable.error);
                    ndef.close();
                    mDisplayRecentlyUpdated = true;
                    return;
                }

                ndef.writeNdefMessage(msg);
                ndef.close();

                // Update the interface
                mNfcMessage.setText(R.string.device_success);
                mNfcIcon.setImageResource(R.drawable.done);
                mDisplayRecentlyUpdated = true;

                // Begin connecting Bluetooth
                ((ConnectDeviceActivity)getActivity()).waitBluetooth();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void formatTag(Tag tag, NdefMessage msg) {
        // Format the tag before writing if not in NDEF format.
        try {
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);

            if (ndefFormatable == null) {
                Log.e(TAG, "Tag is not NDEF formatable.");
                mNfcMessage.setText(R.string.nfc_not_writeable);
                mNfcIcon.setImageResource(R.drawable.error);
                mDisplayRecentlyUpdated = true;
                return;
            }

            ndefFormatable.connect();
            ndefFormatable.format(msg);
            ndefFormatable.close();

            // Update the interface
            mNfcMessage.setText(R.string.device_success);
            mNfcIcon.setImageResource(R.drawable.done);
            mDisplayRecentlyUpdated = true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void enableForegroundDispatchSystem() {
        /* Foreground dispatch system intercepts intent created by scanning an NFC tag, so while
         * this activity is running it gets first dibs on any NFC tags read.
         */
        Intent intent = new Intent(getActivity(), ConnectDeviceActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        mNfcAdapter.enableForegroundDispatch(getActivity(), pendingIntent, intentFilters, null);
    }

    private void disableForegroundDispatchSystem() {
        mNfcAdapter.disableForegroundDispatch(getActivity());
    }

}
