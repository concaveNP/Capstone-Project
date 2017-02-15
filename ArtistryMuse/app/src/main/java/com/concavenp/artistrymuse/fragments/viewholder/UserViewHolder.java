package com.concavenp.artistrymuse.fragments.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.concavenp.artistrymuse.model.Following;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by dave on 12/2/2016.
 */
public class UserViewHolder extends BaseViewHolder {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = UserViewHolder.class.getSimpleName();

    public UserViewHolder(View itemView) {

        super(itemView);

    }

    @Override
    public void bindToPost(Object pojoJson, final OnDetailsInteractionListener listener) {

        Following following;

        // We are expected an Following object and nothing else
        if (pojoJson instanceof Following) {

            following = (Following) pojoJson;

        }
        else {

            Log.e(TAG, "Unexpected object type found when expecting an Following object");

            return;

        }

        // Display items to be populated
        final ImageView headerImageView = (ImageView) itemView.findViewById(R.id.header_imageview);
        final ImageView profileImageView = (ImageView) itemView.findViewById(R.id.profile_imageview);
        final TextView usernameTextView = (TextView) itemView.findViewById(R.id.username_textview);
        final TextView summaryTextView = (TextView) itemView.findViewById(R.id.summary_textview);
        final TextView descriptionTextView = (TextView) itemView.findViewById(R.id.description_textView);
        final TextView followedTextView = (TextView) itemView.findViewById(R.id.followed_textview);
        final TextView followingTextView = (TextView) itemView.findViewById(R.id.views_textView);

        mDatabase.child("users").child(following.uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Perform the JSON to Object conversion
                User user = dataSnapshot.getValue(User.class);

                // TODO: what to do when it is null, Log message at the least for now....!!!!

                // Verify there is a user to work with
                if (user != null) {

                    populateImageView(buildFileReference(user.getUid(), user.getHeaderImageUid(), StorageDataType.USERS), headerImageView);
                    populateImageView(buildFileReference(user.getUid(), user.getProfileImageUid(), StorageDataType.USERS), profileImageView);
                    populateTextView(user.getUsername(), usernameTextView);
                    populateTextView(user.getSummary(), summaryTextView);
                    populateTextView(user.getDescription(), descriptionTextView);
                    populateTextView(Integer.toString(user.getFollowedCount()), followedTextView);
                    populateTextView(Integer.toString(user.getFollowing().size()), followingTextView);

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

                Log.w(TAG, "Unexpected cancellation of a Firebase Database query");

            }

        });

    }

}

