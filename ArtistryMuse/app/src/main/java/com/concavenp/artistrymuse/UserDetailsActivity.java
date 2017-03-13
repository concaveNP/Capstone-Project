package com.concavenp.artistrymuse;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.concavenp.artistrymuse.fragments.UserDetailsFragment;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.concavenp.artistrymuse.model.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserDetailsActivity extends AppCompatActivity implements
        OnDetailsInteractionListener,
        UserDetailsFragment.OnFragmentInteractionListener {

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

    protected DatabaseReference mDatabase;
    protected StorageReference mStorageRef;
    protected FirebaseUser mUser;
    protected String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Initialize the Database connection
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize the Storage connection
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Get the authenticated user
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser != null) {
            mUid = mUser.getUid();
        }

        setContentView(R.layout.activity_user_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

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

                // TODO: what to do when it is null

                // Verify there is a user to work with
                if (user != null) {

                    populateImageView(buildFileReference(user.getUid(), user.getHeaderImageUid(), StorageDataType.USERS), backdropImageView);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        // Create the new fragment and give it the user data
        UserDetailsFragment fragment = UserDetailsFragment.newInstance(uidForDetails);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_user_details_container, fragment).commit();

    }

    protected String buildFileReference(String uid, String imageUid, StorageDataType type) {

        String fileReference = null;

        // Verify there is image data to work with
        if ((imageUid != null) && (!imageUid.isEmpty())) {

            // Verify there is user data to work with
            if ((uid != null) && (!uid.isEmpty())) {

                fileReference = type.getType() + "/" + uid + "/" + imageUid + ".jpg";

            }
            else {

                Log.e(TAG, "Unexpected null project UID");

            }

        }
        else {

            Log.e(TAG, "Unexpected null image UID");

        }

        return fileReference;

    }

    protected void populateImageView(String fileReference, ImageView imageView) {

        // It is possible for the file reference string to be null, so check for it
        if (fileReference != null) {

            StorageReference storageReference = mStorageRef.child(fileReference);

            // Download directly from StorageReference using Glide
            Glide.with(imageView.getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .fitCenter()
                    .crossFade()
                    .into(imageView);

        }

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO: fill in later if needed
    }

    /**
     * The purpose of this interface implementation is to start the Details Activity of either a
     * user or a project.  The point to making the Main Activity implement is to support both the
     * phone and tablet layout of the app.  Phone layouts will just start a new activity and
     * tablet layouts will populate a neighboring fragment with the details results.
     *
     * @param uid - This will be the UID of other the User or the Project as specified in the type param
     * @param type - The type will either be a user or a project
     */
    @Override
    public void onDetailsSelection(String uid, StorageDataType type) {

        switch(type) {

            case PROJECTS: {

                // Create and start the details activity along with passing it the UID of the Project in question
                Intent intent = new Intent(this, ProjectDetailsActivity.class);
                intent.putExtra(ProjectDetailsActivity.EXTRA_DATA, uid);
                startActivity(intent);

                break;
            }
            case USERS: {

                // Create and start the details activity along with passing it the UID of the User in question
                Intent intent = new Intent(this, UserDetailsActivity.class);
                intent.putExtra(UserDetailsActivity.EXTRA_DATA, uid);
                startActivity(intent);

                break;

            }
            default: {
                // TODO: log an error and whatnot
            }

        }
       // TODO: support the phone and tablet layout, for now it is just phone

//        if (mPhoneLayout) {
//
//            // Convert the GSON object back to a JSON string in order to pass to the activity
//            Gson gson = new Gson();
//            String json = gson.toJson(item);
//
//            // Create and start the details activity along with passing it the Movie Item details information via JSON string
//            Intent intent = new Intent(this, MovieDetailsActivity.class);
//            intent.putExtra(MovieDetailsActivity.EXTRA_DATA, json);
//            startActivity(intent);
//
//        } else {
//
//            MovieDetailsFragment fragment = (MovieDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.movie_details_fragment);
//            fragment.updateMovieDetailInfo(item);
//
//        }

    }

}

