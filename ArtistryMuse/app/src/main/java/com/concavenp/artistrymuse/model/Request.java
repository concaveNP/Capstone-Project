package com.concavenp.artistrymuse.model;

/**
 * Created by dave on 12/22/2016.
 */

public class Request {

    public String index;
    public String query;
    public String type;

    public Request() {
        // do nothing
    }

    public Request(String index, String query, String type) {
        this.index = index;
        this.query = query;
        this.type = type;
    }
}
