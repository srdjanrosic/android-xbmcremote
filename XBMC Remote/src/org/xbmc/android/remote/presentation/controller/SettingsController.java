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

import java.util.ArrayList;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.controller.HostPreference;
import org.xbmc.android.remote.presentation.activity.HostSettingsActivity;
import org.xbmc.android.remote.presentation.activity.SettingsActivity;
import org.xbmc.android.remote.presentation.wizard.setupwizard.SetupWizard;
import org.xbmc.android.util.ClientFactory;
import org.xbmc.android.util.HostFactory;
import org.xbmc.api.object.Host;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsController extends org.xbmc.android.remote.lib.presentation.controller.SettingsController {

    // These are not used directly in case someone wants to extend the methods of these static classes.
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
    
    protected ArrayList<Host> getHostFactoryHosts(Context applicationContext) {
        return HostFactory.getHosts(applicationContext);
    }
    
    protected void HostFactorySaveHost(Context applicationContext, Host host) {
        HostFactory.saveHost(applicationContext, host);        
    }   
    
    protected HostPreference getHostPreferenceInstance(Activity activity) {
        return new HostPreference(activity);
    }
    
    
    public SettingsController(PreferenceActivity activity, Handler handler) {
        super(activity, handler);
    }
    
	public static final int MENU_EXIT = 2;
	public static final int MENU_ADD_HOST_WIZARD = 3;

	public void onCreateOptionsMenu(Menu menu) {
		menu.addSubMenu(0, MENU_ADD_HOST, 0, "Add Host").setIcon(R.drawable.menu_add_host);
		menu.addSubMenu(0, MENU_ADD_HOST_WIZARD, 0, "Host Wizard").setIcon(R.drawable.menu_add_host);
		menu.addSubMenu(0, MENU_EXIT, 0, "Exit").setIcon(R.drawable.menu_exit);
	}
	
	public void onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD_HOST:
				HostPreference pref = new HostPreference(mActivity);
				pref.setTitle("New XBMC Host");
				pref.create(mPreferenceActivity.getPreferenceManager());
				mPreferenceActivity.getPreferenceScreen().addPreference(pref);
				break;
			case MENU_ADD_HOST_WIZARD:
				Intent i = new Intent(mPreferenceActivity, SetupWizard.class);
				mPreferenceActivity.startActivity(i);
				break;
			case MENU_EXIT:
				System.exit(0);
				break;
		}
	}
	
}