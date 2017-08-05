package com.concavenp.artistrymuse.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.fragments.adapter.SearchFragmentPagerAdapter;
import com.concavenp.artistrymuse.fragments.adapter.SearchResultAdapter;
import com.concavenp.artistrymuse.fragments.viewholder.ProjectResponseViewHolder;
import com.concavenp.artistrymuse.fragments.viewholder.UserResponseViewHolder;
import com.concavenp.artistrymuse.model.ProjectResponse;
import com.concavenp.artistrymuse.model.ProjectResponseHit;
import com.concavenp.artistrymuse.model.Request;
import com.concavenp.artistrymuse.model.UserResponse;
import com.concavenp.artistrymuse.model.UserResponseHit;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchResultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchResultFragment extends BaseFragment implements SearchFragmentPagerAdapter.OnSearchInteractionListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = SearchResultFragment.class.getSimpleName();

    // Parameter arguments, these are names that match the fragment initialization parameters
    private static final String TYPE_PARAM = "type";

    // Types of parameters
    private StorageDataType mType;

    private SearchResultAdapter<UserResponseHit, UserResponseViewHolder> mUsersAdapter;
    private SearchResultAdapter<ProjectResponseHit, ProjectResponseViewHolder> mProjectsAdapter;
    private RecyclerView mUsersRecycler;
    private RecyclerView mProjectsRecycler;
    private EndlessRecyclerOnScrollListener mUsersScrollListener;
    private EndlessRecyclerOnScrollListener mProjectsScrollListener;

    private GridLayoutManager mUsersManager;
    private GridLayoutManager mProjectsManager;
    private ValueEventListener mUsersValueEventListener;
    private ChildEventListener mUsersChildEventListener;
    private DataSnapshot mUsersDataSnapshot;
    private ValueEventListener mProjectsValueEventListener;
    private ChildEventListener mProjectsChildEventListener;
    private DataSnapshot mProjectsDataSnapshot;

    private String mSearchText;

    // This flipper allows the content of the fragment to show the user either the list search
    // results or a informative message stating that a search needs to be performed to find
    // results.
    private ViewFlipper mFlipper;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param type - The type of search result
     * @return A new instance of fragment SearchResultFragment.
     */
    public static SearchResultFragment newInstance(StorageDataType type) {

        SearchResultFragment fragment = new SearchResultFragment();

        Bundle args = new Bundle();
        args.putInt(TYPE_PARAM, type.ordinal());
        fragment.setArguments(args);

        return fragment;

    }

    /**
     * Required empty public constructor
     */
    public SearchResultFragment() {

        // Do nothing

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            mType = StorageDataType.values()[getArguments().getInt(TYPE_PARAM)];

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_search_result, container, false);

        // Save off the flipper for use in deciding which view to show
        mFlipper = (ViewFlipper) mainView.findViewById(R.id.fragment_search_ViewFlipper);

        // The widgets that will "view" the search result data contained within their corresponding adapters
        switch (mType) {
            case PROJECTS: {
                mProjectsRecycler = (RecyclerView) mainView.findViewById(R.id.search_recycler_view);

                // Use this setting to improve performance if you know that changes in content do not change the layout size of the RecyclerView
                mProjectsRecycler.setHasFixedSize(true);

                int columnCount = getResources().getInteger(R.integer.list_column_count);

                mProjectsManager = new GridLayoutManager(getContext(), columnCount);
                mProjectsRecycler.setLayoutManager(mProjectsManager);

                // Create the adapter that will be used to hold and paginate through the resulting search data
                mProjectsAdapter = new SearchResultAdapter<>(ProjectResponseViewHolder.class, mInteractionListener, R.layout.item_project);
                mProjectsAdapter.clearData();
                mProjectsRecycler.setAdapter(mProjectsAdapter);

                // Setup the endless scrolling
                mProjectsScrollListener = new EndlessRecyclerOnScrollListener(mProjectsManager) {
                    @Override
                    public void onLoadMore(int currentPage) {

                        // Log that we are doing another search of data on a different "page"
                        Log.i(TAG, "Searching for more paginated data from position: " + (currentPage*10));

                        // Check that listener for the previous search results is removed
                        if (mProjectsValueEventListener != null) {
                            Log.i(TAG, "Search listener removed");
                            mDatabase.removeEventListener(mProjectsValueEventListener);
                        }

                        // Clear the child listener from the previous search
                        if (mProjectsChildEventListener != null) {
                            Log.i(TAG, "child Search listener removed");
                            mDatabase.removeEventListener(mProjectsChildEventListener );
                        }

                        // Clear any data that saved from the last search
                        mProjectsDataSnapshot = null;

                        // Get the data
                        projectsSearch(currentPage);

                    }
                };
                mProjectsScrollListener.initValues();
                mProjectsRecycler.addOnScrollListener(mProjectsScrollListener);

                break;
            }
            case USERS: {
                mUsersRecycler = (RecyclerView) mainView.findViewById(R.id.search_recycler_view);

                // Use this setting to improve performance if you know that changes in content do not change the layout size of the RecyclerView
                mUsersRecycler.setHasFixedSize(true);

                int columnCount = getResources().getInteger(R.integer.list_column_count);

                mUsersManager = new GridLayoutManager(getContext(), columnCount);
                mUsersRecycler.setLayoutManager(mUsersManager);

                // Create the adapter that will be used to hold and paginate through the resulting search data
                mUsersAdapter = new SearchResultAdapter<>(UserResponseViewHolder.class, mInteractionListener, R.layout.item_user);
                mUsersAdapter.clearData();
                mUsersRecycler.setAdapter(mUsersAdapter);

                // Setup the endless scrolling
                mUsersScrollListener = new EndlessRecyclerOnScrollListener(mUsersManager) {
                    @Override
                    public void onLoadMore(int currentPage) {

                        // Log that we are doing another search of data on a different "page"
                        Log.i(TAG, "Searching for more paginated data from position: " + (currentPage*10));

                        // Check that listener for the previous search results is removed
                        if (mUsersValueEventListener != null) {
                            Log.i(TAG, "Search listener removed");
                            mDatabase.removeEventListener(mUsersValueEventListener);
                        }

                        // Clear the child listener from the previous search
                        if (mUsersChildEventListener != null) {
                            Log.i(TAG, "child Search listener removed");
                            mDatabase.removeEventListener(mUsersChildEventListener );
                        }

                        // Clear any data that saved from the last search
                        mUsersDataSnapshot = null;

                        // Get the data
                        usersSearch(currentPage);

                    }
                };
                mUsersScrollListener.initValues();
                mUsersRecycler.addOnScrollListener(mUsersScrollListener);

                break;
            }
        }

        return mainView;

    }




    // remove this one - keep the other...



    /**
     * Performs the work of re-querying the cloud services for data to be displayed.  An adapter
     * is used to translate the data retrieved into the populated displayed view.
     */
    private void usersSearch(int dataPosition) {

        UUID requestId = UUID.randomUUID();

        // Build the query to be used
        final Query responseQuery = getResponseQuery(mDatabase, requestId);

        // Create the JSON request object that will be placed into the database
        Request request = new Request("firebase", mSearchText, "user", dataPosition*10);

        mUsersChildEventListener = responseQuery.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Log.d(TAG, mType + " - Here is the added child added: " + s);

                // This should be null - meaning that that the child has been created and it is time to query for the data
                if (s == null) {

                    // We no longer need this child listener
                    mDatabase.removeEventListener(mUsersChildEventListener);

                    // Listen for the result.
                    //
                    // NOTE: this cannot be done as a one off due to the unpredictable time nature of
                    // the processed response becoming available.
                    //mUsersValueEventListener = responseQuery.addValueEventListener(new ValueEventListener() {
                    responseQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Log.d(TAG, mType + "-Here is the data for the added child added");

                            // Check to see if the data is there yet
                            if (dataSnapshot.exists()) {

                                Log.d(TAG, mType + "-the data does exist");

                                // Save the data
                                mUsersDataSnapshot = dataSnapshot;

                                // Convert the JSON to Object
                                UserResponse response = dataSnapshot.getValue(UserResponse.class);

                                if ( (response != null) && (response.getHits() != null) ) {

                                    if  ((response.getHits().getTotal() > 0) && (response.getHits().getHits() != null)) {

                                        // We are now performing a search, flip control to the individual fragments of the TabLayout
                                        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.search_recycler_view)));

                                        // Add the new data
                                        mUsersAdapter.add(response.getHits().getHits());

                                    }
                                    else {

                                        Log.e(TAG, "There does not appear to be any results from the search query");

                                        // We are now performing a search, flip control to the individual fragments of the TabLayout
                                        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_search_no_results_Flipper)));

                                    }

                                }
                                else {

                                    Log.e(TAG, "Expected response from search query was null");

                                    // We are now performing a search, flip control to the individual fragments of the TabLayout
                                    mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_search_error_Flipper)));

                                }

                            }
                            else {

                                Log.e(TAG, "There is no data in the snapshot");

                                // We are now performing a search, flip control to the individual fragments of the TabLayout
                                mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_search_no_data_Flipper)));

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Do nothing
                        }

                    });

                } else {

                    Log.d(TAG, mType + " - is not null: " + s);

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                // Do nothing

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                // Do nothing

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                // Do nothing

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Do nothing

            }

        });

        // Add the search request to the database.  The Flashlight service will see this and
        // consume the request and generate a response containing the results of the elasticsearch.
        mDatabase.child("search").child("request").child(requestId.toString()).setValue(request);

    }

    /**
     * Performs the work of re-querying the cloud services for data to be displayed.  An adapter
     * is used to translate the data retrieved into the populated displayed view.
     */
    private void projectsSearch(final int dataPosition) {

        UUID requestId = UUID.randomUUID();

        // Build the query to be used
        final Query responseQuery = getResponseQuery(mDatabase, requestId);

        // Create the JSON request object that will be placed into the database
        Request request = new Request("firebase", mSearchText, "project", dataPosition*10);

        Log.d(TAG, "Here is the query that will be used: " + responseQuery);

        // It took a while to get this right.  So, I'll put in some words for what's going on here.
        // When performing a search this App will create a new DB entry within the "search" table.
        // A Heroku hosted Node application is monitoring this "search" table for the entries
        // created here.  The entries and processed with the results placed in the same table using
        // the the request UID as the lookup key.  This application is simply waiting for the
        // result entry node within the DB to be created.  This can take some time given several
        // different variables to the situation.  So, when the DB node is detected it will then
        // query for the Value of the node.  Turns out just listening for the Value of the node
        // has bugs within the Firebase codebase.  I know this because Google has contacted me
        // regarding the bugs I've exposed.  (YEH ME!)  The bug in question appears to be the result
        // of not waiting until the writing of data within the DB in complete before issued a message
        // out the listeners of the data (this App) that it is complete.  The result is that this
        // App would get errors back the DB query because it was only ever partially finished.
        mProjectsChildEventListener = responseQuery.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String childNode) {

                Log.d(TAG, mType + " - Here is the added child added: " + childNode);

                // This should be null - meaning that that the child has been created and it is time to query for the data
                if (childNode == null) {

                    // We no longer need this child listener
                    mDatabase.removeEventListener(mProjectsChildEventListener);

                    // Listen for the result.
                    //
                    // NOTE: this cannot be done as a one off due to the unpredictable time nature of
                    // the processed response becoming available.
                    //mProjectsValueEventListener = responseQuery.addValueEventListener(new ValueEventListener() {
                    responseQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Log.d(TAG, mType + "-Here is the data for the added child added");

                            // Check to see if the data is there yet
                            if (dataSnapshot.exists()) {

                                Log.d(TAG, mType + "-the data does exist");

                                // Save the data
                                mProjectsDataSnapshot = dataSnapshot;

                                // Convert the JSON to Object
                                ProjectResponse response = dataSnapshot.getValue(ProjectResponse.class);

                                if ( (response != null) && (response.getHits() != null) ) {

                                    if  ((response.getHits().getTotal() > 0) && (response.getHits().getHits() != null)) {

                                        // We are now performing a search, flip control to the individual fragments of the TabLayout
                                        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.search_recycler_view)));

                                        // Add the new data
                                        mProjectsAdapter.add(response.getHits().getHits());

                                    }
                                    else {

                                        Log.d(TAG, "There does not appear to be any results from the search query");

                                        // We are now performing a search, flip control to the individual fragments of the TabLayout
                                        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_search_no_results_Flipper)));

                                    }

                                }
                                else {

                                    Log.e(TAG, "Expected response from search query was null");

                                    // We are now performing a search, flip control to the individual fragments of the TabLayout
                                    mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_search_error_Flipper)));

                                }

                            }
                            else {

                                Log.e(TAG, "There is no data in the snapshot");

                                // We are now performing a search, flip control to the individual fragments of the TabLayout
                                mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_search_no_data_Flipper)));

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                            // I have not yet seen this occur yet
                            Log.e(TAG, "The Value query for the search results has encountered an errorth: " + databaseError);

                        }

                    });

                } else {

                    // Skip, we only care about the creation of the node which will result in
                    // a null value, so do nothing.

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                // Do nothing

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                // Do nothing

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                // Do nothing

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Do nothing

            }

        });

        // Add the search request to the database.  The Flashlight (this is a Heroku hosted
        // Node.js Application along with an instance of Elasticsearch) service will see this and
        // consume the request and generate a response containing the results of the Elasticsearch.
        mDatabase.child("search").child("request").child(requestId.toString()).setValue(request);

    }

    private Query getResponseQuery(DatabaseReference databaseReference, UUID uuid) {

        Query myTopPostsQuery = databaseReference.child("search").child("response").child(uuid.toString());

        return myTopPostsQuery;

    }

    @Override
    public void onSearchInteraction(String searchString) {

        mSearchText = searchString;

        // We are now performing a search, flip control to the individual fragments of the TabLayout
        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_search_searching_Flipper)));

        // The widgets that will "view" the search result data contained within their corresponding adapters
        switch (mType) {

            case PROJECTS: {

                // Log that we are doing another search of data on a different "page"
                Log.i(TAG, "Searching for more paginated data on page: " + 0);

                // Clear any results that are being stored within the adapter scroll listener
                mProjectsAdapter.clearData();
                mProjectsScrollListener.initValues();

                // Check that listener for the previous search results is removed
                if (mProjectsValueEventListener != null) {
                    Log.i(TAG, "Search listener removed");
                    mDatabase.removeEventListener(mProjectsValueEventListener);
                }

                // Perform a search and display the data
                projectsSearch(0);

                break;

            }
            case USERS: {

                // Log that we are doing another search of data on a different "page"
                Log.i(TAG, "Searching for more paginated data on page: " + 0);

                // Clear any results that are being stored within the adapter scroll listener
                mUsersAdapter.clearData();
                mUsersScrollListener.initValues();

                // Check that listener for the previous search results is removed
                if (mUsersValueEventListener != null) {
                    Log.i(TAG, "Search listener removed");
                    mDatabase.removeEventListener(mUsersValueEventListener);
                }

                // Perform a search and display the data
                usersSearch(0);

                break;

            }

        }

    }

}

