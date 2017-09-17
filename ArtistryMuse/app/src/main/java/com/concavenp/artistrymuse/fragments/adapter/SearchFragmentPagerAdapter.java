package com.concavenp.artistrymuse.fragments.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;

import com.concavenp.artistrymuse.R;
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

    /**
     * These are the different search results tabs displayed to the user
     */
    private Pair<String, SearchResultFragment> tabs[];

    public SearchFragmentPagerAdapter(Fragment fragment, FragmentManager fm) {

        super(fm);

        // Initialize the tabs
        tabs = new Pair[] {
                new Pair(fragment.getString(R.string.users_title), SearchResultFragment.newInstance(StorageDataType.USERS)),
                new Pair(fragment.getString(R.string.projects_title), SearchResultFragment.newInstance(StorageDataType.PROJECTS))
        };

    }

    @Override
    public int getCount() {

        return tabs.length;

    }

    @Override
    public Fragment getItem(int position) {

        return tabs[position].second;

    }

    // Generate title based on item position
    @Override
    public CharSequence getPageTitle(int position) {

        return tabs[position].first;

    }

    @Override
    public void onSearchButtonInteraction(String search) {

        for (Pair<String, SearchResultFragment> tab : tabs) {
            tab.second.onSearchInteraction(search);
        }

    }

    /**
     * This interface must be implemented by the fragments created by this adapter in order to
     * signal to the fragments that the user is searching for data.
     */
    public interface OnSearchInteractionListener {

        @SuppressWarnings("unused")
        void onSearchInteraction(String searchString);

    }

}

