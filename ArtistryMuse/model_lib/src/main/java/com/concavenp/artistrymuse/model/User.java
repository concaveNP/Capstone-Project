package com.concavenp.artistrymuse.model;

import java.util.HashMap;
import java.util.Map;

import jdk.nashorn.internal.ir.annotations.Ignore;

/**
 * This class represents the the data model of a "User" object.  This data will be stored within
 * the DB
 */
@SuppressWarnings("unused")
public class User {

    public static final String USER = "user";

    public static final String AUTH_UID = "authUid";
    public static final String CREATION_DATE = "creationDate";
    public static final String DESCRIPTION = "description";
    public static final String FAVORITES = "favorites";
    public static final String FOLLOWED_COUNT = "followedCount";
    public static final String FOLLOWING = "following";
    public static final String HEADER_IMAGE_UID = "headerImageUid";
    public static final String LAST_UPDATED_DATE = "lastUpdatedDate";
    public static final String NAME = "name";
    public static final String PROFILE_IMAGE_UID = "profileImageUid";
    public static final String PROJECTS = "projects";
    public static final String SUMMARY = "summary";
    public static final String UID = "uid";
    public static final String USERNAME = "username";

    public String authUid = "";
    public Long creationDate = 0L;
    public String description = "";
    public Map<String, Favorite> favorites = new HashMap<>();
    public Integer followedCount = 0;
    public Map<String, Following> following = new HashMap<>();
    public String headerImageUid = "";
    public Long lastUpdatedDate = 0L;
    public String name = "";
    public String profileImageUid = "";
    public Map<String, String> projects = new HashMap<>();
    public String summary = "";
    public String uid = "";
    public String username = "";

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
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
