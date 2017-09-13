/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.concavenp.artistrymuse.services;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.concavenp.artistrymuse.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service to handle downloading files from Firebase Storage.
 */
public class DownloadService extends BaseTaskService {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = DownloadService.class.getSimpleName();

    /** Actions **/
    // TODO: strings
    public static final String ACTION_DOWNLOAD = "action_download";
    public static final String DOWNLOAD_COMPLETED = "download_completed";
    public static final String DOWNLOAD_ERROR = "download_error";

    /** Extras **/
    // TODO: strings
    public static final String EXTRA_DOWNLOAD_FILENAME = "extra_download_filename";
    public static final String EXTRA_BYTES_DOWNLOADED = "extra_bytes_downloaded";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (ACTION_DOWNLOAD.equals(intent.getAction())) {

            // Get the path to download from the intent
            String filename = intent.getStringExtra(EXTRA_DOWNLOAD_FILENAME);

            // TODO: strings
            final String fileReference = getString(R.string.users_directory_name) + "/" + getUid() + "/" + filename;

            downloadFromPath(fileReference);
        }

        return START_REDELIVER_INTENT;

    }

    private void downloadFromPath(final String downloadPath) {
        // TODO: strings
        Log.d(TAG, "downloadFromPath:" + downloadPath);

        // Mark task started
        taskStarted();

        // Download and get total bytes
        mStorageRef.child(downloadPath).getStream(
                new StreamDownloadTask.StreamProcessor() {
                    @Override
                    public void doInBackground(StreamDownloadTask.TaskSnapshot taskSnapshot, InputStream inputStream) throws IOException {
                        // Close the stream at the end of the Task
                        inputStream.close();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                        // TODO: strings
                        Log.d(TAG, "download:SUCCESS");

                        // Send success broadcast with number of bytes downloaded
                        Intent broadcast = new Intent(DOWNLOAD_COMPLETED);
                        broadcast.putExtra(EXTRA_DOWNLOAD_FILENAME, downloadPath);
                        broadcast.putExtra(EXTRA_BYTES_DOWNLOADED, taskSnapshot.getTotalByteCount());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);

                        // Mark task completed
                        taskCompleted();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // TODO: strings
                        Log.w(TAG, "download:FAILURE", exception);

                        // Send failure broadcast
                        Intent broadcast = new Intent(DOWNLOAD_ERROR);
                        broadcast.putExtra(EXTRA_DOWNLOAD_FILENAME, downloadPath);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);

                        // Mark task completed
                        taskCompleted();
                    }
                });
    }

}
