package com.concavenp.artistrymuse.services;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Base class for Services that keep track of the number of active jobs and self-stop when the
 * count is zero.
 *
 * Created by dave on 11/21/2016.
 *
 * References:
 * - Essentially copied from the Firebase Quickstart example for Firebase Storage use.
 */
public abstract class BaseTaskService extends BaseService {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = BaseTaskService.class.getSimpleName();

    private int mNumTasks = 0;

    public void taskStarted() {
        changeNumberOfTasks(1);
    }

    public void taskCompleted() {
        changeNumberOfTasks(-1);
    }

    private synchronized void changeNumberOfTasks(int delta) {

        Log.d(TAG, "changeNumberOfTasks:" + mNumTasks + ":" + delta);
        mNumTasks += delta;

        // If there are no tasks left, stop the service
        if (mNumTasks <= 0) {

            Log.d(TAG, "stopping");
            stopSelf();

        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
