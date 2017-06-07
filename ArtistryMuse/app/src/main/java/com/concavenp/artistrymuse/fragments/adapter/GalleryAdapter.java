package com.concavenp.artistrymuse.fragments.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.UserInteractionType;
import com.concavenp.artistrymuse.fragments.viewholder.GalleryViewHolder;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;

import java.util.Map;

/**
 * Created by dave on 3/20/2017.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryViewHolder> {

    /**
     * The model is a list of Strings that are the UID of the projects that the user has created.
     */
    private final Map<String, String> mModel;

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

    public GalleryAdapter(Map<String, String> projects, final OnInteractionListener listener) {

        // Project UIDs
        mModel = projects;

        // Details listener
        mListener = listener;

    }

    public GalleryAdapter(Map<String, String> projects, final OnInteractionListener listener, UserInteractionType userInteractionType) {

        this(projects, listener);

        mUserInteractionType = userInteractionType;

    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View result = inflater.inflate(R.layout.item_gallery, parent, false);

        GalleryViewHolder viewHolder = new GalleryViewHolder(result, mUserInteractionType);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {

        holder.bindToPost(mModel.values().toArray()[position], mListener);

    }

    @Override
    public int getItemCount() {

        return mModel.size();

    }

}
