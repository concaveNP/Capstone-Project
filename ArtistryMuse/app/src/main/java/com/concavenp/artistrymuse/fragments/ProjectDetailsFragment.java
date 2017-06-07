package com.concavenp.artistrymuse.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.fragments.adapter.InspirationAdapter;
import com.concavenp.artistrymuse.model.Project;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectDetailsFragment extends BaseFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ProjectDetailsFragment.class.getSimpleName();

    // The key lookup name to the parameter passed into this Fragment
    private static final String UID_PARAM = "uid";

    // The UID for the User in question to display the details about
    private String mUidForDetails;

    // Widgets for displaying all of the recycled items
    private RecyclerView mRecycler;
    private InspirationAdapter mAdapter;

    // This flipper allows the content of the fragment to show the User details or a message to
    // the user telling them there is no details to show.
    private ViewFlipper mFlipper;

    // The model data that is the user's, which will be used to link with following, favoriting, etc.
    private User mUserModel;

    // The model data of the Project in question that the user wants to see the details of.
    private Project mProjectInQuestionModel;

    // The Owner of the Project in question
    private User mUserInQuestionModel;

    public ProjectDetailsFragment() {

        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uid This is the UID for the User to retrieve details for display within this Fragment
     * @return A new instance of fragment UserDetailsFragment.
     */
    public static ProjectDetailsFragment newInstance(String uid) {

        ProjectDetailsFragment fragment = new ProjectDetailsFragment();
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

        // Need to display the share trailer action bar icon
//        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_project_details, container, false);

        // Save off the flipper for use in decided which view to show
        mFlipper = (ViewFlipper) view.findViewById(R.id.fragment_project_details_ViewFlipper);

        mRecycler = (RecyclerView) view.findViewById(R.id.project_details_RecyclerView);
        mRecycler.setHasFixedSize(true);

        // Set up Layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(linearLayoutManager);

        return view;
    }

    @Override
    public void onStart() {

        super.onStart();

        Bundle args = getArguments();

        // Determine what kind of state our User data situation is in.  If we have already pulled
        // data down then display it.  Otherwise, go get it.
        if (mUserModel != null) {

            // There is data to work with, so display it
            updateUserDetails(mUserModel);

        } else if (args != null) {

            // Pull the User info from the Database just once
            mDatabase.child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

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

            });

        } else {

            // There is no data to display and nothing to lookup
            updateUserDetails(null);

        }

        // Determine what kind of state our User In Question data is in.  If we have already pulled
        // data down then display it.  Otherwise, go get it.
        if (mProjectInQuestionModel != null) {

            // There is data to work with, so display it
            updateProjectInQuestionDetails(mProjectInQuestionModel);

        } else if (args != null) {

            // Pull the User in question info from the Database
            mDatabase.child("projects").child(mUidForDetails).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final Project project = dataSnapshot.getValue(Project.class);

                    // Verify there is a Project to work with
                    if (project != null) {

                        // Set article based on saved instance state defined during onCreateView
                        updateProjectInQuestionDetails(project);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            });


        } else {

            // There is no data to display and nothing to lookup
            updateProjectInQuestionDetails(null);

        }

    }

    private void updateUserDetails(User model) {

        mUserModel = model;

        // If there is model data then show the details otherwise tell the user to choose something
        if (mUserModel != null) {

//            // The follow/unfollow toggle button
//            final ToggleButton followButton = (ToggleButton) getActivity().findViewById(R.id.follow_unfollow_toggleButton);
//
//            // Determine the initial state of the button given the user's list of "following"
//            final Map<String, Following> following = mUserModel.getFollowing();
//
//            // Set the initial state of the button
//            if (following.containsKey(mUidForDetails)) {
//
//                followButton.setChecked(true);
//
//            } else {
//
//                followButton.setChecked(false);
//
//            }
//
//            followButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                    if (isChecked) {
//
//                        // The new object that will be added to the DB
//                        Following following = new Following();
//                        following.setLastUpdatedDate(new Date().getTime());
//                        following.setUid(mUidForDetails);
//
//                        // Add the user in question to the map of people the user is following
//                        mDatabase.child("users").child(getUid()).child("following").child(mUidForDetails).setValue(following);
//
//                        // Update the followed count for the user in question
//                        Map<String, Object> childUpdates = new HashMap<>();
//                        childUpdates.put("/users/" + mUidForDetails + "/followedCount", Integer.valueOf(mUserInQuestionModel.getFollowedCount() + 1));
//                        mDatabase.updateChildren(childUpdates);
//
//                    } else {
//
//                        // TODO: this needs an addition user confirmation dialog to get express desire to un-follow the user in question
//
//                        // Remove the user in question from the map of people the user is following
//                        mDatabase.child("users").child(getUid()).child("following").child(mUidForDetails).removeValue();
//
//                        // Update the followed count for the user in question
//                        Map<String, Object> childUpdates = new HashMap<>();
//                        childUpdates.put("/users/" + mUidForDetails + "/followedCount", Integer.valueOf(mUserInQuestionModel.getFollowedCount() - 1));
//                        mDatabase.updateChildren(childUpdates);
//                    }
//
//                }
//            });
//
//        }
        }
    }

    private void updateProjectInQuestionDetails(Project model) {

        mProjectInQuestionModel = model;

        // If there is model data then show the details otherwise tell the user to choose something
        if (mProjectInQuestionModel != null) {

            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.content_project_details)));

            // TODO: decide if there is a need for some other menu buttons
//            setMenuVisibility(true);

            // Display items to be populated
            final TextView descriptionTextView = (TextView) getActivity().findViewById(R.id.description_TextView);

            populateTextView(mProjectInQuestionModel.getDescription(), descriptionTextView);

            // Provide the recycler view the list of project strings to display
            mAdapter = new InspirationAdapter(mProjectInQuestionModel.getInspirations(), mInteractionListener);
            mRecycler.setAdapter(mAdapter);





            // Retrieve the user associated with the project just once
            mDatabase.child("users").child(mProjectInQuestionModel.getOwnerUid()).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    mUserInQuestionModel = dataSnapshot.getValue(User.class);

                    // Verify there is a user to work with
                    if (mUserInQuestionModel != null) {

                        // Set the profile image
                        ImageView profileImageView = (ImageView) getActivity().findViewById(R.id.profile_ImageView);
                        populateImageView(buildFileReference(mUserInQuestionModel.getUid(), mUserInQuestionModel.getProfileImageUid(), StorageDataType.USERS), profileImageView);

                        // Set the name of the author and the username
                        TextView authorTextView = (TextView) getActivity().findViewById(R.id.author_TextView);
                        populateTextView(mUserInQuestionModel.getName(), authorTextView);
                        TextView usernameTextView = (TextView) getActivity().findViewById(R.id.username_TextView);
                        populateTextView("@" + mUserInQuestionModel.getUsername(), usernameTextView);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            });

        } else {

            // There is no data to display so tell the user
            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_project_details_TextView)));

            // TODO: decide if there is a need for some other menu buttons
//            setMenuVisibility(false);

        }

    }

}

