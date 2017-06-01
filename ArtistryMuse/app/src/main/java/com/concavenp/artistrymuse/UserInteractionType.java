package com.concavenp.artistrymuse;

/**
 * Created by dave on 1/30/2017.
 */

public enum UserInteractionType {

    DETAILS("details"),
    EDIT("edit");

    private String type;

    UserInteractionType(String type) {

        setType(type);

    }

    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }

}
