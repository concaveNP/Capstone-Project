package com.concavenp.artistrymuse.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.UserInteractionType;
import com.concavenp.artistrymuse.fragments.viewholder.GalleryViewHolder;
import com.concavenp.artistrymuse.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;


/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link GalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryFragment extends BaseFragment {

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

    private OnCreateProjectInteractionListener mCreateProjectListener;

    private FirebaseRecyclerAdapter<String, GalleryViewHolder> mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecycler;

    // This flipper allows the content of the fragment to show the user either the list of the
    // user's projects or message stating they need to create some projects (list is empty).
    private ViewFlipper mFlipper;

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
                mInteractionListener.onInteractionSelection(null, StorageDataType.PROJECTS, UserInteractionType.EDIT);
//                mCreateProjectListener.onCreateProjectInteraction(null);

            }
        });

        // Save off the flipper for use in decided which view to show
        mFlipper = (ViewFlipper) mainView.findViewById(R.id.fragment_gallery_ViewFlipper);

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

        // First check to see if the user is has any projects yet
        mDatabase.child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Perform the JSON to Object conversion
                final User user = dataSnapshot.getValue(User.class);

                // Verify there is a user to work with
                if (user != null) {

                    Map<String, String> projects = user.getProjects();

                    // Check to see if the user is following anybody
                    if ((projects != null) && (!projects.isEmpty())) {

                        // Yes, the user has projects, so flip to that view
                        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.gallery_swipe_refresh_layout)));

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

                                viewHolder.bindToPost(uid, mInteractionListener);

                            }

                            @Override
                            public void onViewRecycled(GalleryViewHolder holder) {

                                super.onViewRecycled(holder);

                                // Clear out the Glide memory used for the images associated with this ViewHolder
                                holder.clearImages();

                            }

                        };

                        mRecycler.setAdapter(mAdapter);

                    }
                    else {

                        // View flip to the Gallery of projects
                        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_gallery_nobody_TextView)));
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }

        });

    }

    // TODO: can this method be pulled into a base class - it's used in several other fragments
    private Query getQuery(DatabaseReference databaseReference) {

        String userId = getUid();

        Query resultQuery = databaseReference.child("users").child(userId).child("projects");

        return resultQuery;
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

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
        mCreateProjectListener = null;

    }

    public interface OnCreateProjectInteractionListener {

        // TODO: Update argument type and name
        void onCreateProjectInteraction(Uri uri);

    }

}

