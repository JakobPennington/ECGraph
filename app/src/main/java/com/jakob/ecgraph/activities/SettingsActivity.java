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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jakob.ecgraph.R;


/**
 * A simple activity which lists settings related options to the user. Currently, only AboutActivity
 * and LicenceActivity are listed, however settings options can be added as they are required by the
 * app.
 *
 * @author  Jakob Pennington
 * @version 1.0
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    // Launch the appropriate activity based on touch event
    public void onClick (View v){
        switch (v.getId()){
            case R.id.settings_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.settings_licenses:
                startActivity(new Intent(this, LicensesActivity.class));
                break;
        }

    }

}
