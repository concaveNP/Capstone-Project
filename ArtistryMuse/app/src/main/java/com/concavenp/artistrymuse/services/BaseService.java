package com.concavenp.artistrymuse.services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.model.Project;
import com.concavenp.artistrymuse.model.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    protected String getUid() {





//        FirebaseUser user = mAuth.getCurrentUser();
//
//        if (user == null) {
//            Log.d(TAG, "User UID from Firebase was NULL");
//            return "";
//        }
//
//        return user.getUid();
//
////        // TODO: this will need to be figured out some other way and probably/maybe saved to local properties
////        // must use the authUid (this is the getUid() call) to get the uid to be the DB primary key index to use as the myUserId value in the query - yuck, i'm doing this wrong
////
////        // TODO: should not be hard coded
////        //return "2a1d3365-118d-4dd7-9803-947a7103c730";
////        //return "8338c7c0-e6b9-4432-8461-f7047b262fbc";
////        //return "d0fc4662-30b3-4e87-97b0-d78e8882a518";
////        //return "54d1e146-a114-45ea-ab66-389f5fd53e53";
////        //return "0045d757-6cac-4a69-81e3-0952a3439a78";
////        return "022ffcf3-38ac-425f-8fbe-382c90d2244f";

    }

}

