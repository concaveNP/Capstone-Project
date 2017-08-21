package com.concavenp.artistrymuse.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dave on 1/4/2017.
 */

public class Data {

    public Map<String,User> users = new HashMap<>();
    public Map<String,Project> projects = new HashMap<>();
    public Map<String,String> auth = new HashMap<>();

    public Data() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public Map<String, Project> getProjects() {
        return projects;
    }

    public void setProjects(Map<String, Project> projects) {
        this.projects = projects;
    }

    public Map<String, String> getAuth() {
        return auth;
    }

    public void setAuth(Map<String, String> auth) {
        this.auth = auth;
    }
}
