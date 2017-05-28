package com.concavenp.artistrymuse.fragments.dialog;

import android.app.Dialog;
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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
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

import static android.app.Activity.RESULT_OK;

/**
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
public class ProfileDialogFragment extends BaseDialogFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ProfileDialogFragment.class.getSimpleName();

    private static final int REQUEST_IMAGE_STORE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    // TODO: Rename parameter arguments, choose names that match the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mProfileImagePath;
    private UUID mProfileImageUid;
    private ImageView mProfileImageView;
    private Uri mSelectedImageUri;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    // The user model
    private User mUser;

    public ProfileDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileDialogFragment newInstance(String param1, String param2) {
        ProfileDialogFragment fragment = new ProfileDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_profile_dialog, container, false);

        Toolbar toolbar = (Toolbar) mainView.findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.action_profile));

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        setHasOptionsMenu(true);

        mProfileImageView = (ImageView) mainView.findViewById(R.id.profile_profile_imageView);

        Button profileButton = (Button) mainView.findViewById(R.id.profile_profile_button);
        profileButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // Determine if this device has "camera" hardware available
                boolean cameraPresent = getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
                if (cameraPresent) {

                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.profile_image_source_title)
                            .setItems(R.array.profile_image_source_choice, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // Act on the choice made by the user
                                    switch (which) {
                                        case 0: {

                                            // Take a picture
                                            dispatchTakePictureIntent();

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

                                            startActivityForResult(intent, REQUEST_IMAGE_STORE);

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
                    startActivityForResult(pickPhoto, REQUEST_IMAGE_STORE);

                }


            }

        });

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
                    populateImageView(buildFileReference(user.getUid(), user.getProfileImageUid(), StorageDataType.USERS), mProfileImageView);








// TODO: fill out all other profile details
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }

        });

        return mainView;

    }

//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        getDialog().getWindow().setLocalFocus(true, false);
//    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = createImageFile();

            // Continue only if the File was successfully created
            if (photoFile != null) {

                Uri photoURI = FileProvider.getUriForFile(getContext(), "com.concavenp.artistrymuse", photoFile);

                Log.d(TAG, "New camera image URI location: " + photoURI.toString() );

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch(requestCode) {

                case REQUEST_IMAGE_CAPTURE: {

                    // Load the captured image into the ImageView widget
                    populateThumbnailImageView(mProfileImagePath, mProfileImageView);

                    // Add the new (at least to this App) image to the system's Media Provider
                    galleryAddPic();

                    break;

                }
                case REQUEST_IMAGE_STORE: {

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
                    int permissionCheck = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE );

                    if (permissionCheck == PackageManager.PERMISSION_DENIED) {

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                        // Continue processing in the callback associated with permissions (onRequestPermissionsResult)

                    } else {

                        // Copy the file locally and set the thumbnail
                        processExternalUri();

                    }

                    break;

                }

            }

        }

    }

    /**
     * Helper method that will add the photo in question to the system's Media Provider
     */
    private void galleryAddPic() {

// TODO: dunno if this works

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mProfileImagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getContext().sendBroadcast(mediaScanIntent);

    }

    private void processExternalUri() {

        try {

            // The output location of the copied file
            File galleryFile = createImageFile();
            FileOutputStream fileOutputStream = new FileOutputStream(galleryFile);

            // The input location of the external file
            ParcelFileDescriptor parcelFileDescriptor = getContext().getContentResolver().openFileDescriptor(mSelectedImageUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            FileInputStream fileInputStream = new FileInputStream(fileDescriptor);

            copyFile(fileInputStream, fileOutputStream);

            populateThumbnailImageView(mProfileImagePath, mProfileImageView);

            parcelFileDescriptor.close();

            // Add the new (at least to this App) image to the system's Media Provider
            galleryAddPic();

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

            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Copy the file locally and set the thumbnail
                    processExternalUri();

                } else {

                    // Permission has been denied.  Keep asking the user for permission when
                    // trying to access the external storage.

                }

                break;
            }

        }

    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_save) {

            // Move the last update time
            mUser.setLastUpdatedDate(new Date().getTime());

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
                getContext()
                        .startService(new Intent(getContext(), UploadService.class)
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

        } else if (id == android.R.id.home) {

            // Handle close button click here
            dismiss();

            return true;

        }

        return super.onOptionsItemSelected(item);

    }

    private File createImageFile() {

        // Create an image file name
        mProfileImageUid = UUID.randomUUID();
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir + "/" + mProfileImageUid.toString() + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mProfileImagePath = image.getAbsolutePath();

        return image;

    }

}

