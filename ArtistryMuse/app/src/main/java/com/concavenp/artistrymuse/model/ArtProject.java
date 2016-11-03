package com.concavenp.artistrymuse.model;

import java.util.Date;
import java.util.List;

/**
 * Created by dave on 10/14/2016.
 */

public class ArtProject {

    public String uid;
    public String ownerUid;
    public String name;
    public String description;
    public Boolean published;
    public Date publishedDate;
    public Double rating;
    public Integer rates;
    public Integer views;
    public Integer favorited;
    public Date creationDate;
    public Date lastUpdate;
    public String mainImageUid;
    public List<Inspiration> inspirations;

    public ArtProject() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


}
