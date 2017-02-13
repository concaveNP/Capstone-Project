package com.concavenp.artistrymuse.fragments.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.concavenp.artistrymuse.model.ProjectResponseHit;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by dave on 12/2/2016.
 */
public class ProjectResponseViewHolder extends BaseViewHolder {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ProjectResponseViewHolder.class.getSimpleName();

    public ProjectResponseViewHolder(View itemView) {

        super(itemView);

    }

    @Override
    public void bindToPost(Object pojoJson, final OnDetailsInteractionListener listener) {

        // TODO: should be able to abstract this code up into the base - the casting part - not the rest

        ProjectResponseHit response;

        // We are expected an Following object and nothing else
        if (pojoJson instanceof ProjectResponseHit) {

            response = (ProjectResponseHit) pojoJson;

        }
        else {

            Log.e(TAG, "Unexpected object type found when expecting an ProjectResponseHit object");

            return;

        }

        // Display items to be populated
        final ImageView mainImageView = (ImageView) itemView.findViewById(R.id.main_imageview);
        final ImageView profileImageView = (ImageView) itemView.findViewById(R.id.profile_imageview);
        final TextView usernameTextView = (TextView) itemView.findViewById(R.id.username_textview);
        final TextView descriptionTextView = (TextView) itemView.findViewById(R.id.description_textview);
        final TextView followedTextView = (TextView) itemView.findViewById(R.id.followed_textview);
        final TextView followingTextView = (TextView) itemView.findViewById(R.id.following_textview);

        // Verify there is data to work with
        if (response._source != null) {

            populateImageView(
                    buildFileReference(
                            response.get_source().getUid(),
                            response.get_source().getMainImageUid(),
                            StorageDataType.PROJECTS),
                    mainImageView);
            populateTextView(
                    response.get_source().getDescription(),
                    descriptionTextView);

//                    populateTextView(Integer.toString(user.getfollowedCount), followedTextView);
//                    populateTextView(Integer.toString(user.getfollowing.size()), followingTextView);


            mDatabase.child("users").child(response.get_source().ownerUid).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    User user = dataSnapshot.getValue(User.class);

                    // TODO: what to do when it is null

                    // Verify there is a user to work with
                    if (user != null) {

                        populateImageView(buildFileReference(user.uid, user.profileImageUid, StorageDataType.USERS), profileImageView);
                        populateTextView(user.username, usernameTextView);

                        // Create stable UID for override
                        final String uid = user.getUid();

                        // Add a click listener to the view in order for the user to get more details about a selected movie
                        itemView.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                // Notify the the listener (aka MainActivity) of the details selection
                                listener.onDetailsSelection(uid, StorageDataType.USERS);

                            }

                        });

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });














            // Create stable UID for override
            final String uid = response.get_source().getUid();

            // Add a click listener to the view in order for the user to get more details about a selected movie
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Notify the the listener (aka MainActivity) of the details selection
                    listener.onDetailsSelection(uid, StorageDataType.USERS);

                }
            });

        }
    }

}
