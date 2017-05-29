package com.concavenp.artistrymuse;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.concavenp.artistrymuse.fragments.viewholder.InspirationViewHolder;
import com.concavenp.artistrymuse.model.Inspiration;
import com.concavenp.artistrymuse.model.Project;
import com.concavenp.artistrymuse.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.UUID;


/**
 *
 * FLOW:
 *
 * creation
 * - are we editing an existing project?
 *      - NO:
 *          - Create new UID
 *          - Set title of activity to "New Project"
 *      - YES:
 *          - Set title of activity to projects title
 *
 * - Show publish button
 * - Show +Inspiration button
 * - Show Save button
 * - Show delete inspiration button (ick)
 * - Show delete project button (ick)
 *
 */
public class ProjectEditActivity extends BaseAppCompatActivity {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ProjectEditActivity.class.getSimpleName();

    /**
     * String used when creating the activity via intent.  This key will be used to retrieve the
     * UID associated with the USER in question.
     */
    public static final String EXTRA_DATA = "uid_string_data";

    private FirebaseRecyclerAdapter<String, InspirationViewHolder> mAdapter;
    private RecyclerView mRecycler;

    // The UID of the project in question.  NOTE: this value can be passed into the activity via
    // intent param or generated if this is a new project.
    private String mProjectUid;

    // The model data to display and update
    private Project mProjectModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_project_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO: what is the purpose of this?????
        mRecycler = (RecyclerView) findViewById(R.id.inspirations_recycler_view);
        mRecycler.setHasFixedSize(true);

        // Set up Layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(linearLayoutManager);

        // Extract the UID from the Activity parameters
        Intent intent = getIntent();
        mProjectUid = intent.getStringExtra(EXTRA_DATA);

        // If there is a UID to work with then we are dealing with editing and existing project otherwise this is a new project
        if ((mProjectUid == null) || (mProjectUid.isEmpty())) {

            // This is a new project for the user, so we must create a new UID for it
            mProjectUid = UUID.randomUUID().toString();

            mProjectModel = new Project();

            //display();

        } else {

            mDatabase.child("projects").child(mProjectUid).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final Project project = dataSnapshot.getValue(Project.class);

                    // Verify there is a user to work with
                    if (project != null) {

                        Map<String, Inspiration> projects = project.getInspirations();

                        // Check to see if the user is following anybody
                        if ((projects != null) && (!projects.isEmpty())) {

                            // Set up FirebaseRecyclerAdapter with the Query
                            Query postsQuery = getQuery(mDatabase);
                            mAdapter = new FirebaseRecyclerAdapter<String, InspirationViewHolder>(String.class, R.layout.item_inspiration, InspirationViewHolder.class, postsQuery) {

                                @Override
                                protected void populateViewHolder(final InspirationViewHolder viewHolder, final String uid, final int position) {

//                                    viewHolder.bindToPost(uid, mDetailsListener);

                                }

                                @Override
                                public void onViewRecycled(InspirationViewHolder holder) {

                                    super.onViewRecycled(holder);

                                    // Clear out the Glide memory used for the images associated with this ViewHolder
//                                    holder.clearImages();

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





    }

    private Query getQuery(DatabaseReference databaseReference) {

        String userId = getUid();

        Query resultQuery = databaseReference.child("users").child(userId).child("projects");

        return resultQuery;
    }

}
