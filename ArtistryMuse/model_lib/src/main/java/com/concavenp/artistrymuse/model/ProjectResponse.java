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
 * Created by dave on 12/22/2016.
 */

public class ProjectResponse implements Serializable {

    private static final long serialVersionUID = 7460045606575152605L;

    public Shard _shards = null;
    public ProjectHits hits = null;
    public boolean timed_out = false;
    public int took = 0;

    public ProjectResponse() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Shard get_shards() {
        return _shards;
    }

    public void set_shards(Shard _shards) {
        this._shards = _shards;
    }

    public ProjectHits getHits() {
        return hits;
    }

    public void setHits(ProjectHits hits) {
        this.hits = hits;
    }

    public boolean getTimed_out() {
        return timed_out;
    }

    public void setTimed_out(boolean timed_out) {
        this.timed_out = timed_out;
    }

    public int getTook() {
        return took;
    }

    public void setTook(int took) {
        this.took = took;
    }

}
