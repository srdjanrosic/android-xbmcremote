/*
 *      Copyright (C) 2005-2009 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.remote.presentation.controller;

import org.xbmc.android.remote.presentation.activity.HostSettingsActivity;
import org.xbmc.android.remote.presentation.activity.SettingsActivity;
import org.xbmc.android.util.ClientFactory;
import org.xbmc.android.util.HostFactory;
import org.xbmc.api.object.Host;

import android.content.Context;

/**
 * Every controller should extend this class. Takes care of the messages.
 * 
 * @author Team XBMC
 */
public abstract class AbstractController extends org.xbmc.android.remote.lib.presentation.controller.AbstractController {

    // These static methods are not the same static methods used in the AbstractController superclass.
    protected void HostFactoryReadHost(Context context) {
        HostFactory.readHost(context);
    }
    
    protected Host getHostFactoryHost() {
        return HostFactory.host;
    }
    
    protected void ClientFactoryResetClient(Host host) {
        ClientFactory.getInstance().resetClient(host);
    }
    
    protected Class<?> getHostSettingsActivityClass() {
        return HostSettingsActivity.class;
    }

    protected Class<?> getSettingsActivityClass() {
        return SettingsActivity.class;      
    }
}