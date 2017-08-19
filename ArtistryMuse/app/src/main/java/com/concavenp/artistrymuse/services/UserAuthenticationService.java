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
import java.util.UUID;

import static com.concavenp.artistrymuse.StorageDataType.USERS;
import static com.concavenp.artistrymuse.StorageDataType.AUTH;

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

        mAuthListener = listener;

    }

    public void clearRegisteredAuthenticationListener() {

        mAuthListener = null;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mBinder ;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Listen for changes in our Firebase authentication state now there is a listener
        mAuth.addAuthStateListener(this);

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public synchronized void onAuthStateChanged(FirebaseAuth auth) {

        final String oldUid = getSharedPreferenceUid();

        // The Firebase Auth has changed
        mAuth = auth;

        final FirebaseUser user = mAuth.getCurrentUser();

        // Check to see if there is a user checked in
        if (user == null) {

            // There is no user currently logged

            // Clear out the preferences UID value
            setSharedPreferenceUid(getResources().getString(R.string.default_application_uid_value));

            // Notify the observers of the need for the user to login
            if (mAuthListener != null) {

                mAuthListener.onLoginInteraction();

            }

        }
        else {

            // There is a authenticated user logged in

            // Retrieve the Firebase UID of the currently authenticated user
            final String authUid = user.getUid();

            // Get the Firebase UID translated to the ArtistryMuse UID via a DB lookup
            mDatabase.child(AUTH.getType()).child(authUid).addListenerForSingleValueEvent(new ValueEventListener() {

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
                        mDatabase.child(AUTH.getType()).child(authUid).setValue(newUid.toString());

                        // Create new default user account info for the given UID
                        User newUser = new User();

                        long currentDate = new Date().getTime();

                        newUser.setAuthUid(authUid);
                        newUser.setCreationDate(currentDate);
                        newUser.setDescription("");
                        newUser.setFollowedCount(0);
                        newUser.setLastUpdatedDate(currentDate);

                        // Use the name if available
                        String displayName = user.getDisplayName();
                        if (displayName != null) {
                            newUser.setName(displayName);
                        } else {
                            newUser.setName("");
                        }

                        newUser.setSummary("");
                        newUser.setUid(newUid.toString());
                        newUser.setUsername("");

                        // Write it to the DB
                        mDatabase.child(USERS.getType()).child(newUid.toString()).setValue(newUser);

                        // Set the UID in the SharedPreferences
                        setSharedPreferenceUid(newUid.toString());

                        // Signal the need for the profile settings activity to be displayed
                        if (mAuthListener != null) {

                            mAuthListener.onProfileInteraction();

                        }

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

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(getResources().getString(R.string.application_uid_key), uid);
        editor.commit();

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
