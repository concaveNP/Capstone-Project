package com.concavenp.artistrymuse.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.fragments.adapter.SearchResultAdapter;
import com.concavenp.artistrymuse.fragments.viewholder.UserResponseViewHolder;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
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
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = SearchFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private OnDetailsInteractionListener mDetailsListener;

    private DatabaseReference mDatabase;
    private SearchResultAdapter<UserResponseHit, UserResponseViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private EndlessRecyclerOnScrollListener mScrollListener;

    //private LinearLayoutManager mManager;
    private GridLayoutManager mManager;
//    private StaggeredGridLayoutManager mManager;


    private EditText mSearchEditText;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }

        // Establish a connection the database
        mDatabase = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_search, container, false);

        mSearchEditText = (EditText) mainView.findViewById(R.id.search_editText);

        mRecycler = (RecyclerView) mainView.findViewById(R.id.search_recycler_view);

        // Use this setting to improve performance if you know that changes in content do not change the layout size of the RecyclerView
        mRecycler.setHasFixedSize(true);

        // Setup Layout Manager, reverse layout
//        mManager = new LinearLayoutManager(getActivity());
//        mManager.setReverseLayout(true);
//        mManager.setStackFromEnd(true);

        int columnCount = getResources().getInteger(R.integer.list_column_count);
        mManager = new GridLayoutManager(getContext(), columnCount);
        mRecycler.setLayoutManager(mManager);

// TODO: see issue (EndlessRecyclerOnScrollListener needs to support StaggeredGridLayoutManager #34) https://github.com/concaveNP/Capstone-Project/issues/34

//        int columnCount = getResources().getInteger(R.integer.list_column_count);
//        mManager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
//        mRecycler.setLayoutManager(mManager);

        // Create the adapter that will be used to hold and paginate through the resulting search data
        mAdapter = new SearchResultAdapter<>(UserResponseViewHolder.class, mDetailsListener, R.layout.item_user);
        mAdapter.clearData();
        mRecycler.setAdapter(mAdapter);

        // Setup the endless scrolling
        mScrollListener = new EndlessRecyclerOnScrollListener(mManager) {
            @Override
            public void onLoadMore(int currentPage) {

                // Log that we are doing another search of data on a different "page"
                Log.i(TAG, "Searching for more paginated data from position: " + (currentPage*10));

                // Get the data
                search(currentPage);

            }
        };
        mScrollListener.initValues();
        mRecycler.addOnScrollListener(mScrollListener);

        ImageButton button = (ImageButton) mainView.findViewById(R.id.search_imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Log that we are doing another search of data on a different "page"
                Log.i(TAG, "Searching for more paginated data on page: " + 0);

                // Clear any results that are being stored within the adapter scroll listener
                mAdapter.clearData();
                mScrollListener.initValues();

                // Perform a search and display the data
                search(0);

            }
        });

        return mainView;

    }

    /**
     * Performs the work of re-querying the cloud services for data to be displayed.  An adapter
     * is used to translate the data retrieved into the populated displayed view.
     */
    private void search(int dataPosition) {

        UUID requestId = UUID.randomUUID();

        // Set up FirebaseRecyclerAdapter with the Query
        final Query postsQuery = getQuery(mDatabase, requestId);

        Log.i(TAG, postsQuery.toString());

        Request request = new Request("firebase", mSearchEditText.getText().toString(), "user", dataPosition*10);
        mDatabase.child("search").child("request").child(requestId.toString()).setValue(request);

        new Thread(new Runnable() {
            public void run() {


                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // Convert the JSON to Object
                        //Hits response = dataSnapshot.getValue(Hits.class);
                        UserResponse response = dataSnapshot.getValue(UserResponse.class);
                        //List<UserResponseHit> response = dataSnapshot.getValue(ArrayList<UserResponseHit.class>.getClass());

                        if ( (response != null) && (response.getHits() != null) ) {

                            if  ((response.getHits().getTotal() > 0) && (response.getHits().getHits() != null)) {

                                Log.i(TAG, response.toString());
                                Log.i(TAG, "Items found: " + response.getHits().getTotal());

                                // Add the new data
                                mAdapter.add(response.getHits().getHits());

                            }
                            else {
                                Log.e(TAG, "There does not appear to be any results from the search query");
                            }

                        }
                        else {
                            Log.e(TAG, "Expected response from search query was null");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Log.i(TAG, databaseError.toString());

                    }

                });



            }
        }).start();





    }

    private Query getQuery(DatabaseReference databaseReference, UUID uuid) {

        String myUserId = getUid();

        Query myTopPostsQuery = databaseReference.child("search").child("response").child(uuid.toString());

        return myTopPostsQuery;

    }

    private String getUid() {

//        return FirebaseAuth.getInstance().getCurrentUser().getUid();

        // TODO: this will need to be figured out some other way and probably/maybe saved to local properties
        // must use the authUid (this is the getUid() call) to get the uid to be the DB primary key index to use as the myUserId value in the query - yuck, i'm doing this wrong

        // TODO: should not be hard coded
        //return "2a1d3365-118d-4dd7-9803-947a7103c730";
        //return "8338c7c0-e6b9-4432-8461-f7047b262fbc";
        //return "d0fc4662-30b3-4e87-97b0-d78e8882a518";
        //return "54d1e146-a114-45ea-ab66-389f5fd53e53";
        return "0045d757-6cac-4a69-81e3-0952a3439a78";

    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        // Re-attach to the parent Activity interface
        if (context instanceof OnFragmentInteractionListener) {

            mListener = (OnFragmentInteractionListener) context;

        } else {

            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");

        }

        // Re-attach to the parent Activity interface
        if (context instanceof OnDetailsInteractionListener) {

            mDetailsListener = (OnDetailsInteractionListener) context;

        } else {

            throw new RuntimeException(context.toString() + " must implement OnDetailsInteractionListener");

        }

    }

    @Override
    public void onDetach() {

        super.onDetach();

        // Detach from the parent Activity interface(s)
        mListener = null;
        mDetailsListener = null;

     }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
