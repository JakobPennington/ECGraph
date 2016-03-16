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

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.jakob.ecgraph.R;
import com.jakob.ecgraph.activities.EventMapActivity;
import com.jakob.ecgraph.adapters.DatabaseAdapter;
import com.jakob.ecgraph.objects.EventRecord;

import java.util.List;

/**
 * This fragment displays a Google Map Fragment, marking the location of EventRecords stored in the
 * database. Markers and Clusters can be selected to show the relevant EventRecord data.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class EventMapFragment extends Fragment implements OnMapReadyCallback,
        ClusterManager.OnClusterItemClickListener<EventRecord>,
        ClusterManager.OnClusterClickListener<EventRecord>, GoogleMap.OnMapClickListener {

    private EventMapActivity mActivity;
    private GoogleMap mMap;
    private ClusterManager<EventRecord> mClusterManager;
    private List<EventRecord> mMappedEvents;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_map, container, false);

        // Set up the Google Map fragment
        GoogleMapOptions mapOptions = new GoogleMapOptions();
        mapOptions.compassEnabled(true);
        mapOptions.rotateGesturesEnabled(true);
        mapOptions.scrollGesturesEnabled(true);
        mapOptions.tiltGesturesEnabled(true);
        mapOptions.zoomControlsEnabled(true);
        mapOptions.zoomGesturesEnabled(true);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(mapOptions);

        // Swap the fragment into the placeholder layout in the EventMapActivity
        FragmentManager fragmentManager = ((EventMapActivity) getActivity()).getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.map_container, mapFragment).commit();
        mapFragment.getMapAsync(this);

        // Get EventRecords from the database
        DatabaseAdapter mDatabaseAdapter = new DatabaseAdapter(getActivity());
        mMappedEvents = mDatabaseAdapter.getEventHistory();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update the title in the ActionBar which is shared with EventListFragment
        ((EventMapActivity)getActivity()).getSupportActionBar().setTitle(getResources()
                .getString(R.string.title_activity_event_map));
    }

    @Override
    public void onPause() {
        super.onPause();
        // Store the camera's position so it can be returned to after EventListFragment is closed
        ((EventMapActivity) getActivity()).setCameraPosition(mMap.getCameraPosition());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (EventMapActivity) getActivity();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // get the GoogleMap
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        // Set the camera's position to the most recently recorded event
        LatLng lastEventPosition;
        if (mMappedEvents.size() > 0) {
            EventRecord lastRecord = mMappedEvents.get(0);
            lastEventPosition = new LatLng(lastRecord.getLatitude(), lastRecord.getLongitude());
        } else {
            // If no events recorded, set map to Flinders at Tonsley
            lastEventPosition = new LatLng(-35.009593, 138.571186);
        }
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(lastEventPosition)
                .zoom(11)
                .bearing(0)
                .tilt(0)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Restore the cameras position if this fragment has been swapped in from the EventListFragment
        cameraPosition = ((EventMapActivity) getActivity()).getCameraPosition();
        if(cameraPosition != null){
            setCameraPosition(cameraPosition);
        }

        setUpClusterer();
    }

    private void setUpClusterer() {
        // Set up the ClusterManager
        mClusterManager = new ClusterManager<>(getActivity(), mMap);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterClickListener(this);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        // Add cluster items (markers) to the cluster manager.
        for (EventRecord record : mMappedEvents) {
            mClusterManager.addItem(record);
        }
    }


    @Override
    public boolean onClusterItemClick(EventRecord record) {
        // Get the record associated with the selected marker
        mActivity.setSelectedRecord(record);
        mActivity.setRecordFooterText(record.getDate(), record.getTime());

        // Show the footer displaying the selected EventRecord's data
        if (mActivity.getListFooterDisplayed()) {
            mActivity.swapFooters();
        } else if (!(mActivity.getRecordFooterDisplayed())) {
            mActivity.showRecordFooter();
        }

        return false;
    }

    @Override
    public boolean onClusterClick(Cluster<EventRecord> cluster) {
        // Get the selected cluster
        mActivity.setSelectedCluster(cluster);

        // Display the  show list footer
        if (mActivity.getRecordFooterDisplayed()) {
            mActivity.swapFooters();
        } else if (!(mActivity.getListFooterDisplayed())) {
            mActivity.showListFooter();
        }

        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // Remove any displayed footers
        if (mActivity.getRecordFooterDisplayed()) {
            mActivity.hideRecordFooter();
        } else if (mActivity.getListFooterDisplayed()) {
            mActivity.hideListFooter();
        }
    }

    public void setCameraPosition(CameraPosition cameraPosition) {
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
