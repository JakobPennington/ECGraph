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

package com.jakob.ecgraph.objects;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;


/**
 * A simple class which represents a single recorded event stored in the database. Extends
 * Serializable so objects can be passed as extras between activities and fragments. Extends
 * ClusterItem to make EventRecords to be displayed as markers in EventMapFragment.
 *
 * @author  Jakob Pennington
 * @version 1.0
 */
public class EventRecord implements Serializable, ClusterItem {
    private String mDate;
    private String mTime;
    private String mDuration;
    private String mFileName;
    private double mLongitude;
    private double mLatitude;

    public EventRecord (String time, String date, String duration, String fileName, double latitude,
                        double longitude){
        this.mTime = time;
        this.mDate = date;
        this.mDuration = duration;
        this.mFileName = fileName;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        this.mTime = time;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        this.mDuration = duration;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(mLatitude, mLongitude);
    }
}
