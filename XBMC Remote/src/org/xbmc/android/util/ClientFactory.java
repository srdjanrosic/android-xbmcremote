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

package org.xbmc.android.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xbmc.android.util.ClientFactory;
//import org.xbmc.android.util.ClientFactory.ClientFactoryHolder;
import org.xbmc.android.util.HostFactory;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IControlClient;
import org.xbmc.api.data.IInfoClient;
import org.xbmc.api.data.IMusicClient;
import org.xbmc.api.data.ITvShowClient;
import org.xbmc.api.data.IVideoClient;
import org.xbmc.api.info.SystemInfo;
import org.xbmc.api.object.Host;
import org.xbmc.httpapi.HttpApi;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.util.Log;

public class ClientFactory extends org.xbmc.android.remote.lib.util.ClientFactory {
    // Singleton
    protected static class ClientFactoryHolder {
        public static ClientFactory instance = new ClientFactory();
    }
    public static ClientFactory getInstance() {
        return ClientFactoryHolder.instance;
    }

    protected org.xbmc.android.remote.lib.api.object.Host getCurrentHostFromFactory() {
        return HostFactory.host;
    }
    
    
	public static int XBMC_REV = -1;

	public static final int MIN_JSONRPC_REV = 27770;
	public static final int MICROHTTPD_REV = 27770;
	public static final int THUMB_TO_VFS_REV = 29743;
	
	public static final int API_TYPE_UNSET = 0;
	public static final int API_TYPE_HTTPIAPI = 1;
	public static final int API_TYPE_JSONRPC = 2;

	private static HttpApi sHttpClient;
//	private static JsonRpc sJsonClient;
	private static int sApiType = API_TYPE_UNSET;
	
	public IInfoClient getInfoClient(INotifiableManager manager, Context context) throws WifiStateException {
		assertWifiState(context);
		probeQueryApiType(manager);
		switch (sApiType) {
			case API_TYPE_JSONRPC:
//				return createJsonClient(manager).info;
			case API_TYPE_UNSET:
			case API_TYPE_HTTPIAPI:
			default:
				return createHttpClient(manager).info;
		}
	}
	
	public IControlClient getControlClient(INotifiableManager manager, Context context) throws WifiStateException {
		assertWifiState(context);
		probeQueryApiType(manager);
		return createHttpClient(manager).control;
	}
	
	public IVideoClient getVideoClient(INotifiableManager manager, Context context) throws WifiStateException {
		assertWifiState(context);
		probeQueryApiType(manager);
		return createHttpClient(manager).video;
	}
	
	public IMusicClient getMusicClient(INotifiableManager manager, Context context) throws WifiStateException {
		assertWifiState(context);
		probeQueryApiType(manager);
		switch (sApiType) {
			case API_TYPE_JSONRPC:
//				return createJsonClient(manager).music;
			case API_TYPE_UNSET:
			case API_TYPE_HTTPIAPI:
			default:
				return createHttpClient(manager).music;
		}
	}
	
	public ITvShowClient getTvShowClient(INotifiableManager manager, Context context) throws WifiStateException {
		assertWifiState(context);
		probeQueryApiType(manager);
		return createHttpClient(manager).shows;
	}
	
	/**
	 * Resets the client so it has to re-read the settings and recreate the instance.
	 * @param host New host settings, can be null.
	 */
	public void resetClient(Host host) {
		sApiType = API_TYPE_UNSET;
		if (sHttpClient != null) {
			sHttpClient.setHost(host);
		} else {
			Log.w(TAG, "Not updating http client's host because no instance is set yet.");
		}
		Log.i(TAG, "Resetting client to " + (host == null ? "<nullhost>" : host.addr));
		super.resetClient(host);
	}

	/**
	 * Returns an instance of the HTTP Client. Instantiation takes place only
	 * once, otherwise the first instance is returned.
	 * 
	 * @param manager Upper layer reference
	 * @return HTTP client
	 */
	private HttpApi createHttpClient(final INotifiableManager manager) {
		final Host host = (Host) getCurrentHostFromFactory();
		if (sHttpClient == null) {
			if (host != null && !host.addr.equals("")){
				sHttpClient = new HttpApi(host, host.timeout >= 0 ? host.timeout : Host.DEFAULT_TIMEOUT);
			} else {
				sHttpClient = new HttpApi(null, -1);
			}
			// do some init stuff
			(new Thread("Init-Connection") {
				public void run() {
					sHttpClient.control.setResponseFormat(manager);
				}
			}).start();
		}
		return sHttpClient;
	}
	
	/**
	 * Returns an instance of the JSON-RPC Client. Instantiation takes place only
	 * once, otherwise the first instance is returned.
	 * 
	 * @param manager Upper layer reference
	 * @return JSON-RPC client
	 *
	private static JsonRpc createJsonClient(final INotifiableManager manager) {
		final Host host = HostFactory.host;
		if (sJsonClient == null) {
			if (host != null && !host.addr.equals("")){
				sJsonClient = new JsonRpc(host, host.timeout >= 0 ? host.timeout : Host.DEFAULT_TIMEOUT);
			} else {
				sJsonClient = new JsonRpc(null, -1);
			}
		}
		return sJsonClient;
	}*/
	
	
	/**
	 * Tries to find out which xbmc flavor and which API is running.
	 * @param manager Upper layer reference
	 */
	private void probeQueryApiType(final INotifiableManager manager) {
		final Host host = (Host) getCurrentHostFromFactory();
		
		if (sApiType != API_TYPE_UNSET) {
			return;
		}
		
		// try to get version string via http api
		final HttpApi httpClient;
		if (host != null && !host.addr.equals("")){
			httpClient = new HttpApi(host, host.timeout >= 0 ? host.timeout : Host.DEFAULT_TIMEOUT);
		} else {
			httpClient = new HttpApi(null, -1);
		}
		final String version = httpClient.info.getSystemInfo(manager, SystemInfo.SYSTEM_BUILD_VERSION);
		Log.i(TAG, "VERSION = " + version);
		
		// 1. try to match xbmc's version
		Pattern pattern = Pattern.compile("r\\d+");
		Matcher matcher = pattern.matcher(version);
		if (matcher.find()) {
			final int rev = Integer.parseInt(matcher.group().substring(1));
			Log.i(TAG, "Found XBMC at revision " + rev + "!");
			XBMC_REV = rev;
			sApiType = rev >= MIN_JSONRPC_REV ? API_TYPE_JSONRPC : API_TYPE_HTTPIAPI;
		} else {
			// parse git version
			pattern = Pattern.compile("Git.([a-f\\d]+)");
			matcher = pattern.matcher(version);
			if (matcher.find()) {
				final String commit = matcher.group(1);
				Log.i(TAG, "Found XBMC at Git commit " + commit + "!");
				
				// set to last revision where we used SVN
				XBMC_REV = 35744;
				sApiType = API_TYPE_JSONRPC;
			}
				
			// 2. try to match boxee's version
			// 3. plex? duh.
			
			sApiType = API_TYPE_UNSET;
		}
	}
	
}