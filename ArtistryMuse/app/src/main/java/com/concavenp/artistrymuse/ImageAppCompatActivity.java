package com.concavenp.artistrymuse;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * TODO: this is wrong, update the comment block
 *
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
 * Disable auto focus on edit text
 *      - http://stackoverflow.com/questions/7593887/disable-auto-focus-on-edit-text
 */
@SuppressWarnings("StatementWithEmptyBody")
public abstract class ImageAppCompatActivity extends BaseAppCompatActivity {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ImageAppCompatActivity.class.getSimpleName();

    // The different activity result items given the user can choose an image from either the
    // camera or an existing image.
    private static final int REQUEST_IMAGE_STORE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    // When required, this app can ask for the user's permission to read from external storage if
    // choosing a image from a gallery is decided.  In this event the result from an activity
    // needs to specify what the result applies to.
    private static final int REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE = 0;

    // The transient values used during the user's choice of what image to use
    protected String mImagePath;
    protected UUID mImageUid;

    // The value that will hold the user selected image from a picker (versus a camera taken photo)
    protected Uri mSelectedImageUri;

    // The transient value that is meaningful to sub-classes
    private int mType;

    abstract ImageView getSpecificImageView(int type);
    abstract void setSpecificImageData(int type);

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    protected class ImageButtonListener implements View.OnClickListener {

        private int mImageType;

        ImageButtonListener(int type) {

            mImageType = type;

        }

        @Override
        public void onClick(View v) {

            // Set the image type that is meaningful to the sub-class within the processing of
            // activity results from intents
            setType(mImageType);

            // Determine if this device has "camera" hardware available
            boolean cameraPresent = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
            if (cameraPresent) {

                new AlertDialog.Builder(ImageAppCompatActivity.this)
                        .setTitle(R.string.profile_image_source_title)
                        .setItems(R.array.profile_image_source_choice, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Act on the choice made by the user.  This will be to either
                                // choose to take a new photo or pick one from the gallery.
                                switch (which) {
                                    case 0: {

                                        // Take a picture
                                        dispatchTakePictureIntent(mImageType);

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

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch(requestCode) {

                case REQUEST_IMAGE_CAPTURE: {

                    // Save off the values generated from the image creation
                    setSpecificImageData(getType());

                    // Load the captured image into the ImageView widget
                    populateThumbnailImageView(mImagePath, getSpecificImageView(getType()));

                    // Add the new (at least to this App) image to the system's Media Provider
                    galleryAddPic(mImagePath);

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
                    int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE );

                    if (permissionCheck == PackageManager.PERMISSION_DENIED) {

                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE);

                        // Continue processing in the callback associated with permissions (onRequestPermissionsResult)

                    } else {

                        // Copy the file locally and set the thumbnail
                        processExternalUri(getSpecificImageView(getType()));

                        // Save off the values generated from the image creation
                        setSpecificImageData(getType());

                    }

                    break;

                }

            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Copy the file locally and set the thumbnail
                    processExternalUri(getSpecificImageView(getType()));

                    // Save off the values generated from the image creation
                    setSpecificImageData(getType());

                } else {

                    // Permission has been denied.  Keep asking the user for permission when
                    // trying to access the external storage.

                }

                break;
            }

        }

    }

    protected void processExternalUri(ImageView view) {

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

    /**
     *
     * @param type - The type is a number that is one of the following: REQUEST_PROFILE_IMAGE_STORE or REQUEST_HEADER_IMAGE_STORE
     */
    protected void dispatchTakePictureIntent(int type) {

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

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }

        }

    }

    protected File createImageFile() {

        // New UUID
        mImageUid = UUID.randomUUID();

        // Create an image file name
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir + "/" + mImageUid.toString() + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mImagePath = image.getAbsolutePath();

        return image;

    }

    /**
     * Helper method that will add the photo in question to the system's Media Provider
     */
    protected void galleryAddPic(String path) {

        // TODO: dunno if this works, Save off this TODO external to code and remove TODO

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(path);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);

    }

}
