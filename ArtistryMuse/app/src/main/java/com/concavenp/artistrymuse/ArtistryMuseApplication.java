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

import android.content.Intent;
import android.support.multidex.MultiDexApplication;

import com.concavenp.artistrymuse.services.UserAuthenticationService;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by dave on 3/25/2017.
 */
public class ArtistryMuseApplication extends MultiDexApplication {

    @Override
    public void onCreate() {

        super.onCreate();

        // Enable disk persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Start authentication service
        startService(new Intent(this, UserAuthenticationService.class));

    }

}
