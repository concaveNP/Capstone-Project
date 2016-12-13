package com.concavenp.artistrymuse.fragments.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.model.Following;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;

/**
 * Created by dave on 12/2/2016.
 */

public class FollowingViewHolder extends RecyclerView.ViewHolder {

    DatabaseReference mDatabase;

    private StorageReference mStorageRef;
    private FirebaseUser mUser;
    private String mUid;

    public TextView uidTextView;
    public EditText lastUpdateEditText;
    public ImageView thumbnailImage;

    public FollowingViewHolder(View itemView) {

        super(itemView);

        uidTextView = (TextView) itemView.findViewById(R.id.uid);
        lastUpdateEditText = (EditText) itemView.findViewById(R.id.last_update);
        thumbnailImage = (ImageView) itemView.findViewById(R.id.thumbnail);

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

    public void bindToPost(Following following, View.OnClickListener clickListener) {

        uidTextView.setText(following.uid);
        System.out.println("Following UI: " + following.uid);
        lastUpdateEditText.setText(new Date(following.lastUpdatedDate).toString());

        uidTextView.setOnClickListener(clickListener);

        mDatabase.child("users").child(following.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = (User) dataSnapshot.getValue(User.class);

                // TODO: what to do when it is null
                // Verify there is a user to work with
                if (user != null) {

                    String profileUid = user.profileImageUid;
                    if ((profileUid  != null) && (!profileUid.isEmpty())) {

                        final String fileReference = "users" + "/" + user.uid + "/" + user.profileImageUid + ".jpg";
                        StorageReference storageReference = mStorageRef.child(fileReference);

                        System.out.println(storageReference);

                        // Download directly from StorageReference using Glide
                        Glide.with(thumbnailImage.getContext())
                                .using(new FirebaseImageLoader())
                                .load(storageReference)
                                .centerCrop()
                                .crossFade()
                                .into(thumbnailImage);

                    }
                }

                System.out.println(user);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

}
