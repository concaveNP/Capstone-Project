package com.concavenp.artistrymuse.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.model.Favorite;
import com.concavenp.artistrymuse.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import static com.concavenp.artistrymuse.StorageDataType.USERS;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends BaseFragment {

    /**
     * The logging tag string to be associated with log data for this class
     */
    @SuppressWarnings("unused")
    private static final String TAG = FavoritesFragment.class.getSimpleName();

    // This flipper allows the content of the fragment to show the user either the list of their
    // favorite projects or message stating they need to favorite some projects (list is empty).
    private ViewFlipper mFlipper;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment FavoritesFragment.
     */
    public static FavoritesFragment newInstance() {

        FavoritesFragment fragment = new FavoritesFragment();

        return fragment;

    }

    /**
     * Required empty public constructor
     */
    public FavoritesFragment() {

        // Do nothing

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_favorites, container, false);

        // Save off the flipper for use in deciding which view to show
        mFlipper = (ViewFlipper) mainView.findViewById(R.id.fragment_favorites_ViewFlipper);

        // Refresh the data displayed
        refresh();

        return mainView;
    }

    /**
     * Performs the work of re-querying the cloud services for data to be displayed.  An adapter
     * is used to translate the data retrieved into the populated displayed view.
     */
    private void refresh() {

        // First check to see if the user favorited any projects anybody yet
        mDatabase.child(USERS.getType()).child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Perform the JSON to Object conversion
                final User user = dataSnapshot.getValue(User.class);

                // Verify there is a user to work with
                if (user != null) {

                    Map<String, Favorite> favorites = user.getFavorites();

                    // Check to see if the user has any favorites
                    if ((favorites != null) && (!favorites.isEmpty())) {

                        // Yes, the user is following someone, so flip to that view
                        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.favorites_frame)));

                    }
                    else {

                        // View flip to the "Follow People"
                        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mFlipper.findViewById(R.id.fragment_favorites_nobody_TextView)));

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }

        });

    }

}

