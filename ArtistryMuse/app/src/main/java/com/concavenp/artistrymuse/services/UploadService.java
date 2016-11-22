package com.concavenp.artistrymuse.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class UploadService extends Service {

    private StorageReference mStorageRef;

    public UploadService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    // TODO: what is this method used for?  The example uses nullable...???
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }

}
