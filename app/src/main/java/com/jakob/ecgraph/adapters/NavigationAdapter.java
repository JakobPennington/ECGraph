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
import android.widget.ImageView;
import android.widget.TextView;

import com.jakob.ecgraph.objects.NavigationElement;
import com.jakob.ecgraph.R;

import java.util.Collections;
import java.util.List;


/**
 * An adapter for the RecyclerView located in the NavigationDrawerFragment. This Recyclerview
 * provides navigation within the activity, presenting a list of activities available within
 * the app.
 *
 * @author  Jakob Pennington
 * @version 1.0
 */
public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private ClickListener mClickListener;
    private List<NavigationElement> mNavigationElements = Collections.emptyList();

    public NavigationAdapter(Context context, List<NavigationElement> navigationElements){
        mInflater = LayoutInflater.from(context);
        this.mNavigationElements =navigationElements;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /* Create a new ViewHolder based on the supplied XML layout file. Only as many ViewHolders
         * as items seen in the RecyclerView are created, plus a few spares.
         */
        View view = mInflater.inflate(R.layout.element_navigation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Bind an navigable element to a ViewHolder as it scrolls into view.
        NavigationElement current = mNavigationElements.get(position);
        holder.title.setText(current.getmTitle());
        holder.icon.setImageResource(current.getmIconId());
        holder.icon.setImageAlpha(138);

    }

    @Override
    public int getItemCount() {
        return mNavigationElements.size();
    }

    /**
     * The ViewHolder represents a visible item in a RecyclerView. ViewHolders are recycled, adding
     * different navigation items as needed then the RecyclerView is scrolled. This leads to greater
     * efficiency over traditional ListViews, as well as other benefits.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.navigation_element_text);
            icon = (ImageView) itemView.findViewById(R.id.navigation_element_icon);
        }

        @Override
        public void onClick(View v) {
            if(mClickListener != null){
                mClickListener.itemClicked(v);
            }
        }
    }

    public void setClickListener(ClickListener clickListener){
        this.mClickListener = clickListener;
    }

    public interface ClickListener{
        void itemClicked (View view);
    }
}
