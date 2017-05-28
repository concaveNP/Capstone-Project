package com.concavenp.artistrymuse.services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
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

public class ArtistryAppWidgetService extends BaseService {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = ArtistryAppWidgetService.class.getSimpleName();

    // Values used to build up the stats
    private int favoritesTotal = 0;
    private double averageRatingTotal = 0.0;
    private int viewsTotal = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final Context context = this.getApplicationContext();
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

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

                    // Perform the JSON to Object conversion
                    User user = dataSnapshot.getValue(User.class);

                    // Verify there is a user to work with
                    if (user != null) {

                        // Set the title and username for the header of the widget and update it
                        final String widgetTitle = context.getResources().getString(R.string.widget_title) + "  @" + user.getUsername();
                        remoteViews.setTextViewText(R.id.widget_title_textView, widgetTitle);
                        appWidgetManager.updateAppWidget(id, remoteViews);

                        // Loop over all of the user's projects and tally up the data
                        for (String projectId : user.getProjects().values()) {

                            mDatabase.child("projects").child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    // Perform the JSON to Object conversion
                                    Project project = dataSnapshot.getValue(Project.class);

                                    // Verify there is a user to work with
                                    if (project != null) {

                                        // Get the needed data out from the JSON
                                        favoritesTotal += project.getFavorited();
                                        averageRatingTotal = (averageRatingTotal + project.getRating()) / 2;
                                        viewsTotal += project.getViews();

                                        // Convert to strings
                                        String favoritesResult = Integer.toString(favoritesTotal);
                                        String ratingsResult = String.format("%.1f", averageRatingTotal);
                                        String viewsResult = Integer.toString(viewsTotal);

                                        // Update the views
                                        remoteViews.setTextViewText(R.id.favorited_textView, favoritesResult);
                                        remoteViews.setTextViewText(R.id.rating_textView, ratingsResult);
                                        remoteViews.setTextViewText(R.id.views_textView, viewsResult);

                                    }

                                    // Populate the images
                                    remoteViews.setImageViewResource(R.id.favorited_imageView, R.drawable.ic_star_black_24dp);
                                    remoteViews.setImageViewResource(R.id.rating_imageView, R.drawable.ic_remove_red_eye_black_24dp);
                                    remoteViews.setImageViewResource(R.id.views_imageView, R.drawable.ic_thumb_up_black_24dp);

                                    // Update all of the views making up the AppWidget
                                    appWidgetManager.updateAppWidget(id, remoteViews);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Do nothing
                                }

                            });

                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do nothing
                }

            });

        }

        stopSelf();

        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;

    }

}

