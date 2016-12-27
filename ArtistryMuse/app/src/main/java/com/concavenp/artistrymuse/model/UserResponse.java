package com.concavenp.artistrymuse.model;

import java.util.List;

/**
 * Created by dave on 12/22/2016.
 */

public class UserResponse {



    public List<UserResponseHit> hits;
    public float max_score;
    public int total;


    public UserResponse() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


}
