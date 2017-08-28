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
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.UserInteractionType;
import com.concavenp.artistrymuse.fragments.viewholder.ProjectViewHolder;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;
import com.concavenp.artistrymuse.model.Favorite;
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
 * Use the {@link ProjectListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectListFragment extends BaseFragment implements OnInteractionListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ProjectListFragment.class.getSimpleName();

    private FirebaseRecyclerAdapter<Favorite, ProjectViewHolder> mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecycler;

    private boolean largeDevice = false;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment FavoritesFragment.
     */
    public static ProjectListFragment newInstance() {

        ProjectListFragment fragment = new ProjectListFragment();

        return fragment;

    }

    /**
     * Required empty public constructor
     */
    public ProjectListFragment() {

        // Do nothing

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_project_list, container, false);

        // Store off the layout size using tag values within the view
        String tag = (String)mainView.getTag();
        if (tag.equals("layout_large")) {
            largeDevice = true;
        }

        mRecycler = (RecyclerView) mainView.findViewById(R.id.favorites_recycler_view);
        mRecycler.setHasFixedSize(true);

        // Set up Layout
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecycler.setLayoutManager(sglm);

        // When the user performs the action of swiping down then refresh the data displayed
        mSwipeRefreshLayout = (SwipeRefreshLayout) mainView.findViewById(R.id.favorites_swipe_refresh_layout);
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

        // First check to see if the user favorited any projects anybody yet
        mDatabase.child(USERS.getType()).child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Perform the JSON to Object conversion
                final User user = dataSnapshot.getValue(User.class);

                // Verify there is a user to work with
                if (user != null) {

                    Map<String, Favorite> favorites = user.getFavorites();

                    // Check to see if the user has any favorites
                    if ((favorites != null) && (!favorites.isEmpty())) {

                        // Let the Swiper know we are swiping
                        if (!mSwipeRefreshLayout.isRefreshing()) {

                            mSwipeRefreshLayout.setRefreshing(true);

                        }

                        // Set up FirebaseRecyclerAdapter with the Query
                        Query postsQuery = getQuery(mDatabase);
                        mAdapter = new FirebaseRecyclerAdapter<Favorite, ProjectViewHolder>(Favorite.class, R.layout.item_project, ProjectViewHolder.class, postsQuery) {

                            @Override
                            protected void populateViewHolder(final ProjectViewHolder viewHolder, final Favorite model, final int position) {

                                // TODO: need a better system for this as I believe this will be called multiple times
                                // See the adapter internal class in the "MakeYourAppMaterial" project's ArticleListActivity class.
                                // Should be able to determine the count of items found in the resulting query that would be good to
                                // perform this on after the count is reached.
                                mSwipeRefreshLayout.setRefreshing(false);

                                // Perform the binding based upon being a tablet or phone
                                if (largeDevice) {
                                    viewHolder.bindToPost(model, ProjectListFragment.this);
                                } else {
                                    viewHolder.bindToPost(model, mInteractionListener);
                                }

                            }

                            @Override
                            public void onViewRecycled(ProjectViewHolder holder) {

                                super.onViewRecycled(holder);

                                // Clear out the Glide memory used for the images associated with this ViewHolder
                                holder.clearImages();

                            }

                        };

                        mRecycler.setAdapter(mAdapter);

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

        Query resultQuery = databaseReference.child(USERS.getType()).child(userId).child("favorites");

        return resultQuery;
    }

    /**
     * This would never get called unless we have a LARGE device (aka tablet)
     */
    @Override
    public void onInteractionSelection(String firstUid, String secondUid, StorageDataType storageDataType, UserInteractionType interactionType) {

        ProjectDetailsFragment fragment = (ProjectDetailsFragment) getFragmentManager().findFragmentById(R.id.fragment_project_details);
        fragment.setUidForDetails(firstUid);
        fragment.performStart();

    }

}

