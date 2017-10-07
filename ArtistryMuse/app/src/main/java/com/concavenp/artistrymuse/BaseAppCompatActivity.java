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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.file_descriptor.FileDescriptorUriLoader;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.concavenp.artistrymuse.InspirationEditActivity.EXTRA_DATA_INSPIRATION;
import static com.concavenp.artistrymuse.InspirationEditActivity.EXTRA_DATA_PROJECT;

/**
 * Created by dave on 3/25/2017.
 *
 * References:
 *
 * How to round an image with Glide library?
 *      - https://stackoverflow.com/questions/25278821/how-to-round-an-image-with-glide-library
 *
 * Get color-int from color resource
 *      - https://stackoverflow.com/questions/5271387/get-color-int-from-color-resource
 *
 * android - How to create a circular ImageView with border
 *      - https://android--examples.blogspot.com/2015/11/android-how-to-create-circular.html
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity implements OnInteractionListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = BaseAppCompatActivity.class.getSimpleName();

    /**
     * String used when creating the activity via intent.  This key will be used to retrieve the
     * UID associated with the USER in question.
     */
    public static final String EXTRA_DATA = "uid_string_data";

    protected DatabaseReference mDatabase;
    protected StorageReference mStorageRef;
    protected FirebaseImageLoader mImageLoader;
    protected FileDescriptorUriLoader mUriLoad;

    protected SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Initialize the Firebase Database connection
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize the Firebase Storage connection
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Create the Firebase image loader
        mImageLoader = new FirebaseImageLoader();

        mUriLoad = new FileDescriptorUriLoader(this);

        // Get ready to read from local storage for this app
        mSharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_preferences_filename), MODE_PRIVATE);

    }

    protected String buildFileReference(String uid, String imageUid, StorageDataType type) {

        String fileReference = null;

        // Verify there is image data to work with
        if ((imageUid != null) && (!imageUid.isEmpty())) {

            // Verify there is user data to work with
            if ((uid != null) && (!uid.isEmpty())) {

                fileReference = type.getType() + getResources().getString(R.string.firebase_separator) + uid + getResources().getString(R.string.firebase_separator) + imageUid + getResources().getString(R.string.firebase_image_type);

            }
            else {

                Log.e(TAG, "Unexpected null project UID");

            }

        }
        else {

            Log.e(TAG, "Unexpected null image UID");

        }

        return fileReference;

    }

    protected StorageReference buildStorageReference(String uid, String imageUid, StorageDataType type) {

        StorageReference result = null;

        String fileReference = buildFileReference(uid, imageUid, type);

        // It is possible for the file reference string to be null, so check for it
        if ((fileReference != null) && (!fileReference.isEmpty())) {

            result = mStorageRef.child(fileReference);

        }

        return result;

    }

    protected void populateImageView(String fileReference, ImageView imageView) {

        // For safety, check as well (I've seen it) ...
        if (imageView != null) {

            // It is possible for the file reference string to be null, so check for it
            if ((fileReference != null) && (!fileReference.isEmpty())) {

                // Protection when the user moves too quickly around the activities
                if (!this.isDestroyed()) {

                    StorageReference storageReference = mStorageRef.child(fileReference);

                    // Protection when the user moves too quickly around the activities
                    if (!this.isDestroyed()) {

                        // Download directly from StorageReference using Glide
                        Glide.with(this)
                                .using(mImageLoader)
                                .load(storageReference)
                                .fitCenter()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageView);

                    }

                }

            }

        }

    }

    protected void populateCircularImageView(StorageReference storageReference, final ImageView imageView) {

        // For safety, check as well (I've seen it) ...
        if (imageView != null) {

            // It is possible for the file reference string to be null, so check for it
            if (storageReference!= null) {

                Glide.with(this)
                        .using(mImageLoader)
                        .load(storageReference)
                        .asBitmap()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(createBitmapImageViewTarget(imageView));

            }

        }

    }

    protected void populateCircularImageView(String fileReference, final ImageView imageView) {

        // For safety, check as well (I've seen it) ...
        if (imageView != null) {

            // It is possible for the file reference string to be null, so check for it
            if ((fileReference != null) && (!fileReference.isEmpty())) {

                // Protection when the user moves too quickly around the activities
                if (!this.isDestroyed()) {

                    Glide.with(this)
                            .load(fileReference)
                            .asBitmap()
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(createBitmapImageViewTarget(imageView));

                }

            }

        }

    }

    protected void populateThumbnailImageView(String fileReference, ImageView imageView) {

        // For safety, check as well (I've seen it) ...
        if (imageView != null) {

            // It is possible for the file reference string to be null, so check for it
            if ((fileReference != null) && (!fileReference.isEmpty())) {

                // Protection when the user moves too quickly around the activities
                if (!this.isDestroyed()) {

                    // Download directly from StorageReference using Glide
                    Glide.with(this)
                            .load(fileReference)
                            .thumbnail(0.1f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imageView);

                }

            }

        }

    }

    /**
     * Helper method that protects against bad data when populating TextView(s).  This overloaded
     * method provides safe conversion protection by wrapping the Double object.
     *
     * @param doubleObject - The Double object to be converted to text and set in the view
     * @param textView - The view to set with the text parameter
     */
    protected void populateTextView(Double doubleObject, TextView textView) {

        // For safety, check as well (I've seen it) ...
        if (textView != null) {

            if (doubleObject != null) {

                String result = "";

                try {

                    result = String.format(textView.getResources().getString(R.string.number_format), doubleObject);

                } catch (Exception ex) {

                    Log.e(TAG, "The double object threw an exception during string translation, defaulting to an empty string");

                }

                // With a string in hand, populate the TextView
                populateTextView(result, textView);

            } else {

                Log.e(TAG, "The double object was null, defaulting to an empty string");

                // The object was null, default to an empty string
                populateTextView("", textView);

            }

        } else {

            Log.e(TAG, "The TextView object was null, unable to populate");

        }

    }

    /**
     * Helper method that protects against bad data when populating TextView(s).  This overloaded
     * method provides safe conversion protection by wrapping the Integer object.
     *
     * @param intObject - The Integer object to be converted to text and set in the view
     * @param textView - The view to set with the text parameter
     */
    protected void populateTextView(Integer intObject, TextView textView) {

        if (intObject != null) {

            String result = "";

            try {

                result = intObject.toString();

            } catch (Exception ex) {

                Log.e(TAG, "The integer object threw an exception during string translation, defaulting to an empty string");

            }

            // With a string in hand, populate the TextView
            populateTextView(result, textView);

        } else {

            Log.e(TAG, "The integer object was null, defaulting to an empty string");

            // The object was null, default to an empty string
            populateTextView("", textView);

        }

    }

    /**
     * Helper method that protects against bad data when populating TextView(s).
     *
     * @param text - The text to set in the view
     * @param textView - The view to set with the text parameter
     */
    protected void populateTextView(String text, TextView textView) {

        // For safety, check as well (I've seen it) ...
        if (textView != null) {

            // Verify there is text to work with and empty out if nothing is there.
            if ((text != null) && (!text.isEmpty())) {

                textView.setText(text);

            } else {

                textView.setText("");

            }

        } else {

            Log.e(TAG, "The TextView object was null, unable to populate");

        }

    }

    /**
     * The purpose of this interface implementation is to start the Details Activity of either a
     * user or a project.  The point to making the Main Activity implement is to support both the
     * phone and tablet layout of the app.  Phone layouts will just start a new activity and
     * tablet layouts will populate a neighboring fragment with the details results.
     *
     * @param firstUid - This will be the UID of other the User or the Project as specified in the type param
     * @param secondUid - This uid will be used for identifying an inspiration
     * @param storageDataType - The type will either be a user or a project
     */
    @Override
    public void onInteractionSelection(String firstUid, String secondUid, StorageDataType storageDataType, UserInteractionType userInteractionType) {

        switch(storageDataType) {

            case PROJECTS: {

                switch(userInteractionType) {

                    case DETAILS: {

                        // Create and start the details activity along with passing it the UID of the Project in question
                        Intent intent = new Intent(this, ProjectDetailsActivity.class);
                        intent.putExtra(EXTRA_DATA, firstUid);
                        startActivity(intent);

                        break;
                    }
                    case EDIT: {

                        // Create and start the inspiration activity along with passing it the UID of the inspiration in question
                        Intent intent = new Intent(this, ProjectEditActivity.class);
                        intent.putExtra(EXTRA_DATA, firstUid);
                        startActivity(intent);

                    }
                }


                break;
            }
            case USERS: {

                switch(userInteractionType) {

                    case DETAILS: {

                        // Create and start the details activity along with passing it the UID of the User in question
                        Intent intent = new Intent(this, UserDetailsActivity.class);
                        intent.putExtra(EXTRA_DATA, firstUid);
                        startActivity(intent);

                        break;
                    }
                    case EDIT: {

                        // NOTE: Currently, there is only the details type

                    }
                }

                break;

            }
            case INSPIRATIONS: {

                switch(userInteractionType) {

                    case DETAILS: {

                        // NOTE: Currently, there is only the edit type

                        break;
                    }
                    case EDIT: {

                        // Create and start the inspiration activity along with passing it the UID of the inspiration in question
                        Intent intent = new Intent(this, InspirationEditActivity.class);
                        intent.putExtra(EXTRA_DATA_PROJECT, firstUid);
                        intent.putExtra(EXTRA_DATA_INSPIRATION, secondUid);
                        startActivity(intent);

                    }
                }

                break;

            }
            default: {
                // Do nothing
            }

        }

    }

    protected String getUid() {

        // Get the UID from the SharedPreferences
        return mSharedPreferences.getString(getResources().getString(R.string.application_uid_key), getResources().getString(R.string.default_application_uid_value));

    }

    /**
     * Helper method that will copy the contents of one (file) to another.
     *
     * @param in - The input stream of bytes to copy from
     * @param out - The output stream of bytes to copy to
     * @throws IOException - thrown when there is a problem writing/reading from either stream
     */
    protected void copyFile(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {

            out.write(buffer, 0, read);

        }

        // Done with the input stream
        in.close();

        // Write the output file (You have now copied the file)
        out.flush();
        out.close();

    }

    private BitmapImageViewTarget createBitmapImageViewTarget(final ImageView imageView) {

        return new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap bitmap) {

                int bitmapWidth = bitmap.getWidth();
                int bitmapHeight = bitmap.getHeight();
                int borderWidthHalf = 10;
                int bitmapSquareWidth = Math.min(bitmapWidth,bitmapHeight);
                int newBitmapSquare = bitmapSquareWidth+borderWidthHalf;

                Bitmap roundedBitmap = Bitmap.createBitmap(newBitmapSquare,newBitmapSquare,Bitmap.Config.ARGB_8888);

                // Initialize a new Canvas to draw empty bitmap
                Canvas canvas = new Canvas(roundedBitmap);

                // Calculation to draw bitmap at the circular bitmap center position
                int x = borderWidthHalf + bitmapSquareWidth - bitmapWidth;
                int y = borderWidthHalf + bitmapSquareWidth - bitmapHeight;

                canvas.drawBitmap(bitmap, x, y, null);

                // Initializing a new Paint instance to draw circular border
                Paint borderPaint = new Paint();
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(borderWidthHalf*2);
                borderPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.myapp_accent_700, null));

                canvas.drawCircle(canvas.getWidth()/2, canvas.getWidth()/2, newBitmapSquare/2, borderPaint);

                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), roundedBitmap);
                circularBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(circularBitmapDrawable);

            }
        };
    }

}

