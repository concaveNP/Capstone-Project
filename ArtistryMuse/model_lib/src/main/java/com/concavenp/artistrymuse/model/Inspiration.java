/*
 * ArtistryMuse is an application that allows artist to share projects
 * they have created along with the inspirations behind them for others to
 * discover and enjoy.
 * Copyright (C) 2017  David A. Todd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.concavenp.artistrymuse.model;

/**
 * Created by dave on 11/3/2016.
 */

public class Inspiration {

    public Long creationDate = 0L;
    public String description = "";
    public String imageUid = "";
    public Long lastUpdateDate = 0L;
    public String name = "";
    public String uid = "";
    public String projectUid = "";

    public Inspiration() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * This is a convenience method to clearing out all of the data making up this class.
     */
    public void clear() {

        setCreationDate(0L);
        setDescription("");
        setImageUid("");
        setLastUpdateDate(0L);
        setName("");
        setUid("");
        setProjectUid("");

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImageUid() {
        return imageUid;
    }

    public void setImageUid(String imageUid) {
        this.imageUid = imageUid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getProjectUid() {
        return projectUid;
    }

    public void setProjectUid(String projectUid) {
        this.projectUid = projectUid;
    }
}
