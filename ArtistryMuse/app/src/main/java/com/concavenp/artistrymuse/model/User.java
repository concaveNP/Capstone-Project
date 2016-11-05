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

    public String uid;
    public String username;
    public String name;
    public String description;
    public Long creationDate;
    public Long lastUpdateDate;
    public String avatarImageUid;
    public List<String> artProjects;
    public List<Favorite> favorites;
    public List<Following> followings;

    public User() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

}
