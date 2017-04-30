package com.concavenp.artistrymuse.fragments.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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

        mProfileImageView = (ImageView) mainView.findViewById(R.id.profile_imageView);

        Button profileButton = (Button) mainView.findViewById(R.id.profile_button);
        profileButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

// TODO: put in the dialog to show a choice of how to get the picture: camera or storage

                dispatchTakePictureIntent();

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

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;
            try {

                photoFile = createImageFile();

            } catch (IOException ex) {

                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(getResources().getString(R.string.profile_file_error_message)).setTitle(getResources().getString(R.string.profile_file_error_title));

                // 3. Add buttons
                builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });

                // 4. Create the AlertDialog
                AlertDialog dialog = builder.create();

                return;
            }

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

                    break;

                }
                case REQUEST_IMAGE_STORE: {

                    // Save the resulting image location

                    // Create a new UUID for the image
                    mProfileImageUid = UUID.randomUUID();

                    // Do I rename the image locally or can I do it all in one fell swoop when moving to Firebase

// TODO: fill out

                    // Load the referenced image into the ImageView widget
                    populateThumbnailImageView(mProfileImagePath, mProfileImageView);

                    break;

                }

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
                            // File deleted successfully
                            Log.d(TAG, "Deleted old profile image (" + oldProfileUid + ") from cloud storage for the user (" + mUser.getUid() + ")");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                            Log.e(TAG, "Error deleting old profile image (" + oldProfileUid + ") from cloud storage for the user (" + mUser.getUid() + ")");
                        }
                    });

                }
                else {
                    // The user did not have an old profile image to replace - do nothing
                }

                // Save the new profile image to the cloud storage
                Uri file = Uri.fromFile(new File(mProfileImagePath));

                Log.d(TAG, "New profile image cloud storage location: " + file.toString());

                // Start MyUploadService to upload the file, so that the file is uploaded even if this Activity is killed or put in the background
                getContext().startService(new Intent(getContext(), UploadService.class).putExtra(UploadService.EXTRA_FILE_URI, file).setAction(UploadService.ACTION_UPLOAD));

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

    private File createImageFile() throws IOException {

        // Create an image file name
        mProfileImageUid = UUID.randomUUID();
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir + "/" + mProfileImageUid.toString() + ".jpg");

        // Create an empty file
        image.createNewFile();

        // Save a file: path for use with ACTION_VIEW intents
        mProfileImagePath = image.getAbsolutePath();

        return image;

    }
}
