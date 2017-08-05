package com.concavenp.artistrymuse;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.MainThread;
import android.support.annotation.StyleRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.concavenp.artistrymuse.fragments.SearchFragment;
import com.concavenp.artistrymuse.fragments.adapter.ArtistryFragmentPagerAdapter;
import com.concavenp.artistrymuse.services.UserAuthenticationService;
import com.firebase.ui.auth.AuthUI;

import java.util.ArrayList;
import java.util.List;

/**
 * References:
 *
 * How to place the Floating Action Button exclusively on one fragment only in Tab layout
 *      - https://stackoverflow.com/questions/30926528/how-to-place-the-floating-action-button-exclusively-on-one-fragment-only-in-tab
 *
 */
public class MainActivity extends BaseAppCompatActivity implements
        UserAuthenticationService.OnAuthenticationListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * URLs for Google specific authentication services explanations to the user
     */
    private static final String GOOGLE_TOS_URL = "https://www.google.com/policies/terms/";
    private static final String GOOGLE_PRIVACY_POLICY_URL = "https://www.google.com/policies/privacy/";

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
     * Value used when referencing results from starting activities via FirebaseUI library.
     */
    private static final int RC_SIGN_IN = 100;

    /**
     * Flag used to indicate if our one time starting/binding of the service has been performed.
     */
    private boolean mBound = false;

    /**
     * The FAB for creating new projects.  This FAB will only be visible in the Gallery fragment.
     */
    private FloatingActionButton fabCreateProject;

    /**
     * Field used in determining how various UI elements are displayed within a Large or Not Large display
     */
    private boolean mIsLargeLayout;

    /**
     * The Shared Preferences key lookup value for identifying the last used tab position.
     */
    private static final String TAB_POSITION = "TAB_POSITION";

    /**
     * Default tab position
     */
    private static final int DEFAULT_TAB_POSITION = 0;

    /**
     * The position of the currently selected tab.  Initialized to the first tab.  This value will
     * be used in saving out the app's Preferences in order to preserve location of where the user
     * left off from either navigating to a different activity or restarting the app.
     */
    private int tabPosition = DEFAULT_TAB_POSITION;

    /**
     * The layout of the tabs.  Used here in a field for the same reason as the tabPosition field.
     */
    private TabLayout tabLayout;

    private ArtistryFragmentPagerAdapter mFragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

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
        mFragmentAdapter = new ArtistryFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mFragmentAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Do nothing
            }

            @Override
            public void onPageSelected(int position) {

                // Save off the tab position
                tabPosition = position;

                // Perform animations (if needed)
                animateFab(position);

                // Close the keyboard if it is open
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Do nothing
            }
        });

        // Give the TabLayout the ViewPager
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                animateFab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });

        // Setup the support for creating a menu (ActionBar functionality)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup the FAB for creating a new user project (only visible in the gallery tab)
        fabCreateProject = (FloatingActionButton) findViewById(R.id.fabCreateProject);
        fabCreateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Notify 'this' of the Create New Project selection
                onInteractionSelection(null, null, StorageDataType.PROJECTS, UserInteractionType.EDIT);

            }
        });

        // Default will be hidden
        fabCreateProject.hide();

    }

    /**
     * Controls the visibility of the FAB button(s) depending on the position of the displayed tab.
     *
     * @param position - The current tab position currently being displayed
     */
    private void animateFab(int position) {
        switch (position) {
            case 0:
                fabCreateProject.hide();
                break;
            case 1:
                fabCreateProject.hide();
                break;
            case 2:
                fabCreateProject.hide();
                break;
            case 3:
                fabCreateProject.show();
                break;
            default:
                fabCreateProject.hide();
                break;
        }
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

                // The user chose to log out
                AuthUI.getInstance().signOut(this);

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
     * notified when the user needs to login into the Firebase service.  The action will be to
     * start a new login activity provided by the FirebaseUI library.
     */
    @Override
    public void onLoginInteraction() {

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(getSelectedTheme())
                        .setLogo(getSelectedLogo())
                        .setAvailableProviders(getSelectedProviders())
                        .setTosUrl(getSelectedTosUrl())
                        .setPrivacyPolicyUrl(getSelectedPrivacyPolicyUrl())
                        .setIsSmartLockEnabled(false)
                        //
                        // Enable smartLocking after more research and testing
                        //.setIsSmartLockEnabled(mEnableCredentialSelector.isChecked(), mEnableHintSelector.isChecked())
                        //
                        .setAllowNewEmailAccounts(true)
                        .build(),
                RC_SIGN_IN);

    }

    @MainThread
    private String getSelectedTosUrl() {
        return GOOGLE_TOS_URL;
    }

    @MainThread
    private String getSelectedPrivacyPolicyUrl() {
        return GOOGLE_PRIVACY_POLICY_URL;
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

    @MainThread
    private List<AuthUI.IdpConfig> getSelectedProviders() {

        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();

        //
        // Enable the following after doing some other research and testing
        //
        // selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        // selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());
        // selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());
        //

        // These will be the available providers
        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());
        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());

        return selectedProviders;

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

    @Override
    protected void onStart() {

        super.onStart();

        // Bind to LocalService
        Intent intent = new Intent(this, UserAuthenticationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // Read in the current tab location from the Shared Preferences and select that tab
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int position = sharedPref.getInt(TAB_POSITION, DEFAULT_TAB_POSITION);
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        tab.select();

    }

    @Override
    protected void onStop() {

        super.onStop();

        // Unbind from the service
        if (mBound) {

            // There should be nobody around to listen now
            mService.clearRegisteredAuthenticationListener();

            unbindService(mConnection);

            mBound = false;

        }

        // Save the current tab location to the Shared Preferences
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(TAB_POSITION, tabPosition);
        editor.commit();

    }

    /**
     * Intercept the signal of when keyboard presses are released.  We are specifically checking for
     * when the Enter key is hit.  If this is the case then we want to check if we are in the
     * Search fragment and signal it of the Enter press if it is.
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {

            case KeyEvent.KEYCODE_ENTER: {

                Class result = mFragmentAdapter.getClassFromPosition(tabPosition);

                if (result == SearchFragment.class) {

                    SearchFragment searchFragment = (SearchFragment)mFragmentAdapter.getItem(tabPosition);

                    searchFragment.onKeyUp();

                }

                return true;

            }
            default: {

                return super.onKeyUp(keyCode, event);

            }

        }

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

            // There should be nobody around to listen now
            mService.clearRegisteredAuthenticationListener();

            mBound = false;

        }

    };

}

