package com.concavenp.artistrymuse;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by dave on 3/25/2017.
 */
public class ArtistryMuseApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        // Enable disk persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }

}
