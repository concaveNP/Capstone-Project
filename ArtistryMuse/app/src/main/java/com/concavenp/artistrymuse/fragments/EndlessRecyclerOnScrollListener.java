package com.concavenp.artistrymuse.fragments;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * This code started off from the below references and then modified for use for use by this
 * application.
 *
 * References:
 *
 * Endless RecyclerView OnScrollListener
 *      - https://gist.github.com/ssinss/e06f12ef66c51252563e
 */
public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    // The total number of items in the dataset after the last load
    private int mPreviousTotal;

    // True if we are still waiting for the last set of data to load.
    private boolean mLoading;
    private int mVisibleThreshold;
    private int mCurrentPage;
    private LinearLayoutManager mLinearLayoutManager;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {

        mLinearLayoutManager = linearLayoutManager;

        // Initializes the fields to a starting point
        initValues();

    }

    public void initValues() {
        mPreviousTotal = 0;
        mLoading = false;
        mVisibleThreshold = 5;
        mCurrentPage = 0;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = mLinearLayoutManager.getItemCount();
        int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        if (mLinearLayoutManager instanceof GridLayoutManager) {
            // The minimum amount of items to have below your current scroll position before loading more.
            // This should be equal to the number of columns in the grid.  Thus, one row.
            mVisibleThreshold = ((GridLayoutManager) mLinearLayoutManager).getSpanCount();
        }

        if (mLoading) {

            if (totalItemCount > mPreviousTotal) {

                mLoading = false;
                mPreviousTotal = totalItemCount;

            }

        }

        // Check to see if the end has been reached
        if ((!mLoading) && ((totalItemCount - visibleItemCount) <= (firstVisibleItem + mVisibleThreshold))) {

            mCurrentPage++;

            onLoadMore(mCurrentPage);

            mLoading = true;
        }
    }

    /**
     * Abstract method that class extenders must implement in order for new data to be loaded when
     * scrolling beyond a defined threshold.
     *
     * @param current_page A value specifically used within RESTful interfaces calls for more data
     */
    public abstract void onLoadMore(int current_page);

}
