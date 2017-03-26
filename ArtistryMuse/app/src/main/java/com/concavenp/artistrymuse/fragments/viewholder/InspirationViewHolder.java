package com.concavenp.artistrymuse.fragments.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.interfaces.OnDetailsInteractionListener;
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

    public InspirationViewHolder(View itemView) {

        super(itemView);

    }

    @Override
    public void bindToPost(Object pojoJson, final OnDetailsInteractionListener listener) {

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
//        final ImageView mainImageView = (ImageView) itemView.findViewById(R.id.main_imageView);
        final TextView titleTextView = (TextView) itemView.findViewById(R.id.title_textView);
        final TextView descriptionTextView = (TextView) itemView.findViewById(R.id.description_textView);

//        populateImageView(buildFileReference(mProjectUid, inspiration.getImageUid(), StorageDataType.PROJECTS), mainImageView);
        titleTextView.setText(inspiration.getName());
        descriptionTextView.setText(inspiration.getDescription());

    }

}

