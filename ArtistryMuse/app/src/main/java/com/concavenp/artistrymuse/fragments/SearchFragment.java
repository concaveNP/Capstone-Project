package com.concavenp.artistrymuse.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

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

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) mainView.findViewById(R.id.search_results_viewpager);
        mSearchPagerAdapter = new SearchFragmentPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(mSearchPagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) mainView.findViewById(R.id.search_tabs);
        tabLayout.setupWithViewPager(viewPager);

        ImageButton button = (ImageButton) mainView.findViewById(R.id.search_imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSearchPagerAdapter.onSearchButtonInteraction(mSearchEditText.getText().toString());

            }
        });

        return mainView;

    }

    public interface OnSearchButtonListener {

        // TODO: Update argument type and name
        void onSearchButtonInteraction(String search);

    }

}

