package com.concavenp.artistrymuse.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;
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
     * Observable pattern object that will be used to notify the observer(s) (MainActivty) that
     * the user needs to login.
     */
    private Observable mLoginObservable = new Observable();

    public void addLoginListener(Observer observer) {
        mLoginObservable.addObserver(observer);
    }

    public void deleteLoginListener(Observer observer) {
        mLoginObservable.deleteObserver(observer);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Listen for changes in our Firebase authentication state
        mAuth.addAuthStateListener(this);

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {

        // Listen for changes in our Firebase authentication state
        mAuth.removeAuthStateListener(this);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAuthStateChanged(FirebaseAuth auth) {

        final String oldUid = getSharedPreferenceUid();

        // The Firebase Auth has changed
        mAuth = auth;

        FirebaseUser user = mAuth.getCurrentUser();

        // Check to see if there is a user checked in
        if (user == null) {

            // There is no user currently logged

            // Clear out the preferences UID value
            setSharedPreferenceUid(getResources().getString(R.string.default_application_uid_value));

            // Notify the observers of the need for the user to login
            mLoginObservable.notifyObservers();

        }
        else {

            // Retrieve the Firebase UID of the currently authenticated user
            final String authUid = user.getUid();

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

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            });






            // If the application's UID has changed then
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

        String uid = "";

        // Check if an UID entry exists already and create it if not
        if (mSharedPreferences.contains(getResources().getString(R.string.application_uid_key))) {

            // Get the UID from the SharedPreferences
            uid = mSharedPreferences.getString(getResources().getString(R.string.application_uid_key), getResources().getString(R.string.default_application_uid_value));

        }
        else {

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

        SharedPreferences.Editor editor = getSharedPreferences(getResources().getString(R.string.shared_preferences_filename), MODE_PRIVATE).edit();
        editor.putString(getResources().getString(R.string.application_uid_key), uid);
        editor.commit();

    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        UserAuthenticationService getService() {
            // Return this instance of UserAuthenticationService so clients can register observers
            return UserAuthenticationService.this;
        }
    }

}

