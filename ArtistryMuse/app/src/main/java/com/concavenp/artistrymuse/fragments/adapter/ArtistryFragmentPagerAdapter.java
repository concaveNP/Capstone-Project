package com.concavenp.artistrymuse.fragments.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;

import com.concavenp.artistrymuse.fragments.FavoritesFragment;
import com.concavenp.artistrymuse.fragments.FollowingFragment;
import com.concavenp.artistrymuse.fragments.GalleryFragment;
import com.concavenp.artistrymuse.fragments.SearchFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dave on 11/12/2016.
 */
public class ArtistryFragmentPagerAdapter extends FragmentPagerAdapter {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ArtistryFragmentPagerAdapter.class.getSimpleName();

//    private Map<Integer, Pair<String, Fragment>> mMap = new HashMap<>();

    // TODO: can I use the string values for this?
 //   private String tabTitles[] = new String[] { "Following", "Favorites", "Search", "Gallery" };

//    private class TabClass {
//
//        int mPosition;
//        String mTitle;
//        Fragment mFragment;
//
//        public TabClass(int position, String title, Fragment fragment) {
//            mPosition = position;
//            mTitle = title;
//            mFragment = fragment;
//        }
//
//    }
//
//    private TabClass tabs[] = new TabClass[] {
//       new TabClass(0,)
//    };

    private Pair<String, Fragment> tabs[] = new Pair[] {
            new Pair("Following", FollowingFragment.newInstance()),
            new Pair("Favorites", FavoritesFragment.newInstance()),
            new Pair("Search", SearchFragment.newInstance()),
            new Pair("Gallery", GalleryFragment.newInstance()),
    };

    public ArtistryFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Class getClassFromPosition(int position) {

        Class result;

        switch (position) {
            case 0: {
                result = FollowingFragment.class;
                break;
            }
            case 1: {
                result = FavoritesFragment.class;
                break;
            }
            case 2: {
                result = SearchFragment.class;
                break;
            }
            case 3: {
                result = GalleryFragment.class;
                break;
            }
            default: {
                result = null;
                break;
            }
        }

        return result;

    }

    @Override
    public int getCount() {
        //return tabTitles.length;
        return tabs.length;
    }

    @Override
    public Fragment getItem(int position) {

        return tabs[position].second;
//        Fragment result;
//
//        switch (position) {
//            default:
//            case 0: {
//                //result = FollowingFragment.newInstance();
//                result = tabs[0].second;
//                break;
//            }
//            case 1: {
//                //result = FavoritesFragment.newInstance();
//                result = tabs[1].second;
//                break;
//            }
//            case 2: {
//                //result = SearchFragment.newInstance();
//                result = tabs[2].second;
//                break;
//            }
//            case 3: {
//                //result = GalleryFragment.newInstance();
//                result = tabs[3].second;
//                break;
//            }
//        }
//
//        return result;

    }

    @Override
    public CharSequence getPageTitle(int position) {

        // Generate title based on item position
        return tabs[position].first;
        //return tabTitles[position];

    }

}

