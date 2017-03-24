package com.concavenp.artistrymuse.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.fragments.adapter.GalleryAdapter;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.concavenp.artistrymuse.model.Following;
import com.concavenp.artistrymuse.model.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserDetailsFragment extends Fragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = UserDetailsFragment.class.getSimpleName();

    // The key lookup name to the parameter passed into this Fragment
    private static final String UID_PARAM = "uid";

    // The UID for the User in question to display the details about
    private String mUidForDetails;

    private OnFragmentInteractionListener mListener;
    private OnDetailsInteractionListener mDetailsListener;

    // The Firebase interaction fields
    protected DatabaseReference mDatabase;
    protected StorageReference mStorageRef;
    protected FirebaseUser mUser;
    protected String mUid;
    private RecyclerView mRecycler;
//    private FirebaseRecyclerAdapter<String, GalleryViewHolder> mAdapter;
    private GalleryAdapter mAdapter;

    // This flipper allows the content of the fragment to show the User details or a message to
    // the user telling them there is no details to show.
    private ViewFlipper mFlipper;

    // The model data displayed
    private User mUserModel;
    private User mUserInQuestionModel;


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

        // Initialize the Database connection
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize the Storage connection
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Get the authenticated user
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser != null) {
            mUid = mUser.getUid();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Need to display the share trailer action bar icon
//        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_details, container, false);

        // Save off the flipper for use in decided which view to show
        mFlipper = (ViewFlipper) view.findViewById(R.id.fragment_user_details_ViewFlipper);

        // TODO: what is the purpose of this?????
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

        Bundle args = getArguments();

        // Determine what kind of state our User data situation is in.  If we have already pulled
        // data down then display it.  Otherwise, go get it.
        if (mUserInQuestionModel != null) {

            // There is data to work with, so display it
            updateUserInQuestionDetails(mUserInQuestionModel);

        } else if (args != null) {

            // Pull the User info from the Database just once
            mDatabase.child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final User user = dataSnapshot.getValue(User.class);

                    // TODO: what to do when it is null

                    // Verify there is a user to work with
                    if (user != null) {

                        // Set article based on saved instance state defined during onCreateView
                        updateUserDetails(user);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });

            // Pull the User in question info from the Database and keep listening for changes
            mDatabase.child("users").child(mUidForDetails).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final User user = dataSnapshot.getValue(User.class);

                    // TODO: what to do when it is null

                    // Verify there is a user to work with
                    if (user != null) {

                        // Set article based on saved instance state defined during onCreateView
                        updateUserInQuestionDetails(user);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });


        } else {

            // There is no data to display and nothing to lookup
            updateUserInQuestionDetails(null);

        }

    }

    private Query getQuery(DatabaseReference databaseReference) {

        String userId = getUid();

        Query resultQuery = databaseReference.child("users").child(userId);

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

    private void updateUserDetails(User model) {

        mUserModel = model;

        // If there is model data then show the details otherwise tell the user to choose something
        if (mUserModel != null) {

            // The follow/unfollow toggle button
            final ToggleButton followButton = (ToggleButton) getActivity().findViewById(R.id.follow_unfollow_toggleButton);

            // Determine the initial state of the button given the user's list of "following"
            final Map<String, Following> following = mUserModel.getFollowing();

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
                        mDatabase.child("users").child(getUid()).child("following").child(mUidForDetails).setValue(following);

                        // Update the followed count for the user in question
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/users/" + mUidForDetails + "/followedCount", Integer.valueOf(mUserInQuestionModel.getFollowedCount() + 1));
                        mDatabase.updateChildren(childUpdates);

                    } else {

                        // Remove the user in question from the map of people the user is following
                        mDatabase.child("users").child(getUid()).child("following").child(mUidForDetails).removeValue();

                        // Update the followed count for the user in question
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/users/" + mUidForDetails + "/followedCount", Integer.valueOf(mUserInQuestionModel.getFollowedCount() - 1));
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

            // TODO: decide if there is a need for some other menu buttons
//            setMenuVisibility(true);

            // Set the profile image
            ImageView profileImageView = (ImageView) getActivity().findViewById(R.id.profile_ImageView);
            populateImageView(buildFileReference(mUserInQuestionModel.getUid(), mUserInQuestionModel.getProfileImageUid(), StorageDataType.USERS), profileImageView);

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
            mAdapter = new GalleryAdapter(mUserInQuestionModel.getProjects(), mDetailsListener);
            mRecycler.setAdapter(mAdapter);

        } else {

            // There is no data to display so tell the user
            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_user_details_TextView)));

            // TODO: decide if there is a need for some other menu buttons
//            setMenuVisibility(false);

        }

    }

    protected String buildFileReference(String uid, String imageUid, StorageDataType type) {

        String fileReference = null;

        // Verify there is image data to work with
        if ((imageUid != null) && (!imageUid.isEmpty())) {

            // Verify there is user data to work with
            if ((uid != null) && (!uid.isEmpty())) {

                fileReference = type.getType() + "/" + uid + "/" + imageUid + ".jpg";

            }
            else {

                Log.e(TAG, "Unexpected null project UID");

            }

        }
        else {

            Log.e(TAG, "Unexpected null image UID");

        }

        return fileReference;

    }

    protected void populateImageView(String fileReference, ImageView imageView) {

        // It is possible for the file reference string to be null, so check for it
        if (fileReference != null) {

            StorageReference storageReference = mStorageRef.child(fileReference);

            // Download directly from StorageReference using Glide
            Glide.with(imageView.getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .fitCenter()
                    .crossFade()
                    .into(imageView);

        }

    }

    protected void populateTextView(String text, TextView textView) {

        // Verify there is text to work with and empty out if nothing is there.
        if ((text != null) && (!text.isEmpty())) {

            textView.setText(text);

        } else {

            textView.setText("");

        }

    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

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

