package com.concavenp.artistrymuse.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 12/22/2016.
 */

public class ProjectResponse {



    public List<ProjectResponseHit> hits = new ArrayList<>();
    public float max_score;
    public int total;


    public ProjectResponse() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
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

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
