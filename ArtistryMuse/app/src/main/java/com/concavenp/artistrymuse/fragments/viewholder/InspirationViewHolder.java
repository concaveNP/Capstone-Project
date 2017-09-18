package com.concavenp.artistrymuse.fragments.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.UserInteractionType;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;
import com.concavenp.artistrymuse.model.Inspiration;

/**
 * Created by dave on 12/2/2016.
 */
public class InspirationViewHolder extends BaseViewHolder {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = InspirationViewHolder.class.getSimpleName();

    /**
     * This will be used by the listeners of the interaction of this view to determine how
     * it should be interpreted as.
     */
    private UserInteractionType mUserInteractionType = UserInteractionType.NONE;

    /**
     * Constructor
     *
     * @param itemView - The View in question this class holds
     */
    public InspirationViewHolder(View itemView) {

        super(itemView);

    }

    /**
     * Constructor that allows for the specifying of the user interaction type.
     *
     * @param itemView - The View in question this class holds
     * @param userInteractionType - The type of interaction the user will have with this view
     */
    public InspirationViewHolder(View itemView, UserInteractionType userInteractionType) {

        this(itemView);

        mUserInteractionType = userInteractionType;

    }

    @Override
    public void bindToPost(Object pojoJson, final OnInteractionListener listener) {

        final Inspiration inspiration;

        // We are expected an Following object and nothing else
        if (pojoJson instanceof Inspiration) {

            inspiration = (Inspiration) pojoJson;

        }
        else {

            Log.e(TAG, "Unexpected object type found when expecting an Inspiration object");

            return;

        }

        // Display items to be populated
        final ImageView mainImageView = itemView.findViewById(R.id.main_imageView);
        final TextView titleEditText = itemView.findViewById(R.id.title_textView);
        final TextView descriptionTextView = itemView.findViewById(R.id.description_textView);

        populateImageView(buildFileReference(inspiration.getProjectUid(), inspiration.getImageUid(), StorageDataType.PROJECTS), mainImageView);
        populateTextView(inspiration.getName(), titleEditText);
        populateTextView(inspiration.getDescription(), descriptionTextView);

        switch (mUserInteractionType) {
            case DETAILS: {
                // Currently, inspiration detailing does not exist, just drop to editing functionality
            }
            case EDIT: {

                final String projectUid = inspiration.getProjectUid();
                final String uid = inspiration.getUid();

                // Protection
                if ((projectUid != null) && (!projectUid.isEmpty())) {

                    // Protection
                    if ((uid != null) && (!uid.isEmpty())) {

                        itemView.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                // Notify the the listener of the Inspiration selection
                                listener.onInteractionSelection(projectUid, uid, StorageDataType.INSPIRATIONS, mUserInteractionType);

                            }

                        });

                    }

                }

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

