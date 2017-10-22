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

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

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
 * TODO: this is wrong, update the comment block
 *
 * References:
 *
 * How to achieve a full-screen dialog as described in material guidelines?
 *      - http://stackoverflow.com/questions/31606871/how-to-achieve-a-full-screen-dialog-as-described-in-material-guidelines
 *
 * Android Material Design Floating Labels for EditText
 *      - http://www.androidhive.info/2015/09/android-material-design-floating-labels-for-edittext/
 *
 * Material “close” button in Toolbar instead of Back
 *      - https://stackoverflow.com/questions/27125340/material-close-button-in-toolbar-instead-of-back
 *
 */
@SuppressWarnings("StatementWithEmptyBody")
public class ProfileActivity extends ImageAppCompatActivity {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ProfileActivity.class.getSimpleName();

    // Members used in the user's Profile image (aka the "Avatar")
    private String mProfileImagePath;
    private UUID mProfileImageUid;
    private ImageView mProfileImageView;

    // Members used in the user's Header image (aka the top most image)
    private String mHeaderImagePath;
    private UUID mHeaderImageUid;
    private ImageView mHeaderImageView;

    // The other widgets making up part of the user's profile
    private EditText mNameEditText;
    private boolean mNameEditTextDirty = false;
    private EditText mUsernameEditText;
    private boolean mUsernameEditTextDirty = false;
    private EditText mSummaryEditText;
    private boolean mSummaryEditTextDirty = false;
    private EditText mDescriptionEditText;
    private boolean mDescriptionEditTextDirty = false;

    private TextView favoritedTextView;
    private TextView viewsTextView;
    private TextView ratingTextView;

    // Values used to build up the stats
    private int favoritesTotal = 0;
    private double averageRatingTotal = 0.0;
    private int viewsTotal = 0;

    // The user model.  This is the POJO that used to pass back and forth between this app and the
    // cloud service (aka Firebase).
    private User mUser;

    // This flipper allows the content of the activity to show the user's profile information
    // or a spinner indicating the app is working on something (in this case it's uploading files
    // and DB changes)
    private ViewFlipper mFlipper;

    // The different types of the images that can be processed by the parent class
    private enum ImageType {

        HEADER,
        PROFILE

    }

