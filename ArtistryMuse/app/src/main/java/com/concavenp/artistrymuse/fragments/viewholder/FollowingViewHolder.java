package com.concavenp.artistrymuse.fragments.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.model.Following;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

/**
 * Created by dave on 12/2/2016.
 */

public class FollowingViewHolder extends RecyclerView.ViewHolder {

    DatabaseReference mDatabase;

    public TextView uidTextView;
    public EditText lastUpdateEditText;
    public ImageView thumbnailImage;

    public FollowingViewHolder(View itemView) {

        super(itemView);

        uidTextView = (TextView) itemView.findViewById(R.id.uid);
        lastUpdateEditText = (EditText) itemView.findViewById(R.id.last_update);
        thumbnailImage = (ImageView) itemView.findViewById(R.id.thumbnail);

        mDatabase = FirebaseDatabase.getInstance().getReference();

    }

    public void bindToPost(Following following, View.OnClickListener clickListener) {

        uidTextView.setText(following.uid);
        lastUpdateEditText.setText(new Date(following.lastUpdatedDate).toString());

        uidTextView.setOnClickListener(clickListener);

        mDatabase.child("user").child(following.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = (User) dataSnapshot.getValue();

                System.out.println(dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

}
