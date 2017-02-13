package com.concavenp.artistrymuse.model;

/**
 * Created by dave on 12/22/2016.
 */

public class UserResponse {

    public Shard _shards;
    public UserHits hits;
    public boolean timed_out;
    public int took;

    public UserResponse() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Shard get_shards() {
        return _shards;
    }

    public void set_shards(Shard _shards) {
        this._shards = _shards;
    }

    public UserHits getHits() {
        return hits;
    }

    public void setHits(UserHits hits) {
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
