package com.concavenp.artistrymuse.model;

import java.util.Date;
import java.util.List;

/**
 * Created by dave on 10/14/2016.
 */

/**
 * This class represents the the data model of a "User" object.  This data will be stored within
 * the DB
 */
public class User {

    public List<ArtProjectOwner> artProjects;
    public String authUid;
    public Long creationDate;
    public String description;
    public List<Favorite> favorites;
    public Integer followedCount;
    public List<Following> following;
    public String headerImageUid;
    public Long lastUpdatedDate;
    public String name;
    public String profileImageUid;
    public String uid;
    public String username;

    public User() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

}
