package com.concavenp.artistrymuse.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by dave on 3/25/2017.
 */
public abstract class BaseFragment extends Fragment implements FirebaseAuth.AuthStateListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = BaseFragment.class.getSimpleName();

    protected OnDetailsInteractionListener mDetailsListener;

    protected DatabaseReference mDatabase;
    protected StorageReference mStorageRef;
    protected FirebaseAuth mAuth;
    protected FirebaseImageLoader mImageLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Initialize the Firebase Database connection
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize the Firebase Storage connection
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Initialize the Firebase Authentication connection
        mAuth = FirebaseAuth.getInstance();

        // Get the authenticated user
        mUser = mAuth.getCurrentUser();

        // Create the Firebase image loader
        mImageLoader = new FirebaseImageLoader();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Listen for changes in our Firebase authentication state
        mAuth.addAuthStateListener(this);

        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onDestroyView() {

        // Listen for changes in our Firebase authentication state
        mAuth.removeAuthStateListener(this);

        super.onDestroyView();

    }

    @Override
    public void onAuthStateChanged(FirebaseAuth auth) {

        // The Firebase Auth has changed
        mAuth = auth;

    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        // Re-attach to the parent Activity interface
        if (context instanceof OnDetailsInteractionListener) {

            mDetailsListener = (OnDetailsInteractionListener) context;

        } else {

            throw new RuntimeException(context.toString() + " must implement OnDetailsInteractionListener");

        }
    }

    @Override
    public void onDetach() {

        super.onDetach();

        // Detach from the parent Activity interface(s)
        mDetailsListener = null;

    }

    protected String getUid() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Log.d(TAG, "User UID from Firebase was NULL");
            return "";
        }

        //return mUser.getUid();
        Log.d(TAG, "User UID from Firebase: " + user.getUid());
        return user.getUid();

        // TODO: this will need to be figured out some other way and probably/maybe saved to local properties
        // must use the authUid (this is the getUid() call) to get the uid to be the DB primary key index to use as the myUserId value in the query - yuck, i'm doing this wrong

        // TODO: should not be hard coded
        //return "2a1d3365-118d-4dd7-9803-947a7103c730";
        //return "8338c7c0-e6b9-4432-8461-f7047b262fbc";
        //return "d0fc4662-30b3-4e87-97b0-d78e8882a518";
        //return "54d1e146-a114-45ea-ab66-389f5fd53e53";
        //return "0045d757-6cac-4a69-81e3-0952a3439a78";
//        return "022ffcf3-38ac-425f-8fbe-382c90d2244f";

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
//                    .crossFade()
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
