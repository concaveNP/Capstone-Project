package com.concavenp.artistrymuse;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.concavenp.artistrymuse.fragments.SearchResultFragment;
import com.concavenp.artistrymuse.fragments.adapter.ArtistryFragmentPagerAdapter;
import com.concavenp.artistrymuse.fragments.FavoritesFragment;
import com.concavenp.artistrymuse.fragments.FollowingFragment;
import com.concavenp.artistrymuse.fragments.GalleryFragment;
import com.concavenp.artistrymuse.fragments.SearchFragment;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity implements
        OnDetailsInteractionListener,
        FollowingFragment.OnFragmentInteractionListener,
        FavoritesFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        GalleryFragment.OnFragmentInteractionListener,
        GalleryFragment.OnCreateProjectInteractionListener,
        SearchResultFragment.OnFragmentInteractionListener
{

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();


        // Start the login Activity if needed
        if (auth.getCurrentUser() == null) {

            // TODO: what is RC_SING_IN used for
            // TODO: the SVG is not apparently going to work here, need an png export of the logo
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                            .setLogo(R.drawable.ic_muse_logo_1_vector)
                            .setProviders(getSelectedProviders())
                            .setIsSmartLockEnabled(true)
                            .build(),
                    RC_SIGN_IN);

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

    @MainThread
    private List<AuthUI.IdpConfig> getSelectedProviders() {
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();

        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
        selectedProviders.add( new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER) .build());
        selectedProviders.add( new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());

        return selectedProviders;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
       // TODO: determine if this needs to be here, this is used by all of the fragments for interaction.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean result;

        switch (item.getItemId()) {

            case R.id.action_settings: {

                // User chose the "Settings" item, show the app settings UI...

                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

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

    /**
     * The purpose of this interface implementation is to start the Details Activity of either a
     * user or a project.  The point to making the Main Activity implement is to support both the
     * phone and tablet layout of the app.  Phone layouts will just start a new activity and
     * tablet layouts will populate a neighboring fragment with the details results.
     *
     * @param uid - This will be the UID of other the User or the Project as specified in the type param
     * @param type - The type will either be a user or a project
     */
    @Override
    public void onDetailsSelection(String uid, StorageDataType type) {

        switch(type) {

            case PROJECTS: {

                // Create and start the details activity along with passing it the Movie Item details information via JSON string
                Intent intent = new Intent(this, DetailsActivity.class);
                // intent.putExtra(DetailsActivity.EXTRA_DATA, json);
                startActivity(intent);

                break;
            }
            case USERS: {

                // Create and start the details activity along with passing it the Movie Item details information via JSON string
                Intent intent = new Intent(this, UserDetailsActivity.class);
                // intent.putExtra(DetailsActivity.EXTRA_DATA, json);
                startActivity(intent);

                break;

            }
            default: {
                // TODO: log an error and whatnot
            }

        }

        // TODO: support the phone and tablet layout, for now it is just phone

//        if (mPhoneLayout) {
//
//            // Convert the GSON object back to a JSON string in order to pass to the activity
//            Gson gson = new Gson();
//            String json = gson.toJson(item);
//
//            // Create and start the details activity along with passing it the Movie Item details information via JSON string
//            Intent intent = new Intent(this, MovieDetailsActivity.class);
//            intent.putExtra(MovieDetailsActivity.EXTRA_DATA, json);
//            startActivity(intent);
//
//        } else {
//
//            MovieDetailsFragment fragment = (MovieDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.movie_details_fragment);
//            fragment.updateMovieDetailInfo(item);
//
//        }

    }

    @Override
    public void onCreateProjectInteraction(Uri uri) {

        // TODO: implement the details activity(s) for both type
        Intent intent = new Intent(this, CreateProjectActivity.class);
        startActivity(intent);

    }
}
