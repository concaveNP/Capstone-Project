package com.concavenp.artistrymuse.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.fragments.viewholder.GalleryViewHolder;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GalleryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryFragment extends Fragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = GalleryFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private OnDetailsInteractionListener mDetailsListener;
    private OnCreateProjectInteractionListener mCreateProjectListener;

    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<String, GalleryViewHolder> mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecycler;

    public GalleryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GalleryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GalleryFragment newInstance(String param1, String param2) {

        GalleryFragment fragment = new GalleryFragment();
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
        View mainView = inflater.inflate(R.layout.fragment_gallery, container, false);

        FloatingActionButton fab = (FloatingActionButton) mainView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Notify the the listener (aka MainActivity) of the Create New Project selection
                mCreateProjectListener.onCreateProjectInteraction(null);

            }
        });

        // TODO: what is the purpose of this?????
        mRecycler = (RecyclerView) mainView.findViewById(R.id.gallery_recycler_view);
        mRecycler.setHasFixedSize(true);

        // Set up Layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(linearLayoutManager);

        // When the user performs the action of swiping down then refresh the data displayed
        mSwipeRefreshLayout = (SwipeRefreshLayout) mainView.findViewById(R.id.gallery_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                // Refresh the data displayed
                refresh();

            }

        });

        // Refresh the data displayed
        refresh();

        return mainView;
    }

    /**
     * Performs the work of re-querying the cloud services for data to be displayed.  An adapter
     * is used to translate the data retrieved into the populated displayed view.
     */
    private void refresh() {

        // Let the Swiper know we are swiping
        if (!mSwipeRefreshLayout.isRefreshing()) {

            mSwipeRefreshLayout.setRefreshing(true);

        }

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<String, GalleryViewHolder>(String.class, R.layout.item_gallery, GalleryViewHolder.class, postsQuery) {

            @Override
            protected void populateViewHolder(final GalleryViewHolder viewHolder, final String uid, final int position) {

                // TODO: need a better system for this as I believe this will be called multiple times
                // See the adapter internal class in the "MakeYourAppMaterial" project's ArticleListActivity class.
                // Should be able to determine the count of items found in the resulting query that would be good to
                // perform this on after the count is reached.
                mSwipeRefreshLayout.setRefreshing(false);

                viewHolder.bindToPost(uid, mDetailsListener);

            }

        };

        mRecycler.setAdapter(mAdapter);

    }

    // TODO: can this method be pulled into a base class - it's used in several other fragments
    private Query getQuery(DatabaseReference databaseReference) {

        String userId = getUid();

        Query resultQuery = databaseReference.child("users").child(userId).child("projects");

        return resultQuery;
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
        //return "0045d757-6cac-4a69-81e3-0952a3439a78";
        return "022ffcf3-38ac-425f-8fbe-382c90d2244f";



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

        // Re-attach to the parent Activity interface
        if (context instanceof OnCreateProjectInteractionListener) {

            mCreateProjectListener = (OnCreateProjectInteractionListener) context;

        } else {

            throw new RuntimeException(context.toString() + " must implement OnCreateProjectInteractionListener");

        }

    }

    @Override
    public void onDetach() {

        super.onDetach();

        // Detach from the parent Activity interface(s)
        mListener = null;
        mDetailsListener = null;
        mCreateProjectListener = null;

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

    public interface OnCreateProjectInteractionListener {

        // TODO: Update argument type and name
        void onCreateProjectInteraction(Uri uri);

    }

}

