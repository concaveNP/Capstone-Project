package com.concavenp.artistrymuse.fragments.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.concavenp.artistrymuse.fragments.FavoritesFragment;
import com.concavenp.artistrymuse.fragments.FollowingFragment;
import com.concavenp.artistrymuse.fragments.GalleryFragment;
import com.concavenp.artistrymuse.fragments.SearchFragment;

/**
 * Created by dave on 11/12/2016.
 */
public class ArtistryFragmentPagerAdapter extends FragmentPagerAdapter {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ArtistryFragmentPagerAdapter.class.getSimpleName();

    // TODO: can I use the string values for this?
    private String tabTitles[] = new String[] { "Following", "Favorites", "Search", "Gallery" };

    public ArtistryFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment result = null;

        // TODO: params to the fragments?  Needed?
        switch (position) {
            default:
            case 0: {
                result = FollowingFragment.newInstance("","");
                break;
            }
            case 1: {
                result = FavoritesFragment.newInstance("","");
                break;
            }
            case 2: {
                result = SearchFragment.newInstance("","");
                break;
            }
            case 3: {
                result = GalleryFragment.newInstance("","");
                break;
            }
        }

        return result;

    }

    @Override
    public CharSequence getPageTitle(int position) {

        // Generate title based on item position
        return tabTitles[position];

    }

}

