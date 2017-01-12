package com.concavenp.artistrymuse.fragments.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.model.Project;
import com.concavenp.artistrymuse.model.Favorite;
import com.concavenp.artistrymuse.model.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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
 * Created by dave on 12/2/2016.
 */

public class ProjectViewHolder extends RecyclerView.ViewHolder {

    DatabaseReference mDatabase;

    private StorageReference mStorageRef;
    private FirebaseUser mUser;
    private String mUid;

    public ImageView mainImageView;
    public ImageView profileImageView;
    public TextView usernameTextView;
    public TextView descriptionTextView;
    public TextView followedTextView;
    public TextView followingTextView;

    public ProjectViewHolder(View itemView) {

        super(itemView);

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

    public void bindToPost(Favorite favorite, View.OnClickListener clickListener) {
        // Display items to be populated
        mainImageView = (ImageView) itemView.findViewById(R.id.main_imageview);
        profileImageView = (ImageView) itemView.findViewById(R.id.profile_imageview);
        usernameTextView = (TextView) itemView.findViewById(R.id.username_textview);
        descriptionTextView = (TextView) itemView.findViewById(R.id.description_textview);
        followedTextView = (TextView) itemView.findViewById(R.id.followed_textview);
        followingTextView = (TextView) itemView.findViewById(R.id.following_textview);

        mDatabase.child("projects").child(favorite.uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Perform the JSON to Object conversion
                Project project = dataSnapshot.getValue(Project.class);

                // TODO: what to do when it is null

                // Verify there is a user to work with
                if (project != null) {

                    populateImageView(project.uid, project.mainImageUid, mainImageView);
                    populateTextView(project.description, descriptionTextView);
//                    populateTextView(Integer.toString(user.followedCount), followedTextView);
//                    populateTextView(Integer.toString(user.following.size()), followingTextView);

                    mDatabase.child("users").child(project.ownerUid).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            // Perform the JSON to Object conversion
                            User user = dataSnapshot.getValue(User.class);

                            // TODO: what to do when it is null

                            // Verify there is a user to work with
                            if (user != null) {

                                populateImageView(user.uid, user.profileImageUid, profileImageView);
                                populateTextView(user.username, usernameTextView);

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    private void populateImageView(String uid, String imageUid, ImageView imageView) {
        if ((imageUid != null) && (!imageUid.isEmpty())) {

            final String fileReference = "projects" + "/" + uid + "/" + imageUid + ".jpg";
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

