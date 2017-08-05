package com.concavenp.artistrymuse.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.fragments.adapter.SearchFragmentPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends BaseFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = SearchFragment.class.getSimpleName();

    private EditText mSearchEditText;
    private SearchFragmentPagerAdapter mSearchPagerAdapter;

    // This flipper allows the content of the fragment to show the user either the list search
    // results or a informative message stating that a search needs to be performed to find
    // results.
    private ViewFlipper mFlipper;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment SearchFragment.
     */
    public static SearchFragment newInstance() {

        SearchFragment fragment = new SearchFragment();

        return fragment;

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
        mSearchEditText = (EditText) mainView.findViewById(R.id.search_editText);

        // Save off the flipper for use in deciding which view to show
        mFlipper = (ViewFlipper) mainView.findViewById(R.id.fragment_search_ViewFlipper);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) mainView.findViewById(R.id.search_results_viewpager);
        mSearchPagerAdapter = new SearchFragmentPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(mSearchPagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) mainView.findViewById(R.id.search_tabs);
        tabLayout.setupWithViewPager(viewPager);

        //mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_search_results_Flipper)));
        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_search_nosearch_Flipper)));

        ImageButton button = (ImageButton) mainView.findViewById(R.id.search_imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                performSearch();

            }
        });

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

        // TODO: Update argument type and name
        void onSearchButtonInteraction(String search);

    }

}

