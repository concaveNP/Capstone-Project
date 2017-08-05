package com.concavenp.artistrymuse.fragments.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.fragments.SearchFragment;
import com.concavenp.artistrymuse.fragments.SearchResultFragment;

/**
 * Created by dave on 2/10/2017.
 */
public class SearchFragmentPagerAdapter extends FragmentPagerAdapter implements SearchFragment.OnSearchButtonListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = SearchFragmentPagerAdapter.class.getSimpleName();

    // TODO: can I use the string values for this?
    private String tabTitles[] = new String[] { "Users", "Projects" };

    private SearchResultFragment mUsers;
    private SearchResultFragment mProjects;

    public SearchFragmentPagerAdapter(FragmentManager fm) {

        super(fm);

    }

    @Override
    public int getCount() {

        return tabTitles.length;

    }

    @Override
    public Fragment getItem(int position) {

        Fragment result;

        switch (position) {
            default:
            case 0: {
                if (mUsers == null) {
                    mUsers = SearchResultFragment.newInstance(StorageDataType.USERS);
                }
                result = mUsers;
                break;
            }
            case 1: {
                if (mProjects == null) {
                    mProjects = SearchResultFragment.newInstance(StorageDataType.PROJECTS);
                }
                result = mProjects;
                break;
            }
        }

        return result;

    }

    // Generate title based on item position
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public void onSearchButtonInteraction(String search) {

        Log.i(TAG, "onSearchButtonInteraction has been called for search: " + search);
        Log.i(TAG, "onSearchButtonInteraction: mProjects=" + mProjects + ", mUsers=" + mUsers);

//        if (mProjects != null) {
//            mProjects.onSearchInteraction(search);
//        }
//
//        if (mUsers != null) {
//            mUsers.onSearchInteraction(search);
//        }

        ((SearchResultFragment)getItem(0)).onSearchInteraction(search);
        ((SearchResultFragment)getItem(1)).onSearchInteraction(search);
    }

    /**
     * This interface must be implemented by the fragments created by this adapter in order to
     * signal to the fragments that the user is searching for data.
     */
    public interface OnSearchInteractionListener {
        void onSearchInteraction(String searchString);
    }

}

