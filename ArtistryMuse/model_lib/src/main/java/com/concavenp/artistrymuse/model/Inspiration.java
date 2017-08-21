package com.concavenp.artistrymuse.model;

/**
 * Created by dave on 11/3/2016.
 */

public class Inspiration {

    public Long creationDate = 0L;
    public String description = "";
    public String imageUid = "";
    public Long lastUpdateDate = 0L;
    public String name = "";
    public String uid = "";
    public String projectUid = "";

    public Inspiration() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * This is a convenience method to clearing out all of the data making up this class.
     */
    public void clear() {

        setCreationDate(0L);
        setDescription("");
        setImageUid("");
        setLastUpdateDate(0L);
        setName("");
        setUid("");
        setProjectUid("");

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImageUid() {
        return imageUid;
    }

    public void setImageUid(String imageUid) {
        this.imageUid = imageUid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getProjectUid() {
        return projectUid;
    }

    public void setProjectUid(String projectUid) {
        this.projectUid = projectUid;
    }
}
