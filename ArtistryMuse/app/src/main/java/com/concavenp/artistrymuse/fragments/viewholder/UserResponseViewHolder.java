package com.concavenp.artistrymuse.fragments.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.concavenp.artistrymuse.model.UserResponseHit;

/**
 * Created by dave on 12/2/2016.
 */
public class UserResponseViewHolder extends BaseViewHolder {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = UserResponseViewHolder.class.getSimpleName();

    public UserResponseViewHolder(View itemView) {

        super(itemView);

    }

    @Override
    public void bindToPost(Object pojoJson, final OnDetailsInteractionListener listener) {

        UserResponseHit response;

        // We are expected an Following object and nothing else
        if (pojoJson instanceof UserResponseHit) {

            response = (UserResponseHit) pojoJson;

        }
        else {

            Log.e(TAG, "Unexpected object type found when expecting an UserResponseHit object");

            return;

        }

        // Display items to be populated
        final ImageView headerImageView = (ImageView) itemView.findViewById(R.id.header_imageview);
        final ImageView profileImageView = (ImageView) itemView.findViewById(R.id.profile_imageview);
        final TextView usernameTextView = (TextView) itemView.findViewById(R.id.username_textview);
        final TextView summaryTextView = (TextView) itemView.findViewById(R.id.summary_textview);
        final TextView descriptionTextView = (TextView) itemView.findViewById(R.id.description_textview);
        final TextView followedTextView = (TextView) itemView.findViewById(R.id.followed_textview);
        final TextView followingTextView = (TextView) itemView.findViewById(R.id.following_textview);

        // Verify there is data to work with
        if (response._source != null) {

            populateImageView(
                    buildFileReference(
                            response.get_source().getUid(),
                            response.get_source().getHeaderImageUid()),
                    headerImageView);
            populateImageView(
                    buildFileReference(
                            response.get_source().getUid(),
                            response.get_source().getProfileImageUid()),
                    profileImageView);
            populateTextView(
                    response.get_source().getUsername(),
                    usernameTextView);
            populateTextView(
                    response.get_source().getSummary(),
                    summaryTextView);
            populateTextView(
                    response.get_source().getDescription(),
                    descriptionTextView);
            populateTextView(
                    Integer.toString(response.get_source().getFollowedCount()),
                    followedTextView);
            populateTextView(
                    Integer.toString(response.get_source().getFollowing().size()),
                    followingTextView);

            // Create stable UID for override
            final String uid = response.get_source().getUid();

            // Add a click listener to the view in order for the user to get more details about a selected movie
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Notify the the listener (aka MainActivity) of the details selection
                    listener.onDetailsSelection(uid, OnDetailsInteractionListener.DETAILS_TYPE.USER);

                }
            });

        }

    }

    @Override
    protected String buildFileReference(String uid, String imageUid) {

        String fileReference = null;

        // Verify there is image data to work with
        if ((imageUid != null) && (!imageUid.isEmpty())) {

            // Verify there is user data to work with
            if ((uid != null) && (!uid.isEmpty())) {

                fileReference = "users" + "/" + uid + "/" + imageUid + ".jpg";

            }
            else {

                Log.e(TAG, "Unexpected null user UID");

            }

        }
        else {

            Log.e(TAG, "Unexpected null image UID");

        }

        return fileReference;

    }

}

