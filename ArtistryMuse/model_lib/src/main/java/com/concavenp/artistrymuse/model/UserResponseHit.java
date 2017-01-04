package com.concavenp.artistrymuse.model;

/**
 * Created by dave on 12/22/2016.
 */

public class UserResponseHit {

    public String _id;
    public String _index;
    public float _score;
    public User _source;
    public String _type;

    public UserResponseHit() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

}
