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

import java.util.ArrayList;

import org.xbmc.android.remote.lib.business.provider.HostProvider;
import org.xbmc.android.remote.lib.business.provider.HostProvider.Hosts;
import org.xbmc.android.remote.lib.api.object.Host;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Helper class that keeps 
 *   a) a reference to the currently used host
 *   b) all the code between the provider and the settings stuff
 * 
 * @author Team XBMC
 */
public abstract class HostFactory {
    public static void getWhich() {
        Log.d(TAG, "lite thing");
    }
	/**
	 * The currently used host
	 */
	public static Host host = null;
	
	/**
	 * The setting that remembers which host has been used last
	 */
	public static final String SETTING_HOST_ID = "setting_host_id";
	public static final String TAG = "HostFactory";
	
	/**
	 * Returns all hosts
	 * @param activity Reference to activity
	 * @return List of all hosts
	 */
	public static ArrayList<? extends Host> getHosts(Context context) {
		Cursor cur = context.getContentResolver().query(HostProvider.Hosts.CONTENT_URI, 
				null,       // All
				null,       // Which rows to return (all rows)
				null,       // Selection arguments (none)
				HostProvider.Hosts.NAME + " ASC"); // Put the results in ascending order by name
		ArrayList<Host> hosts = new ArrayList<Host>();
		try {
			if (cur.moveToFirst()) {
				final int idCol = cur.getColumnIndex(HostProvider.Hosts._ID);
				final int nameCol = cur.getColumnIndex(HostProvider.Hosts.NAME);
				final int hostCol = cur.getColumnIndex(HostProvider.Hosts.ADDR);
				final int esPortCol = cur.getColumnIndex(HostProvider.Hosts.ESPORT);
				final int wifiOnlyCol = cur.getColumnIndex(HostProvider.Hosts.WIFI_ONLY);
				final int accessPointCol = cur.getColumnIndex(HostProvider.Hosts.ACCESS_POINT);
				do {
					final Host host = new Host();
					host.id = cur.getInt(idCol);
					host.name = cur.getString(nameCol);
					host.addr = cur.getString(hostCol);
					host.esPort = cur.getInt(esPortCol);
					host.access_point = cur.getString(accessPointCol);
					host.wifi_only = cur.getInt(wifiOnlyCol)==1; //stored as 1 = true and 0 = false in sqlite
					hosts.add(host);
				} while (cur.moveToNext());
			}
		} finally {
			cur.close();
		}
		return hosts;
	}
	
	/**
	 * Adds a host to the database.
	 * @param context Reference to context
	 * @param host Host to add
	 */
	public static void addHost(Context context, Host host) {
		ContentValues values = new ContentValues();
		values.put(HostProvider.Hosts.NAME, host.name);
		values.put(HostProvider.Hosts.ADDR, host.addr);
		values.put(HostProvider.Hosts.ESPORT, host.esPort);
		values.put(HostProvider.Hosts.WIFI_ONLY, host.wifi_only?1:0);
		values.put(HostProvider.Hosts.ACCESS_POINT, host.access_point);
		context.getContentResolver().insert(HostProvider.Hosts.CONTENT_URI, values);
	}
	
	/**
	 * Updates a host
	 * @param context Reference to context
	 * @param host Host to update
	 */
	public static void updateHost(Context context, Host host) {
		ContentValues values = new ContentValues();
		values.put(HostProvider.Hosts.NAME, host.name);
		values.put(HostProvider.Hosts.ADDR, host.addr);
		values.put(HostProvider.Hosts.ESPORT, host.esPort);
		values.put(HostProvider.Hosts.WIFI_ONLY, host.wifi_only?1:0);
		values.put(HostProvider.Hosts.ACCESS_POINT, host.access_point);
		context.getContentResolver().update(HostProvider.Hosts.CONTENT_URI, values, HostProvider.Hosts._ID + "=" + host.id, null);
	}
	
	/**
	 * Deletes a host.
	 * @param activity Reference to activity
	 * @param host Host to delete
	 * @return
	 */
	public static void deleteHost(Context context, Host host) {
		Uri hostUri = ContentUris.withAppendedId(Hosts.CONTENT_URI, host.id);
		context.getContentResolver().delete(hostUri, null, null);
	}
	
	/**
	 * Saves the host to the preference file.
	 * @param context
	 * @param addr
	 */
	public static void saveHost(Context context, Host h) {
		SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(context).edit();
		if (h != null) {
			ed.putInt(SETTING_HOST_ID, h.id);
		} else {
			ed.putInt(SETTING_HOST_ID, 0);
		}
		ed.commit();
		host = h;
		ClientFactory.getInstance().resetClient(h);
	}
	
	/**
	 * Reads the preferences and returns the currently set host. If there is no
	 * preference set, return the first host. If there is no host set, return
	 * null.
	 * @param activity Reference to current activity
	 * @return Current host
	 */
	public static void readHost(Context context) {
	    int hostId = PreferenceManager.getDefaultSharedPreferences(context).getInt(SETTING_HOST_ID, -1);
		if (hostId < 0) {
			host = getHost(context);
		} else {
			host = getHost(context, hostId);
		}
		Log.i(TAG, "XBMC Host = " + (host == null ? "[host=null]" : host.addr));
	}
	
	/**
	 * Returns a host based on its database ID.
	 * @param activity Reference to activity
	 * @param id Host database ID
	 * @return
	 */
	private static Host getHost(Context context, int id) {
		Uri hostUri = ContentUris.withAppendedId(Hosts.CONTENT_URI, id);
		Cursor cur = context.getContentResolver().query(hostUri, null, null, null, null);
		try {
			if (cur.moveToFirst()) {
				final Host host = new Host();
				host.id = cur.getInt(cur.getColumnIndex(HostProvider.Hosts._ID));
				host.name = cur.getString(cur.getColumnIndex(HostProvider.Hosts.NAME));
				host.addr = cur.getString(cur.getColumnIndex(HostProvider.Hosts.ADDR));
				host.esPort = cur.getInt(cur.getColumnIndex(HostProvider.Hosts.ESPORT));
				host.wifi_only = cur.getInt(cur.getColumnIndex(HostProvider.Hosts.WIFI_ONLY))==1;
				host.access_point = cur.getString(cur.getColumnIndex(HostProvider.Hosts.ACCESS_POINT));
				return host;
			}
		} finally {
			cur.close();
		}
		return null;
	}
	
	/**
	 * Returns the first host found. This is useful if hosts are defined but
	 * the settings have been erased and need to be reset to a host. If nothing
	 * found, return null.
	 * @param activity Reference to activity
	 * @return First host found or null if hosts table empty.
	 */
	private static Host getHost(Context context) {
		ArrayList<? extends Host> hosts = getHosts(context);
		if (hosts.size() > 0) {
			return hosts.get(0);
		} else {
			return null;
		}
	}
}