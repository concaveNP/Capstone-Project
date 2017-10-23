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

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.UserInteractionType;
import com.concavenp.artistrymuse.fragments.viewholder.UserViewHolder;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;
import com.concavenp.artistrymuse.model.Following;
import com.concavenp.artistrymuse.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import static com.concavenp.artistrymuse.StorageDataType.USERS;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link UserListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * References:
 *
 *  How can I detect which layout is selected by Android in my application?
 *      - https://stackoverflow.com/questions/11205020/how-can-i-detect-which-layout-is-selected-by-android-in-my-application
 */
public class UserListFragment extends BaseFragment implements OnInteractionListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = UserListFragment.class.getSimpleName();

    private FirebaseRecyclerAdapter<Following, UserViewHolder> mAdapter;
    private RecyclerView mRecycler;

    private ValueEventListener mEventListener;

    /**
     * Flag that indicates if the user's device should be treated as "large" or not.  The flag will
     * be used to determine how clicks within a list entry of users should handled.
     */
    private boolean largeDevice = false;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment FollowingFragment.
     */
    public static UserListFragment newInstance() {

        UserListFragment fragment = new UserListFragment();

        return fragment;

    }

    /**
     * Required empty public constructor
     */
    public UserListFragment() {

        // Do nothing

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_user_list, container, false);

        // Store off the layout size using tag values within the view
        String tag = (String)mainView.getTag();
        if (tag.equals(getString(R.string.layout_large))) {
            largeDevice = true;
        }

        mRecycler = mainView.findViewById(R.id.following_recycler_view);
        mRecycler.setHasFixedSize(true);

        // Set up Layout
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecycler.setLayoutManager(staggeredGridLayoutManager);

        return mainView;

    }

    @Override
    public void onStart() {

        super.onStart();

        mDatabase.child(USERS.getType()).child(getUid()).addValueEventListener(getListener());

    }

    @Override
    public void onStop() {

        super.onStop();

        mDatabase.child(USERS.getType()).child(getUid()).removeEventListener(getListener());

    }

    /**
     * Performs the work of re-querying the cloud services for data to be displayed.  An adapter
     * is used to translate the data retrieved into the populated displayed view.
     */
    private ValueEventListener getListener() {

        if (mEventListener == null) {

            mEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final User user = dataSnapshot.getValue(User.class);

                    // Verify there is a user to work with
                    if (user != null) {

                        Map<String, Following> following = user.getFollowing();

                        // Check to see if the user is following anybody
                        if ((following != null) && (!following.isEmpty())) {

                            // Set up FirebaseRecyclerAdapter with the Query
                            Query postsQuery = getQuery(mDatabase);
                            mAdapter = new FirebaseRecyclerAdapter<Following, UserViewHolder>(Following.class, R.layout.item_user, UserViewHolder.class, postsQuery) {

                                @Override
                                protected void populateViewHolder(final UserViewHolder viewHolder, final Following model, final int position) {

                                    // Perform the binding based upon being a tablet or phone
                                    if (largeDevice) {
                                        viewHolder.bindToPost(model, UserListFragment.this);
                                    } else {
                                        viewHolder.bindToPost(model, mInteractionListener);
                                    }

                                }

                                @Override
                                public void onViewRecycled(UserViewHolder holder) {

                                    super.onViewRecycled(holder);

                                    // Clear out the Glide memory used for the images associated with this ViewHolder
                                    holder.clearImages();

                                }

                            };

                            mRecycler.setAdapter(mAdapter);

                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            };

        }

        return mEventListener;

    }

    private Query getQuery(DatabaseReference databaseReference) {

        String userId = getUid();

        Query resultQuery = databaseReference.child(USERS.getType()).child(userId).child(User.FOLLOWING);

        return resultQuery;
    }

    /**
     * This would never get called unless we have a LARGE device (aka tablet)
     */
    @Override
    public void onInteractionSelection(String firstUid, String secondUid, StorageDataType storageDataType, UserInteractionType interactionType) {

        UserDetailsFragment fragment = (UserDetailsFragment) getFragmentManager().findFragmentById(R.id.fragment_user_details);
        fragment.setUidForDetails(firstUid);
        fragment.performStart();

    }

}

