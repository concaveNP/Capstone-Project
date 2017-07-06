package com.concavenp.artistrymuse.fragments.dialog;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.file_descriptor.FileDescriptorUriLoader;
import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by dave on 4/30/2017.
 *
 * References:
 *
 * How to programmatically move, copy and delete files and directories on SD?
 *      - http://stackoverflow.com/questions/4178168/how-to-programmatically-move-copy-and-delete-files-and-directories-on-sd
 *
 */
public abstract class BaseDialogFragment extends DialogFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = BaseDialogFragment.class.getSimpleName();

    protected DatabaseReference mDatabase;
    protected StorageReference mStorageRef;
    protected FirebaseImageLoader mImageLoader;
    protected FileDescriptorUriLoader mUriLoad;

    protected SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Initialize the Firebase Database connection
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize the Firebase Storage connection
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Create the Firebase image loader
        mImageLoader = new FirebaseImageLoader();

        mUriLoad = new FileDescriptorUriLoader(getContext());

        // Get ready to read from local storage for this app
        //mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSharedPreferences = getContext().getSharedPreferences(getResources().getString(R.string.shared_preferences_filename), MODE_PRIVATE);

    }

    protected String getUid() {

        // Get the UID from the SharedPreferences
        return mSharedPreferences.getString(getResources().getString(R.string.application_uid_key), getResources().getString(R.string.default_application_uid_value));

    }

    protected void populateImageView(String fileReference, ImageView imageView) {

        // It is possible for the file reference string to be null, so check for it
        if (fileReference != null) {

            StorageReference storageReference = mStorageRef.child(fileReference);

            // Download directly from StorageReference using Glide
            Glide.with(imageView.getContext())
                    .using(mImageLoader)
                    .load(storageReference)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);

        }

    }

    protected void populateThumbnailImageView(Uri uri, ImageView imageView) {

        // For safety, check as well (I've seen it) ...
        if (imageView != null) {

            // It is possible for the file reference string to be null, so check for it
            if (uri != null) {

                // Download directly from StorageReference using Glide
                Glide.with(imageView.getContext())
                        .using(mUriLoad)
                        .load(uri)
                        .thumbnail(0.1f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);

            }

        }

    }
    protected void populateThumbnailImageView(String fileReference, ImageView imageView) {

        // For safety, check as well (I've seen it) ...
        if (imageView != null) {

            // It is possible for the file reference string to be null, so check for it
            if ((fileReference != null) && (!fileReference.isEmpty())) {

                // Download directly from StorageReference using Glide
                Glide.with(imageView.getContext())
                        .load(fileReference)
                        .thumbnail(0.1f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);

            }

        }

    }

    protected String buildFileReference(String uid, String imageUid, StorageDataType type) {

        String fileReference = null;

        // Verify there is image data to work with
        if ((imageUid != null) && (!imageUid.isEmpty())) {

            // Verify there is user data to work with
            if ((uid != null) && (!uid.isEmpty())) {

                fileReference = type.getType() + "/" + uid + "/" + imageUid + ".jpg";

            }
            else {

                Log.e(TAG, "Unexpected null project UID");

            }

        }
        else {

            Log.e(TAG, "Unexpected null image UID");

        }

        return fileReference;

    }

    /**
     * Helper method that will copy the contents of one (file) to another.
     *
     * @param in - The input stream of bytes to copy from
     * @param out - The output stream of bytes to copy to
     * @throws IOException - thrown when there is a problem writing/reading from either stream
     */
    protected void copyFile(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {

            out.write(buffer, 0, read);

        }

        // Done with the input stream
        in.close();

        // Write the output file (You have now copied the file)
        out.flush();
        out.close();

    }

}
