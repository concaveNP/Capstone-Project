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

package com.concavenp.artistrymuse.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.fragments.adapter.SearchFragmentPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * References:
 *
 * How to set edittext to show search button or enter button on keyboard?
 *      - https://stackoverflow.com/questions/6529485/how-to-set-edittext-to-show-search-button-or-enter-button-on-keyboard
 * Setting onClickListner for the Drawable right of an EditText [duplicate]
 *      - https://stackoverflow.com/questions/13135447/setting-onclicklistner-for-the-drawable-right-of-an-edittext
 * How to add icon inside EditText view in Android ?
 *      - https://stackoverflow.com/questions/4281749/how-to-add-icon-inside-edittext-view-in-android
 * android determine if device is in right to left language/layout
 *      - https://stackoverflow.com/questions/26549354/android-determine-if-device-is-in-right-to-left-language-layout
 */
public class SearchFragment extends BaseFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = SearchFragment.class.getSimpleName();

    private EditText mSearchEditText;
    private SearchFragmentPagerAdapter mSearchPagerAdapter;

    /**
     * This flipper allows the content of the fragment to show the user either the list search
     * results or a informative message stating that a search needs to be performed to find results.
     */
    private ViewFlipper mFlipper;
    /**
     * The Shared Preferences key lookup value for identifying the last used tab position.
     */
    private static final String SEARCH_STRING = "SEARCH_STRING";

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment SearchFragment.
     */
    public static SearchFragment newInstance() {

        return new SearchFragment();

    }

    /**
     * Required empty public constructor
     */
    public SearchFragment() {

        // Do nothing

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_search, container, false);

        // The search text the user will input
        mSearchEditText = mainView.findViewById(R.id.search_editText);
        mSearchEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if ((actionId == EditorInfo.IME_ACTION_SEARCH) || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){

                    performSearch();

                    return true;

                }

                return false;
            }

        });
        mSearchEditText.setOnTouchListener(new View.OnTouchListener() {

            @SuppressWarnings("unused")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {

                    // Check the layout direction to support LTR or RTL
                    if (ViewCompat.getLayoutDirection(mSearchEditText) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                        if(event.getRawX() >= (mSearchEditText.getRight() - mSearchEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                            performSearch();

                            return true;
                        }
                    }
                    else {
                        if(event.getRawX() <= (mSearchEditText.getLeft() + mSearchEditText.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {

                            performSearch();

                            return true;
                        }
                    }

                }

                return false;
            }

        });

        // Save off the flipper for use in deciding which view to show
        mFlipper = mainView.findViewById(R.id.fragment_search_ViewFlipper);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager mViewPager = mainView.findViewById(R.id.search_results_viewpager);
        mSearchPagerAdapter = new SearchFragmentPagerAdapter(this, getChildFragmentManager());
        mViewPager.setAdapter(mSearchPagerAdapter);
        mViewPager.setOffscreenPageLimit(mSearchPagerAdapter.getCount());

        // Give the TabLayout the ViewPager
        TabLayout mTabLayout = mainView.findViewById(R.id.search_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_search_no_search_Flipper)));

        return mainView;

    }

    /**
     * Called when the owning Activity detects a key press that occurred during this Fragment.
     */
    public void onKeyUp() {

        performSearch();

    }

    /**
     * This method will close the soft keyboard, flip the fragment to displaying the SearchResultFragment
     * and will perform the search given the text within the search box.
     */
    private void performSearch() {

        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // We are now performing a search, flip control to the individual fragments of the TabLayout
        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_search_results_Flipper)));

        // Perform the search
        mSearchPagerAdapter.onSearchButtonInteraction(mSearchEditText.getText().toString());

    }

    public interface OnSearchButtonListener {

        @SuppressWarnings("unused")
        void onSearchButtonInteraction(String search);

    }

    @Override
    public void onStart() {

        super.onStart();

        // Read in the current tab location from the Shared Preferences and select that tab
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String searchString = sharedPref.getString(SEARCH_STRING, "");
        mSearchEditText.setText(searchString);

    }

    @Override
    public void onStop() {

        super.onStop();

        // Save the current tab location to the Shared Preferences
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SEARCH_STRING, mSearchEditText.getText().toString());
        editor.apply();

    }

}

