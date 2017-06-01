package com.concavenp.artistrymuse.interfaces;

import com.concavenp.artistrymuse.StorageDataType;
import com.concavenp.artistrymuse.UserInteractionType;

/**
 * Created by dave on 1/27/2017.
 */
public interface OnInteractionListener {

    void onInteractionSelection(String uid, StorageDataType storageDataType, UserInteractionType interactionType);

}
