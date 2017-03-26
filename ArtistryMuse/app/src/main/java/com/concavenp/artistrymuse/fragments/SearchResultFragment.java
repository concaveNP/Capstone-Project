package com.concavenp.artistrymuse.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.fragments.adapter.SearchFragmentPagerAdapter;
import com.concavenp.artistrymuse.fragments.adapter.SearchResultAdapter;
import com.concavenp.artistrymuse.fragments.viewholder.ProjectResponseViewHolder;
import com.concavenp.artistrymuse.fragments.viewholder.UserResponseViewHolder;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.concavenp.artistrymuse.model.ProjectResponse;
import com.concavenp.artistrymuse.model.ProjectResponseHit;
import com.concavenp.artistrymuse.model.Request;
import com.concavenp.artistrymuse.model.UserResponse;
import com.concavenp.artistrymuse.model.UserResponseHit;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    // TODO: Rename parameter arguments, choose names that match the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TYPE_PARAM = "type";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private StorageDataType mType;

    private SearchResultAdapter<UserResponseHit, UserResponseViewHolder> mUsersAdapter;
    private SearchResultAdapter<ProjectResponseHit, ProjectResponseViewHolder> mProjectsAdapter;
    private RecyclerView mUsersRecycler;
    private RecyclerView mProjectsRecycler;
    private EndlessRecyclerOnScrollListener mUsersScrollListener;
    private EndlessRecyclerOnScrollListener mProjectsScrollListener;

    //private LinearLayoutManager mUsersManager;
    private GridLayoutManager mUsersManager;
    private GridLayoutManager mProjectsManager;
    private ValueEventListener mUsersValueEventListener;
    private ValueEventListener mProjectsValueEventListener;
//    private StaggeredGridLayoutManager mUsersManager;

    private String mSearchText;

    public SearchResultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchResultFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchResultFragment newInstance(String param1, String param2, StorageDataType type) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putInt(TYPE_PARAM, type.ordinal());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mType = StorageDataType.values()[getArguments().getInt(TYPE_PARAM)];

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_search_result, container, false);

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
                mProjectsAdapter = new SearchResultAdapter<>(ProjectResponseViewHolder.class, mDetailsListener, R.layout.item_project);
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
                mUsersAdapter = new SearchResultAdapter<>(UserResponseViewHolder.class, mDetailsListener, R.layout.item_user);
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

        // Add the search request to the database.  The Flashlight service will see this and
        // consume the request and generate a response containing the results of the elasticsearch.
        mDatabase.child("search").child("request").child(requestId.toString()).setValue(request);

        // Listen for the result.
        //
        // NOTE: this cannot be done as a one off due to the unpredictable time nature of
        // the processed response becoming available.
        mUsersValueEventListener = responseQuery.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Check to see if the data is there yet
                if (dataSnapshot.exists()) {

                    // Convert the JSON to Object
                    UserResponse response = dataSnapshot.getValue(UserResponse.class);

                    if ( (response != null) && (response.getHits() != null) ) {

                        if  ((response.getHits().getTotal() > 0) && (response.getHits().getHits() != null)) {

                            // Add the new data
                            mUsersAdapter.add(response.getHits().getHits());

                        }
                        else {

                            Log.e(TAG, "There does not appear to be any results from the search query");

                        }

                    }
                    else {

                        Log.e(TAG, "Expected response from search query was null");

                    }

                }
                else {

                    Log.e(TAG, "There is no data in the snapshot");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w(TAG, databaseError.toString());

            }

        });

    }

    /**
     * Performs the work of re-querying the cloud services for data to be displayed.  An adapter
     * is used to translate the data retrieved into the populated displayed view.
     */
    private void projectsSearch(int dataPosition) {

        UUID requestId = UUID.randomUUID();

        // Build the query to be used
        final Query responseQuery = getResponseQuery(mDatabase, requestId);

        // Create the JSON request object that will be placed into the database
        Request request = new Request("firebase", mSearchText, "project", dataPosition*10);

        // Add the search request to the database.  The Flashlight service will see this and
        // consume the request and generate a response containing the results of the elasticsearch.
        mDatabase.child("search").child("request").child(requestId.toString()).setValue(request);

        // Listen for the result.
        //
        // NOTE: this cannot be done as a one off due to the unpredictable time nature of
        // the processed response becoming available.
        mProjectsValueEventListener = responseQuery.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Check to see if the data is there yet
                if (dataSnapshot.exists()) {

                    // Convert the JSON to Object
                    ProjectResponse response = dataSnapshot.getValue(ProjectResponse.class);

                    if ( (response != null) && (response.getHits() != null) ) {

                        if  ((response.getHits().getTotal() > 0) && (response.getHits().getHits() != null)) {

                            // Add the new data
                            mProjectsAdapter.add(response.getHits().getHits());

                        }
                        else {

                            Log.e(TAG, "There does not appear to be any results from the search query");

                        }

                    }
                    else {

                        Log.e(TAG, "Expected response from search query was null");

                    }

                }
                else {

                    Log.e(TAG, "There is no data in the snapshot");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w(TAG, databaseError.toString());

            }

        });

    }

    private Query getResponseQuery(DatabaseReference databaseReference, UUID uuid) {

        String myUserId = getUid();

        Query myTopPostsQuery = databaseReference.child("search").child("response").child(uuid.toString());

        return myTopPostsQuery;

    }

    @Override
    public void onSearchInteraction(String searchString) {

       mSearchText = searchString;

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

