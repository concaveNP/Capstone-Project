package com.concavenp.artistrymuse.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dave on 10/14/2016.
 */

public class Project {

    public Long creationDate;
    public String description;
    public Integer favorited;
    public Map<String, Inspiration> inspirations = new HashMap<>();
    public Long lastUpdateDate;
    public String mainImageUid;
    public String name;
    public String ownerUid;
    public Boolean published;
    public Long publishedDate;
    public Double rating;
    public Integer ratingsCount;
    public String uid;
    public Integer views;

    public Project() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Long getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Long publishedDate) {
        this.publishedDate = publishedDate;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(Integer ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getFavorited() {
        return favorited;
    }

    public void setFavorited(Integer favorited) {
        this.favorited = favorited;
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

    public String getMainImageUid() {
        return mainImageUid;
    }

    public void setMainImageUid(String mainImageUid) {
        this.mainImageUid = mainImageUid;
    }

    public Map<String, Inspiration> getInspirations() {
        return inspirations;
    }

    public void setInspirations(Map<String, Inspiration> inspirations) {
        this.inspirations = inspirations;
    }
}
