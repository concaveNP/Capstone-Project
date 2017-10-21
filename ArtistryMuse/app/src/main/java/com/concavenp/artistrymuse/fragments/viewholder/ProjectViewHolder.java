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
import com.concavenp.artistrymuse.model.Favorite;
import com.concavenp.artistrymuse.model.Project;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.concavenp.artistrymuse.StorageDataType.PROJECTS;
import static com.concavenp.artistrymuse.StorageDataType.USERS;

/**
 * Created by dave on 12/2/2016.
 */
public class ProjectViewHolder extends BaseViewHolder {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ProjectViewHolder.class.getSimpleName();

    public ProjectViewHolder(View itemView) {

        super(itemView);

    }

    @Override
    public void bindToPost(Object pojoJson, final OnInteractionListener listener) {

        final Favorite favorite;

        // We are expected an Following object and nothing else
        if (pojoJson instanceof Favorite) {

            favorite = (Favorite) pojoJson;

        }
        else {

            Log.e(TAG, "Unexpected object type found when expecting an Favorite object");

            return;

        }

        // Display items to be populated
        final ImageView mainImageView = itemView.findViewById(R.id.main_imageView);
        final TextView authorTextView = itemView.findViewById(R.id.author_TextView);
        final TextView usernameTextView = itemView.findViewById(R.id.username_TextView);
        final TextView descriptionTextView = itemView.findViewById(R.id.description_textView);
        final TextView favoritedTextView = itemView.findViewById(R.id.favorited_textView);
        final TextView viewsTextView = itemView.findViewById(R.id.views_textView);
        final TextView ratingTextView = itemView.findViewById(R.id.rating_textView);

        mDatabase.child(PROJECTS.getType()).child(favorite.uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Perform the JSON to Object conversion
                final Project project = dataSnapshot.getValue(Project.class);

                // Verify there is a user to work with
                if (project != null) {

                    populateImageView(buildFileReference(project.getUid(), project.getMainImageUid(), StorageDataType.PROJECTS), mainImageView);
                    populateTextView(project.getDescription(), descriptionTextView);
                    populateTextView(project.getFavorited(), favoritedTextView);
                    populateTextView(project.getViews(), viewsTextView);
                    populateTextView(project.getRating(), ratingTextView);

                    // Create stable UID for override
                    final String uid = project.getUid();

                    // Protection
                    if ((uid != null) && (!uid.isEmpty())){

                        // Add a click listener to the view in order for the user to get more details about a selected movie
                        itemView.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                // Notify the the listener (aka MainActivity) of the details selection
                                listener.onInteractionSelection(project.getUid(), null, StorageDataType.PROJECTS, UserInteractionType.DETAILS);

                            }

                        });

                    }

                    final String ownerUid = project.ownerUid;

                    // Protection
                    if ((ownerUid != null) && (!ownerUid.isEmpty())) {

                        // Query for the User specific data (aka the Project Owner)
                        mDatabase.child(USERS.getType()).child(ownerUid).addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                // Perform the JSON to Object conversion
                                User user = dataSnapshot.getValue(User.class);

                                // Verify there is a user to work with
                                if (user != null) {

                                    populateTextView(user.getName(), authorTextView);
                                    populateTextView(usernameTextView.getResources().getString(R.string.user_indication_symbol) + user.getUsername(), usernameTextView);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Do nothing
                            }

                        });

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    public void clearImages() {

        final ImageView mainImageView = itemView.findViewById(R.id.main_imageView);

        if (mainImageView != null) {
            Glide.clear(mainImageView);
        }

    }
}

