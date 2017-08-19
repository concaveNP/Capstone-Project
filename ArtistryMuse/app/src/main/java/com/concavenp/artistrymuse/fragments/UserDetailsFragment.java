package com.concavenp.artistrymuse.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.UserInteractionType;
import com.concavenp.artistrymuse.fragments.adapter.GalleryAdapter;
import com.concavenp.artistrymuse.model.Following;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.concavenp.artistrymuse.StorageDataType.USERS;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link UserDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserDetailsFragment extends BaseFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = UserDetailsFragment.class.getSimpleName();

    // The key lookup name to the parameter passed into this Fragment
    private static final String UID_PARAM = "uid";

    // The UID for the User in question to display the details about
    private String mUidForDetails;

    // Widgets for displaying all of the recycled items
    private RecyclerView mRecycler;
    private GalleryAdapter mAdapter;

    // This flipper allows the content of the fragment to show the User details or a message to
    // the user telling them there is no details to show.
    private ViewFlipper mFlipper;

    // The model data that is the user's, which will be used to link with following, favoriting, etc.
    private User mUserModel;

    // The model data of the User in question that the user wants to see the details of.
    private User mUserInQuestionModel;

    // Listeners for DB value changes
    private ValueEventListener userValueEventListener;
    private ValueEventListener userInQuestionValueEventListener;

    public UserDetailsFragment() {

        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uid This is the UID for the User to retrieve details for display within this Fragment
     * @return A new instance of fragment UserDetailsFragment.
     */
    public static UserDetailsFragment newInstance(String uid) {

        UserDetailsFragment fragment = new UserDetailsFragment();
        Bundle args = new Bundle();
        args.putString(UID_PARAM, uid);
        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            mUidForDetails = getArguments().getString(UID_PARAM);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_details, container, false);

        // Save off the flipper for use in deciding which view to show
        mFlipper = (ViewFlipper) view.findViewById(R.id.fragment_user_details_ViewFlipper);

        mRecycler = (RecyclerView) view.findViewById(R.id.user_details_RecyclerView);
        mRecycler.setHasFixedSize(true);

        // Set up Layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(linearLayoutManager);

        return view;

    }

    @Override
    public void onStart() {

        super.onStart();

        // Display whatever data we currently have to work with to get the cycle going
        updateUserDetails(mUserModel);

        // Subscribe to the user's data
        mDatabase.child(USERS.getType()).child(getUid()).addValueEventListener(getUserValueEventListener());

        // Display whatever data we currently have to work with to get the cycle going
        updateUserInQuestionDetails(mUserInQuestionModel);

        // Pull the User in question info from the Database and keep listening for changes
        if ((mUidForDetails != null) && (!mUidForDetails.isEmpty())) {

            mDatabase.child(USERS.getType()).child(mUidForDetails).addValueEventListener(getUserInQuestionValueEventListener());

        }

    }

    @Override
    public void onStop() {

        super.onStop();

        // Un-subscribe to the user's data
        mDatabase.child(USERS.getType()).child(getUid()).removeEventListener(getUserValueEventListener());

        // Un-subscribe to the user in question's data if there
        if ((mUidForDetails != null) && (!mUidForDetails.isEmpty())) {

            mDatabase.child(USERS.getType()).child(mUidForDetails).removeEventListener(getUserInQuestionValueEventListener());

        }

    }

    private ValueEventListener getUserValueEventListener() {

        if (userValueEventListener == null) {

            userValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final User user = dataSnapshot.getValue(User.class);

                    // Verify there is a user to work with
                    if (user != null) {

                        // Set article based on saved instance state defined during onCreateView
                        updateUserDetails(user);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            };

        }

        return userValueEventListener;

    }

    private ValueEventListener getUserInQuestionValueEventListener() {

        if (userInQuestionValueEventListener == null) {

            userInQuestionValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final User user = dataSnapshot.getValue(User.class);

                    // Verify there is a user to work with
                    if (user != null) {

                        // Set article based on saved instance state defined during onCreateView
                        updateUserInQuestionDetails(user);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            };

        }

        return userInQuestionValueEventListener;

    }

    private void updateUserDetails(User model) {

        mUserModel = model;

        // If there is model data then show the details otherwise tell the user to choose something
        if (mUserModel != null) {

            // The follow/unfollow toggle button
            final ToggleButton followButton = (ToggleButton) getActivity().findViewById(R.id.follow_unfollow_toggleButton);

            // Determine the initial state of the button given the user's list of "following"
            final Map<String, Following> following = mUserModel.getFollowing();

            // Set the initial state of the button
            if (following.containsKey(mUidForDetails)) {

                followButton.setChecked(true);

            } else {

                followButton.setChecked(false);

            }

            followButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {

                        // The new object that will be added to the DB
                        Following following = new Following();
                        following.setLastUpdatedDate(new Date().getTime());
                        following.setUid(mUidForDetails);

                        // Add the user in question to the map of people the user is following
                        mDatabase.child(USERS.getType()).child(getUid()).child("following").child(mUidForDetails).setValue(following);

                        // Update the followed count for the user in question
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/users/" + mUidForDetails + "/followedCount", mUserInQuestionModel.getFollowedCount() + 1);
                        mDatabase.updateChildren(childUpdates);

                    } else {

                        // Remove the user in question from the map of people the user is following
                        mDatabase.child(USERS.getType()).child(getUid()).child("following").child(mUidForDetails).removeValue();

                        // Update the followed count for the user in question
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/users/" + mUidForDetails + "/followedCount", mUserInQuestionModel.getFollowedCount() - 1);
                        mDatabase.updateChildren(childUpdates);

                    }

                }

            });

        }

    }

    private void updateUserInQuestionDetails(User model) {

        mUserInQuestionModel = model;

        // If there is model data then show the details otherwise tell the user to choose something
        if (mUserInQuestionModel != null) {

            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.content_user_details_FrameLayout)));

            // Set the profile image
            ImageView profileImageView = (ImageView) getActivity().findViewById(R.id.avatar_ImageView);
            populateImageView(buildFileReference(mUserInQuestionModel.getUid(), mUserInQuestionModel.getProfileImageUid(), USERS), profileImageView);

            // Set the name of the author and the username
            TextView authorTextView = (TextView) getActivity().findViewById(R.id.author_TextView);
            populateTextView(mUserInQuestionModel.getName(), authorTextView);
            TextView usernameTextView = (TextView) getActivity().findViewById(R.id.username_TextView);
            populateTextView("@" + mUserInQuestionModel.getUsername(), usernameTextView);

            // Set the summary description
            TextView summaryTextView = (TextView) getActivity().findViewById(R.id.summary_TextView);
            populateTextView(mUserInQuestionModel.getSummary(), summaryTextView);

            // Set the counts for the projects, followed and following
            TextView projectsTextView = (TextView) getActivity().findViewById(R.id.project_count_textView);
            populateTextView(Integer.toString(mUserInQuestionModel.getProjects().size()), projectsTextView);
            TextView followingTextView = (TextView) getActivity().findViewById(R.id.following_TextView);
            populateTextView(Integer.toString(mUserInQuestionModel.getFollowing().size()), followingTextView);
            TextView followedTextView = (TextView) getActivity().findViewById(R.id.followed_TextView);
            populateTextView(Integer.toString(mUserInQuestionModel.getFollowedCount()), followedTextView);

            // Set the favorited number
            TextView favoritedTextView = (TextView) getActivity().findViewById(R.id.favorited_TextView);
            populateTextView(Integer.toString(mUserInQuestionModel.getFavorites().size()), favoritedTextView);
            TextView ratingsTextView = (TextView) getActivity().findViewById(R.id.ratings_TextView);
            populateTextView("hmmm, this needs thought", ratingsTextView);

            // Provide the recycler view the list of project strings to display
            mAdapter = new GalleryAdapter(mUserInQuestionModel.getProjects(), mInteractionListener, UserInteractionType.DETAILS);
            mRecycler.setAdapter(mAdapter);

        } else {

            // There is no data to display so tell the user
            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_user_details_TextView)));

        }

    }

}

