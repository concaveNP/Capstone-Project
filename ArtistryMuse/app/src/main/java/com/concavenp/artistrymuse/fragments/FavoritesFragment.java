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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.model.Favorite;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import static com.concavenp.artistrymuse.StorageDataType.USERS;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends BaseFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = FavoritesFragment.class.getSimpleName();

    /**
     * This flipper allows the content of the fragment to show the user either the list of their
     * favorite projects or message stating they need to favorite some projects (list is empty).
     */
    private ViewFlipper mFlipper;

    private ValueEventListener mEventListener;
    private View mainView;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment FavoritesFragment.
     */
    public static FavoritesFragment newInstance() {

        FavoritesFragment fragment = new FavoritesFragment();

        return fragment;

    }

    /**
     * Required empty public constructor
     */
    public FavoritesFragment() {

        // Do nothing

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_favorites, container, false);

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

                    // Save off the flipper for use in deciding which view to show
                    mFlipper = mainView.findViewById(R.id.fragment_favorites_ViewFlipper);

                    // Perform the JSON to Object conversion
                    final User user = dataSnapshot.getValue(User.class);

                    // Verify there is a user to work with
                    if (user != null) {

                        Map<String, Favorite> favorites = user.getFavorites();

                        // Check to see if the user has any favorites
                        if ((favorites != null) && (!favorites.isEmpty())) {

                            // Yes, the user is following someone, so flip to that view
                            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.favorites_frame)));

                        } else {

                            // View flip to the "Follow People"
                            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_favorites_nobody_TextView)));

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

}

