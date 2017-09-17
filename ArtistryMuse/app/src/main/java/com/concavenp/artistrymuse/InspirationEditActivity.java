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

import com.concavenp.artistrymuse.model.Inspiration;
import com.concavenp.artistrymuse.model.Project;
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
    private EditText mDescriptionEditText;

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

    // The different types of the images that can be processed by the parent class
    private enum ImageType {

        INSPIRATION

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inspiration_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_dialog_close_dark);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mInspirationImageView = (ImageView) findViewById(R.id.inspiration_imageView);
        mTitleEditText = (EditText) findViewById(R.id.title_editText);
        mDescriptionEditText = (EditText) findViewById(R.id.description_editText);

        Button inspirationButton = (Button) findViewById(R.id.inspiration_image_button);
        inspirationButton.setOnClickListener(new ImageButtonListener(ImageType.INSPIRATION.ordinal()));

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
            mInspirationModel = new Inspiration();
            mInspirationModel.clear();

            // Set the members according to view of this user
            mInspirationModel.setUid(mInspirationUid);
            mInspirationModel.setProjectUid(mProjectUid);
            mInspirationModel.setCreationDate(new Date().getTime());

            display(mInspirationModel);

        }

    }

    private void display(Inspiration inspiration) {

        mTitleEditText.setText(inspiration.getName());
        mDescriptionEditText.setText(inspiration.getDescription());
        populateImageView(buildFileReference(mProjectUid, inspiration.getImageUid(), StorageDataType.PROJECTS), mInspirationImageView);

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

                // Move the last update time
                mInspirationModel.setLastUpdateDate(new Date().getTime());

                // Title
                String title = mTitleEditText.getText().toString();
                if ((title != null) && (!title.isEmpty())) {
                    mInspirationModel.setName(title);
                }

                // Description
                String description = mDescriptionEditText.getText().toString();
                if ((description != null) && (!description.isEmpty())) {
                    mInspirationModel.setDescription(description);
                }

                // Check to see if the user set a new header image
                if (mInspirationImageUid != null) {

                    final String oldMainUid = mInspirationModel.getImageUid();

                    // Check if the old profile image needs to be deleted
                    if ((oldMainUid != null) && (!oldMainUid.isEmpty())) {

                        StorageReference deleteFile = mStorageRef.child(StorageDataType.PROJECTS.getType() + getString(R.string.firebase_separator) + mProjectUid + getString(R.string.firebase_separator) + oldMainUid + getString(R.string.firebase_image_type));

                        // Delete the old image from Firebase storage
                        deleteFile.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // File deleted successfully
                                Log.d(TAG, "Deleted old image (" + oldMainUid + ") from cloud storage for the project (" + mProjectUid + ")");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                Log.e(TAG, "Error deleting old image (" + oldMainUid + ") from cloud storage for the project (" + mProjectUid + ")");
                            }
                        });

                    }
                    else {
                        // The user did not have an old image to replace - do nothing
                    }

                    // Save the new project image to the cloud storage
                    Uri file = Uri.fromFile(new File(mInspirationImagePath));

                    Log.d(TAG, "New image cloud storage location: " + file.toString());

                    // Start MyUploadService to upload the file, so that the file is uploaded even if
                    // this Activity is killed or put in the background
                    startService(new Intent(this, UploadService.class)
                            .putExtra(UploadService.EXTRA_FILE_URI, file)
                            .putExtra(UploadService.EXTRA_FILE_RENAMED_FILENAME, mInspirationImageUid.toString() + getString(R.string.firebase_image_type))
                            .putExtra(UploadService.EXTRA_UPLOAD_DATABASE, StorageDataType.PROJECTS.getType())
                            .putExtra(UploadService.EXTRA_UPLOAD_UID, mProjectUid)
                            .setAction(UploadService.ACTION_UPLOAD));

                    // Update the project model reference to the project image uid for database update
                    mInspirationModel.setImageUid(mInspirationImageUid.toString());

                }
                else {
                    // The user did not change the project image - do nothing
                }

                // Write the inspiration model data it to the database
                mDatabase.child(PROJECTS.getType()).child(mProjectUid).child(Project.INSPIRATIONS).child(mInspirationUid).setValue(mInspirationModel);

                // Update the project's last update time
                mDatabase.child(PROJECTS.getType()).child(mProjectUid).child(Project.LAST_UPDATE_DATE).setValue(new Date().getTime());

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

}

