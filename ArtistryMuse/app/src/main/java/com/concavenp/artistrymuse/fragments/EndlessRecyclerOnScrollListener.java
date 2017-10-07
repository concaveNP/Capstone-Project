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

package com.concavenp.artistrymuse.fragments;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * This code started off from the below references and then modified for use for use by this
 * application.
 *
 * References:
 *
 * Endless RecyclerView OnScrollListener
 *      - https://gist.github.com/ssinss/e06f12ef66c51252563e
 * How to implement endless list with RecyclerView?
 *      - https://stackoverflow.com/questions/26543131/how-to-implement-endless-list-with-recyclerview
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
    private StaggeredGridLayoutManager mLayoutManager;

    private int visibleItemCount;
    private int totalItemCount;
    private int firstVisibleItem;

    public EndlessRecyclerOnScrollListener(StaggeredGridLayoutManager layoutManager) {

        mLayoutManager = layoutManager;

        // Initializes the fields to a starting point
        initValues();

    }

    public void initValues() {
        mPreviousTotal = 0;
        mLoading = true;
        mVisibleThreshold = 5;
        mCurrentPage = 0;
        visibleItemCount = 0;
        totalItemCount = 0;
        firstVisibleItem = 0;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLayoutManager.getItemCount();

        int[] firstVisibleItems = null;
        firstVisibleItems = mLayoutManager.findFirstVisibleItemPositions(firstVisibleItems);
        if(firstVisibleItems != null && firstVisibleItems.length > 0) {
            firstVisibleItem = firstVisibleItems[0];
        }

        if (mLoading) {
            if (totalItemCount > mPreviousTotal) {
                mLoading = false;
                mPreviousTotal = totalItemCount;
            }
        }

        if (!mLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + mVisibleThreshold)) {

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
