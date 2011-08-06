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

package org.xbmc.android.remote.business;

import org.xbmc.android.util.ClientFactory;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IEventClient;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.util.Log;

/**
 * Super class of the wrappers, keeps common code.
 * 
 * @author Team XBMC
 */
public class EventClientManager extends org.xbmc.android.remote.lib.business.EventClientManager {
	protected IEventClient getEventClient(INotifiableManager manager, Context context) throws WifiStateException {
	    Log.d(TAG, "Getting an EventClient ClientFactory");
	    return ClientFactory.getInstance().getEventClient(manager, context);
	}
    
	protected static final String TAG = "EventClientManager";
	protected static final Boolean DEBUG = false;
	
	protected static EventClientManager sInstance = null;

	public static EventClientManager getInstance(INotifiableController controller, Context context) {
	    Log.d(TAG, "Making and returning an EventClientManager");
	    if (sInstance == null) {
	        sInstance = new EventClientManager(controller, context);
	    }
	    return sInstance;
	}

	public EventClientManager(INotifiableController controller, Context context) {
	    super(controller, context);
	}
	
}