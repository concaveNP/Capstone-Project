package com.concavenp.artistrymuse;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.MainThread;
import android.support.annotation.StyleRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.concavenp.artistrymuse.fragments.adapter.ArtistryFragmentPagerAdapter;
import com.concavenp.artistrymuse.services.UserAuthenticationService;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.Scopes;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseAppCompatActivity implements
        UserAuthenticationService.OnAuthenticationListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * This service is used to translate the Firebase UID of the authenticated user to an app
     * specified ArtistryMuse UID.  The service listens for Firebase authentication events
     * and sets a SharedPreferences value accordingly.  SharedPreferences is where the app
     * specific UID will be stored for the duration of the the user's "logged in" experience.
     *
     * The rest of the app will use the app specific UID for Firebase database and storage
     * lookups.
     */
    private UserAuthenticationService mService;

    private static final int RC_SIGN_IN = 100;

    /**
     * Flag used to indicate if our one time starting/binding of the service has been performed.
     */
    private boolean mBound = false;

    /**
     * Field used in determining how various UI elements are displayed within a Large or Not Large display
     */
    private boolean mIsLargeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Bind to the UserAuthenticationService
        //
        // NOTE: this service require a little more explanation as it appears to be started and
        // never stopped.  That is exactly what we want in this case.  Hence, the "startService"
        // is used to which allows the service to run indefinitely.  The service monitors the
        // authentication service provided by Firebase for an authenticated user to log in.
        // Once the user logs in the service will translate the Firebase authentication ID into
        // an application specific (ArtistryMuse) ID.  This class will also monitor the login and
        // logout of the user in order to perform the additional duty of starting a login activity
        // if needed.
        if (mBound) {
            Intent intent = new Intent(this, UserAuthenticationService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
        }

        // If there is not a user logged in, there should be
        if (getUid().isEmpty()) {
            onLoginInteraction();
        }

        setContentView(R.layout.activity_main);

        // If the application has not run before then initialize the preference settings with default values
        if (savedInstanceState == null) {
            // These are the "general" preferences (its all this app has)
            PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        }

        // Force the scroll view to fill the area, dunno why this is not the default.
        NestedScrollView scrollView = (NestedScrollView) findViewById (R.id.nest_scrollview);
        scrollView.setFillViewport (true);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new ArtistryFragmentPagerAdapter(getSupportFragmentManager()));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Setup the support for creating a menu (ActionBar functionality)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // The default result is that, no, we did not handle the given item
        boolean result;

        switch (item.getItemId()) {

            case R.id.action_profile: {

                // User chose to open the User Profile Editor
                onProfileInteraction();

                // We handled it
                result = true;

                break;

            }
            case R.id.action_settings: {

                // User chose the "Settings" item, show the app settings UI...

                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                // We handled it
                result = true;

                break;

            }
            case R.id.action_logoff: {

                // User wants to logoff of the backend service


//                mService.logoff();




/*
                AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            recreate();
                        } else {
                            // TODO: failed
                        }
                    }
                });
                */

                // We handled it
                result = true;

                break;

            }
            default: {

                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                result = super.onOptionsItemSelected(item);

                break;

            }

        }

        return result;

    }

    /**
     * Implementation for the interface that provides the ability for this activity to be
     * notified when the user needs to login into the Firebase service.
     */
    @Override
    public void onLoginInteraction() {

        // TODO: what is RC_SING_IN used for
        // TODO: the SVG is not apparently going to work here, need an png export of the logo
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(getSelectedTheme())
                        .setLogo(getSelectedLogo())
                        .setAvailableProviders(getSelectedProviders())
//                        .setTosUrl(getSelectedTosUrl())
//                        .setPrivacyPolicyUrl(getSelectedPrivacyPolicyUrl())
                        .setIsSmartLockEnabled(false)
//                        .setIsSmartLockEnabled(mEnableCredentialSelector.isChecked(), mEnableHintSelector.isChecked())
//                        .setAllowNewEmailAccounts(mAllowNewEmailAccounts.isChecked())
                        .build(),
                RC_SIGN_IN);
    }

    @MainThread
    @StyleRes
    private int getSelectedTheme() {
        return R.style.AppTheme;
    }

    @MainThread
    @DrawableRes
    private int getSelectedLogo() {
        return R.drawable.ic_muse_logo_2_vector;
    }

//    /**
//     * This method is used to set what providers will be used for account authentication.
//     *
//     * @return The list of authentication providers
//     */
//    @MainThread
//    private List<AuthUI.IdpConfig> getSelectedProviders() {
//
//        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();
//
//        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
//
//        // Current, we only want to allow user to verify themselves against an Email address.
//        // Future work would include other verification methods that would be enabled here.
//        //        selectedProviders.add( new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());
//        selectedProviders.add( new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
//        //        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());
//
//        return selectedProviders;
//
//    }

    @MainThread
    private List<AuthUI.IdpConfig> getSelectedProviders() {
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();

            selectedProviders.add( new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER) .setPermissions(getGooglePermissions()) .build());
//            selectedProviders.add( new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER) .setPermissions(getFacebookPermissions()) .build());
//            selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());
            selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
//            selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());

        return selectedProviders;
    }

    @MainThread
    private List<String> getGooglePermissions() {
        List<String> result = new ArrayList<>();
           result.add(Scopes.DRIVE_FILE);
        return result;
    }
    /**
     * Implementation for the interface that provides the ability for this activity to be
     * notified when the user needs to fill out profile information about themselves.  As in a
     * new user situation.
     */
    @Override
    public void onProfileInteraction() {

        // TODO: implement the details activity(s) for both type
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        ProfileDialogFragment profileDialogFragment = new ProfileDialogFragment();
//
//        if (mIsLargeLayout) {
//
//            // The device is using a large layout, so show the fragment as a dialog
//            profileDialogFragment.show(fragmentManager, "dialog");
//
//        } else {
//
//            // The device is smaller, so show the fragment fullscreen
//            FragmentTransaction transaction = fragmentManager.beginTransaction();
//
//            // For a little polish, specify a transition animation
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//
//            // To make it fullscreen, use the 'content' root view as the container
//            // for the fragment, which is always the root view for the activity
//            transaction.add(android.R.id.content, profileDialogFragment).addToBackStack(null).commit();
//
//        }

    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            UserAuthenticationService.LocalBinder binder = (UserAuthenticationService.LocalBinder) service;
            mService = binder.getService();

            // Register this class as a listener for login and profile events
            mService.registerAuthenticationListener(MainActivity.this);

            // Our one time bounding has occurred
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

            // Do nothing

        }

    };

}

