/*
 * ArtistryMuse is an application that allows artist to share projects
 * they have created along with the inspirations behind them for others to
 * discover and enjoy.
 * Copyright (C) 2017  David A. Todd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.concavenp.artistrymuse.fragments.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import android.view.ViewGroup;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.fragments.SearchFragment;
import com.concavenp.artistrymuse.fragments.SearchResultFragment;

/**
 * Created by dave on 2/10/2017.
 *
 * References:
 *
 * Android ViewPager, FragmentPagerAdapter and orientation changes
 *      - https://medium.com/@roideuniverse/android-viewpager-fragmentpageradapter-and-orientation-changes-256c23bee035
 * FragmentPagerAdapter - How to handle Orientation Changes?
 *      - https://stackoverflow.com/questions/17629463/fragmentpageradapter-how-to-handle-orientation-changes
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
    private Pair<String, SearchResultFragment> tabs[] = new Pair[] {null,null};

    public SearchFragmentPagerAdapter(Fragment fragment, FragmentManager fm) {

        super(fm);

        // Initialize the tabs
        tabs = new Pair[] {
                new Pair(fragment.getString(R.string.users_title), SearchResultFragment.newInstance(StorageDataType.USERS)),
                new Pair(fragment.getString(R.string.projects_title), SearchResultFragment.newInstance(StorageDataType.PROJECTS))
        };

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        SearchResultFragment fragment = (SearchResultFragment ) super.instantiateItem(container, position);
        tabs[position] = new Pair(tabs[position].first, fragment);
        return fragment;

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

