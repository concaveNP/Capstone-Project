package com.concavenp.artistrymuse;

/**
 * Created by dave on 1/30/2017.
 */

public enum StorageDataType {

    // TODO: strings
    USERS("users"),
    PROJECTS("projects"),
    INSPIRATIONS("inspirations"),
    AUTH("auth");

    private String type;

    StorageDataType(String type) {

        setType(type);

    }

    private void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static StorageDataType fromType(String inputType) {
        StorageDataType result = null;

        for (StorageDataType checkType : StorageDataType.values()) {
            if (checkType.getType().equals(inputType)) {
                result = checkType;
                break;
            }
        }

        return result;
    }

}
