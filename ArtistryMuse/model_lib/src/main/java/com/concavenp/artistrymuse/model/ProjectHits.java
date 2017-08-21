package com.concavenp.artistrymuse.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 1/23/2017.
 */

public class ProjectHits {

    public List<ProjectResponseHit> hits = new ArrayList<>();
    public float max_score = 0.0f;
    public int total = 0;

    public ProjectHits() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<ProjectResponseHit> getHits() {
        return hits;
    }

    public void setHits(List<ProjectResponseHit> hits) {
        this.hits = hits;
    }

    public float getMax_score() {
        return max_score;
    }

    public void setMax_score(float max_score) {
        this.max_score = max_score;
    }

}
