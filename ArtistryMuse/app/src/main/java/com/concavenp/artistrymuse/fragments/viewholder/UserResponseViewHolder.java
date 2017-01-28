package com.concavenp.artistrymuse.fragments.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.concavenp.artistrymuse.model.UserResponseHit;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by dave on 12/2/2016.
 */
public class UserResponseViewHolder extends RecyclerView.ViewHolder {

    private StorageReference mStorageRef;
    private FirebaseUser mUser;
    private String mUid;

    public ImageView headerImageView;
    public ImageView profileImageView;
    public TextView usernameTextView;
    public TextView summaryTextView;
    public TextView descriptionTextView;
    public TextView followedTextView;
    public TextView followingTextView;

    public UserResponseViewHolder(View itemView) {

        super(itemView);

         // Initialize the Storage connection
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Get the authenticated user
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser != null) {
            mUid = mUser.getUid();
        }

    }

    public void bindToPost(UserResponseHit response, final OnDetailsInteractionListener listener) {

        // Display items to be populated
        headerImageView = (ImageView) itemView.findViewById(R.id.header_imageview);
        profileImageView = (ImageView) itemView.findViewById(R.id.profile_imageview);
        usernameTextView = (TextView) itemView.findViewById(R.id.username_textview);
        summaryTextView = (TextView) itemView.findViewById(R.id.summary_textview);
        descriptionTextView = (TextView) itemView.findViewById(R.id.description_textview);
        followedTextView = (TextView) itemView.findViewById(R.id.followed_textview);
        followingTextView = (TextView) itemView.findViewById(R.id.following_textview);

        // Verify there is data to work with
        if (response._source != null) {

            populateImageView(
                    response.get_source().getUid(),
                    response.get_source().getHeaderImageUid(),
                    headerImageView);
            populateImageView(
                    response.get_source().getUid(),
                    response.get_source().getProfileImageUid(),
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

    private void populateImageView(String uid, String imageUid, ImageView imageView) {
        if ((imageUid != null) && (!imageUid.isEmpty())) {

            final String fileReference = "users" + "/" + uid + "/" + imageUid + ".jpg";
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

    private void populateTextView(String text, TextView textView) {

        // Verify there is text to work with and empty out if nothing is there.
        if ((text != null) && (!text.isEmpty())) {

            textView.setText(text);

        } else {

            textView.setText("");

        }

    }

}

