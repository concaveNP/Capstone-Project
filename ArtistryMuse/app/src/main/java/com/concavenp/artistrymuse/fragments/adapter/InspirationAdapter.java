package com.concavenp.artistrymuse.fragments.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.fragments.viewholder.GalleryViewHolder;
import com.concavenp.artistrymuse.fragments.viewholder.InspirationViewHolder;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.concavenp.artistrymuse.model.Inspiration;

import java.util.List;
import java.util.Map;

/**
 * Created by dave on 3/20/2017.
 */

public class InspirationAdapter extends RecyclerView.Adapter<InspirationViewHolder> {

    /**
     * The model is a map of the project's inspirations.
     */
    private final Map<String, Inspiration> mModel;

    // TODO: dunno about this listener
    /**
     * The listener that will be used when the user requests details for a given project.
     */
    private final OnDetailsInteractionListener mListener;

    public InspirationAdapter(Map<String, Inspiration> inspirations, final OnDetailsInteractionListener listener) {

        // Project UIDs
        mModel = inspirations;

        // Details listener
        mListener = listener;

    }

    @Override
    public InspirationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View result = inflater.inflate(R.layout.item_inspiration, parent, false);

        InspirationViewHolder viewHolder = new InspirationViewHolder(result);

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
