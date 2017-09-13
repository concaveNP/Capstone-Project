package com.concavenp.artistrymuse;

/**
 * Created by dave on 1/30/2017.
 */

public enum UserInteractionType {

    // TODO: strings
    DETAILS("details"),
    EDIT("edit"),
    NONE("none");

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

    public static UserInteractionType fromType(String inputType) {
        UserInteractionType  result = null;

        for (UserInteractionType checkType : UserInteractionType.values()) {
            if (checkType.getType().equals(inputType)) {
                result = checkType;
                break;
            }
        }

        return result;
    }

}
