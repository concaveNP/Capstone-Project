package com.concavenp.artistrymuse.fragments.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.fragments.viewholder.GalleryViewHolder;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;

import java.util.List;

/**
 * Created by dave on 3/20/2017.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryViewHolder> {

    /**
     * The model is a list of Strings that are the UID of the projects that the user has created.
     */
    private final List<String> mModel;

    /**
     * The listener that will be used when the user requests details for a given project.
     */
    private final OnDetailsInteractionListener mListener;

    public GalleryAdapter(List<String> projects, final OnDetailsInteractionListener listener) {

        // Project UIDs
        mModel = projects;

        // Details listener
        mListener = listener;

    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View result = inflater.inflate(R.layout.item_gallery, parent, false);

        GalleryViewHolder viewHolder = new GalleryViewHolder(result);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {

        holder.bindToPost(mModel.get(position), mListener);

    }

    @Override
    public int getItemCount() {

        return mModel.size();

    }

}
