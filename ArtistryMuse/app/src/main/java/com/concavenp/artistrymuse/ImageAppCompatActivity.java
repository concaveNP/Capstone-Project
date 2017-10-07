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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
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

    // The different type of image shapes that can be used
    public enum ImageShape {
        IMAGE_SHAPE_RECTANGLE,
        IMAGE_SHAPE_CIRCLE
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
                                        dispatchTakePictureIntent();

                                        break;

                                    }
                                    case 1: {

                                        // ACTION_GET_CONTENT is the intent to choose a file via the system's file
                                        // browser.
                                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

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
                    switch(getRectangleOrCircle(getType())) {
                        case IMAGE_SHAPE_CIRCLE: {
                            populateCircularImageView(mImagePath, getSpecificImageView(getType()));
                            break;
                        }
                        case IMAGE_SHAPE_RECTANGLE:
                        default: {
                            populateThumbnailImageView(mImagePath, getSpecificImageView(getType()));
                            break;
                        }

                    }

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

                        // Copy the file locally and set the thumbnail and Save off the values generated from the image creation
                        new ProcessExternalUriTask().execute(getSpecificImageView(getType()));

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

                    // Copy the file locally and set the thumbnail and Save off the values generated from the image creation
                    new ProcessExternalUriTask().execute(getSpecificImageView(getType()));

                } else {

                    // Permission has been denied.  Keep asking the user for permission when
                    // trying to access the external storage.

                }

                break;
            }

        }

    }

    protected void dispatchTakePictureIntent() {

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
     * Method that given the image type (value important to sub-classes) the determined shape
     * will be returned.  Sub-classes that wish to provide a circular image presentation should
     * overload this method.
     *
     * @param type - the type of image the presented shape will be in
     * @return - the image shape type
     */
    protected ImageShape getRectangleOrCircle(int type) {

        // The default will always be to provide a rectangle image shape
        return ImageShape.IMAGE_SHAPE_RECTANGLE;

    }

    private class ProcessExternalUriTask extends AsyncTask<ImageView, Void, Void> {

        // This will be the view that will be updated in the Post work method
        private ImageView mImageView;

        @Override
        protected Void doInBackground(ImageView... imageViews) {

            // Store the particular view that will be updated
            mImageView = imageViews[0];

            try {

                // The output location of the copied file
                File galleryFile = createImageFile();
                FileOutputStream fileOutputStream = new FileOutputStream(galleryFile);

                // The input location of the external file
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(mSelectedImageUri, "r");

                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                FileInputStream fileInputStream = new FileInputStream(fileDescriptor);

                // Perform the actual work of moving bits
                copyFile(fileInputStream, fileOutputStream);

                parcelFileDescriptor.close();

            } catch (FileNotFoundException ex) {

                Log.e(TAG, "Unable to retrieve file: " + ex.toString());

            } catch (IOException | NullPointerException ex) {

                Log.e(TAG, "Unable to retrieve file: " + ex.toString());

            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);

            // Load the captured image into the ImageView widget
            switch(getRectangleOrCircle(getType())) {
                case IMAGE_SHAPE_CIRCLE: {

                    populateCircularImageView(mImagePath, mImageView);

                    break;
                }
                case IMAGE_SHAPE_RECTANGLE:
                default: {

                    populateThumbnailImageView(mImagePath, mImageView);

                    break;
                }

            }

            // Save off the values generated from the image creation
            setSpecificImageData(getType());

        }

    }

}

