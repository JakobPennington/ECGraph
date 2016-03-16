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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.jakob.ecgraph.R;
import com.jakob.ecgraph.activities.EventHistoryActivity;
import com.jakob.ecgraph.adapters.DatabaseAdapter;
import com.jakob.ecgraph.objects.EventRecord;


/**
 * A dialog located in the EventViewerActivity which is displayed when the user tried to delete a
 * record. The dialog confirms whether the user wants to delete the record, enabling them to cancel
 * the action.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class DeleteDialogFragment extends DialogFragment {
    DeleteDialogListener mListener;

    public static DeleteDialogFragment newInstance(EventRecord record) {
        /* An EventRecord is passed as an extra to this fragment, allosing the EventRecord to be
         * deleted on which option is selected
         */
        DeleteDialogFragment fragment = new DeleteDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("record", record);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DeleteDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_dialog_message);

        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EventRecord record = (EventRecord) getArguments().getSerializable("record");
                DatabaseAdapter adapter = new DatabaseAdapter(getActivity());
                int deleted = adapter.deleteRecord(record);
                /* deleteRecord() returns the number of records deleted from the database. If 0 is
                 * returned, nothing was deleted.
                 */
                if (deleted == 0){
                    // Return to EventViewerActivity and display error
                    mListener.onDialogPositiveClick(DeleteDialogFragment.this);
                } else {
                    // Start EventHistoryActivity and display successful deletion message
                    Intent intent = new Intent(getActivity(), EventHistoryActivity.class);
                    intent.putExtra("deleted", true);
                    startActivity(intent);
                }

            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        return builder.create();
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DeleteDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
}
