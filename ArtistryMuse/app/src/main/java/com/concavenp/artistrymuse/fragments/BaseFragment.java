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

package com.concavenp.artistrymuse.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by dave on 3/25/2017.
 *
 *
 */
public abstract class BaseFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = BaseFragment.class.getSimpleName();

    protected OnInteractionListener mInteractionListener;

    protected DatabaseReference mDatabase;
    protected StorageReference mStorageRef;
    protected FirebaseImageLoader mImageLoader;

    protected SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Initialize the Firebase Database connection
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize the Firebase Storage connection
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Create the Firebase image loader
        mImageLoader = new FirebaseImageLoader();

        // Get ready to read from local storage for this app
        loadSharedPreferences(getContext());

    }

    public void refresh() {
       // Do nothing
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Just for the UID changes
        if (key.equals(getResources().getString(R.string.application_uid_key))) {

            // All of the overriding subclasses will be informed to refresh their data
            refresh();

        }

    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        // Re-attach to the parent Activity interface
        if (context instanceof OnInteractionListener) {

            mInteractionListener = (OnInteractionListener) context;

        } else {

            throw new RuntimeException(context.toString() + " must implement OnInteractionListener");

        }

        // Register as a listener to SharedPreference ArtistryMuseUID changes
        loadSharedPreferences(context);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onDetach() {

        super.onDetach();

        // Detach from the parent Activity interface(s)
        mInteractionListener = null;

        // Unregister as a listener to SharedPreference ArtistryMuseUID changes
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

    }

    protected String getUid() {

        // Get the UID from the SharedPreferences
        return mSharedPreferences.getString(getResources().getString(R.string.application_uid_key), getResources().getString(R.string.default_application_uid_value));

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

                StorageReference storageReference = mStorageRef.child(fileReference);

                // Download directly from StorageReference using Glide
                Glide.with(getContext())
                        .using(mImageLoader)
                        .load(storageReference)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);

            }

        }

    }

    protected void populateCircularImageView(StorageReference storageReference, final ImageView imageView) {

        // For safety, check as well (I've seen it) ...
        if (imageView != null) {

            // It is possible for the file reference string to be null, so check for it
            if (storageReference!= null) {

                // Download directly from StorageReference using Glide
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

    protected void populateTextView(String text, TextView textView) {

        // For safety, check as well (I've seen it) ...
        if (textView != null) {

            // Verify there is text to work with and empty out if nothing is there.
            if ((text != null) && (!text.isEmpty())) {

                textView.setText(text);

            } else {

                textView.setText("");

            }

        }

    }

    /**
     * A lazy loader of the SharedPreferences.
     *
     * @param context - The context for which to aquire the SharedPreferences
     */
    private void loadSharedPreferences(Context context) {

        // Check to see if this has been done before
        if (mSharedPreferences == null) {

            mSharedPreferences = context.getSharedPreferences(getResources().getString(R.string.shared_preferences_filename), MODE_PRIVATE);

        }

    }

    private BitmapImageViewTarget createBitmapImageViewTarget(final ImageView imageView) {

        return new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap bitmap) {

                int bitmapWidth = bitmap.getWidth();
                int bitmapHeight = bitmap.getHeight();
                int borderWidthHalf = 5;
                int bitmapSquareWidth = Math.min(bitmapWidth,bitmapHeight);
                int newBitmapSquare = bitmapSquareWidth+borderWidthHalf;

                Bitmap roundedBitmap = Bitmap.createBitmap(newBitmapSquare,newBitmapSquare, Bitmap.Config.ARGB_8888);

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
                borderPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.myApp_accent_700, null));

                canvas.drawCircle(canvas.getWidth()/2, canvas.getWidth()/2, newBitmapSquare/2, borderPaint);

                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), roundedBitmap);
                circularBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(circularBitmapDrawable);

            }
        };
    }

}

