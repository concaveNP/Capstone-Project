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

package com.concavenp.artistrymuse.fragments.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by dave on 1/29/2017.
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    protected DatabaseReference mDatabase;
    protected StorageReference mStorageRef;
    protected FirebaseUser mUser;
    protected String mUid;
    protected FirebaseImageLoader mImageLoader;

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = UserViewHolder.class.getSimpleName();

    public BaseViewHolder(View itemView) {

        super(itemView);

        // Initialize the Database connection
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize the Storage connection
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Get the authenticated user
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser != null) {
            mUid = mUser.getUid();
        }

        // Create the Firebase image loader
        mImageLoader = new FirebaseImageLoader();

    }

    abstract public void bindToPost(Object jsonObject, final OnInteractionListener listener);

    protected String buildFileReference(String uid, String imageUid, StorageDataType type) {

        String fileReference = null;

        // Verify there is image data to work with
        if ((imageUid != null) && (!imageUid.isEmpty())) {

            // Verify there is user data to work with
            if ((uid != null) && (!uid.isEmpty())) {

                fileReference = type.getType() + itemView.getResources().getString(R.string.firebase_separator) + uid + itemView.getResources().getString(R.string.firebase_separator) + imageUid + itemView.getResources().getString(R.string.firebase_image_type);

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

    protected void populateImageView(String fileReference, ImageView imageView) {

        // For safety, check as well (I've seen it) ...
        if (imageView != null) {

            // It is possible for the file reference string to be null, so check for it
            if ((fileReference != null) && (!fileReference.isEmpty())) {

                StorageReference storageReference = mStorageRef.child(fileReference);

                // Download directly from StorageReference using Glide
                Glide.with(imageView.getContext())
                        .using(mImageLoader)
                        .load(storageReference)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);

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

}

