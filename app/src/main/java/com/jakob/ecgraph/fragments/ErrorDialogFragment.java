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

package com.jakob.ecgraph.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.gms.common.GoogleApiAvailability;
import com.jakob.ecgraph.activities.MainActivity;

/**
 * A fragment to display an error dialog
 *
 * @author Google
 * Sourced from https://developers.google.com/android/guides/api-client
 */
public class ErrorDialogFragment extends DialogFragment {
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";

    public ErrorDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the error code and retrieve the appropriate dialog
        int errorCode = this.getArguments().getInt(DIALOG_ERROR);
        return GoogleApiAvailability.getInstance().getErrorDialog(
                this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ((MainActivity) getActivity()).onDialogDismissed();
    }
}
