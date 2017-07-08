package com.concavenp.artistrymuse;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Capture the AppBar for manipulating it after data is available to do so
        final CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        // ImageView for setting the User backdrop
        backdropImageView = (ImageView) appBarLayout.findViewById(R.id.user_details_backdrop);

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

                        populateImageView(buildFileReference(user.getUid(), user.getHeaderImageUid(), StorageDataType.USERS), backdropImageView);

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

