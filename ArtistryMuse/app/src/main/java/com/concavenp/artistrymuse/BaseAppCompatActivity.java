package com.concavenp.artistrymuse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.file_descriptor.FileDescriptorUriLoader;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.concavenp.artistrymuse.interfaces.OnInteractionListener;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.concavenp.artistrymuse.InspirationEditActivity.EXTRA_DATA_INSPIRATION;
import static com.concavenp.artistrymuse.InspirationEditActivity.EXTRA_DATA_PROJECT;

/**
 * Created by dave on 3/25/2017.
 *
 * References:
 *
 * How to round an image with Glide library?
 *      - https://stackoverflow.com/questions/25278821/how-to-round-an-image-with-glide-library
 *
 * Get color-int from color resource
 *      - https://stackoverflow.com/questions/5271387/get-color-int-from-color-resource
 *
 * android - How to create a circular ImageView with border
 *      - https://android--examples.blogspot.com/2015/11/android-how-to-create-circular.html
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity implements OnInteractionListener {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = BaseAppCompatActivity.class.getSimpleName();

    /**
     * String used when creating the activity via intent.  This key will be used to retrieve the
     * UID associated with the USER in question.
     */
    public static final String EXTRA_DATA = "uid_string_data";

    protected DatabaseReference mDatabase;
    protected StorageReference mStorageRef;
    protected FirebaseImageLoader mImageLoader;
    protected FileDescriptorUriLoader mUriLoad;

    protected SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Initialize the Firebase Database connection
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize the Firebase Storage connection
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Create the Firebase image loader
        mImageLoader = new FirebaseImageLoader();

        mUriLoad = new FileDescriptorUriLoader(this);

        // Get ready to read from local storage for this app
        //mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_preferences_filename), MODE_PRIVATE);

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

    protected StorageReference buildStorageReference(String uid, String imageUid, StorageDataType type) {

        StorageReference result = null;

        String fileReference = buildFileReference(uid, imageUid, type);

        // It is possible for the file reference string to be null, so check for it
        if ((fileReference != null) && (!fileReference.isEmpty())) {

            result = mStorageRef.child(fileReference);

        }

        return result;

    }

    protected void populateImageView(String fileReference, ImageView imageView) {

        // For safety, check as well (I've seen it) ...
        if (imageView != null) {

            // It is possible for the file reference string to be null, so check for it
            if ((fileReference != null) && (!fileReference.isEmpty())) {

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

    }

    protected void populateCircularImageView(StorageReference storageReference, final ImageView imageView) {

        // For safety, check as well (I've seen it) ...
        if (imageView != null) {

            // It is possible for the file reference string to be null, so check for it
            if (storageReference!= null) {

                Glide.with(imageView.getContext())
                        .using(mImageLoader)
                        .load(storageReference)
                        .asBitmap()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(createBitmapImageViewTarget(imageView));

            }

        }

    }

    protected void populateCircularImageView(String fileReference, final ImageView imageView) {

        // For safety, check as well (I've seen it) ...
        if (imageView != null) {

            // It is possible for the file reference string to be null, so check for it
            if ((fileReference != null) && (!fileReference.isEmpty())) {

                Glide.with(imageView.getContext())
                        .load(fileReference)
                        .asBitmap()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(createBitmapImageViewTarget(imageView));
//                        .into(new BitmapImageViewTarget(imageView) {
//                            @Override
//                            protected void setResource(Bitmap bitmap) {
//
//                                int bitmapWidth = bitmap.getWidth();
//                                int bitmapHeight = bitmap.getHeight();
//                                int borderWidthHalf = 10;
//                                int bitmapSquareWidth = Math.min(bitmapWidth,bitmapHeight);
//                                int newBitmapSquareWidth = bitmapSquareWidth+borderWidthHalf;
//
//                                Bitmap roundedBitmap = Bitmap.createBitmap(newBitmapSquareWidth,newBitmapSquareWidth,Bitmap.Config.ARGB_8888);
//
//                                // Initialize a new Canvas to draw empty bitmap
//                                Canvas canvas = new Canvas(roundedBitmap);
//
//                                // Calculation to draw bitmap at the circular bitmap center position
//                                int x = borderWidthHalf + bitmapSquareWidth - bitmapWidth;
//                                int y = borderWidthHalf + bitmapSquareWidth - bitmapHeight;
//
//                                canvas.drawBitmap(bitmap, x, y, null);
//
//                                // Initializing a new Paint instance to draw circular border
//                                Paint borderPaint = new Paint();
//                                borderPaint.setStyle(Paint.Style.STROKE);
//                                borderPaint.setStrokeWidth(borderWidthHalf*2);
//                                borderPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.myapp_accent_700, null));
//
//                                canvas.drawCircle(canvas.getWidth()/2, canvas.getWidth()/2, newBitmapSquareWidth/2, borderPaint);
//
//                                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(imageView.getContext().getResources(), roundedBitmap);
//                                circularBitmapDrawable.setCircular(true);
//                                imageView.setImageDrawable(circularBitmapDrawable);
//
//                            }
//                        });

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

    /**
     * The purpose of this interface implementation is to start the Details Activity of either a
     * user or a project.  The point to making the Main Activity implement is to support both the
     * phone and tablet layout of the app.  Phone layouts will just start a new activity and
     * tablet layouts will populate a neighboring fragment with the details results.
     *
     * @param firstUid - This will be the UID of other the User or the Project as specified in the type param
     * @param secondUid - This uid will be used for identifying an inspiration
     * @param storageDataType - The type will either be a user or a project
     */
    @Override
    public void onInteractionSelection(String firstUid, String secondUid, StorageDataType storageDataType, UserInteractionType userInteractionType) {

        switch(storageDataType) {

            case PROJECTS: {

                switch(userInteractionType) {

                    case DETAILS: {

                        // Create and start the details activity along with passing it the UID of the Project in question
                        Intent intent = new Intent(this, ProjectDetailsActivity.class);
                        intent.putExtra(EXTRA_DATA, firstUid);
                        startActivity(intent);

                        break;
                    }
                    case EDIT: {

                        // Create and start the inspiration activity along with passing it the UID of the inspiration in question
                        Intent intent = new Intent(this, ProjectEditActivity.class);
                        intent.putExtra(EXTRA_DATA, firstUid);
                        startActivity(intent);

                    }
                }


                break;
            }
            case USERS: {

                switch(userInteractionType) {

                    case DETAILS: {

                        // Create and start the details activity along with passing it the UID of the User in question
                        Intent intent = new Intent(this, UserDetailsActivity.class);
                        intent.putExtra(EXTRA_DATA, firstUid);
                        startActivity(intent);

                        break;
                    }
                    case EDIT: {

                        // NOTE: Currently, there is only the details type

                    }
                }

                break;

            }
            case INSPIRATIONS: {

                switch(userInteractionType) {

                    case DETAILS: {

                        // NOTE: Currently, there is only the edit type

                        break;
                    }
                    case EDIT: {

                        // Create and start the inspiration activity along with passing it the UID of the inspiration in question
                        Intent intent = new Intent(this, InspirationEditActivity.class);
                        intent.putExtra(EXTRA_DATA_PROJECT, firstUid);
                        intent.putExtra(EXTRA_DATA_INSPIRATION, secondUid);
                        startActivity(intent);

                    }
                }

                break;

            }
            default: {
                // TODO: log an error and whatnot
            }

        }

    }

    protected String getUid() {

        // Get the UID from the SharedPreferences
        return mSharedPreferences.getString(getResources().getString(R.string.application_uid_key), getResources().getString(R.string.default_application_uid_value));

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

    private BitmapImageViewTarget createBitmapImageViewTarget(final ImageView imageView) {

        return new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap bitmap) {

                int bitmapWidth = bitmap.getWidth();
                int bitmapHeight = bitmap.getHeight();
                int borderWidthHalf = 10;
                int bitmapSquareWidth = Math.min(bitmapWidth,bitmapHeight);
                int newBitmapSquareWidth = bitmapSquareWidth+borderWidthHalf;

                Bitmap roundedBitmap = Bitmap.createBitmap(newBitmapSquareWidth,newBitmapSquareWidth,Bitmap.Config.ARGB_8888);

                // Initialize a new Canvas to draw empty bitmap
                Canvas canvas = new Canvas(roundedBitmap);

                // Calculation to draw bitmap at the circular bitmap center position
                int x = borderWidthHalf + bitmapSquareWidth - bitmapWidth;
                int y = borderWidthHalf + bitmapSquareWidth - bitmapHeight;

                canvas.drawBitmap(bitmap, x, y, null);

                // Initializing a new Paint instance to draw circular border
                Paint borderPaint = new Paint();
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(borderWidthHalf*2);
                borderPaint.setColor(ResourcesCompat.getColor(getResources(), R.color.myapp_accent_700, null));

                canvas.drawCircle(canvas.getWidth()/2, canvas.getWidth()/2, newBitmapSquareWidth/2, borderPaint);

                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(imageView.getContext().getResources(), roundedBitmap);
                circularBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(circularBitmapDrawable);

            }
        };
    }

}

