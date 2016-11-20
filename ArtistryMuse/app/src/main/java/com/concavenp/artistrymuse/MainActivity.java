package com.concavenp.artistrymuse;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.concavenp.artistrymuse.fragments.ArtistryFragmentPagerAdapter;
import com.concavenp.artistrymuse.fragments.FavoritesFragment;
import com.concavenp.artistrymuse.fragments.FollowingFragment;
import com.concavenp.artistrymuse.fragments.GalleryFragment;
import com.concavenp.artistrymuse.fragments.SearchFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity implements
        FollowingFragment.OnFragmentInteractionListener,
        FavoritesFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        GalleryFragment.OnFragmentInteractionListener {

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
        viewPager.setAdapter(new ArtistryFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
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

        boolean result = false;

        switch (item.getItemId()) {

            case R.id.action_settings:

                // User chose the "Settings" item, show the app settings UI...

                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                result = true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                result = super.onOptionsItemSelected(item);

        }

        return result;

    }

}
