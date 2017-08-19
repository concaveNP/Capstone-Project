package com.concavenp.artistrymuse.fragments.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concavenp.artistrymuse.fragments.viewholder.BaseViewHolder;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 12/26/2016.
 *
 * E = The model data (i.e. UserResponseHit)
 * VH = The ViewHolder (i.e. UserResponseViewHolder, ProjectResponseViewHolder)
 */
public class SearchResultAdapter<E, VH extends BaseViewHolder> extends RecyclerView.Adapter<VH> {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = SearchResultAdapter.class.getSimpleName();

    private List<E> mResultItems = new ArrayList<>();
    private Class<VH> mViewHolder;
    private int mResource;

    /**
     * Interface that will be used for the signalling the details of a item
     */
    private OnInteractionListener mListener;

    public SearchResultAdapter(Class<VH> viewHolder, OnInteractionListener listener, int resource) {

        super();

        // The View Holder
        mViewHolder = viewHolder;

        // The listener that will be notified when an item is selected
        mListener = listener;

        // Set the resource that will be inflated
        mResource = resource;

    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View result = inflater.inflate(mResource, parent, false);

        BaseViewHolder viewHolder = null;

        try {
            viewHolder = mViewHolder.getDeclaredConstructor(View.class).newInstance(result);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return (VH) viewHolder;

    }

    @Override
    public void onBindViewHolder(VH viewHolder, final int position) {

        // Get the model data that will be used to populate all of the views
        E response = mResultItems.get(position);

        // Adapt the given data into the view holder given (aka "bind the data")
        viewHolder.bindToPost(response, mListener);

    }

    @Override
    public int getItemCount() {

        return mResultItems.size();

    }

    public void add(List<E> results) {

        mResultItems.addAll(results);

        this.notifyDataSetChanged();

    }

    public void clearData() {

        mResultItems.clear();

        this.notifyDataSetChanged();

    }

}

