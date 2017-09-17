package com.concavenp.artistrymuse.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
     * @return - The SharedPreferences
     */
    private void loadSharedPreferences(Context context) {

        // Check to see if this has been done before
        if (mSharedPreferences == null) {

            mSharedPreferences = context.getSharedPreferences(getResources().getString(R.string.shared_preferences_filename), MODE_PRIVATE);

        }

    }

}

