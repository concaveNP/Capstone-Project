package com.concavenp.artistrymuse.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 1/23/2017.
 */

public class Hits {

    public List<UserResponseHit> hits = new ArrayList<>();
    public float max_score;
    public int total;

    public Hits() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<UserResponseHit> getHits() {
        return hits;
    }

    public void setHits(List<UserResponseHit> hits) {
        this.hits = hits;
    }

    public float getMax_score() {
        return max_score;
    }

    public void setMax_score(float max_score) {
        this.max_score = max_score;
    }

}
