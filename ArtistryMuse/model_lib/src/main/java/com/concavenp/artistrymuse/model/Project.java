/*
 * ArtistryMuse is an application that allows artist to share projects
 * they have created along with the inspirations behind them for others to
 * discover and enjoy.
 * Copyright (C) 2017  David A. Todd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.concavenp.artistrymuse.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dave on 10/14/2016.
 */
@SuppressWarnings("unused")
public class Project implements Serializable {

    private static final long serialVersionUID = 2006663677075260559L;

    public static final String PROJECT = "project";

    public static final String CREATION_DATE = "creationDate";
    public static final String DESCRIPTION = "description";
    public static final String FAVORITED = "favorited";
    public static final String INSPIRATIONS = "inspirations";
    public static final String LAST_UPDATE_DATE = "lastUpdateDate";
    public static final String MAIN_IMAGE_UID = "mainImageUid";
    public static final String NAME = "name";
    public static final String OWNER_UID = "ownerUid";
    public static final String PUBLISHED = "published";
    public static final String PUBLISHED_DATE = "publishedDate";
    public static final String RATING = "rating";
    public static final String RATINGS_COUNT = "ratingsCount";
    public static final String UID = "uid";
    public static final String VIEWS = "views";

    public Long creationDate = 0L;
    public String description = "";
    public Integer favorited = 0;
    public Map<String, Inspiration> inspirations = new HashMap<>();
    public Long lastUpdateDate = 0L;
    public String mainImageUid = "";
    public String name = "";
    public String ownerUid = "";
    public Boolean published = true;
    public Long publishedDate = 0L;
    public Double rating = 0.0;
    public Integer ratingsCount = 0;
    public String uid = "";
    public Integer views = 0;

    public Project() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * This is a convenience method to clearing out all of the data making up this class.
     */
    public void clear() {

        setUid("");
        setOwnerUid("");
        setName("");
        setDescription("");
        setPublished(false);
        setPublishedDate(0L);
        setRating(0.0);
        setRatingsCount(0);
        setViews(0);
        setFavorited(0);
        setCreationDate(0L);
        setLastUpdateDate(0L);
        setMainImageUid("");

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
