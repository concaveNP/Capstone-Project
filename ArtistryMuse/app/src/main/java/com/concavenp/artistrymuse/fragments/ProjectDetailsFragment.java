package com.concavenp.artistrymuse.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.fragments.adapter.InspirationAdapter;
import com.concavenp.artistrymuse.model.Project;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
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

    private RecyclerView mRecycler;
    //private FirebaseRecyclerAdapter<Inspiration, InspirationViewHolder> mAdapter;
    private InspirationAdapter mAdapter;

    // This flipper allows the content of the fragment to show the User details or a message to
    // the user telling them there is no details to show.
    private ViewFlipper mFlipper;

    // The model data displayed
    private Project mModel;

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

        // TODO: what is the purpose of this?????
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

        // Determine what kind of state our Project data situation is in.  If we have already pulled
        // data down then display it.  Otherwise, go get it.
        if (mModel != null) {

            // There is data to work with, so display it
            updateProjectDetails(mModel);

        } else if (args != null) {

            // Pull the Project info from the Database
            mDatabase.child("projects").child(mUidForDetails).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final Project project = dataSnapshot.getValue(Project.class);

                    // TODO: what to do when it is null

                    // Verify there is a Project to work with
                    if (project != null) {

                        // Set article based on saved instance state defined during onCreateView
                        updateProjectDetails(project);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });


        } else {

            // There is no data to display and nothing to lookup
            updateProjectDetails(null);

        }

    }

    private Query getQuery(DatabaseReference databaseReference) {

        String projectId = mUidForDetails;

        Query resultQuery = databaseReference.child("projects").child(projectId).child("inspirations");

        return resultQuery;
    }

    private void updateProjectDetails(Project model) {

        mModel = model;

        // If there is model data then show the details otherwise tell the user to choose something
        if (mModel != null) {

            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.content_project_details_FrameLayout)));

            // TODO: decide if there is a need for some other menu buttons
//            setMenuVisibility(true);

            // Display items to be populated
            final TextView nameTextView = (TextView) getActivity().findViewById(R.id.name_TextView);
            final TextView descriptionTextView = (TextView) getActivity().findViewById(R.id.description_TextView);

            populateTextView(mModel.getName(), nameTextView);
            populateTextView(mModel.getDescription(), descriptionTextView);

            // Provide the recycler view the list of project strings to display
            mAdapter = new InspirationAdapter(mModel.getInspirations(), mDetailsListener);
            mRecycler.setAdapter(mAdapter);

        } else {

            // There is no data to display so tell the user
            mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_project_details_TextView)));

            // TODO: decide if there is a need for some other menu buttons
//            setMenuVisibility(false);

        }

    }

}

