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
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.model.Inspiration;
import com.concavenp.artistrymuse.model.Project;
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
import java.util.UUID;

import static com.concavenp.artistrymuse.StorageDataType.PROJECTS;

public class InspirationEditActivity extends ImageAppCompatActivity {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = InspirationEditActivity.class.getSimpleName();

    /**
     * String used when creating the activity via intent.  This key will be used to retrieve the
     * UID associated with the PROJECT in question.
     */
    public static final String EXTRA_DATA_PROJECT = "uid_string_data";

    /**
     * String used when creating the activity via intent.  This key will be used to retrieve the
     * UID associated with the PROJECT in question.
     */
    public static final String EXTRA_DATA_INSPIRATION = "uid_inspiration_string_data";

    private EditText mTitleEditText;
    private boolean mTitleEditTextDirty = false;
    private EditText mDescriptionEditText;
    private boolean mDescriptionEditTextDirty = false;

    // Members used in the inspiration's image (aka the "main" image for the project)
    private String mInspirationImagePath;
    private UUID mInspirationImageUid;
    private ImageView mInspirationImageView;

    // The UID of the project in question.  NOTE: this value can be passed into the activity via
    // intent param or generated if this is a new project.
    private String mProjectUid;

    // The UID of the inspiration in question.  This value can either be passed to this activity
    // via intent parameter or when param is null it is assumed that the user is creating a new
    // inspiration and thus a new UID will be generated for it.
    private String mInspirationUid;

    // Listeners for DB value changes
    private ValueEventListener inspirationInQuestionValueEventListener;

    // The inspiration model.  This is the POJO that used to pass back and forth between this app and the
    // cloud service (aka Firebase).
    private Inspiration mInspirationModel;

    // This flipper allows the content of the activity to show the user either the list of projects
    // or a spinner indicating the app is working on something (in this case it's uploading files
    // and DB changes)
    private ViewFlipper mFlipper;

    // The different types of the images that can be processed by the parent class
    private enum ImageType {

        INSPIRATION

    }

