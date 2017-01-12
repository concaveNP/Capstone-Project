package com.concavenp.artistrymuse.fragments.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.concavenp.artistrymuse.fragments.viewholder.UserResponseViewHolder;
import com.concavenp.artistrymuse.model.UserResponseHit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 12/26/2016.
 */

public class UserSearchResultAdapter extends RecyclerView.Adapter<UserResponseViewHolder> {

    /**
     * The logging tag string to be associated with log data for this class
     */
    private static final String TAG = UserSearchResultAdapter.class.getSimpleName();

    private List<UserResponseHit> mResultItems = new ArrayList<>();

    public UserSearchResultAdapter(MovieListingFragment.OnMovieSelectionListener listener) {
        super();
        mListener = listener;
    }

    @Override
    public UserResponseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ImageView imageView;

        imageView = new ImageView(parent.getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_START);
        imageView.setAdjustViewBounds(true);
        imageView.setClickable(true);

        ViewHolder viewHolder = new ViewHolder(imageView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        // Get the ImageView that will be manipulated
        ImageView imageView = holder.mImageView;

        // Get the context that will be used
        Context context = imageView.getContext();

        // Get the model data that will be used to determine what image will be displayed
        MovieItems.MovieItem item = mMovieItems.get(position);

        // Get the movie poster UID from the GSON object
        String poster = mMovieItems.get(position).getPoster_path();
        if (poster != null) {
            String posterURL = context.getResources().getString(R.string.base_url_image_retrieval) + poster;
            Picasso.with(context).load(posterURL).into(imageView);
        }

        // Add a click listener to the view in order for the user to get more details about a selected movie
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Notify the the listener (aka MainActivity) of the movie selection
                mListener.onMovieSelection(mMovieItems.get(position));

            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovieItems.size();
    }

    public void add(MovieItems movieItems) {
        mMovieItems.addAll(movieItems.getResults());
        this.notifyDataSetChanged();
    }

    public void clearData() {
        mMovieItems.clear();
        this.notifyDataSetChanged();
    }

}
