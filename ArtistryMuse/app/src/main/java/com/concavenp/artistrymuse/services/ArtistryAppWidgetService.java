/*
 * ArtistryMuse is an application that allows artist to share projects
 * they have created along with the inspirations behind them for others to
 * discover and enjoy.
 * Copyright (C) 2017  David A. Todd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.concavenp.artistrymuse.services;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import static com.concavenp.artistrymuse.StorageDataType.PROJECTS;
import static com.concavenp.artistrymuse.StorageDataType.USERS;

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

            mDatabase.child(USERS.getType()).child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Perform the JSON to Object conversion
                    User user = dataSnapshot.getValue(User.class);

                    // Verify there is a user to work with
                    if (user != null) {

                        // Set the title and username for the header of the widget and update it
                        final String widgetTitle = getString(R.string.widget_title) + "  " + getString(R.string.user_indication_symbol) + user.getUsername();
                        remoteViews.setTextViewText(R.id.widget_title_textView, widgetTitle);
                        appWidgetManager.updateAppWidget(id, remoteViews);

                        Map<String, String> projects = user.getProjects();

                        // Protection
                        if (projects != null) {

                            // Loop over all of the user's projects and tally up the data
                            for (String projectId : projects.values()) {

                                // Protection
                                if ((projectId != null) && (!projectId.isEmpty())) {

                                    mDatabase.child(PROJECTS.getType()).child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            // Perform the JSON to Object conversion
                                            Project project = dataSnapshot.getValue(Project.class);

                                            // Verify there is a user to work with
                                            if (project != null) {

                                                try {

                                                    // Get the needed data out from the JSON
                                                    favoritesTotal += project.getFavorited();
                                                    averageRatingTotal = (averageRatingTotal + project.getRating()) / 2;
                                                    viewsTotal += project.getViews();

                                                    // Convert to strings
                                                    String favoritesResult = Integer.toString(favoritesTotal);
                                                    String ratingsResult = String.format(getString(R.string.number_format), averageRatingTotal);
                                                    String viewsResult = Integer.toString(viewsTotal);

                                                    // Update the views
                                                    remoteViews.setTextViewText(R.id.favorited_textView, favoritesResult);
                                                    remoteViews.setTextViewText(R.id.rating_textView, ratingsResult);
                                                    remoteViews.setTextViewText(R.id.views_textView, viewsResult);

                                                } catch(Exception ex) {

                                                    Log.e(TAG, "Unable to update widget due to problems with the data");

                                                }

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

