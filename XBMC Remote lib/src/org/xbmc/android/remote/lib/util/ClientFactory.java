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

package org.xbmc.android.remote.lib.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.xbmc.android.util.WifiHelper;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IEventClient;
import org.xbmc.android.remote.lib.api.object.Host;
import org.xbmc.eventclient.EventClient;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.util.Log;

public class ClientFactory {
    // Singleton
    protected ClientFactory() {}
    protected static class ClientFactoryHolder {
        public static ClientFactory instance = new ClientFactory();
    }
    public static ClientFactory getInstance() {
        return ClientFactoryHolder.instance;
    }

    protected Host getCurrentHostFromFactory() {
        Log.d(TAG, "getting host from lib HostFactory");
        return HostFactory.host;
    }
    
    
	protected EventClient sEventClient;
	
	protected static final String TAG = "ClientFactory";
	protected static final String NAME = "Android XBMC Remote";
	
	protected void assertWifiState(Context context) throws WifiStateException {
		if (context != null && getCurrentHostFromFactory() != null && getCurrentHostFromFactory().wifi_only){
			final int state = WifiHelper.getInstance(context).getWifiState();
			switch (state) {
			case WifiHelper.WIFI_STATE_DISABLED:
			case WifiHelper.WIFI_STATE_UNKNOWN:
				throw new WifiStateException(state);
			}
		}
	}
	  
	/**
     * Returns an instance of the Event Server Client. Instantiation takes
     * place only once, otherwise the first instance is returned.
     * 
     * @param manager Upper layer reference
     * @return Client for XBMC's Event Server
	 * @throws WifiStateException 
     */
	public IEventClient getEventClient(INotifiableManager manager, Context context) throws WifiStateException {
	    assertWifiState(context);
	    if (sEventClient == null) {
            final Host host = getCurrentHostFromFactory();
            if (host != null) {
                try {
                    Log.i(TAG, "EventClient being made on " + host.addr);
                    final InetAddress addr = Inet4Address.getByName(host.addr);
                    sEventClient = new EventClient(addr, host.esPort > 0 ? host.esPort : Host.DEFAULT_EVENTSERVER_PORT, NAME);
                    Log.i(TAG, "EventClient created on " + addr);
                } catch (UnknownHostException e) {
                    manager.onMessage("EventClient: Cannot parse address \"" + host.addr + "\".");
                    Log.e(TAG, "EventClient: Cannot parse address \"" + host.addr + "\".");
                    sEventClient = new EventClient(NAME);
                }
            } else {
                manager.onMessage("EventClient: Failed to read host settings.");
                Log.e(TAG, "EventClient: Failed to read host settings.");
                sEventClient = new EventClient(NAME);
            }
        }
        return sEventClient;
	}
	
	/**
	 * Resets the client so it has to re-read the settings and recreate the instance.
	 * @param host New host settings, can be null.
	 */
	public void resetClient(Host host) {
		Log.i(TAG, "Resetting client to " + (host == null ? "<nullhost>" : host.addr));
		if (sEventClient != null) {
			try {
				if (host != null) {
					InetAddress addr = Inet4Address.getByName(host.addr);
					sEventClient.setHost(addr, host.esPort > 0 ? host.esPort : Host.DEFAULT_EVENTSERVER_PORT);
				} else {
					sEventClient.setHost(null, 0);
				}
			} catch (UnknownHostException e) {
				Log.e(TAG, "Unknown host: " + (host == null ? "<nullhost>" : host.addr));
			}
		} else {
			Log.w(TAG, "Not updating event client's host because no instance is set yet.");
		}
	}

}