    private static final String PROFILE_IMAGE_PATH_STRING = "PROFILE_IMAGE_PATH_STRING";
    private static final String PROFILE_IMAGE_UID_STRING = "PROFILE_IMAGE_UID_STRING";
    private static final String HEADER_IMAGE_PATH_STRING = "HEADER_IMAGE_PATH_STRING";
    private static final String HEADER_IMAGE_UID_STRING = "HEADER_IMAGE_UID_STRING";
    private static final String NAME_STRING = "NAME_STRING";
    private static final String USERNAME_STRING = "USERNAME_STRING ";
    private static final String DESCRIPTION_STRING = "DESCRIPTION_STRING";
    private static final String SUMMARY_STRING = "SUMMARY_STRING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        // Protection
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_dialog_close_dark);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        mProfileImageView = findViewById(R.id.profile_profile_imageView);
        mHeaderImageView = findViewById(R.id.profile_header_imageView);
        mNameEditText = findViewById(R.id.name_editText);
        mUsernameEditText = findViewById(R.id.username_editText);
        mDescriptionEditText = findViewById(R.id.description_editText);
        mSummaryEditText = findViewById(R.id.summary_editText);

        // The "statistical" fields are generated data that is not "saved" by the user
        favoritedTextView = findViewById(R.id.favorited_textView);
        viewsTextView = findViewById(R.id.views_textView);
        ratingTextView = findViewById(R.id.rating_textView);

        Button profileButton = findViewById(R.id.profile_profile_button);
        profileButton.setOnClickListener(new ImageButtonListener(ImageType.PROFILE.ordinal()));

        Button headerButton = findViewById(R.id.profile_header_button);
        headerButton.setOnClickListener(new ImageButtonListener(ImageType.HEADER.ordinal()));

        // Save off the flipper for use in deciding which view to show
        mFlipper = findViewById(R.id.activity_profile_ViewFlipper);

        if (savedInstanceState != null) {

            // Project Image
            mProfileImagePath = savedInstanceState.getString(PROFILE_IMAGE_PATH_STRING, "");
            String profileImageUid = savedInstanceState.getString(PROFILE_IMAGE_UID_STRING, "");
            if ((mProfileImagePath.isEmpty()) || (profileImageUid.isEmpty())) {

                mProfileImagePath = null;
                mProfileImageUid = null;

            } else {

                // There is a valid path and UUID to convert from
                mProfileImageUid = UUID.fromString(profileImageUid);

                // Populate from the User's current unsaved change
                populateThumbnailImageView(mProfileImagePath , mProfileImageView);

            }

            // Header Image
            mHeaderImagePath = savedInstanceState.getString(HEADER_IMAGE_PATH_STRING, "");
            String headerImageUid = savedInstanceState.getString(HEADER_IMAGE_UID_STRING, "");
            if ((mHeaderImagePath.isEmpty()) || (headerImageUid.isEmpty())) {

                mHeaderImagePath = null;
                mHeaderImageUid = null;

            } else {

                // There is a valid path and UUID to convert from
                mHeaderImageUid = UUID.fromString(headerImageUid);

            }

            // Name
            if (savedInstanceState.containsKey(NAME_STRING)) {
                mNameEditText.setText(savedInstanceState.getString(NAME_STRING,""));
                mNameEditTextDirty = true;
            }

            // Username
            if (savedInstanceState.containsKey(USERNAME_STRING)) {
                mUsernameEditText.setText(savedInstanceState.getString(USERNAME_STRING,""));
                mUsernameEditTextDirty = true;
            }

            // Description
            if (savedInstanceState.containsKey(DESCRIPTION_STRING)) {
                mDescriptionEditText.setText(savedInstanceState.getString(DESCRIPTION_STRING,""));
                mDescriptionEditTextDirty = true;
            }

            // Summary
            if (savedInstanceState.containsKey(DESCRIPTION_STRING)) {
                mSummaryEditText.setText(savedInstanceState.getString(SUMMARY_STRING,""));
                mSummaryEditTextDirty = true;
            }

        }

        // Update the display by using DB value if required
        display();

    }

    private void display() {

        // Query for the currently saved user uid via the Saved Preferences
        mDatabase.child(USERS.getType()).child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Perform the JSON to Object conversion
                final User user = dataSnapshot.getValue(User.class);

                // Verify there is a user to work with
                if (user != null) {

                    // Set the user model data
                    mUser = user;

                    // Update the profile details

                    Log.d(TAG, "PRE solution");
                    // If there is an Image path set then the User is actively changing the image
                    if ((mProfileImagePath == null) || (mProfileImagePath.isEmpty())) {
                        Log.d(TAG, "DB solution PROFILE");
                        populateCircularImageView(buildStorageReference(user.getUid(), user.getProfileImageUid(), StorageDataType.USERS), mProfileImageView);
                    } else {
                        Log.d(TAG, "USER solution PROFILE");
                        populateCircularImageView(mProfileImagePath, mProfileImageView);
                    }
                    if ((mHeaderImagePath == null) || (mHeaderImagePath.isEmpty())) {
                        Log.d(TAG, "DB solution HEADER");
                        populateImageView(buildFileReference(user.getUid(), user.getHeaderImageUid(), StorageDataType.USERS), mHeaderImageView);
                    } else {
                        Log.d(TAG, "USER solution HEADER");
                        populateImageView(mHeaderImagePath, mHeaderImageView);
                    }
                    if (!mNameEditTextDirty) {
                        populateTextView(user.getName(), mNameEditText);
                    }
                    if (!mUsernameEditTextDirty) {
                        populateTextView(user.getUsername(), mUsernameEditText);
                    }
                    if (!mDescriptionEditTextDirty) {
                        populateTextView(user.getDescription(), mDescriptionEditText);
                    }
                    if (!mSummaryEditTextDirty) {
                        populateTextView(user.getSummary(), mSummaryEditText);
                    }

                    // Initialize the data points before running the numbers
                    favoritesTotal = 0;
                    averageRatingTotal = 0.0;
                    viewsTotal = 0;

                    Map<String, String> projects = user.getProjects();

                    if (projects != null) {

                        // Loop over all of the user's projects and tally up the data
                        for (String projectId : projects.values()) {

                            mDatabase.child(PROJECTS.getType()).child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    // Perform the JSON to Object conversion
                                    Project project = dataSnapshot.getValue(Project.class);

                                    // Verify there is a user to work with
                                    if (project != null) {

                                        try {

                                            // Get the needed data out from the JSON
                                            favoritesTotal += project.getFavorited();
                                            averageRatingTotal = (averageRatingTotal + project.getRating()) / 2;
                                            viewsTotal += project.getViews();

                                            // Convert to strings
                                            String favoritesResult = Integer.toString(favoritesTotal);
                                            String ratingsResult = String.format(getString(R.string.number_format), averageRatingTotal);
                                            String viewsResult = Integer.toString(viewsTotal);

                                            // Update the views
                                            favoritedTextView.setText(favoritesResult);
                                            viewsTextView.setText(viewsResult);
                                            ratingTextView.setText(ratingsResult);

                                        } catch(Exception ex) {

                                            Log.e(TAG, "Unable to update view totals due to problems with the data");

                                        }

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Do nothing
                                }

                            });

                        }

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }

        });

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
                mUser.setLastUpdatedDate(new Date().getTime());

                // Name
                String name = mNameEditText.getText().toString();
                if (!name.isEmpty()) {
                    mUser.setName(name);
                }

                // Username
                String username = mUsernameEditText.getText().toString();
                if (!username.isEmpty()) {
                    mUser.setUsername(username);
                }

                // Description
                String description = mDescriptionEditText.getText().toString();
                if (!description.isEmpty()) {
                    mUser.setDescription(description);
                }

                // Summary
                String summary = mSummaryEditText.getText().toString();
                if (!summary.isEmpty()) {
                    mUser.setSummary(summary);
                }

                // Check to see if the user set a new header image
                if (mHeaderImageUid != null) {

                    final String oldHeaderUid = mUser.getHeaderImageUid();

                    // Check if the old profile image needs to be deleted
                    if ((oldHeaderUid != null) && (!oldHeaderUid.isEmpty())) {

                        StorageReference deleteFile = mStorageRef.child(StorageDataType.USERS.getType() + getString(R.string.firebase_separator) + mUser.getUid() + getString(R.string.firebase_separator) + oldHeaderUid + getString(R.string.firebase_image_type));

                        // Delete the old header image from Firebase storage
                        Task deleteTask = deleteFile.delete();
                        tasks.add(deleteTask);

                    }
                    else {
                        // The user did not have an old header image to replace - do nothing
                    }

                    // Save the new profile image to the cloud storage
                    Uri file = Uri.fromFile(new File(mHeaderImagePath));
                    Log.d(TAG, "New header image cloud storage location: " + file.toString());

                    // Upload file to Firebase Storage
                    StorageReference photoRef = mStorageRef.child(StorageDataType.USERS.getType()).child(mUser.getUid()).child(mHeaderImageUid.toString() + getString(R.string.firebase_image_type));
                    Task uploadTask = photoRef.putFile(file);
                    tasks.add(uploadTask);

                    // Update the user model reference to the header image uid for database update
                    mUser.setHeaderImageUid(mHeaderImageUid.toString());

                }
                else {
                    // The user did not change the header image - do nothing
                }

                // Check to see if the user set a new profile image
                if (mProfileImageUid != null) {

                    final String oldProfileUid = mUser.getProfileImageUid();

                    // Check if the old profile image needs to be deleted
                    if ((oldProfileUid != null) && (!oldProfileUid.isEmpty())) {

                        StorageReference deleteFile = mStorageRef.child(StorageDataType.USERS.getType() + getString(R.string.firebase_separator) + mUser.getUid() + getString(R.string.firebase_separator) + oldProfileUid + getString(R.string.firebase_image_type));

                        // Delete the old profile image from Firebase storage
                        Task deleteTask = deleteFile.delete();
                        tasks.add(deleteTask);

                    }
                    else {
                        // The user did not have an old profile image to replace - do nothing
                    }

                    // Save the new profile image to the cloud storage
                    Uri file = Uri.fromFile(new File(mProfileImagePath));
                    Log.d(TAG, "New profile image cloud storage location: " + file.toString());

                    // Upload file to Firebase Storage
                    StorageReference photoRef = mStorageRef.child(StorageDataType.USERS.getType()).child(mUser.getUid()).child(mProfileImageUid.toString() + getString(R.string.firebase_image_type));
                    Task uploadTask = photoRef.putFile(file);
                    tasks.add(uploadTask);

                    // Update the user model reference to the profile image uid for database update
                    mUser.setProfileImageUid(mProfileImageUid.toString());

                }
                else {
                    // The user did not change the profile image - do nothing
                }

                // Write the user model data it to the database
                Task profileTask = mDatabase.child(USERS.getType()).child(mUser.getUid()).setValue(mUser);
                tasks.add(profileTask);

                // Switch to the progress spinner view flipper
                mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.activity_profile_saving_Flipper)));

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

        if (type == ImageType.HEADER.ordinal()) {
            result = mHeaderImageView;
        }
        else if (type == ImageType.PROFILE.ordinal()) {
            result = mProfileImageView;
        }

        return result;

    }

    @Override
    public void setSpecificImageData(int type) {

        if (type == ImageType.HEADER.ordinal()) {
            mHeaderImagePath = mImagePath;
            mHeaderImageUid = mImageUid;
        }
        else if (type == ImageType.PROFILE.ordinal()) {
            mProfileImagePath = mImagePath;
            mProfileImageUid = mImageUid;
        }

    }

    @Override
    protected ImageShape getRectangleOrCircle(int type) {

        ImageShape result = ImageShape.IMAGE_SHAPE_RECTANGLE;

        if (type == ImageType.HEADER.ordinal()) {
            result = ImageShape.IMAGE_SHAPE_RECTANGLE;
        }
        else if (type == ImageType.PROFILE.ordinal()) {
            result = ImageShape.IMAGE_SHAPE_CIRCLE;
        }

        return result;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Profile image
        if ((mProfileImagePath != null) && (!mProfileImagePath.isEmpty())) {
            outState.putString(PROFILE_IMAGE_PATH_STRING, mProfileImagePath);
            outState.putString(PROFILE_IMAGE_UID_STRING, mProfileImageUid.toString());
        }

        // Header image
        if ((mHeaderImagePath != null) && (!mHeaderImagePath.isEmpty())) {
            outState.putString(HEADER_IMAGE_PATH_STRING, mHeaderImagePath);
            outState.putString(HEADER_IMAGE_UID_STRING, mHeaderImageUid.toString());
        }

        // Name
        String name = mNameEditText.getText().toString();
        if (!name.isEmpty()) {
            outState.putString(NAME_STRING,name);
        }

        // Username
        String username = mUsernameEditText.getText().toString();
        if (!username.isEmpty()) {
            outState.putString(USERNAME_STRING,username);
        }

        // Description
        String description = mDescriptionEditText.getText().toString();
        if (!description.isEmpty()) {
            outState.putString(DESCRIPTION_STRING,description);
        }

        // Summary
        String summary = mSummaryEditText.getText().toString();
        if (!summary.isEmpty()) {
            outState.putString(SUMMARY_STRING,summary);
        }

    }

}

