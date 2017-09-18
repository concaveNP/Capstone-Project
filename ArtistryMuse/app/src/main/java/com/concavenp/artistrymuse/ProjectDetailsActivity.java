package com.concavenp.artistrymuse;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.concavenp.artistrymuse.fragments.ProjectDetailsFragment;
import com.concavenp.artistrymuse.model.Project;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.concavenp.artistrymuse.StorageDataType.PROJECTS;

public class ProjectDetailsActivity extends BaseAppCompatActivity {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ProjectDetailsActivity.class.getSimpleName();

    // ImageView for setting the Project in question's backdrop
    private ImageView backdropImageView;

    // Listeners for DB value changes
    private ValueEventListener projectInQuestionValueEventListener;

    // The UID for the Project in question to display the details about
    private String mUidForDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_project_details);

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

        // ImageView for setting the Project backdrop
        backdropImageView = appBarLayout.findViewById(R.id.project_details_backdrop);

        // Extract the UID from the Activity parameters
        Intent intent = getIntent();
        mUidForDetails = intent.getStringExtra(EXTRA_DATA);

        // Create the new fragment and give it the user data
        ProjectDetailsFragment fragment = ProjectDetailsFragment.newInstance(mUidForDetails);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_project_details_container, fragment).commit();

    }

    @Override
    protected void onStart() {

        super.onStart();

        // Pull the Project in question info from the Database and keep listening for changes
        if ((mUidForDetails != null) && (!mUidForDetails.isEmpty())) {

            mDatabase.child(PROJECTS.getType()).child(mUidForDetails).addValueEventListener(getProjectInQuestionValueEventListener());

        }

    }

    @Override
    public void onStop() {

        super.onStop();

        // Un-subscribe to the user in question's data if there
        if ((mUidForDetails != null) && (!mUidForDetails.isEmpty())) {

            mDatabase.child(PROJECTS.getType()).child(mUidForDetails).removeEventListener(getProjectInQuestionValueEventListener());

        }

    }

    private ValueEventListener getProjectInQuestionValueEventListener() {

        if (projectInQuestionValueEventListener == null) {

            projectInQuestionValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final Project project = dataSnapshot.getValue(Project.class);

                    // Verify there is a user to work with
                    if (project != null) {

                        final String uid = project.getUid();

                        // Protection
                        if ((uid != null) && (!uid.isEmpty())) {

                            final String mainImageUid = project.getMainImageUid();

                            // Protection
                            if ((mainImageUid != null) && (!mainImageUid.isEmpty())) {

                                populateImageView(buildFileReference(uid, mainImageUid, StorageDataType.PROJECTS), backdropImageView);

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

        return projectInQuestionValueEventListener;

    }

}

