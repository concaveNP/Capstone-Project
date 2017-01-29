package com.concavenp.artistrymuse.interfaces;

/**
 * Created by dave on 1/27/2017.
 */

public interface OnDetailsInteractionListener {

    public enum DETAILS_TYPE {
       USER,PROJECT
    }

    void onDetailsSelection(String uid, DETAILS_TYPE type);


}
