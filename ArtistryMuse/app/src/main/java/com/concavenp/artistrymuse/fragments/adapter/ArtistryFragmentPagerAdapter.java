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

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;

import com.concavenp.artistrymuse.R;
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

    private Pair<String, Pair<Class, Fragment>> tabs[];

    public ArtistryFragmentPagerAdapter(Activity activity, FragmentManager fm) {

        super(fm);

        // Initialize the tabs
        tabs = new Pair[] {
                new Pair(activity.getString(R.string.main_tab_following), new Pair(FollowingFragment.class, FollowingFragment.newInstance())),
                new Pair(activity.getString(R.string.main_tab_favorites), new Pair(FavoritesFragment.class, FavoritesFragment.newInstance())),
                new Pair(activity.getString(R.string.main_tab_search), new Pair(SearchFragment.class, SearchFragment.newInstance())),
                new Pair(activity.getString(R.string.main_tab_gallery), new Pair(GalleryFragment.class, GalleryFragment.newInstance()))
        };

    }

    public Class getClassFromPosition(int position) {

        return tabs[position].second.first;

    }

    @Override
    public int getCount() {

        return tabs.length;

    }

    @Override
    public Fragment getItem(int position) {

        return tabs[position].second.second;

    }

    @Override
    public CharSequence getPageTitle(int position) {

        return tabs[position].first;

    }

}

