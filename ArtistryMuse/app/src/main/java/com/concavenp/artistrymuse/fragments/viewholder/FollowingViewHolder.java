package com.concavenp.artistrymuse.fragments.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.concavenp.artistrymuse.R;
import com.concavenp.artistrymuse.model.Following;

import java.util.Date;

/**
 * Created by dave on 12/2/2016.
 */

public class FollowingViewHolder extends RecyclerView.ViewHolder {

    public TextView uidTextView;
    public EditText lastUpdateEditText;

    public FollowingViewHolder(View itemView) {

        super(itemView);

        uidTextView = (TextView) itemView.findViewById(R.id.uid);
        lastUpdateEditText = (EditText) itemView.findViewById(R.id.last_update);


    }

    public void bindToPost(Following following, View.OnClickListener clickListener) {

        uidTextView.setText(following.uid);
        lastUpdateEditText.setText(new Date(following.lastUpdatedDate).toString());

        uidTextView.setOnClickListener(clickListener);

    }

}
