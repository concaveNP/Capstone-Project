package com.concavenp.artistrymuse.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.UUID;

/**
 * Created by dave on 4/7/2017.
 */
public class UserAuthenticationService extends BaseService implements FirebaseAuth.AuthStateListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = UserAuthenticationService.class.getSimpleName();

    /**
     * This listener will be used to notify the starting activity (MainActivty) that
     * the user needs to ether login or fill out new user profile data.  The point here is that
     * this service continually monitors the authentication state of the user in regards to
     * Firebase.  However, it will need to signal the main activity in the event that a new
     * needs to be started (i.e. user login, user profile).
     */
    protected OnAuthenticationListener mAuthListener;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public void registerAuthenticationListener(OnAuthenticationListener listener) {

        Log.d(TAG, "Auth listener has been registered");

        mAuthListener = listener;

        Log.d(TAG, "Listening for Auth changes");

        // Listen for changes in our Firebase authentication state now there is a listener
        mAuth.addAuthStateListener(this);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG, "Activity is binding");

        return mBinder ;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // Fire it
        onAuthStateChanged(mAuth);




        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAuthStateChanged(FirebaseAuth auth) {

        Log.d(TAG, "onAuthStateChanged");

        final String oldUid = getSharedPreferenceUid();

        // The Firebase Auth has changed
        mAuth = auth;

        FirebaseUser user = mAuth.getCurrentUser();

        // Check to see if there is a user checked in
        if (user == null) {

            // There is no user currently logged
            Log.d(TAG, "There is no user currently logged");

            // Clear out the preferences UID value
            setSharedPreferenceUid(getResources().getString(R.string.default_application_uid_value));

            // Notify the observers of the need for the user to login
            Log.d(TAG, "Notifying listener of a required login");
            mAuthListener.onLoginInteraction();

        }
        else {

            Log.d(TAG, "There is an authenticated user");

            // Retrieve the Firebase UID of the currently authenticated user
            final String authUid = user.getUid();

            Log.d(TAG, "Requesting DB entry for the authenticated user");

            // Get the Firebase UID translated to the ArtistryMuse UID via a DB lookup
            mDatabase.child("auth").child(authUid).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    String artistryMuseUid = dataSnapshot.getValue(String.class);

                    // Verify there is a UID to work with
                    if (artistryMuseUid != null) {

                        // Check to see if the Old UID in the Shared Preferences is the same as
                        // this one.  If it is then we don't need to do anything, otherwise it
                        // needs to be written to the SharedPreferences.
                        if (!artistryMuseUid.equals(oldUid)) {
                            setSharedPreferenceUid(artistryMuseUid);
                        }

                    }
                    else {

                        // This is a new user that will need a new ArtistryMuse UID and user account settings

                        // Create the new UID
                        UUID newUid = UUID.randomUUID();

                        // Write it to the DB
                        mDatabase.child("auth").child(authUid).setValue(newUid.toString());

                        // Create new default user account info for the given UID
                        User newUser = new User();

                        long currentDate = new Date().getTime();

                        newUser.setAuthUid(authUid);
                        newUser.setCreationDate(currentDate);
                        newUser.setLastUpdatedDate(currentDate);
                        newUser.setUid(newUid.toString());

                        // Write it to the DB
                        mDatabase.child("users").child(newUid.toString()).setValue(newUser);

                        // Signal the need for the profile settings activity to be displayed
                        mAuthListener.onProfileInteraction();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            });

        }

    }

    /**
     * This is a helper method for retrieving the SharedPreference value for the Application's UID
     * of the user currently authenticated.  This is not the Firebase UID of the user in question,
     * but instead is the ArtistryMuse's UID of the user.
     *
     * If the UID value is not present then an empty string will be created as its default value.
     *
     * @return the Application's UID of the current Firebase authenticated user logged in
     */
    private String getSharedPreferenceUid() {

        Log.d(TAG, "Getting the ArtistryMuseUID");

        String uid = "";

        // Check if an UID entry exists already and create it if not
        if (mSharedPreferences.contains(getResources().getString(R.string.application_uid_key))) {

            // Get the UID from the SharedPreferences
            uid = mSharedPreferences.getString(getResources().getString(R.string.application_uid_key), getResources().getString(R.string.default_application_uid_value));

            Log.d(TAG, "The ArtistryMuseUID: " + uid);

        }
        else {

            Log.d(TAG, "The ArtistryMuseUID is being cleared out");

            // Set the UID in the SharedPreferences
            setSharedPreferenceUid(getResources().getString(R.string.default_application_uid_value));


        }

        return uid;

    }

    /**
     * This is a helper method for setting the SharedPreference value for the Application's UID
     * of the user currently authenticated.  This is not the Firebase UID of the user in question,
     * but instead is the ArtistryMuse's UID of the user.
     *
     * @param uid - the Application's UID of the current Firebase authenticated user logged in
     */
    private void setSharedPreferenceUid(String uid) {

        Log.d(TAG, "Setting the ArtistryMuseUID: " + uid);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        //SharedPreferences.Editor editor = getSharedPreferences(getResources().getString(R.string.shared_preferences_filename), MODE_PRIVATE).edit();
        editor.putString(getResources().getString(R.string.application_uid_key), uid);
        editor.commit();

    }


    public void logoff() {

        mAuth.signOut();

//
//
//        AuthUI.getInstance()
//                .signOut(this)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            recreate();
//                        } else {
//                            // TODO: failed
//                        }
//                    }
//                });
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public UserAuthenticationService getService() {
            // Return this instance of UserAuthenticationService so clients can register observers
            return UserAuthenticationService.this;
        }
    }

    /**
     * This interface is used by this service to signal the main activity that the user needs to
     * either login or fill out profile specific information about themselves.
     */
    public interface OnAuthenticationListener {

        void onLoginInteraction();
        void onProfileInteraction();

    }

}
