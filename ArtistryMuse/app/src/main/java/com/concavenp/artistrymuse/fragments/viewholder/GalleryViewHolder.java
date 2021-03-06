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
import com.concavenp.artistrymuse.model.Project;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.concavenp.artistrymuse.StorageDataType.PROJECTS;

/**
 * Created by dave on 12/2/2016.
 */
public class GalleryViewHolder extends BaseViewHolder {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = GalleryViewHolder.class.getSimpleName();

    /**
     * This will be used by the listeners of the interaction of this view to determine how
     * it should be interpreted as.
     */
    private UserInteractionType mUserInteractionType = UserInteractionType.NONE;

    public GalleryViewHolder(View itemView) {

        super(itemView);

    }

    /**
     * Constructor that allows for the specifying of the user interaction type.
     *
     * @param itemView - The View in question this class holds
     * @param userInteractionType - The type of interaction the user will have with this view
     */
    public GalleryViewHolder(View itemView, UserInteractionType userInteractionType) {

        this(itemView);

        mUserInteractionType = userInteractionType;

    }

    public void setUserInteractionType(UserInteractionType userInteractionType) {
        mUserInteractionType = userInteractionType;
    }

    @Override
    public void bindToPost(Object pojoJson, final OnInteractionListener listener) {

        final String projectUid;

        // We are expected an Following object and nothing else
        if (pojoJson instanceof String) {

            projectUid = (String) pojoJson;

            // Protection
            if (projectUid.isEmpty()) {

                Log.e(TAG, "The Project UID was not valid");

                return;

            }

        }
        else {

            Log.e(TAG, "Unexpected object type found when expecting an Favorite object");

            return;

        }

        // Display items to be populated
        final ImageView mainImageView = itemView.findViewById(R.id.main_imageView);
        final TextView titleTextView = itemView.findViewById(R.id.title_textView);
        final TextView descriptionTextView = itemView.findViewById(R.id.description_textView);
        final TextView favoritedTextView = itemView.findViewById(R.id.favorited_textView);
        final TextView viewsTextView = itemView.findViewById(R.id.views_textView);
        final TextView ratingTextView = itemView.findViewById(R.id.rating_textView);

        mDatabase.child(PROJECTS.getType()).child(projectUid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Perform the JSON to Object conversion
                Project project = dataSnapshot.getValue(Project.class);

                // Verify there is a user to work with
                if (project != null) {

                    populateImageView(buildFileReference(project.getUid(), project.getMainImageUid(), StorageDataType.PROJECTS), mainImageView);
                    populateTextView(project.getName(), titleTextView);
                    populateTextView(project.getDescription(), descriptionTextView);
                    populateTextView(project.getFavorited(), favoritedTextView);
                    populateTextView(project.getViews(), viewsTextView);
                    populateTextView(project.getRating(), ratingTextView);

                    // Add a click listener to the view in order for the user to get more details about a selected project
                    switch (mUserInteractionType) {
                        case DETAILS:
                        case EDIT: {

                            itemView.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    // Notify the the listener of the Project selection
                                    listener.onInteractionSelection(projectUid, null, StorageDataType.PROJECTS, mUserInteractionType);

                                }

                            });

                            break;
                        }
                        case NONE:
                        default: {
                            // There is no action to be associated with this list item view
                            break;
                        }
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }

        });

    }

    public void clearImages() {

        final ImageView mainImageView = itemView.findViewById(R.id.main_imageView);

        Glide.clear(mainImageView);

    }

}

