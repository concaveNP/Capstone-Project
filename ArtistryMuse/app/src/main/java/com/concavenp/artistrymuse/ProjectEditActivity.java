package com.concavenp.artistrymuse;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.concavenp.artistrymuse.fragments.adapter.InspirationAdapter;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;
import com.concavenp.artistrymuse.model.Project;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;


/**
 *
 * FLOW:
 *
 * creation
 * - are we editing an existing project?
 *      - NO:
 *          - Create new UID
 *          - Set title of activity to "New Project"
 *      - YES:
 *          - Set title of activity to projects title
 *
 * - Show publish button
 * - Show +Inspiration button
 * - Show Save button
 * - Show delete inspiration button (ick)
 * - Show delete project button (ick)
 *
 */
public class ProjectEditActivity extends ImageAppCompatActivity {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ProjectEditActivity.class.getSimpleName();

    /**
     * String used when creating the activity via intent.  This key will be used to retrieve the
     * UID associated with the USER in question.
     */
    public static final String EXTRA_DATA = "uid_string_data";

    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private ImageView mProjectImageView;
    private InspirationAdapter mAdapter;
    private RecyclerView mRecycler;

    // The UID of the project in question.  NOTE: this value can be passed into the activity via
    // intent param or generated if this is a new project.
    private String mProjectUid;

    // The model data to display and update
    private Project mProjectModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_project_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Create new Inspiration



                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProjectImageView = (ImageView) findViewById(R.id.project_imageView);
        mTitleEditText = (EditText) findViewById(R.id.title_editText);
        mDescriptionEditText = (EditText) findViewById(R.id.description_editText);

        // TODO: what is the purpose of this?????
        mRecycler = (RecyclerView) findViewById(R.id.inspirations_recycler_view);
        mRecycler.setHasFixedSize(true);

        // Set up Layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(linearLayoutManager);

        // Extract the UID from the Activity parameters
        Intent intent = getIntent();
        mProjectUid = intent.getStringExtra(EXTRA_DATA);

        // If there is a UID to work with then we are dealing with editing an existing project
        // otherwise this is a new project and we will need to create a new UID for it.
        if ((mProjectUid != null) && (!mProjectUid.isEmpty())) {

            // Set the title
            setTitle(getString(R.string.edit_project_title));

            mDatabase.child("projects").child(mProjectUid).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final Project project = dataSnapshot.getValue(Project.class);

                    // Verify there is a user to work with
                    if (project != null) {

                        mProjectModel = project;

                        display(mProjectModel);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            });

        } else {

            // Set the title
            setTitle(getString(R.string.new_project_title));

            // This is a new project for the user, so we must create a new UID for it
            mProjectUid = UUID.randomUUID().toString();

            mProjectModel = new Project();

            display(mProjectModel);

        }

    }

    private void display(Project project) {

        mTitleEditText.setText(project.getName());
        mDescriptionEditText.setText(project.getDescription());
        populateImageView(buildFileReference(project.getUid(), project.getMainImageUid(), StorageDataType.PROJECTS), mProjectImageView);

        // Provide the recycler view the list of project strings to display
        mAdapter = new InspirationAdapter(project.getInspirations(), this, UserInteractionType.EDIT);
        mRecycler.setAdapter(mAdapter);

    }

    private Query getQuery() {

        Query resultQuery  = mDatabase.child("projects").child(mProjectUid).child("inspirations");

        return resultQuery;
    }

    @Override
    ImageView getSpecificImageView(int type) {
        return null;
    }

    @Override
    void setSpecificImageData(int type) {

    }
}
