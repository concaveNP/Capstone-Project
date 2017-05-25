package com.concavenp.artistrymuse;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.concavenp.artistrymuse.model.User;
import com.concavenp.artistrymuse.services.UploadService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * TODO: this is wrong, update the comment block
 *
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * References:
 *
 * How to achieve a full-screen dialog as described in material guidelines?
 *      - http://stackoverflow.com/questions/31606871/how-to-achieve-a-full-screen-dialog-as-described-in-material-guidelines
 *
 * Dialog to pick image from gallery or from camera
 *      - http://stackoverflow.com/questions/10165302/dialog-to-pick-image-from-gallery-or-from-camera
 *
 * Taking Photos Simply
 *      - https://developer.android.com/training/camera/photobasics.html
 *
 * Android Material Design Floating Labels for EditText
 *      - http://www.androidhive.info/2015/09/android-material-design-floating-labels-for-edittext/
 *
 * Disable auto focus on edit text
 *      - http://stackoverflow.com/questions/7593887/disable-auto-focus-on-edit-text
 */
@SuppressWarnings("StatementWithEmptyBody")
public class ProfileActivity extends BaseAppCompatActivity {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ProfileActivity.class.getSimpleName();

    // The different activity result items given the user can choose an image from either the
    // camera or an existing image.  Additionally, the user has a couple of places where the resulting
    // image might be applied (i.e. the profile or header image).
    private static final int REQUEST_PROFILE_IMAGE_STORE = 0;
    private static final int REQUEST_PROFILE_IMAGE_CAPTURE = 1;
    private static final int REQUEST_HEADER_IMAGE_STORE = 2;
    private static final int REQUEST_HEADER_IMAGE_CAPTURE = 3;

    // When required, this app can ask for the user's permission to read from external storage if
    // choosing a image from a gallery is decided.  In this event the result from an activity
    // needs to specify what the result applies to.  Exactly the same purpose as the block of
    // constants above only for the needing permission first scenario.
    private static final int REQUEST_PROFILE_PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private static final int REQUEST_HEADER_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;

    // Members used in the user's Profile image (aka the "Avatar")
    private String mProfileImagePath;
    private UUID mProfileImageUid;
    private ImageView mProfileImageView;

    // Members used in the user's Header image (aka the top most image)
    private String mHeaderImagePath;
    private UUID mHeaderImageUid;
    private ImageView mHeaderImageView;

    // The value that will hold the user selected image from a picker (versus a camera taken photo)
    private Uri mSelectedImageUri;

    // The transient values used during the user's choice of what image to use
    private String mImagePath;
    private UUID mImageUid;

    // The other widgets making up part of the user's profile
    private EditText mNameEditText;
    private EditText mUsernameEditText;
    private EditText mSummaryEditText;
    private EditText mDescriptionEditText;

    // The user model.  This is the POJO that used to pass back and forth between this app and the
    // cloud service (aka Firebase).
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        setContentView(R.layout.activity_profile);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
//        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mProfileImageView = (ImageView) findViewById(R.id.profile_profile_imageView);
        mHeaderImageView = (ImageView) findViewById(R.id.profile_header_imageView);
        mNameEditText = (EditText) findViewById(R.id.name_editText);
        mUsernameEditText = (EditText) findViewById(R.id.username_editText);
        mDescriptionEditText = (EditText) findViewById(R.id.description_editText);
        mSummaryEditText = (EditText) findViewById(R.id.summary_editText);

        Button profileButton = (Button) findViewById(R.id.profile_profile_button);
        profileButton.setOnClickListener(new ImageButtonListener(ImageType.PROFILE));

        Button headerButton = (Button) findViewById(R.id.profile_header_button);
        headerButton.setOnClickListener(new ImageButtonListener(ImageType.HEADER));

        // Query for the currently saved user uid via the Saved Preferences
        mDatabase.child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Perform the JSON to Object conversion
                final User user = dataSnapshot.getValue(User.class);

