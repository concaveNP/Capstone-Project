package com.concavenp.artistrymuse.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.fragments.adapter.InspirationAdapter;
import com.concavenp.artistrymuse.model.Favorite;
import com.concavenp.artistrymuse.model.Project;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.concavenp.artistrymuse.StorageDataType.PROJECTS;
import static com.concavenp.artistrymuse.StorageDataType.USERS;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * References:
 *
 * Adding to an average without unknown total sum
 *      - https://math.stackexchange.com/questions/1153794/adding-to-an-average-without-unknown-total-sum
 * How to add and subtract values from an average?
 *      - https://math.stackexchange.com/questions/22348/how-to-add-and-subtract-values-from-an-average
 */
public class ProjectDetailsFragment extends BaseFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ProjectDetailsFragment.class.getSimpleName();

    // The key lookup name to the parameter passed into this Fragment
    private static final String UID_PARAM = "uid";

    // The UID for the Project in question to display the details about
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

    // Listeners for DB value changes
    private ValueEventListener userValueEventListener;
    private ValueEventListener projectInQuestionValueEventListener;
    private ValueEventListener projectOwnerValueEventListener;

    // This will contain the user's favorite object for the Project in question that it is favoring
    private Favorite favoriteInQuestion;

    // This member will be used in determining if the View count associated with a project should
    // be incremented.
    private boolean performViewing = false;

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

        // The creation of the fragment is the only place where a "viewing" of the Project will be allowed
        performViewing = true;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_project_details, container, false);

        // Save off the flipper for use in deciding which view to show
        mFlipper = (ViewFlipper) view.findViewById(R.id.fragment_project_details_ViewFlipper);

        mRecycler = (RecyclerView) view.findViewById(R.id.project_details_RecyclerView);
        mRecycler.setHasFixedSize(true);

        // Set up Layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(linearLayoutManager);

        return view;
    }

    /**
     * Public setter for the UID of the user in question to show the details of.
     *
     * This method will be used when a LARGE device is being used (aka tablet)
     *
     * @param uid - The UID of the User in question to show details of
     */
    public void setUidForDetails(String uid) {
        mUidForDetails = uid;
    }

    @Override
    public void onStart() {

        super.onStart();

        performStart();

    }

    public void performStart() {

        // Only perform if there is a UID to show details for
        if ((mUidForDetails != null) && (!mUidForDetails.isEmpty())) {

            // Display whatever data we currently have to work with to get the cycle going
            updateUserDetails(mUserModel);

            // Subscribe to the user's data
            mDatabase.child(USERS.getType()).child(getUid()).addValueEventListener(getUserValueEventListener());

            // Display whatever data we currently have to work with to get the cycle going
            updateProjectInQuestionDetails(mProjectInQuestionModel);

            // Pull the Project in question info from the Database and keep listening for changes
            mDatabase.child(PROJECTS.getType()).child(mUidForDetails).addValueEventListener(getProjectInQuestionValueEventListener());

        }

    }

    @Override
    public void onStop() {

        super.onStop();

        // Un-subscribe to the user's data
        if ((mUidForDetails != null) && (!mUidForDetails.isEmpty())) {

            mDatabase.child(USERS.getType()).child(getUid()).removeEventListener(getUserValueEventListener());

            // Un-subscribe to the project in question's data if there
            mDatabase.child(PROJECTS.getType()).child(mUidForDetails).removeEventListener(getProjectInQuestionValueEventListener());

            // Un-subscribe to the project in question's owner if data is there
            if (mProjectInQuestionModel != null) {

                String ownerUid = mProjectInQuestionModel.getOwnerUid();

                if ((ownerUid != null) && (!ownerUid.isEmpty())) {

                    mDatabase.child(USERS.getType()).child(mProjectInQuestionModel.getOwnerUid()).removeEventListener(getProjectOwnerValueEventListener());

                }

            }

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

    private ValueEventListener getProjectInQuestionValueEventListener() {

        if (projectInQuestionValueEventListener == null) {

            projectInQuestionValueEventListener  = new ValueEventListener() {

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

            };

        }

        return projectInQuestionValueEventListener;

    }

    private ValueEventListener getProjectOwnerValueEventListener() {

        if (projectOwnerValueEventListener == null) {

            projectOwnerValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    mUserInQuestionModel = dataSnapshot.getValue(User.class);

                    // Verify there is a user to work with
                    if (mUserInQuestionModel != null) {

                        View mainView = ProjectDetailsFragment.this.getView();

                        // Set the profile image
                        ImageView profileImageView = (ImageView) mainView.findViewById(R.id.avatar_ImageView);
                        populateImageView(buildFileReference(mUserInQuestionModel.getUid(), mUserInQuestionModel.getProfileImageUid(), StorageDataType.USERS), profileImageView);

                        // Set the name of the author and the username
                        TextView authorTextView = (TextView) mainView.findViewById(R.id.author_TextView);
                        populateTextView(mUserInQuestionModel.getName(), authorTextView);
                        TextView usernameTextView = (TextView) mainView.findViewById(R.id.username_TextView);
                        populateTextView("@" + mUserInQuestionModel.getUsername(), usernameTextView);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            };

        }

        return projectOwnerValueEventListener;

    }

    private void updateUserDetails(User model) {

        mUserModel = model;

        // If there is model data then show the details otherwise tell the user to choose something
        if (mUserModel != null) {

            // The favorite/unfavorite toggle button
            final ToggleButton favoriteButton = (ToggleButton) getView().findViewById(R.id.favorite_unfavorite_toggleButton);

            // Determine the initial state of the button given the user's list of "favorites"
            final Map<String, Favorite> favorites = mUserModel.getFavorites();

            // Set the initial state of the button
            if (favorites.containsKey(mUidForDetails)) {

                favoriteInQuestion = favorites.get(mUidForDetails);

                favoriteButton.setChecked(true);

            } else {

                favoriteInQuestion = null;

                favoriteButton.setChecked(false);

            }

            favoriteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {

                        // The new object that will be added to the DB
                        favoriteInQuestion = new Favorite();
                        favoriteInQuestion.setFavoritedDate(new Date().getTime());
                        favoriteInQuestion.setRating(new Random().nextDouble()*10.0); // TODO: should be user chosen value
                        favoriteInQuestion.setUid(mUidForDetails);

                        // Add the Project in question to the map of projects the user has favorited
                        mDatabase.child(USERS.getType()).child(getUid()).child("favorites").child(mUidForDetails).setValue(favoriteInQuestion);

                        // Create a has map of the values updates to the Project in question
                        Map<String, Object> childUpdates = new HashMap<>();

                        // Update the ratings count for the project in question
                        int ratingCount = mProjectInQuestionModel.getRatingsCount() + 1;
                        childUpdates.put("/projects/" + mUidForDetails + "/ratingsCount", ratingCount);

                        // Update the rating for the project in question
                        double newRating = ((mProjectInQuestionModel.getRating() * mProjectInQuestionModel.getRatingsCount()) + favoriteInQuestion.getRating()) / ratingCount;
                        childUpdates.put("/projects/" + mUidForDetails + "/rating", newRating);

                        // Update the Favorited count
                        int favoritedCount = mProjectInQuestionModel.getFavorited() + 1;
                        childUpdates.put("/projects/" + mUidForDetails + "/favorited", favoritedCount);

                        // Update the Project in question
                        mDatabase.updateChildren(childUpdates);

                    } else {

                        // Only perform the operation if it appears that we have favorited this project before
                        if (favoriteInQuestion != null) {

                            // Create a has map of the values updates to the Project in question
                            Map<String, Object> childUpdates = new HashMap<>();

                            // Update the ratings count for the project in question
                            int ratingCount = mProjectInQuestionModel.getRatingsCount() - 1;
                            childUpdates.put("/projects/" + mUidForDetails + "/ratingsCount", ratingCount);

                            // Update the rating for the project in question
                            double newRating = ((mProjectInQuestionModel.getRating() * mProjectInQuestionModel.getRatingsCount()) - favoriteInQuestion.getRating()) / ratingCount;
                            if (Double.isNaN(newRating)) {
                                newRating = 0.0;
                            }
                            childUpdates.put("/projects/" + mUidForDetails + "/rating", newRating);

                            // Update the Favorited count
                            int favoritedCount = mProjectInQuestionModel.getFavorited() - 1;
                            childUpdates.put("/projects/" + mUidForDetails + "/favorited", favoritedCount);

                            // Update the Project in question
                            mDatabase.updateChildren(childUpdates);

                            // Remove the favorite object in question from the map of people the user is following
                            mDatabase.child(USERS.getType()).child(getUid()).child("favorites").child(mUidForDetails).removeValue();

                            // Clear out the local storage of the favorite object
                            favoriteInQuestion = null;

                        }

                    }

                }

            });

        }

    }

    private void updateProjectInQuestionDetails(Project model) {

        mProjectInQuestionModel = model;

        // If there is model data then show the details otherwise tell the user to choose something
        if (mProjectInQuestionModel != null) {

            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.content_project_details)));

            // Display items to be populated
            final TextView descriptionTextView = (TextView) getView().findViewById(R.id.description_TextView);

            populateTextView(mProjectInQuestionModel.getDescription(), descriptionTextView);

            // Provide the recycler view the list of project strings to display
            mAdapter = new InspirationAdapter(mProjectInQuestionModel.getInspirations(), mInteractionListener);
            mRecycler.setAdapter(mAdapter);

            // Retrieve the user associated with the project just once
            mDatabase.child(USERS.getType()).child(mProjectInQuestionModel.getOwnerUid()).addValueEventListener(getProjectOwnerValueEventListener());

            // Check the "Viewed" member to see if we should update the view count of the Project in question
            if (performViewing) {

                // Create a has map of the values updates to the Project in question
                Map<String, Object> childUpdates = new HashMap<>();

                // Update the ratings count for the project in question
                int viewCount = mProjectInQuestionModel.getViews() + 1;
                childUpdates.put("/projects/" + mUidForDetails + "/views", viewCount);

                // Update the Project in question
                mDatabase.updateChildren(childUpdates);

                // The Project has been viewed by this Fragment
                performViewing = false;

            }

        } else {

            // There is no data to display so tell the user
            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_project_details_TextView)));

        }

    }

}

