package com.concavenp.artistrymuse.fragments.dialog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by dave on 4/30/2017.
 */

public abstract class BaseDialogFragment extends DialogFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = BaseDialogFragment.class.getSimpleName();

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

    protected String getUid() {

        // Get the UID from the SharedPreferences
        return mSharedPreferences.getString(getResources().getString(R.string.application_uid_key), getResources().getString(R.string.default_application_uid_value));


//        FirebaseUser user = mAuth.getCurrentUser();
//
//        if (user == null) {
//            Log.d(TAG, "User UID from Firebase was NULL");
//            return "";
//        }
//
//        //return mUser.getUid();
//        Log.d(TAG, "User UID from Firebase: " + user.getUid());
//        return user.getUid();
//
//        // TODO: this will need to be figured out some other way and probably/maybe saved to local properties
//        // must use the authUid (this is the getUid() call) to get the uid to be the DB primary key index to use as the myUserId value in the query - yuck, i'm doing this wrong
//
//        // TODO: should not be hard coded
//        //return "2a1d3365-118d-4dd7-9803-947a7103c730";
//        //return "8338c7c0-e6b9-4432-8461-f7047b262fbc";
//        //return "d0fc4662-30b3-4e87-97b0-d78e8882a518";
//        //return "54d1e146-a114-45ea-ab66-389f5fd53e53";
//        //return "0045d757-6cac-4a69-81e3-0952a3439a78";
////        return "022ffcf3-38ac-425f-8fbe-382c90d2244f";

    }

    protected void populateImageView(String fileReference, ImageView imageView) {

        // It is possible for the file reference string to be null, so check for it
        if (fileReference != null) {

            StorageReference storageReference = mStorageRef.child(fileReference);

            // Download directly from StorageReference using Glide
            Glide.with(imageView.getContext())
                    .using(mImageLoader)
                    .load(storageReference)
                    .fitCenter()
//                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);

        }

    }

    protected void populateThumbnailImageView(String fileReference, ImageView imageView) {

        // It is possible for the file reference string to be null, so check for it
        if (fileReference != null) {

            //StorageReference storageReference = mStorageRef.child(fileReference);

            // Download directly from StorageReference using Glide
            Glide.with(imageView.getContext())
//                    .using(mImageLoader)
                    .load(fileReference)
                    .thumbnail(0.1f)
 //                   .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);

        }

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

}
