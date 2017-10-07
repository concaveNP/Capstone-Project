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

package com.concavenp.artistrymuse;

/**
 * Created by dave on 1/30/2017.
 */

public enum UserInteractionType {

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
