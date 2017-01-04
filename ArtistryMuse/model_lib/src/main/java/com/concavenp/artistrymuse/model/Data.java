package com.concavenp.artistrymuse.model;

import java.util.List;

/**
 * Created by dave on 1/4/2017.
 */

public class Data {

    public List<User> users;
    public List<ArtProject> projects;

    public Data() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

}