    private static final String TITLE_STRING = "TITLE_STRING";
    private static final String NAME_STRING = "NAME_STRING";
    private static final String DESCRIPTION_STRING = "DESCRIPTION_STRING";
    private static final String INSPIRATION_IMAGE_PATH_STRING = "INSPIRATION_IMAGE_PATH_STRING";
    private static final String INSPIRATION_IMAGE_UID_STRING = "INSPIRATION_IMAGE_UID_STRING";
    private static final String PROJECT_UID_STRING = "PROJECT_UID_STRING";
    private static final String INSPIRATION_UID_STRING = "INSPIRATION_UID_STRING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inspiration_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        // Protection
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_dialog_close_dark);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        mInspirationImageView = findViewById(R.id.inspiration_imageView);
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
                mDescriptionEditTextDirty = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing
            }
        });

        Button inspirationButton = findViewById(R.id.inspiration_image_button);
        inspirationButton.setOnClickListener(new ImageButtonListener(ImageType.INSPIRATION.ordinal()));

        // Save off the flipper for use in deciding which view to show
        mFlipper = findViewById(R.id.activity_inspiration_ViewFlipper);

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

            // Inspiration Image
            mInspirationImagePath = savedInstanceState.getString(INSPIRATION_IMAGE_PATH_STRING, "");
            String inspirationImageUid = savedInstanceState.getString(INSPIRATION_IMAGE_UID_STRING, "");
            if ((mInspirationImagePath.isEmpty()) || (inspirationImageUid.isEmpty())) {

                mInspirationImagePath = null;
                mInspirationImageUid = null;

            } else {

                // There is a valid path and UUID to convert from
                mInspirationImageUid = UUID.fromString(inspirationImageUid);

            }

            // Project UID
            mProjectUid = savedInstanceState.getString(PROJECT_UID_STRING, UUID.randomUUID().toString());

            // Inspiration UID
            mInspirationUid = savedInstanceState.getString(INSPIRATION_UID_STRING, UUID.randomUUID().toString());

            // Create and clear a new Project model object to work with
            createAndDisplayModel();

        } else {

            // Extract the UID(s) from the Activity parameters
            Intent intent = getIntent();
            mProjectUid = intent.getStringExtra(EXTRA_DATA_PROJECT);
            mInspirationUid = intent.getStringExtra(EXTRA_DATA_INSPIRATION);

            // If there is a UID to work with then we are dealing with editing an existing project
            // otherwise this is a new project and we will need to create a new UID for it.
            if ((mInspirationUid != null) && (!mInspirationUid.isEmpty())) {

                // Set the title
                setTitle(getString(R.string.edit_inspiration_title));

            } else {

                // Set the title
                setTitle(getString(R.string.new_inspiration_title));

                // This is a new inspiration for the project, so we must create a new UID for it
                mInspirationUid = UUID.randomUUID().toString();

                // Create and clear a new Inspiration model object to work with
                createAndDisplayModel();

            }

        }

    }

    private void createAndDisplayModel() {

        // Create and clear a new Inspiration model object to work with
        mInspirationModel = new Inspiration();
        mInspirationModel.clear();

        // Set the members according to view of this user
        mInspirationModel.setUid(mInspirationUid);
        mInspirationModel.setProjectUid(mProjectUid);
        mInspirationModel.setCreationDate(new Date().getTime());

        display(mInspirationModel);

    }

    private void display(Inspiration inspiration) {

        // Only update the these values if the User is not actively trying to change them (aka dirty flagged)
        if (!mTitleEditTextDirty) {
            populateTextView(inspiration.getName(), mTitleEditText);
        }
        if (!mDescriptionEditTextDirty) {
            populateTextView(inspiration.getDescription(), mDescriptionEditText);
        }

        // If there is an Image path set then the User is actively changing the image
        if ((mInspirationImagePath == null) || (mInspirationImagePath.isEmpty())) {
            populateImageView(buildFileReference(mProjectUid, inspiration.getImageUid(), StorageDataType.PROJECTS), mInspirationImageView);
        } else {
            populateThumbnailImageView(mInspirationImagePath, mInspirationImageView);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inspiration, menu);

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
                mInspirationModel.setLastUpdateDate(new Date().getTime());

                // Title
                String title = mTitleEditText.getText().toString();
                if (!title.isEmpty()) {
                    mInspirationModel.setName(title);
                }

                // Description
                String description = mDescriptionEditText.getText().toString();
                if (!description.isEmpty()) {
                    mInspirationModel.setDescription(description);
                }

                // Check to see if the user set a new header image
                if (mInspirationImageUid != null) {

                    final String oldMainUid = mInspirationModel.getImageUid();

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
                    Uri file = Uri.fromFile(new File(mInspirationImagePath));
                    Log.d(TAG, "New image cloud storage location: " + file.toString());

                    // Upload file to Firebase Storage
                    StorageReference photoRef = mStorageRef.child(StorageDataType.PROJECTS.getType()).child(mProjectUid).child(mInspirationImageUid.toString() + getString(R.string.firebase_image_type));
                    Task uploadTask = photoRef.putFile(file);
                    tasks.add(uploadTask);

                    // Update the project model reference to the project image uid for database update
                    mInspirationModel.setImageUid(mInspirationImageUid.toString());

                }
                else {
                    // The user did not change the project image - do nothing
                }

                // Write the inspiration model data it to the database
                Task inspirationTask = mDatabase.child(PROJECTS.getType()).child(mProjectUid).child(Project.INSPIRATIONS).child(mInspirationUid).setValue(mInspirationModel);
                tasks.add(inspirationTask);

                // Update the project's last update time
                Task dateTask = mDatabase.child(PROJECTS.getType()).child(mProjectUid).child(Project.LAST_UPDATE_DATE).setValue(new Date().getTime());
                tasks.add(dateTask);

                // Switch to the progress spinner view flipper
                mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.activity_inspiration_saving_Flipper)));

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

        if (type == ImageType.INSPIRATION.ordinal()) {
            result = mInspirationImageView;
        }

        return result;

    }

    @Override
    public void setSpecificImageData(int type) {

        if (type == ImageType.INSPIRATION.ordinal()) {
            mInspirationImagePath = mImagePath;
            mInspirationImageUid = mImageUid;
        }

    }

    @Override
    protected void onStart() {

        super.onStart();

        // Pull the Inspiration in question info from the Database and keep listening for changes
        if ((mProjectUid != null) && (!mProjectUid.isEmpty())) {

            if ((mInspirationUid != null) && (!mInspirationUid.isEmpty())) {

                // Verify there is a title set
                if (getTitle() != null) {

                    String title = getTitle().toString();

                    // If this is a new Inspiration then we can't subscribe to values for it
                    if (!title.equals(getString(R.string.new_inspiration_title))) {

                        mDatabase.child(PROJECTS.getType()).child(mProjectUid).child(Project.INSPIRATIONS).child(mInspirationUid).addValueEventListener(getInspirationInQuestionValueEventListener());

                    }

                }

            }

        }

    }

    @Override
    public void onStop() {

        super.onStop();

        // Un-subscribe to the Inspiration in question's data if there
        if ((mProjectUid != null) && (!mProjectUid.isEmpty())) {

            if ((mInspirationUid != null) && (!mInspirationUid.isEmpty())) {

                // Verify there is a title set
                if (getTitle() != null) {

                    String title = getTitle().toString();

                    // If this is a new Inspiration then we can't subscribe to values for it
                    if (title.equals(getString(R.string.new_inspiration_title))) {

                        mDatabase.child(PROJECTS.getType()).child(mProjectUid).child(Project.INSPIRATIONS).child(mInspirationUid).removeEventListener(getInspirationInQuestionValueEventListener());

                    }

                }

            }

        }

    }

    private ValueEventListener getInspirationInQuestionValueEventListener() {

        if (inspirationInQuestionValueEventListener == null) {

            inspirationInQuestionValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final Inspiration inspiration = dataSnapshot.getValue(Inspiration.class);

                    // Verify there is a user to work with
                    if (inspiration != null) {

                        mInspirationModel = inspiration;

                        display(mInspirationModel);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            };

        }

        return inspirationInQuestionValueEventListener;

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

        // Inspiration image
        if ((mInspirationImagePath != null) && (!mInspirationImagePath.isEmpty())) {
            outState.putString(INSPIRATION_IMAGE_PATH_STRING, mInspirationImagePath);
            outState.putString(INSPIRATION_IMAGE_UID_STRING, mInspirationImageUid.toString());
        }

        // Project UID
        String projectUid = mProjectUid;
        if ((projectUid != null) && (!projectUid.isEmpty())) {
            outState.putString(PROJECT_UID_STRING, projectUid);
        }

        // Inspiration UID
        String inspirationUid = mInspirationUid;
        if ((inspirationUid != null) && (!inspirationUid.isEmpty())) {
            outState.putString(INSPIRATION_UID_STRING, inspirationUid);
        }

    }

}

