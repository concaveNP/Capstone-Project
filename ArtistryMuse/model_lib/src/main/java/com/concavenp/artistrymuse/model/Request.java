package com.concavenp.artistrymuse.model;

/**
 * Created by dave on 12/22/2016.
 */

public class Request {

    public String index;
    public String query;
    public String type;
    public Integer from;
    public Integer size;

    public Request() {
        // do nothing
    }

    public Request(String index, String query, String type, int currentPage) {
        this.index = index;
        this.query = query;
        this.type = type;
        this.from = currentPage;
        this.size = 10;
    }
}
