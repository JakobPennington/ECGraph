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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakob.ecgraph.R;
import com.jakob.ecgraph.activities.ConnectDeviceActivity;
import com.jakob.ecgraph.activities.EventHistoryActivity;
import com.jakob.ecgraph.activities.EventMapActivity;
import com.jakob.ecgraph.activities.NfcActivity;
import com.jakob.ecgraph.activities.SettingsActivity;
import com.jakob.ecgraph.adapters.NavigationAdapter;
import com.jakob.ecgraph.objects.NavigationElement;

import java.util.ArrayList;
import java.util.List;


/**
 * A fragment containing a RecyclerView enabling navigation throughout the application. This
 * fragment is located in the MainActivity, aligning with Google's Material Design Guidelines.
 *
 * @author  Jakob Pennington
 * @version 1.0
 */
public class NavigationDrawerFragment extends Fragment implements NavigationAdapter.ClickListener {
    private static final String TAG = "NavigationDrawerFragment";

    public NavigationDrawerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        // Set up the adapter for the RecyclerView
        NavigationAdapter mNavigationAdapter = new NavigationAdapter(getActivity(), getNavigationElementList());
        mNavigationAdapter.setClickListener(this);

        // Set up the RecyclerView
        RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.navigation_recycler_view);
        mRecyclerView.setAdapter(mNavigationAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Set the aplha of the settings icon as per Material Design specifications
        layout.findViewById(R.id.settings_icon).setAlpha((float) 0.54);

        return layout;
    }

    private static List<NavigationElement> getNavigationElementList(){
        // Set up the navigation elements in the RecyclerView
        List<NavigationElement> navigationElementList = new ArrayList<>();
        int[] icons = {R.drawable.watch, R.drawable.list, R.drawable.map};
        String[] titles = {"Connect Device", "Event History", "Event Map"};

        // Create a list of NavigationElements to be added to the RecyclerView
        for (int i = 0; i < icons.length && i < titles.length; i++){
            NavigationElement newElement = new NavigationElement();
            newElement.setmIconId(icons[i]);
            newElement.setmTitle(titles[i]);
            navigationElementList.add(newElement);
        }
        return navigationElementList;
    }

    public void setUp(DrawerLayout drawerLayout, Toolbar toolbar) {
        // Set up the hamburger menu in the ActionBar
        final ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }
        };

        drawerLayout.setDrawerListener(mDrawerToggle);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    // Item clicked in the RecyclerView
    @Override
    public void itemClicked(View view) {
        String itemClicked = (String) ((TextView) view.findViewById(R.id.navigation_element_text)).getText();
        switch (itemClicked){
            case "Connect Device":
                startActivity(new Intent(getActivity(), ConnectDeviceActivity.class));
                break;
            case "Event History":
                startActivity(new Intent(getActivity(), EventHistoryActivity.class));
                break;
            case "Event Map":
                startActivity(new Intent(getActivity(), EventMapActivity.class));
                break;
            case "Settings":
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
        }
    }

}
