package com.concavenp.artistrymuse;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by dave on 11/12/2016.
 */
public class ArtistryFragmentPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 4;

    // TODO: can I use the string values for this?
    private String tabTitles[] = new String[] { "Following", "Favorites", "Search", "Gallery" };
    private Context context;

    public ArtistryFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
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
