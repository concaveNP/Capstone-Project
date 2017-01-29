package com.concavenp.artistrymuse.fragments.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.fragments.viewholder.UserResponseViewHolder;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.concavenp.artistrymuse.model.UserResponseHit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 12/26/2016.
 */
public class UserSearchResultAdapter extends RecyclerView.Adapter<UserResponseViewHolder> {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = UserSearchResultAdapter.class.getSimpleName();

    private List<UserResponseHit> mResultItems = new ArrayList<>();

    /**
     * Interface that will be used for the signalling the details of a item
     */
    private OnDetailsInteractionListener mListener;

    public UserSearchResultAdapter(OnDetailsInteractionListener listener) {

        super();

        mListener = listener;

    }

    @Override
    public UserResponseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View result = inflater.inflate(R.layout.item_following, parent, false);

        UserResponseViewHolder viewHolder = new UserResponseViewHolder(result);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(UserResponseViewHolder holder, final int position) {

        // Get the model data that will be used to populate all of the views
        UserResponseHit response = mResultItems.get(position);

        holder.bindToPost(response, mListener);

    }

    @Override
    public int getItemCount() {
        return mResultItems.size();
    }

    public void add(List<UserResponseHit> results) {
        mResultItems.addAll(results);
        this.notifyDataSetChanged();
    }

    public void clearData() {
        mResultItems.clear();
        this.notifyDataSetChanged();
    }

}
