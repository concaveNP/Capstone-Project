package com.concavenp.artistrymuse.fragments.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.UserInteractionType;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;
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
    public void bindToPost(Object pojoJson, final OnInteractionListener listener) {

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
        final ImageView headerImageView = itemView.findViewById(R.id.header_ImageView);
        final ImageView profileImageView = itemView.findViewById(R.id.avatar_ImageView);
        final TextView authorTextView = itemView.findViewById(R.id.author_TextView);
        final TextView usernameTextView = itemView.findViewById(R.id.username_TextView);
        final TextView descriptionTextView = itemView.findViewById(R.id.description_textView);
        final TextView followedTextView = itemView.findViewById(R.id.followed_textview);
        final TextView followingTextView = itemView.findViewById(R.id.views_textView);

        // Verify there is data to work with
        if (response._source != null) {

            populateImageView( buildFileReference( response.get_source().getUid(), response.get_source().getHeaderImageUid(), StorageDataType.USERS), headerImageView);
            populateImageView( buildFileReference( response.get_source().getUid(), response.get_source().getProfileImageUid(), StorageDataType.USERS), profileImageView);
            populateTextView( usernameTextView.getResources().getString(R.string.user_indication_symbol) + response.get_source().getUsername(), usernameTextView);
            populateTextView( response.get_source().getSummary(), authorTextView);
            populateTextView( response.get_source().getDescription(), descriptionTextView);
            populateTextView( response.get_source().getFollowedCount(), followedTextView);

            // Protection against bad data
            try {
                populateTextView(Integer.toString(response.get_source().getFollowing().size()), followingTextView);
            }
            catch (NullPointerException ex) {
                populateTextView("", followingTextView);
            }

            // Create stable UID for override
            final String uid = response.get_source().getUid();

            // Protection
            if ((uid != null) && (!uid.isEmpty())) {

                // Add a click listener to the view in order for the user to get more details about a selected movie
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // Notify the the listener (aka MainActivity) of the details selection
                        listener.onInteractionSelection(uid, null, StorageDataType.USERS, UserInteractionType.DETAILS);

                    }
                });

            }

        }

    }

}

