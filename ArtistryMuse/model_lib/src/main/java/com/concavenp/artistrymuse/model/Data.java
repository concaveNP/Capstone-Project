package com.concavenp.artistrymuse.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 1/4/2017.
 */

public class Data {

    public List<User> users = new ArrayList<>();
    public List<ArtProject> projects = new ArrayList<>();

    public Data() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

}
