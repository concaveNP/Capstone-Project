/*
 * ArtistryMuse is an application that allows artist to share projects
 * they have created along with the inspirations behind them for others to
 * discover and enjoy.
 * Copyright (C) 2017  David A. Todd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.concavenp.artistrymuse.fragments.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.UserInteractionType;
import com.concavenp.artistrymuse.fragments.viewholder.InspirationViewHolder;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;
import com.concavenp.artistrymuse.model.Inspiration;

import java.util.Map;

/**
 * Created by dave on 3/20/2017.
 */

public class InspirationAdapter extends RecyclerView.Adapter<InspirationViewHolder> {

    /**
     * The model is a map of the project's inspirations.
     */
    private final Map<String, Inspiration> mModel;

    /**
     * The listener that will be used when the user requests details for a given project.
     */
    private final OnInteractionListener mListener;

    /**
     * This will be used by the listeners of the interaction of this view to determine how
     * it should be interpreted as.  The default will be none, and thus, no action will be taken
     * when the user selects this item within the recycler view.
     */
    private UserInteractionType mUserInteractionType = UserInteractionType.NONE;

    public InspirationAdapter(Map<String, Inspiration> inspirations, final OnInteractionListener listener) {

        // Project UIDs
        mModel = inspirations;

        // Details listener
        mListener = listener;

    }

    public InspirationAdapter(Map<String, Inspiration> inspirations, final OnInteractionListener listener, UserInteractionType userInteractionType) {

        this(inspirations, listener);

        mUserInteractionType = userInteractionType;

    }

    @Override
    public InspirationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View result = inflater.inflate(R.layout.item_inspiration, parent, false);

        InspirationViewHolder viewHolder = new InspirationViewHolder(result, mUserInteractionType);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(InspirationViewHolder holder, int position) {

        holder.bindToPost(mModel.values().toArray()[position], mListener);

    }

    @Override
    public int getItemCount() {

        return mModel.size();

    }

}
