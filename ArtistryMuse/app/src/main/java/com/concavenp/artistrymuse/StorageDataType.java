package com.concavenp.artistrymuse;

/**
 * Created by dave on 1/30/2017.
 */

public enum StorageDataType {

    USERS("users"),
    PROJECTS("projects");

    private String type;

    StorageDataType(String type) {

        setType(type);

    }

    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }

}
