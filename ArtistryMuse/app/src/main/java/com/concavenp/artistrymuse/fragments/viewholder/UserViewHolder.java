/*
 * ArtistryMuse is an application that allows artist to share projects
 * they have created along with the inspirations behind them for others to
 * discover and enjoy.
 * Copyright (C) 2017  David A. Todd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.concavenp.artistrymuse.fragments.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.UserInteractionType;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;
import com.concavenp.artistrymuse.model.Following;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.concavenp.artistrymuse.StorageDataType.USERS;

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
    public void bindToPost(Object pojoJson, final OnInteractionListener listener) {

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
        final ImageView headerImageView = itemView.findViewById(R.id.header_ImageView);
        final TextView authorTextView = itemView.findViewById(R.id.author_TextView);
        final TextView usernameTextView = itemView.findViewById(R.id.username_TextView);
        final TextView descriptionTextView = itemView.findViewById(R.id.description_textView);
        final TextView projectsTextView = itemView.findViewById(R.id.project_count_textView);
        final TextView followedTextView = itemView.findViewById(R.id.followed_textView);
        final TextView followingTextView = itemView.findViewById(R.id.following_textView);

        final String uid = following.uid;

        // Protection
        if ((uid != null) && (!uid.isEmpty())) {

            mDatabase.child(USERS.getType()).child(following.uid).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    final User user = dataSnapshot.getValue(User.class);

                    // Verify there is a user to work with
                    if (user != null) {

                        populateImageView(buildFileReference(user.getUid(), user.getHeaderImageUid(), StorageDataType.USERS), headerImageView);
                        populateTextView(user.getName(), authorTextView);
                        populateTextView(usernameTextView.getResources().getString(R.string.user_indication_symbol) + user.getUsername(), usernameTextView);
                        populateTextView(user.getDescription(), descriptionTextView);
                        populateTextView(user.getProjects().size(), projectsTextView);
                        populateTextView(user.getFollowedCount(), followedTextView);
                        populateTextView(user.getFollowing().size(), followingTextView);

                        // Create stable UID for override
                        final String userUid = user.getUid();

                        // Protection
                        if ((userUid != null) && (!userUid.isEmpty())) {

                            // Add a click listener to the view in order to get more details about the user
                            itemView.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {

                                    // Notify the the listener (aka MainActivity) of the details selection
                                    listener.onInteractionSelection(userUid, null, StorageDataType.USERS, UserInteractionType.DETAILS);

                                }

                            });

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

    public void clearImages() {

        final ImageView headerImageView = itemView.findViewById(R.id.header_ImageView);

        if (headerImageView != null) {
            Glide.clear(headerImageView);
        }

    }

}

