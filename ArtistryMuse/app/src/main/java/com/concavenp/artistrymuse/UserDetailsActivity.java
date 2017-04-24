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

public class UserDetailsActivity extends BaseAppCompatActivity  {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = UserDetailsActivity.class.getSimpleName();

    /**
     * String used when creating the activity via intent.  This key will be used to retrieve the
     * UID associated with the USER in question.
     */
    public static final String EXTRA_DATA = "uid_string_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

// TODO: make final decision about not needing a FAB
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
//            }
//        });

        // Capture the AppBar for manipulating it after data is available to do so
        final CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        // ImageView for setting the User backdrop
        final ImageView backdropImageView = (ImageView) appBarLayout.findViewById(R.id.user_details_backdrop);

        // Extract the UID from the Activity parameters
        Intent intent = getIntent();
        final String uidForDetails = intent.getStringExtra(EXTRA_DATA);

        mDatabase.child("users").child(uidForDetails).addListenerForSingleValueEvent(new ValueEventListener() {

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

        });

        // Create the new fragment and give it the user data
        UserDetailsFragment fragment = UserDetailsFragment.newInstance(uidForDetails);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_user_details_container, fragment).commit();

    }

    // TODO: support the phone and tablet layout, for now it is just phone

}

