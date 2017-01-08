package com.concavenp.artistrymuse.model;

/**
 * Created by dave on 11/3/2016.
 */

public class Favorite {

    public String uid;
    public Double rating;
    public Long favoritedDate;

    public Favorite() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Long getFavoritedDate() {
        return favoritedDate;
    }

    public void setFavoritedDate(Long favoritedDate) {
        this.favoritedDate = favoritedDate;
    }
}
