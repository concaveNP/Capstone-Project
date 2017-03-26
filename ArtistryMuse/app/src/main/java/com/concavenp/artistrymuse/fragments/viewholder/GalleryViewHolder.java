package com.concavenp.artistrymuse.fragments.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
import com.concavenp.artistrymuse.model.Project;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

/**
 * Created by dave on 12/2/2016.
 */
public class GalleryViewHolder extends BaseViewHolder {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = GalleryViewHolder.class.getSimpleName();

    public GalleryViewHolder(View itemView) {

        super(itemView);

    }

    @Override
    public void bindToPost(Object pojoJson, final OnDetailsInteractionListener listener) {

        final String projectUid;

        // We are expected an Following object and nothing else
        if (pojoJson instanceof String) {

            projectUid = (String) pojoJson;

        }
        else {

            Log.e(TAG, "Unexpected object type found when expecting an Favorite object");

            return;

        }

        // Display items to be populated
        final ImageView mainImageView = (ImageView) itemView.findViewById(R.id.main_imageView);
        final TextView titleTextView = (TextView) itemView.findViewById(R.id.title_textView);
        final TextView descriptionTextView = (TextView) itemView.findViewById(R.id.description_textView);
        final TextView publicationTextView = (TextView) itemView.findViewById(R.id.publication_textView);
        final TextView favoritedTextView = (TextView) itemView.findViewById(R.id.favorited_textView);
        final TextView viewsTextView = (TextView) itemView.findViewById(R.id.views_textView);
        final TextView ratingTextView = (TextView) itemView.findViewById(R.id.rating_textView);

        mDatabase.child("projects").child(projectUid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Perform the JSON to Object conversion
                Project project = dataSnapshot.getValue(Project.class);

                // TODO: what to do when it is null

                // Verify there is a user to work with
                if (project != null) {

                    populateImageView(buildFileReference(project.getUid(), project.getMainImageUid(), StorageDataType.PROJECTS), mainImageView);
                    populateTextView(project.getName(), titleTextView);
                    populateTextView(project.getDescription(), descriptionTextView);
                    populateTextView(Integer.toString(project.getFavorited()), favoritedTextView);
                    populateTextView(Integer.toString(project.getViews()), viewsTextView);
                    populateTextView(String.format("%.1f", project.getRating()), ratingTextView);

                    Boolean published = project.getPublished();
                    if (published) {
                        populateTextView("Published: " + new Date(project.getPublishedDate()).toString(), publicationTextView);
                    }
                    else {
                        populateTextView("Unpublished", publicationTextView);
                    }

                    // Add a click listener to the view in order for the user to get more details about a selected movie
                    itemView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            // Notify the the listener (aka MainActivity) of the details selection
                            listener.onDetailsSelection(projectUid, StorageDataType.PROJECTS);

                        }

                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    public void clearImages() {

        final ImageView mainImageView = (ImageView) itemView.findViewById(R.id.main_imageView);

        Glide.clear(mainImageView);

    }

}

