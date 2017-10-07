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
 * Created by dave on 12/22/2016.
 */
@SuppressWarnings("unused")
public class Request {

    public static final String SEARCH = "search";
    public static final String RESPONSE = "response";
    public static final String REQUEST = "request";
    public static final String FIREBASE = "firebase";

    public String index = "";
    public String query = "";
    public String type = "";
    public Integer from = 0;
    public Integer size = 0;

    public Request() {
        // Do nothing
    }

    public Request(String index, String query, String type, int currentPage) {
        this.index = index;
        this.query = query;
        this.type = type;
        this.from = currentPage;
        this.size = 10;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
