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

package com.concavenp.artistrymuse;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.concavenp.artistrymuse.fragments.UserDetailsFragment;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.concavenp.artistrymuse.StorageDataType.USERS;

public class UserDetailsActivity extends BaseAppCompatActivity  {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = UserDetailsActivity.class.getSimpleName();

    // ImageView for setting the User in question's backdrop
    private ImageView backdropImageView;

    // Listeners for DB value changes
    private ValueEventListener userInQuestionValueEventListener;

    // The UID for the User in question to display the details about
    private String mUidForDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        // Protection
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // Capture the AppBar for manipulating it after data is available to do so
        final CollapsingToolbarLayout appBarLayout = findViewById(R.id.toolbar_layout);

        // ImageView for setting the User backdrop
        backdropImageView = appBarLayout.findViewById(R.id.user_details_backdrop);

        // Extract the UID from the Activity parameters
        Intent intent = getIntent();
        mUidForDetails = intent.getStringExtra(EXTRA_DATA);

        // Create the new fragment and give it the user data
        UserDetailsFragment fragment = UserDetailsFragment.newInstance(mUidForDetails);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_user_details_container, fragment).commit();

    }

    @Override
    protected void onStart() {

        super.onStart();

        // Pull the User in question info from the Database and keep listening for changes
        if ((mUidForDetails != null) && (!mUidForDetails.isEmpty())) {

            mDatabase.child(USERS.getType()).child(mUidForDetails).addValueEventListener(getUserInQuestionValueEventListener());

        }

    }

    @Override
    public void onStop() {

        super.onStop();

        // Un-subscribe to the user in question's data if there
        if ((mUidForDetails != null) && (!mUidForDetails.isEmpty())) {

            mDatabase.child(USERS.getType()).child(mUidForDetails).removeEventListener(getUserInQuestionValueEventListener());

        }

    }

    private ValueEventListener getUserInQuestionValueEventListener() {

        if (userInQuestionValueEventListener == null) {

            userInQuestionValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final User user = dataSnapshot.getValue(User.class);

                    // Verify there is a user to work with
                    if (user != null) {

                        final String uid = user.getUid();

                        // Protection
                        if ((uid != null) && (!uid.isEmpty())) {

                            final String headerImageUid = user.getHeaderImageUid();

                            // Protection
                            if ((headerImageUid != null) && (!headerImageUid.isEmpty())) {

                                populateImageView(buildFileReference(uid, headerImageUid, StorageDataType.USERS), backdropImageView);

                            }

                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            };

        }

        return userInQuestionValueEventListener;

    }

}

