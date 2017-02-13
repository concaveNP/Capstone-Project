package com.concavenp.artistrymuse.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 12/22/2016.
 */

public class ProjectResponse {

    public Shard _shards;
    public ProjectHits hits;
    public boolean timed_out;
    public int took;

    public ProjectResponse() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Shard get_shards() {
        return _shards;
    }

    public void set_shards(Shard _shards) {
        this._shards = _shards;
    }

    public ProjectHits getHits() {
        return hits;
    }

    public void setHits(ProjectHits hits) {
        this.hits = hits;
    }

    public boolean getTimed_out() {
        return timed_out;
    }

    public void setTimed_out(boolean timed_out) {
        this.timed_out = timed_out;
    }

    public int getTook() {
        return took;
    }

    public void setTook(int took) {
        this.took = took;
    }
}
