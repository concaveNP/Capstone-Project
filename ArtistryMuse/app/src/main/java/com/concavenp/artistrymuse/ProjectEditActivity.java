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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.fragments.adapter.InspirationAdapter;
import com.concavenp.artistrymuse.model.Inspiration;
import com.concavenp.artistrymuse.model.Project;
import com.concavenp.artistrymuse.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.concavenp.artistrymuse.StorageDataType.PROJECTS;
import static com.concavenp.artistrymuse.StorageDataType.USERS;

/**
 * - Show publish button (ick - implement after Udacity)
 * - Show +Inspiration button (aka FAB)
 * - Show Save button
 * - Show delete inspiration button (ick - implement after Udacity)
 * - Show delete project button (ick - implement after Udacity)
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
    private boolean mTitleEditTextDirty = false;
    private EditText mDescriptionEditText;
    private boolean mDescriptionEditTextDirty = false;
    private RecyclerView mRecycler;

    // Members used in the project's image (aka the "main" image for the project)
    private String mProjectImagePath;
    private UUID mProjectImageUid;
    private ImageView mProjectImageView;

    // The UID of the project in question.  NOTE: this value can be passed into the activity via
    // intent param or generated if this is a new project.
    private String mProjectUid;

    // Listeners for DB value changes
    private ValueEventListener projectInQuestionValueEventListener;

    // The project model.  This is the POJO that used to pass back and forth between this app and the
    // cloud service (aka Firebase).
    private Project mProjectModel;

    // This flipper allows the content of the activity to show the user's project information
    // or a spinner indicating the app is working on something (in this case it's uploading files
    // and DB changes)
    private ViewFlipper mFlipper;

    // The different types of the images that can be processed by the parent class
    private enum ImageType {

        PROJECT

    }

    private static final String TITLE_STRING = "TITLE_STRING";
    private static final String NAME_STRING = "NAME_STRING";
    private static final String DESCRIPTION_STRING = "DESCRIPTION_STRING";
    private static final String PROJECT_IMAGE_PATH_STRING = "PROJECT_IMAGE_PATH_STRING";
    private static final String PROJECT_IMAGE_UID_STRING = "PROJECT_IMAGE_UID_STRING";
    private static final String PROJECT_UID_STRING = "PROJECT_UID_STRING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_project_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        // Protection
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_dialog_close_dark);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        mProjectImageView = findViewById(R.id.project_imageView);
        mTitleEditText = findViewById(R.id.title_editText);
        mTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mTitleEditTextDirty = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing
            }
        });
        mDescriptionEditText = findViewById(R.id.description_editText);
        mDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mDescriptionEditTextDirty = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing
            }
        });

        mRecycler = findViewById(R.id.inspirations_recycler_view);
        mRecycler.setHasFixedSize(true);

        // Set up Layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(linearLayoutManager);

        Button projectButton = findViewById(R.id.project_image_button);
        projectButton.setOnClickListener(new ImageButtonListener(ImageType.PROJECT.ordinal()));

        // Save off the flipper for use in deciding which view to show
        mFlipper = findViewById(R.id.activity_project_ViewFlipper);

        if (savedInstanceState != null) {

            // Activity Title
            setTitle(savedInstanceState.getString(TITLE_STRING,""));

            // Name
            if (savedInstanceState.containsKey(NAME_STRING)) {
                mTitleEditText.setText(savedInstanceState.getString(NAME_STRING,""));
                mTitleEditTextDirty = true;
            }

            // Description
            if (savedInstanceState.containsKey(DESCRIPTION_STRING)) {
                mDescriptionEditText.setText(savedInstanceState.getString(DESCRIPTION_STRING,""));
                mDescriptionEditTextDirty = true;
            }

            // Project Image
            mProjectImagePath = savedInstanceState.getString(PROJECT_IMAGE_PATH_STRING, "");
            String projectImageUid = savedInstanceState.getString(PROJECT_IMAGE_UID_STRING, "");
            if ((mProjectImagePath.isEmpty()) || (projectImageUid.isEmpty())) {

                mProjectImagePath = null;
                mProjectImageUid = null;

            } else {

                // There is a valid path and UUID to convert from
                mProjectImageUid = UUID.fromString(projectImageUid);

            }

            // Project UID
            mProjectUid = savedInstanceState.getString(PROJECT_UID_STRING, UUID.randomUUID().toString());

            // Create and clear a new Project model object to work with
            createAndDisplayModel();

        } else {

            // Extract the UID from the Activity parameters
            Intent intent = getIntent();
            mProjectUid = intent.getStringExtra(EXTRA_DATA);

            // If there is a UID to work with then we are dealing with editing an existing project
            // otherwise this is a new project and we will need to create a new UID for it.
            if ((mProjectUid != null) && (!mProjectUid.isEmpty())) {

                // Set the title
                setTitle(getString(R.string.edit_project_title));

            } else {

                // Set the title
                setTitle(getString(R.string.new_project_title));

                // This is a new project for the user, so we must create a new UID for it
                mProjectUid = UUID.randomUUID().toString();

                // Create and clear a new Project model object to work with
                createAndDisplayModel();

            }

        }

        // Setup the FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // Notify the the listener (aka this activity) of the Create New Inspiration selection
                onInteractionSelection(mProjectUid, null, StorageDataType.INSPIRATIONS, UserInteractionType.EDIT);

            }

        });

    }

    private void createAndDisplayModel() {

        // Create and clear a new Project model object to work with
        mProjectModel = new Project();
        mProjectModel.clear();

        // Set the members according to view of this user
        mProjectModel.setUid(mProjectUid);
        mProjectModel.setOwnerUid(getUid());
        mProjectModel.setCreationDate(new Date().getTime());

        // Display the current data
        display(mProjectModel);

    }

    private void display(Project project) {

        // Only update the these values if the User is not actively trying to change them (aka dirty flagged)
        if (!mTitleEditTextDirty) {
            populateTextView(project.getName(), mTitleEditText);
        }
        if (!mDescriptionEditTextDirty) {
            populateTextView(project.getDescription(), mDescriptionEditText);
        }

        // If there is an Image path set then the User is actively changing the image
        if ((mProjectImagePath == null) || (mProjectImagePath.isEmpty())) {
            populateImageView(buildFileReference(project.getUid(), project.getMainImageUid(), StorageDataType.PROJECTS), mProjectImageView);
        } else {
            populateThumbnailImageView(mProjectImagePath, mProjectImageView);
        }

        Map<String, Inspiration> inspirations = project.getInspirations();

        // Protection
        if (inspirations != null) {

            // Provide the recycler view the list of project strings to display
            InspirationAdapter mAdapter = new InspirationAdapter(inspirations, this, UserInteractionType.EDIT);
            mRecycler.setAdapter(mAdapter);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home: {

                // Navigate back to the Project that this Inspiration spawned from - we are not going save anything
                finish();

                return true;

            }
            case R.id.action_save: {

                List<Task<?>> tasks = new ArrayList<>();

                // Move the last update time
                mProjectModel.setLastUpdateDate(new Date().getTime());

                // Title
                String title = mTitleEditText.getText().toString();
                if (!title.isEmpty()) {
                    mProjectModel.setName(title);
                }

                // Description
                String description = mDescriptionEditText.getText().toString();
                if (!description.isEmpty()) {
                    mProjectModel.setDescription(description);
                }

                // Check to see if the user set a new header image
                if (mProjectImageUid != null) {

                    final String oldMainUid = mProjectModel.getMainImageUid();

                    // Check if the old profile image needs to be deleted
                    if ((oldMainUid != null) && (!oldMainUid.isEmpty())) {

                        StorageReference deleteFile = mStorageRef.child(StorageDataType.PROJECTS.getType() + getString(R.string.firebase_separator) + mProjectUid + getString(R.string.firebase_separator) + oldMainUid + getString(R.string.firebase_image_type));

                        // Delete the old image from Firebase storage
                        Task deleteTask = deleteFile.delete();
                        tasks.add(deleteTask);

                    }
                    else {
                        // The user did not have an old image to replace - do nothing
                    }

                    // Save the new project image to the cloud storage
                    Uri file = Uri.fromFile(new File(mProjectImagePath));
                    Log.d(TAG, "New image cloud storage location: " + file.toString());

                    // Upload file to Firebase Storage
                    StorageReference photoRef = mStorageRef.child(StorageDataType.PROJECTS.getType()).child(mProjectUid).child(mProjectImageUid.toString() + getString(R.string.firebase_image_type));
                    Task uploadTask = photoRef.putFile(file);
                    tasks.add(uploadTask);

                    // Update the project model reference to the project image uid for database update
                    mProjectModel.setMainImageUid(mProjectImageUid.toString());

                }
                else {
                    // The user did not change the project image - do nothing
                }

                // Write the project model data it to the database
                Task projectTask = mDatabase.child(PROJECTS.getType()).child(mProjectUid).setValue(mProjectModel);
                tasks.add(projectTask);

                // Update the user's list of projects to add this one if needed (if it was new)
                Task projectListingTask = mDatabase.child(USERS.getType()).child(getUid()).child(User.PROJECTS).child(mProjectUid).setValue(mProjectUid);
                tasks.add(projectListingTask);

                // Switch to the progress spinner view flipper
                mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.activity_project_saving_Flipper)));

                // Close the activity when all of the tasks complete
                Tasks.whenAll(tasks)
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                // Navigate back to the Project that this Inspiration spawned from
                                finish();

                            }
                        });

                // We are handling the button click
                return true;
            }

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public ImageView getSpecificImageView(int type) {

        ImageView result = null;

        if (type == ImageType.PROJECT.ordinal()) {
            result = mProjectImageView;
        }

        return result;

    }

    @Override
    public void setSpecificImageData(int type) {

        if (type == ImageType.PROJECT.ordinal()) {
            mProjectImagePath = mImagePath;
            mProjectImageUid = mImageUid;
        }

    }

    @Override
    protected void onStart() {

        super.onStart();

        // Pull the Project in question info from the Database and keep listening for changes
        if ((mProjectUid != null) && (!mProjectUid.isEmpty())) {

            // Verify there is a title set
            if (getTitle() != null) {

                String title = getTitle().toString();

                // If this is a new Project then we can't subscribe to values for it
                if (!title.equals(getString(R.string.new_project_title))) {

                    mDatabase.child(PROJECTS.getType()).child(mProjectUid).addValueEventListener(getProjectInQuestionValueEventListener());

                }

            }

        }

    }

    @Override
    public void onStop() {

        super.onStop();

        // Un-subscribe to the Inspiration in question's data if there
        if ((mProjectUid != null) && (!mProjectUid.isEmpty())) {

            // Verify there is a title set
            if (getTitle() != null) {

                String title = getTitle().toString();

                // If this is a new Project then we can't subscribe to values for it
                if (title.equals(getString(R.string.new_inspiration_title))) {

                    mDatabase.child(PROJECTS.getType()).child(mProjectUid).removeEventListener(getProjectInQuestionValueEventListener());

                }

            }

        }

    }

    private ValueEventListener getProjectInQuestionValueEventListener() {

        if (projectInQuestionValueEventListener == null) {

            projectInQuestionValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final Project project = dataSnapshot.getValue(Project.class);

                    // Verify there is a Project to work with
                    if (project != null) {

                        mProjectModel = project;

                        display(mProjectModel);

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        // Activity title
        String activityTitle = getTitle().toString();
        if (!activityTitle.isEmpty()) {
            outState.putString(TITLE_STRING, activityTitle);
        }

        // Title
        String name = mTitleEditText.getText().toString();
        if ((!name.isEmpty()) && (mTitleEditTextDirty)) {
            outState.putString(NAME_STRING, name);
        }

        // Description
        String description = mDescriptionEditText.getText().toString();
        if ((!description.isEmpty()) && (mDescriptionEditTextDirty)) {
            outState.putString(DESCRIPTION_STRING, description);
        }

        // Project image
        if ((mProjectImagePath != null) && (!mProjectImagePath.isEmpty())) {
            outState.putString(PROJECT_IMAGE_PATH_STRING, mProjectImagePath);
            outState.putString(PROJECT_IMAGE_UID_STRING,mProjectImageUid.toString());
        }

        // Project UID
        String projectUid = mProjectUid;
        if ((projectUid != null) && (!projectUid.isEmpty())) {
            outState.putString(PROJECT_UID_STRING, projectUid);
        }

    }

}

