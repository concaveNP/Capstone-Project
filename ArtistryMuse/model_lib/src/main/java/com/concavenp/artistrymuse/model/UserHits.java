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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dave on 1/23/2017.
 */

public class UserHits implements Serializable {

    private static final long serialVersionUID = -7898109408617361289L;

    public List<UserResponseHit> hits = new ArrayList<>();
    public float max_score = 0.0f;
    public int total = 0;

    public UserHits() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<UserResponseHit> getHits() {
        return hits;
    }

    public void setHits(List<UserResponseHit> hits) {
        this.hits = hits;
    }

    public float getMax_score() {
        return max_score;
    }

    public void setMax_score(float max_score) {
        this.max_score = max_score;
    }

}
