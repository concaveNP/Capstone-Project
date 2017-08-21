package com.concavenp.artistrymuse.model;

/**
 * Created by dave on 1/23/2017.
 */

public class Shard {

    public int failed = 0;
    public int successful = 0;
    public int total = 0;

    public Shard() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getSuccessful() {
        return successful;
    }

    public void setSuccessful(int successful) {
        this.successful = successful;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

}
