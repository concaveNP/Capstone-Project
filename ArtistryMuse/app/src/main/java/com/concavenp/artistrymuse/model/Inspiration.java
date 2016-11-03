package com.concavenp.artistrymuse.model;

import java.util.Date;

/**
 * Created by dave on 11/3/2016.
 */

public class Inspiration {

    public String uid;
    public String imageUid;
    public String description;
    public String name;
    public Date createdDate;
    public Date lastUpdate;

    public Inspiration() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
}
