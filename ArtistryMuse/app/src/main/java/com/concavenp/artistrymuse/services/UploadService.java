package com.concavenp.artistrymuse.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.concavenp.artistrymuse.MainActivity;
import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.StorageDataType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class UploadService extends BaseTaskService {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = UploadService.class.getSimpleName();

    private static final int NOTIF_ID_DOWNLOAD = 0;

    /** Intent Actions **/
    public static final String ACTION_UPLOAD = "action_upload";
    public static final String UPLOAD_COMPLETED = "upload_completed";
    public static final String UPLOAD_ERROR = "upload_error";

    /** Intent Extras **/
    public static final String EXTRA_FILE_URI = "extra_file_uri";
    public static final String EXTRA_FILE_RENAMED_FILENAME = "extra_file_renamed_filename";
    public static final String EXTRA_UPLOAD_URL = "extra_upload_url";
    public static final String EXTRA_UPLOAD_DATABASE = "extra_upload_database";
    public static final String EXTRA_UPLOAD_UID = "extra_upload_uid";

    private String mLastPathSegment;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (ACTION_UPLOAD.equals(intent.getAction())) {

            // The file in question to upload
            Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);

            // The ending filename to use for the file
            mLastPathSegment = intent.getStringExtra(EXTRA_FILE_RENAMED_FILENAME);

            // Which database (aka collection in Mongo speak) to use
            String type = intent.getStringExtra(EXTRA_UPLOAD_DATABASE);
            StorageDataType dataType = StorageDataType.fromType(type);

            // The UID folder for to the file being loading
            String uid = intent.getStringExtra(EXTRA_UPLOAD_UID);

            uploadFromUri(fileUri, dataType, uid);

        }

        return START_REDELIVER_INTENT;

    }

    private void uploadFromUri(final Uri fileUri, final StorageDataType dataType, final String uidFolder ) {

        taskStarted();

        // Check for having the last path segment already specified via intent extras
        String filename;
        if (mLastPathSegment != null) {
            filename = mLastPathSegment;

            // Reset
            mLastPathSegment = null;
        }
        else {
            filename = fileUri.getLastPathSegment();
        }

        // Check for having the specific UID folder being passed to the intent
        String uid;
        if (uidFolder != null) {
            uid = uidFolder;
        }
        else {
            // Default to the user
            uid = getUid();
        }

        // Check for having the database type
        StorageDataType type;
        if (dataType != null) {
            type = dataType;
        }
        else {
            // Default to the user
            type = StorageDataType.USERS;
        }

        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child(type.getType()).child(uid).child(filename);

        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");

                        // Get the public download URL
                        Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();

                        broadcastUploadFinished(downloadUri, fileUri);
                        showUploadFinishedNotification(downloadUri, fileUri);
                        taskCompleted();

                    }

                })
                .addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        broadcastUploadFinished(null, fileUri);
                        showUploadFinishedNotification(null, fileUri);
                        taskCompleted();

                    }

                });

    }

    /**
     * Broadcast finished upload (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {

        boolean success = downloadUrl != null;

        String action = success ? UPLOAD_COMPLETED : UPLOAD_ERROR;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_UPLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri);

        return LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);

    }

    /**
     * Show a notification for a finished upload.
     */
    private void showUploadFinishedNotification(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {

        // Make Intent to MainActivity
        Intent intent = new Intent(this, MainActivity.class)
                .putExtra(EXTRA_UPLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Make PendingIntent for notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* requestCode */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Set message and icon based on success or failure
        boolean success = downloadUrl != null;
        String message = success ? "Upload finished" : "Upload failed";
        int icon = success ? R.drawable.ic_check_black_24dp: R.drawable.ic_error_black_24dp;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(NOTIF_ID_DOWNLOAD, builder.build());

    }

    /**
     * Show notification with an indeterminate upload progress bar.
     */
    private void showUploadProgressNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Uploading...")
                .setProgress(0, 0, true)
                .setOngoing(true)
                .setAutoCancel(false);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(NOTIF_ID_DOWNLOAD, builder.build());

    }

    public static IntentFilter getIntentFilter() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(UPLOAD_COMPLETED);
        filter.addAction(UPLOAD_ERROR);

        return filter;

    }

}