                // Verify there is a user to work with
                if (user != null) {

                    // Set the user model data
                    mUser = user;

                    // Update the profile details
                    populateCircularImageView(buildFileReference(user.getUid(), user.getProfileImageUid(), StorageDataType.USERS), mProfileImageView);
                    populateImageView(buildFileReference(user.getUid(), user.getHeaderImageUid(), StorageDataType.USERS), mHeaderImageView);
                    mNameEditText.setText(user.getName());
                    mUsernameEditText.setText(user.getUsername());
                    mDescriptionEditText.setText(user.getDescription());
                    mSummaryEditText.setText(user.getSummary());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }

        });

    }

    /**
     *
     * @param type - The type is a number that is one of the following: REQUEST_PROFILE_IMAGE_STORE or REQUEST_HEADER_IMAGE_STORE
     */
    private void dispatchTakePictureIntent(int type) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = createImageFile();

            // Continue only if the File was successfully created
            if (photoFile != null) {

                Uri photoURI = FileProvider.getUriForFile(this, "com.concavenp.artistrymuse", photoFile);

                Log.d(TAG, "New camera image URI location: " + photoURI.toString() );

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, type);

            }

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch(requestCode) {

                case REQUEST_HEADER_IMAGE_CAPTURE: {

                    // Save off the values generated from the image creation (however it was done)
                    mHeaderImagePath = mImagePath;
                    mHeaderImageUid = mImageUid;

                    // Load the captured image into the ImageView widget
                    populateThumbnailImageView(mHeaderImagePath, mHeaderImageView);

                    // Add the new (at least to this App) image to the system's Media Provider
                    galleryAddPic(mHeaderImagePath);

                    break;

                }
                case REQUEST_PROFILE_IMAGE_CAPTURE: {

                    // Save off the values generated from the image creation (however it was done)
                    mProfileImagePath = mImagePath;
                    mProfileImageUid = mImageUid;

                    // Load the captured image into the ImageView widget
                    populateThumbnailImageView(mProfileImagePath, mProfileImageView);

                    // Add the new (at least to this App) image to the system's Media Provider
                    galleryAddPic(mProfileImagePath);

                    break;

                }
                case REQUEST_HEADER_IMAGE_STORE: {

                    // Use the returned URI by passing it to the content resolver in order to get
                    // access to he file chosen by the user.  At this point copy the file locally
                    // so it can be processed in the exact same fashion as the camera retrieved image.

                    // The resulting URI of the user's image pick
                    mSelectedImageUri = data.getData();

                    // We are about the retrieve files outside of this App's area.  To do so, we
                    // must have the right permission.  Check to see if we do and then process the
                    // image.  If we do not, then request permission by presenting the user a
                    // popup asking for permission.  Since, we only bring this up when the user
                    // hits the gallery I've decided to present the obvious reason why this App is
                    // requesting permission.
                    int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE );

                    if (permissionCheck == PackageManager.PERMISSION_DENIED) {

                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_HEADER_PERMISSIONS_READ_EXTERNAL_STORAGE);

                        // Continue processing in the callback associated with permissions (onRequestPermissionsResult)

                    } else {

                        // Copy the file locally and set the thumbnail
                        processExternalUri(mHeaderImageView);

                        // Save off the values generated from the image creation (however it was done)
                        mHeaderImagePath = mImagePath;
                        mHeaderImageUid = mImageUid;

                    }

                    break;

                }
                case REQUEST_PROFILE_IMAGE_STORE: {

                    // Use the returned URI by passing it to the content resolver in order to get
                    // access to he file chosen by the user.  At this point copy the file locally
                    // so it can be processed in the exact same fashion as the camera retrieved image.

                    // The resulting URI of the user's image pick
                    mSelectedImageUri = data.getData();

                    // We are about the retrieve files outside of this App's area.  To do so, we
                    // must have the right permission.  Check to see if we do and then process the
                    // image.  If we do not, then request permission by presenting the user a
                    // popup asking for permission.  Since, we only bring this up when the user
                    // hits the gallery I've decided to present the obvious reason why this App is
                    // requesting permission.
                    int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE );

                    if (permissionCheck == PackageManager.PERMISSION_DENIED) {

                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_PROFILE_PERMISSIONS_READ_EXTERNAL_STORAGE);

                        // Continue processing in the callback associated with permissions (onRequestPermissionsResult)

                    } else {

                        // Copy the file locally and set the thumbnail
                        processExternalUri(mProfileImageView);

                        // Save off the values generated from the image creation (however it was done)
                        mProfileImagePath = mImagePath;
                        mProfileImageUid = mImageUid;

                    }

                    break;

                }

            }

        }

    }

    /**
     * Helper method that will add the photo in question to the system's Media Provider
     */
    private void galleryAddPic(String path) {

// TODO: dunno if this works

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);

    }

    private void processExternalUri(ImageView view) {

        try {

            // The output location of the copied file
            File galleryFile = createImageFile();
            FileOutputStream fileOutputStream = new FileOutputStream(galleryFile);

            // The input location of the external file
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(mSelectedImageUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            FileInputStream fileInputStream = new FileInputStream(fileDescriptor);

            copyFile(fileInputStream, fileOutputStream);

            populateThumbnailImageView(mImagePath, view);

            parcelFileDescriptor.close();

            // Add the new (at least to this App) image to the system's Media Provider
            galleryAddPic(mImagePath);

        } catch (FileNotFoundException e) {

            // TODO: better error handling

            Log.d(TAG,e.getMessage());
            e.printStackTrace();

        } catch (IOException e) {

            Log.d(TAG,e.getMessage());
            e.printStackTrace();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case REQUEST_HEADER_PERMISSIONS_READ_EXTERNAL_STORAGE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Copy the file locally and set the thumbnail
                    processExternalUri(mHeaderImageView);

                    // Save off the values generated from the image creation (however it was done)
                    mHeaderImagePath = mImagePath;
                    mHeaderImageUid = mImageUid;

                } else {

                    // Permission has been denied.  Keep asking the user for permission when
                    // trying to access the external storage.

                }

                break;
            }
            case REQUEST_PROFILE_PERMISSIONS_READ_EXTERNAL_STORAGE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Copy the file locally and set the thumbnail
                    processExternalUri(mProfileImageView);

                    // Save off the values generated from the image creation (however it was done)
                    mProfileImagePath = mImagePath;
                    mProfileImageUid = mImageUid;

                } else {

                    // Permission has been denied.  Keep asking the user for permission when
                    // trying to access the external storage.

                }

                break;
            }

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

        int id = item.getItemId();

        if (id == R.id.action_save) {

            // Move the last update time
            mUser.setLastUpdatedDate(new Date().getTime());

            // Name
            String name = mNameEditText.getText().toString();
            if ((name != null) && (!name.isEmpty())) {
                mUser.setName(name);
            }

            // Username
            String username = mUsernameEditText.getText().toString();
            if ((username != null) && (!username.isEmpty())) {
                mUser.setUsername(username);
            }

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
                        .setAction(UploadService.ACTION_UPLOAD));

                // Update the user model reference to the profile image uid for database update
                mUser.setProfileImageUid(mProfileImageUid.toString());

            }
            else {
                // The user did not change the profile image - do nothing
            }

            // Write the user model data it to the database
            mDatabase.child("users").child(mUser.getUid()).setValue(mUser);

            // We are handling the button click
            return true;

        }

        return super.onOptionsItemSelected(item);

    }

    private File createImageFile() {

        // New UUID
        mImageUid = UUID.randomUUID();

        // Create an image file name
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir + "/" + mImageUid.toString() + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mImagePath = image.getAbsolutePath();

        return image;

    }

    private enum ImageType {

        HEADER,
        PROFILE

    }

    private class ImageButtonListener implements View.OnClickListener {

        private ImageType mImageType;

        ImageButtonListener(ImageType type) {

            mImageType = type;

        }

        @Override
        public void onClick(View v) {

            // Determine if this device has "camera" hardware available
            boolean cameraPresent = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
            if (cameraPresent) {

                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle(R.string.profile_image_source_title)
                        .setItems(R.array.profile_image_source_choice, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Act on the choice made by the user.  This will be to either
                                // choose to take a new photo or pick one from the gallery.
                                switch (which) {
                                    case 0: {

                                        // Take a picture
                                        switch (mImageType) {
                                            case PROFILE: {
                                                dispatchTakePictureIntent(REQUEST_PROFILE_IMAGE_CAPTURE);
                                                break;
                                            }
                                            case HEADER: {
                                                dispatchTakePictureIntent(REQUEST_HEADER_IMAGE_CAPTURE);
                                                break;
                                            }
                                        }

                                        break;

                                    }
                                    case 1: {

                                        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                                        // browser.
                                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                                        // Filter to only show results that can be "opened", such as a
                                        // file (as opposed to a list of contacts or timezones)
                                        intent.addCategory(Intent.CATEGORY_OPENABLE);

                                        // Filter to show only images, using the image MIME data type.
                                        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                                        // To search for all documents available via installed storage providers,
                                        // it would be "*/*".
                                        intent.setType("image/*");

                                        switch (mImageType) {
                                            case PROFILE: {
                                                startActivityForResult(intent, REQUEST_PROFILE_IMAGE_STORE);
                                                break;
                                            }
                                            case HEADER: {
                                                startActivityForResult(intent, REQUEST_HEADER_IMAGE_STORE);
                                                break;
                                            }
                                        }

                                        break;

                                    }
                                }

                                // Regardless of the choice, close the dialog
                                dialog.dismiss();

                            }

                        })
                        .show();

            } else {

                // There is no camera present on this device, so just have the user pick from the gallery
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                switch (mImageType) {
                    case PROFILE: {
                        startActivityForResult(pickPhoto, REQUEST_PROFILE_IMAGE_STORE);
                        break;
                    }
                    case HEADER: {
                        startActivityForResult(pickPhoto, REQUEST_HEADER_IMAGE_STORE);
                        break;
                    }

                }

            }

        }

    }

}

