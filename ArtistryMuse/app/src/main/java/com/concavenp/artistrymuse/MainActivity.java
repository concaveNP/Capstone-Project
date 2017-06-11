package com.concavenp.artistrymuse;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.concavenp.artistrymuse.fragments.GalleryFragment;
import com.concavenp.artistrymuse.fragments.adapter.ArtistryFragmentPagerAdapter;
import com.concavenp.artistrymuse.services.UserAuthenticationService;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends BaseAppCompatActivity implements
        GalleryFragment.OnCreateProjectInteractionListener,
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

    /**
     * Field used in determining how various UI elements are displayed within a Large or Not Large display
     */
    private boolean mIsLargeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Bind to the UserAuthenticationService
        if (mService == null) {
            Intent intent = new Intent(this, UserAuthenticationService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
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

    /**
     * This method is used to set what providers will be used for account authentication.
     *
     * @return The list of authentication providers
     */
    @MainThread
    private List<AuthUI.IdpConfig> getSelectedProviders() {

        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();

        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());

        // Current, we only want to allow user to verify themselves against an Email address.
        // Future work would include other verification methods that would be enabled here.
        //        selectedProviders.add( new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());
        //        selectedProviders.add( new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        //        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());

        return selectedProviders;

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

                AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            recreate();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                        }
                    }
                });

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

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(findViewById(R.id.coordinatorLayout), errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCreateProjectInteraction(Uri uri) {

        Intent intent = new Intent(this, ProjectEditActivity.class);
        startActivity(intent);

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
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setLogo(R.drawable.ic_muse_logo_1_vector)
                        .setProviders(getSelectedProviders())
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);

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

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

            // Do nothing

        }

    };

}

