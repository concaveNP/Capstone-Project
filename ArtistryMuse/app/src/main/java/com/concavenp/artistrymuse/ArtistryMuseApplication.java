package com.concavenp.artistrymuse;

import android.content.Intent;
import android.support.multidex.MultiDexApplication;

import com.concavenp.artistrymuse.services.UserAuthenticationService;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by dave on 3/25/2017.
 */
public class ArtistryMuseApplication extends MultiDexApplication {

    @Override
    public void onCreate() {

        super.onCreate();

        // Enable disk persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Start authentication service
        startService(new Intent(this, UserAuthenticationService.class));

    }

}
