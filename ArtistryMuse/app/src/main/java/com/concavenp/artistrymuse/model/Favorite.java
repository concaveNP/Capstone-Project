package com.concavenp.artistrymuse.model;

import java.util.Date;

/**
 * Created by dave on 11/3/2016.
 */

public class Favorite {

    public String uid;
    public Double rating;
    public Date favoriteDate;

    public Favorite() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

}
