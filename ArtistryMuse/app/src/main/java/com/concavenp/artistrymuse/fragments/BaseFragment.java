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
public abstract class BaseFragment extends Fragment {

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
        //mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSharedPreferences = getContext().getSharedPreferences(getResources().getString(R.string.shared_preferences_filename), MODE_PRIVATE);

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
    }

    @Override
    public void onDetach() {

        super.onDetach();

        // Detach from the parent Activity interface(s)
        mInteractionListener = null;

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

                fileReference = type.getType() + "/" + uid + "/" + imageUid + ".jpg";

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

        // It is possible for the file reference string to be null, so check for it
        if (fileReference != null) {

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

    protected void populateTextView(String text, TextView textView) {

        // Verify there is text to work with and empty out if nothing is there.
        if ((text != null) && (!text.isEmpty())) {

            textView.setText(text);

        } else {

            textView.setText("");

        }

    }

}
