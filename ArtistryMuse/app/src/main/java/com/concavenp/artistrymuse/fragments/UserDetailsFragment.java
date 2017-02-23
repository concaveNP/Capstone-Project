package com.concavenp.artistrymuse.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserDetailsFragment extends Fragment {

    // The key lookup name to the parameter passed into this Fragment
    private static final String UID_PARAM = "uid";

    // The UID for the User in question to display the details about
    private String mUidForDetails;

    private OnFragmentInteractionListener mListener;

    // The Firebase interaction fields
    protected DatabaseReference mDatabase;
    protected StorageReference mStorageRef;
    protected FirebaseUser mUser;
    protected String mUid;

    // This flipper allows the content of the fragment to show the User details or a message to
    // the user telling them there is no details to show.
    private ViewFlipper mFlipper;

    // The model data displayed
    private User mModel;

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

        return view;
    }

    @Override
    public void onStart() {

        super.onStart();

        Bundle args = getArguments();

        // Determine what kind of state our User data situation is in.  If we have already pulled
        // data down then display it.  Otherwise, go get it.
        if (mModel != null) {

            // There is data to work with, so display it
            updateUserDetails(mModel);

        } else if (args != null) {

            // Pull the User info from the Database
            mDatabase.child("users").child(mUidForDetails).addListenerForSingleValueEvent(new ValueEventListener() {

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


        } else {

            // There is no data to display and nothing to lookup
            updateUserDetails(null);

        }

    }

    private void updateUserDetails(User model) {

        mModel = model;

        // If there is model data then show the details otherwise tell the user to choose something
        if (mModel != null) {

            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.content_user_details_FrameLayout)));
//            setMenuVisibility(true);

        } else {

            // There is no data to display so tell the user
            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_user_details_TextView)));
//            setMenuVisibility(false);

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
    }

    @Override
    public void onDetach() {

        super.onDetach();

        mListener = null;

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

