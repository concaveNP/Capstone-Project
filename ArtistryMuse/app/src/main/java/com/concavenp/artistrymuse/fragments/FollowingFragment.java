package com.concavenp.artistrymuse.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.fragments.viewholder.UserViewHolder;
import com.concavenp.artistrymuse.model.Following;
import com.concavenp.artistrymuse.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import static com.concavenp.artistrymuse.StorageDataType.USERS;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link FollowingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FollowingFragment extends BaseFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = FollowingFragment.class.getSimpleName();

    private FirebaseRecyclerAdapter<Following, UserViewHolder> mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecycler;

    // This flipper allows the content of the fragment to show the user either the list of people
    // they are following or message stating they need to follow somebody (list is empty).
    private ViewFlipper mFlipper;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment FollowingFragment.
     */
    public static FollowingFragment newInstance() {

        FollowingFragment fragment = new FollowingFragment();

        return fragment;

    }

    /**
     * Required empty public constructor
     */
    public FollowingFragment() {

        // Do nothing

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_following, container, false);

        // Save off the flipper for use in deciding which view to show
        mFlipper = (ViewFlipper) mainView.findViewById(R.id.fragment_following_ViewFlipper);

        mRecycler = (RecyclerView) mainView.findViewById(R.id.following_recycler_view);
        mRecycler.setHasFixedSize(true);

        // Set up Layout
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecycler.setLayoutManager(sglm);

        // When the user performs the action of swiping down then refresh the data displayed
        mSwipeRefreshLayout = (SwipeRefreshLayout) mainView.findViewById(R.id.following_swipe_refresh_layout);
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

        // First check to see if the user is following anybody yet
        mDatabase.child(USERS.getType()).child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Perform the JSON to Object conversion
                final User user = dataSnapshot.getValue(User.class);

                // Verify there is a user to work with
                if (user != null) {

                    Map<String, Following> following = user.getFollowing();

                    // Check to see if the user is following anybody
                    if ((following != null) && (!following.isEmpty())) {

                        // Yes, the user is following someone, so flip to that view
                        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.following_swipe_refresh_layout)));

                        // Let the Swiper know we are swiping
                        if (!mSwipeRefreshLayout.isRefreshing()) {

                            mSwipeRefreshLayout.setRefreshing(true);

                        }

                        // Set up FirebaseRecyclerAdapter with the Query
                        Query postsQuery = getQuery(mDatabase);
                        mAdapter = new FirebaseRecyclerAdapter<Following, UserViewHolder>(Following.class, R.layout.item_user, UserViewHolder.class, postsQuery) {

                            @Override
                            protected void populateViewHolder(final UserViewHolder viewHolder, final Following model, final int position) {

                                // TODO: need a better system for this as I believe this will be called multiple times
                                // See the adapter internal class in the "MakeYourAppMaterial" project's ArticleListActivity class.
                                // Should be able to determine the count of items found in the resulting query that would be good to
                                // perform this on after the count is reached.
                                mSwipeRefreshLayout.setRefreshing(false);

                                viewHolder.bindToPost(model, mInteractionListener);

                            }

                            @Override
                            public void onViewRecycled(UserViewHolder holder) {

                                super.onViewRecycled(holder);

                                // Clear out the Glide memory used for the images associated with this ViewHolder
                                holder.clearImages();

                            }

                        };

                        mRecycler.setAdapter(mAdapter);

                    }
                    else {

                        // View flip to the "Follow People"
                        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_following_nobody_TextView)));
                    }


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }

        });

    }

    private Query getQuery(DatabaseReference databaseReference) {

        String userId = getUid();

        Query resultQuery = databaseReference.child(USERS.getType()).child(userId).child("following");

        return resultQuery;
    }

}

