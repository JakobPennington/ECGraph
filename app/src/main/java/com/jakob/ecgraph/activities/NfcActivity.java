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

package com.jakob.ecgraph.activities;

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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakob.ecgraph.R;
import com.jakob.ecgraph.fragments.NfcDialogFragment;


/**
 * An activity which writes an NDEF formatted message to compatible NFC tags which enables the
 * tag to launch the application and begin recording at any time when the device is unlocked.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class NfcActivity extends AppCompatActivity {
    private static final String TAG = "NfcActivity";
    private TextView mNfcMessage;
    private ImageView mNfcIcon;
    private NfcAdapter mNfcAdapter;
    private Boolean mDisplayRecentlyUpdated = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        // Allow the elements on the screen to update and prompt the user.
        mNfcMessage = (TextView) findViewById(R.id.connect_message);
        mNfcIcon = (ImageView) findViewById(R.id.device_icon);

        // Get the default NFC adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Gives this activity priority of NFC events
        enableForegroundDispatchSystem();

        // onResume called after NFC tag is written. This statement allows success/errors to remain
        // and not be immediately overwritten.
        if (!mDisplayRecentlyUpdated) {
            // If NFC is available and on, prompt the user to touch tag. If not, prompt the user to
            // turn on NFC in settings.
            if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
                mNfcMessage.setText(R.string.configure_nfc);
                mNfcIcon.setImageResource(R.drawable.nfc);
            } else {
                mNfcMessage.setText(R.string.nfc_unavailable);
                mNfcIcon.setImageResource(R.drawable.error);
                showNFCDialog();
            }
        }
        mDisplayRecentlyUpdated = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Releases NFC event priority to other applications
        disableForegroundDispatchSystem();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

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
                mNfcMessage.setText(R.string.nfc_success);
                mNfcIcon.setImageResource(R.drawable.done);
                mDisplayRecentlyUpdated = true;
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
            mNfcMessage.setText(R.string.nfc_success);
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
        Intent intent = new Intent(this, NfcActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    private void disableForegroundDispatchSystem() {
        mNfcAdapter.disableForegroundDispatch(this);
    }

    private void showNFCDialog() {
        // A dialog to prompt the user to turn on NFC in the device's settings
        NfcDialogFragment nfcDialogFragment = new NfcDialogFragment();
        nfcDialogFragment.show(getSupportFragmentManager(), "nfc");
    }
}
