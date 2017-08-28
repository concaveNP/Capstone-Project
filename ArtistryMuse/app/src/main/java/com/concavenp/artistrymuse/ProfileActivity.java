package com.concavenp.artistrymuse;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.concavenp.artistrymuse.model.Project;
import com.concavenp.artistrymuse.model.User;
import com.concavenp.artistrymuse.services.UploadService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Date;
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
    private EditText mUsernameEditText;
    private EditText mSummaryEditText;
    private EditText mDescriptionEditText;

    // Values used to build up the stats
    private int favoritesTotal = 0;
    private double averageRatingTotal = 0.0;
    private int viewsTotal = 0;

    // The user model.  This is the POJO that used to pass back and forth between this app and the
    // cloud service (aka Firebase).
    private User mUser;

    // The different types of the images that can be processed by the parent class
    private enum ImageType {

        HEADER,
        PROFILE

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_dialog_close_dark);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mProfileImageView = (ImageView) findViewById(R.id.profile_profile_imageView);
        mHeaderImageView = (ImageView) findViewById(R.id.profile_header_imageView);
        mNameEditText = (EditText) findViewById(R.id.name_editText);
        mUsernameEditText = (EditText) findViewById(R.id.username_editText);
        mDescriptionEditText = (EditText) findViewById(R.id.description_editText);
        mSummaryEditText = (EditText) findViewById(R.id.summary_editText);

        // The "statistical" fields are generated data that is not "saved" by the user
        final TextView favoritedTextView = (TextView) findViewById(R.id.favorited_textView);
        final TextView viewsTextView = (TextView) findViewById(R.id.views_textView);
        final TextView ratingTextView = (TextView) findViewById(R.id.rating_textView);

        Button profileButton = (Button) findViewById(R.id.profile_profile_button);
        profileButton.setOnClickListener(new ImageButtonListener(ImageType.PROFILE.ordinal()));

        Button headerButton = (Button) findViewById(R.id.profile_header_button);
        headerButton.setOnClickListener(new ImageButtonListener(ImageType.HEADER.ordinal()));

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
                    populateCircularImageView(buildStorageReference(user.getUid(), user.getProfileImageUid(), StorageDataType.USERS), mProfileImageView);
                    populateImageView(buildFileReference(user.getUid(), user.getHeaderImageUid(), StorageDataType.USERS), mHeaderImageView);
                    mNameEditText.setText(user.getName());
                    mUsernameEditText.setText(user.getUsername());
                    mDescriptionEditText.setText(user.getDescription());
                    mSummaryEditText.setText(user.getSummary());

                    // Initialize the data points before running the numbers
                    favoritesTotal = 0;
                    averageRatingTotal = 0.0;
                    viewsTotal = 0;

                    // Loop over all of the user's projects and tally up the data
                    for (String projectId : user.getProjects().values()) {

                        mDatabase.child(PROJECTS.getType()).child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                // Perform the JSON to Object conversion
                                Project project = dataSnapshot.getValue(Project.class);

                                // Verify there is a user to work with
                                if (project != null) {

                                    // Get the needed data out from the JSON
                                    favoritesTotal += project.getFavorited();
                                    averageRatingTotal = (averageRatingTotal + project.getRating()) / 2;
                                    viewsTotal += project.getViews();

                                    // Convert to strings
                                    String favoritesResult = Integer.toString(favoritesTotal);
                                    String ratingsResult = String.format("%.1f", averageRatingTotal);
                                    String viewsResult = Integer.toString(viewsTotal);

                                    // Update the views
                                    favoritedTextView.setText(favoritesResult);
                                    viewsTextView.setText(ratingsResult);
                                    ratingTextView.setText(viewsResult);

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

                // Move the last update time
                mUser.setLastUpdatedDate(new Date().getTime());

                // Name
                String name = mNameEditText.getText().toString();
                if ((name != null) && (!name.isEmpty())) {
                    mUser.setName(name);
                }

                // TODO: put in a check to verify there is something here for the "name"

                // Username
                String username = mUsernameEditText.getText().toString();
                if ((username != null) && (!username.isEmpty())) {
                    mUser.setUsername(username);
                }

                // TODO: put in a check to verify there is something here for the "username"

                // Description
                String description = mDescriptionEditText.getText().toString();
                if ((description != null) && (!description.isEmpty())) {
                    mUser.setDescription(description);
                }

                // Summary
                String summary = mSummaryEditText.getText().toString();
                if ((summary != null) && (!summary.isEmpty())) {
                    mUser.setSummary(summary);
                }

                // Check to see if the user set a new header image
                if (mHeaderImageUid != null) {

                    final String oldHeaderUid = mUser.getHeaderImageUid();

                    // Check if the old profile image needs to be deleted
                    if ((oldHeaderUid != null) && (!oldHeaderUid.isEmpty())) {

                        StorageReference deleteFile = mStorageRef.child("users/" + mUser.getUid() + "/" + oldHeaderUid + ".jpg");

                        // Delete the old header image from Firebase storage
                        deleteFile.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // TODO: better error handling
                                // File deleted successfully
                                Log.d(TAG, "Deleted old header image (" + oldHeaderUid +
                                        ") from cloud storage for the user (" + mUser.getUid() + ")");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                Log.e(TAG, "Error deleting old header image (" + oldHeaderUid +
                                        ") from cloud storage for the user (" + mUser.getUid() + ")");
                            }
                        });

                    }
                    else {
                        // The user did not have an old header image to replace - do nothing
                    }

                    // Save the new profile image to the cloud storage
                    Uri file = Uri.fromFile(new File(mHeaderImagePath));

                    Log.d(TAG, "New header image cloud storage location: " + file.toString());

                    // Start MyUploadService to upload the file, so that the file is uploaded even if
                    // this Activity is killed or put in the background
                    startService(new Intent(this, UploadService.class)
                            .putExtra(UploadService.EXTRA_FILE_URI, file)
                            .putExtra(UploadService.EXTRA_FILE_RENAMED_FILENAME, mHeaderImageUid.toString() + ".jpg")
                            .putExtra(UploadService.EXTRA_UPLOAD_DATABASE, StorageDataType.USERS.getType())
                            .putExtra(UploadService.EXTRA_UPLOAD_UID, mUser.getUid())
                            .setAction(UploadService.ACTION_UPLOAD));

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

                        StorageReference deleteFile = mStorageRef.child("users/" + mUser.getUid() + "/" + oldProfileUid + ".jpg");

                        // Delete the old profile image from Firebase storage
                        deleteFile.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // TODO: better error handling
                                // File deleted successfully
                                Log.d(TAG, "Deleted old profile image (" + oldProfileUid +
                                        ") from cloud storage for the user (" + mUser.getUid() + ")");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                Log.e(TAG, "Error deleting old profile image (" + oldProfileUid +
                                        ") from cloud storage for the user (" + mUser.getUid() + ")");
                            }
                        });

                    }
                    else {
                        // The user did not have an old profile image to replace - do nothing
                    }

                    // Save the new profile image to the cloud storage
                    Uri file = Uri.fromFile(new File(mProfileImagePath));

                    Log.d(TAG, "New profile image cloud storage location: " + file.toString());

                    // Start MyUploadService to upload the file, so that the file is uploaded even if
                    // this Activity is killed or put in the background
                    startService(new Intent(this, UploadService.class)
                            .putExtra(UploadService.EXTRA_FILE_URI, file)
                            .putExtra(UploadService.EXTRA_FILE_RENAMED_FILENAME, mProfileImageUid.toString() + ".jpg")
                            .putExtra(UploadService.EXTRA_UPLOAD_DATABASE, StorageDataType.USERS.getType())
                            .putExtra(UploadService.EXTRA_UPLOAD_UID, mUser.getUid())
                            .setAction(UploadService.ACTION_UPLOAD));

                    // Update the user model reference to the profile image uid for database update
                    mUser.setProfileImageUid(mProfileImageUid.toString());

                }
                else {
                    // The user did not change the profile image - do nothing
                }

                // Write the user model data it to the database
                mDatabase.child(USERS.getType()).child(mUser.getUid()).setValue(mUser);

                // Navigate back to the Project that this Inspiration spawned from
                finish();

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
}

