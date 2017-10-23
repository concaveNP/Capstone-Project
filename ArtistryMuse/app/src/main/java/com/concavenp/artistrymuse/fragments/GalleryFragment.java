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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.UserInteractionType;
import com.concavenp.artistrymuse.fragments.viewholder.GalleryViewHolder;
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
 * Use the {@link GalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryFragment extends BaseFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = GalleryFragment.class.getSimpleName();

    private FirebaseRecyclerAdapter<String, GalleryViewHolder> mAdapter;
    private RecyclerView mRecycler;

    /**
     * This flipper allows the content of the fragment to show the user either the list of the
     * user's projects or message stating they need to create some projects (list is empty).
     */
    private ViewFlipper mFlipper;

    private ValueEventListener mEventListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment GalleryFragment.
     */
    public static GalleryFragment newInstance() {

        GalleryFragment fragment = new GalleryFragment();

        return fragment;

    }

    /**
     * Required empty public constructor
     */
    public GalleryFragment() {

        // Do nothing

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Save off the flipper for use in deciding which view to show
        mFlipper = mainView.findViewById(R.id.fragment_gallery_ViewFlipper);

        mRecycler = mainView.findViewById(R.id.gallery_recycler_view);
        mRecycler.setHasFixedSize(true);

        // Set up Layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(linearLayoutManager);

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

                        Map<String, String> projects = user.getProjects();

                        // Check to see if the user is following anybody
                        if ((projects != null) && (!projects.isEmpty())) {

                            // Yes, the user has projects, so flip to that view
                            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.gallery_recycler_view)));

                            // Set up FirebaseRecyclerAdapter with the Query
                            Query postsQuery = getQuery(mDatabase);
                            mAdapter = new FirebaseRecyclerAdapter<String, GalleryViewHolder>(String.class, R.layout.item_gallery, GalleryViewHolder.class, postsQuery) {

                                @Override
                                protected void populateViewHolder(final GalleryViewHolder viewHolder, final String uid, final int position) {

                                    viewHolder.setUserInteractionType(UserInteractionType.EDIT);
                                    viewHolder.bindToPost(uid, mInteractionListener);

                                }

                                @Override
                                public void onViewRecycled(GalleryViewHolder holder) {

                                    super.onViewRecycled(holder);

                                    // Clear out the Glide memory used for the images associated with this ViewHolder
                                    holder.clearImages();

                                }

                            };

                            mRecycler.setAdapter(mAdapter);

                        }
                        else {

                            // View flip to the Gallery of projects
                            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_gallery_nobody_TextView)));
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

        Query resultQuery = databaseReference.child(USERS.getType()).child(userId).child(User.PROJECTS);

        return resultQuery;
    }

}

