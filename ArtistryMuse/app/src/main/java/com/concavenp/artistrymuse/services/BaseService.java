package com.concavenp.artistrymuse.services;

import android.app.Service;
import android.content.SharedPreferences;

import com.concavenp.artistrymuse.R;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by dave on 4/7/2017.
 */
public abstract class BaseService extends Service {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = BaseService.class.getSimpleName();

    protected DatabaseReference mDatabase;
    protected StorageReference mStorageRef;
    protected FirebaseAuth mAuth;
    protected FirebaseImageLoader mImageLoader;
    protected SharedPreferences mSharedPreferences;

    @Override
    public void onCreate() {

        super.onCreate();

        // Initialize the Firebase Database connection
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize the Firebase Storage connection
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Initialize the Firebase Authentication connection
        mAuth = FirebaseAuth.getInstance();

        // Create the Firebase image loader
        mImageLoader = new FirebaseImageLoader();

        // Get ready to read and write to local storage for this app
        mSharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_preferences_filename), MODE_PRIVATE);

    }

    protected String getUid() {

        // Get the UID from the SharedPreferences
        return mSharedPreferences.getString(getResources().getString(R.string.application_uid_key), getResources().getString(R.string.default_application_uid_value));

    }

}

