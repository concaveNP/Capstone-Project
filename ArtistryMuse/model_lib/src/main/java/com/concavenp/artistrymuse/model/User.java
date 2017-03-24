package com.concavenp.artistrymuse.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dave on 10/14/2016.
 */

/**
 * This class represents the the data model of a "User" object.  This data will be stored within
 * the DB
 */
public class User {

    public String authUid;
    public Long creationDate;
    public String description;
    public Map<String, Favorite> favorites = new HashMap<>();
    public Integer followedCount;
    public Map<String, Following> following = new HashMap<>();
    public String headerImageUid;
    public Long lastUpdatedDate;
    public String name;
    public String profileImageUid;
    public Map<String, String> projects = new HashMap<>();
    public String summary;
    public String uid;
    public String username;

    public User() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Map<String, String> getProjects() {
        return projects;
    }

    public void setProjects(Map<String, String> projects) {
        this.projects = projects;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthUid() {
        return authUid;
    }

    public void setAuthUid(String authUid) {
        this.authUid = authUid;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(Map<String, Favorite> favorites) {
        this.favorites = favorites;
    }

    public Integer getFollowedCount() {
        return followedCount;
    }

    public void setFollowedCount(Integer followedCount) {
        this.followedCount = followedCount;
    }

    public Map<String, Following> getFollowing() {
        return following;
    }

    public void setFollowing(Map<String, Following> following) {
        this.following = following;
    }

    public String getHeaderImageUid() {
        return headerImageUid;
    }

    public void setHeaderImageUid(String headerImageUid) {
        this.headerImageUid = headerImageUid;
    }

    public Long getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Long lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUid() {
        return profileImageUid;
    }

    public void setProfileImageUid(String profileImageUid) {
        this.profileImageUid = profileImageUid;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
