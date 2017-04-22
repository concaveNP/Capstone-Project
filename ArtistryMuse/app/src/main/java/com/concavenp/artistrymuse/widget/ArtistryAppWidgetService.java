package com.concavenp.artistrymuse.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.model.Project;
import com.concavenp.artistrymuse.model.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
/**
 * Created by dave on 4/7/2017.
 */

public class ArtistryAppWidgetService extends Service {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ArtistryAppWidgetService.class.getSimpleName();

    protected DatabaseReference mDatabase;
    protected StorageReference mStorageRef;
    protected FirebaseAuth mAuth;
    protected FirebaseUser mUser;
    protected String mUid;
    protected FirebaseImageLoader mImageLoader;

    private int favoritesTotal = 0;
    private double averageRatingTotal = 0.0;
    private int viewsTotal = 0;

    @Override
    public void onCreate() {

        super.onCreate();

        // Initialize the Firebase Database connection
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize the Firebase Storage connection
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Initialize the Firebase Authentication connection
        mAuth = FirebaseAuth.getInstance();

        // Get the authenticated user
        mUser = mAuth.getCurrentUser();

        if (mUser != null) {
            mUid = mUser.getUid();
        }

        // Create the Firebase image loader
        mImageLoader = new FirebaseImageLoader();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final Context context = this.getApplicationContext();
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

// TODO: This block is something Vogella put in, looks like it could be useful
//                ComponentName thisWidget = new ComponentName(getApplicationContext(),
//                                MyWidgetProvider.class);
//                int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);

        final RemoteViews remoteViews = new RemoteViews(this .getApplicationContext().getPackageName(), R.layout.artistry_app_widget);

        // Initialize the data points before running the numbers
        favoritesTotal = 0;
        averageRatingTotal = 0.0;
        viewsTotal = 0;

        for (int widgetId : allWidgetIds) {

            final int id = widgetId;

            mDatabase.child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Log.d( TAG, "onDataChange: users" );

                    // Perform the JSON to Object conversion
                    User user = dataSnapshot.getValue(User.class);

                    // TODO: what to do when it is null

                    // Verify there is a user to work with
                    if (user != null) {

                        // Loop over all of the user's projects and tally up the data
                        for (String projectId : user.getProjects().values()) {

                            mDatabase.child("projects").child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Log.d( TAG, "onDataChange: projects" );

                                    // Perform the JSON to Object conversion
                                    Project project = dataSnapshot.getValue(Project.class);

                                    // TODO: what to do when it is null

                                    // Verify there is a user to work with
                                    if (project != null) {

                                        favoritesTotal += project.getFavorited();
                                        averageRatingTotal = (averageRatingTotal + project.getRating()) / 2;
                                        viewsTotal += project.getViews();

                                        String favoritesResult = Integer.toString(favoritesTotal);
                                        String ratingsResult = String.format("%.1f", averageRatingTotal);
                                        String viewsResult = Integer.toString(viewsTotal);

                                        populateTextView(remoteViews, favoritesResult, R.id.favorited_textView);
                                        populateTextView(remoteViews, ratingsResult, R.id.rating_textView);
                                        populateTextView(remoteViews, viewsResult, R.id.views_textView);

//                                        appWidgetManager.updateAppWidget(id, remoteViews);

                                    }

                                    populateImageView(remoteViews, R.drawable.ic_star_black_24dp, R.id.favorited_imageView);
                                    populateImageView(remoteViews, R.drawable.ic_remove_red_eye_black_24dp , R.id.rating_imageView);
                                    populateImageView(remoteViews, R.drawable.ic_thumb_up_black_24dp, R.id.views_imageView);

                                    appWidgetManager.updateAppWidget(id, remoteViews);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }

                            });

                        }


                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

                protected void populateImageView(RemoteViews rv, int resourceId, int id) {
                    rv.setImageViewResource(id, resourceId);
                }

                protected void populateTextView(RemoteViews rv, String text, int id) {

                    // Verify there is text to work with and empty out if nothing is there.
                    if ((text != null) && (!text.isEmpty())) {

                        rv.setTextViewText(id, text);

                    } else {

                        rv.setTextViewText(id, "");

                    }

                }

            });

//            // Register an onClickListener
//            Intent clickIntent = new Intent(this.getApplicationContext(), ArtistryAppWidgetProvider.class);
//
//            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
//
//            PendingIntent pendingIntent = PendingIntent.getBroadcast( getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
//            appWidgetManager.updateAppWidget(widgetId, remoteViews);

        }

        stopSelf();

        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;

    }

    protected String getUid() {

//        return mUser.getUid();

        // TODO: this will need to be figured out some other way and probably/maybe saved to local properties
        // must use the authUid (this is the getUid() call) to get the uid to be the DB primary key index to use as the myUserId value in the query - yuck, i'm doing this wrong

        // TODO: should not be hard coded
        //return "2a1d3365-118d-4dd7-9803-947a7103c730";
        //return "8338c7c0-e6b9-4432-8461-f7047b262fbc";
        //return "d0fc4662-30b3-4e87-97b0-d78e8882a518";
        //return "54d1e146-a114-45ea-ab66-389f5fd53e53";
        //return "0045d757-6cac-4a69-81e3-0952a3439a78";
        return "022ffcf3-38ac-425f-8fbe-382c90d2244f";

    }

}

