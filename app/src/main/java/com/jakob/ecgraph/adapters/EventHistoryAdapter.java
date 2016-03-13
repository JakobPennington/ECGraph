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

package com.jakob.ecgraph.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakob.ecgraph.objects.EventRecord;
import com.jakob.ecgraph.R;

import java.util.Collections;
import java.util.List;

/**
 * An adapter for the RecyclerView contained in the EventHistoryActivity and EventListFragment. The
 * RecyclerView is populated by a list of EventRecords retrieved from the database.
 *
 * @author Jakob Pennington
 * @version 1.0
 */
public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.ViewHolder> {
    private static final String TAG = "EventHistoryAdapter";
    private LayoutInflater mInflater;
    private ClickListener mClickListener;
    private List<EventRecord> mEventRecords = Collections.emptyList();

    public EventHistoryAdapter(Context context, List<EventRecord> eventRecords) {
        mInflater = LayoutInflater.from(context);
        this.mEventRecords = eventRecords;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /* Create a new ViewHolder based on the supplied XML layout file. Only as many ViewHolders
         * as items seen in the RecyclerView are created, plus a few spares.
         */
        View view = mInflater.inflate(R.layout.element_event_history, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Bind an EventRecord to a ViewHolder as it scrolls into view.
        EventRecord current = mEventRecords.get(position);
        holder.title.setText(current.getDate());
        holder.description.setText(current.getTime());
        holder.itemView.setTag(current);
    }

    @Override
    public int getItemCount() {
        return mEventRecords.size();
    }

    /**
     * The ViewHolder represents a visible item in a RecyclerView. ViewHolders are recycled, adding
     * different EventRecords as needed then the RecyclerView is scrolled. This leads to greater
     * efficiency over traditional ListViews, as well as other benefits.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            title = (TextView) itemView.findViewById(R.id.event_record_title);
            description = (TextView) itemView.findViewById(R.id.event_record_description);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) {
                mClickListener.itemClicked(v);
            }
        }
    }

    public void setClickListener(ClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    public interface ClickListener {
        void itemClicked(View view);
    }
}
