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

import org.xbmc.android.remote.R;
import org.xbmc.android.util.HostFactory;
import org.xbmc.android.util.MacAddressResolver;
import org.xbmc.api.object.Host;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * One of those contains name, host, port, user and pass of an XBMC instance.
 * 
 * @author Team XBMC
 */
public class HostPreference extends org.xbmc.android.remote.lib.presentation.controller.HostPreference {
    
    // These are in addition to ones already defined.
	protected EditText  mPortView, mUserView, mPassView, mTimeoutView,  mMacAddrView, mWolWaitView, mWolPortView;
	
	//protected Host mHost;
	
	public HostPreference(Context context) {
        super(context);
    }

	public HostPreference(Context context, AttributeSet attrs) {
	    super(context,attrs);
	}
	
	protected void deleteHostFactoryHost(Context context, Host host) {
	    HostFactory.deleteHost(context, host);
	}
	
	@Override
	protected View onCreateDialogView() {
		final ViewGroup parent = (ViewGroup)super.onCreateDialogView();
		mNameView = (EditText)parent.findViewById(R.id.pref_name);
		mHostView = (EditText)parent.findViewById(R.id.pref_host);
		mHostView.setOnFocusChangeListener(new OnFocusChangeListener() {
			Handler handler = new Handler(){
				public void handleMessage(android.os.Message message){
					if(message.getData().containsKey(MacAddressResolver.MESSAGE_MAC_ADDRESS)){
						String mac = message.getData().getString(MacAddressResolver.MESSAGE_MAC_ADDRESS);
						if(!mac.equals("")){
							mMacAddrView.setText(mac);
							Toast toast = Toast.makeText(getContext(), "Updated MAC for host: " + mHostView.getText().toString() + "\nto: " + mac, Toast.LENGTH_SHORT);
							toast.show();
						}
						
					}
				}
			};
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
					return;
				if(mMacAddrView.getText().toString().equals(""))
					handler.post(new MacAddressResolver(mHostView.getText().toString(), handler));
			}
		});
		mPortView = (EditText)parent.findViewById(R.id.pref_port);
		mUserView = (EditText)parent.findViewById(R.id.pref_user);
		mPassView = (EditText)parent.findViewById(R.id.pref_pass);
		mEsPortView = (EditText)parent.findViewById(R.id.pref_eventserver_port);
		mTimeoutView = (EditText)parent.findViewById(R.id.pref_timeout);
		mMacAddrView = (EditText)parent.findViewById(R.id.pref_mac_addr);
		mAccPointView = (EditText)parent.findViewById(R.id.pref_access_point);
		mWifiOnlyView = (CheckBox)parent.findViewById(R.id.pref_wifi_only);
		mWolPortView = (EditText)parent.findViewById(R.id.pref_wol_port);
		mWolWaitView = (EditText)parent.findViewById(R.id.pref_wol_wait);
		return parent;
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		if (mHost != null) {
			mNameView.setText(mHost.name);
			mHostView.setText(mHost.addr);
			mPortView.setText(String.valueOf(((Host)mHost).port));
			mUserView.setText(((Host)mHost).user);
			mPassView.setText(((Host)mHost).pass);
			
			mEsPortView.setText(String.valueOf(mHost.esPort));
			mTimeoutView.setText(String.valueOf(((Host)mHost).timeout));
			mMacAddrView.setText(((Host)mHost).mac_addr);
			mAccPointView.setText(mHost.access_point);
			mWifiOnlyView.setChecked(mHost.wifi_only);
			mWolPortView.setText(String.valueOf(((Host)mHost).wol_port));
			mWolWaitView.setText(String.valueOf(((Host)mHost).wol_wait));
		} else {
			//set defaults:
			mPortView.setText("" + Host.DEFAULT_HTTP_PORT);
			mEsPortView.setText("" + Host.DEFAULT_EVENTSERVER_PORT);
			mTimeoutView.setText("" + Host.DEFAULT_TIMEOUT);
			mWolPortView.setText("" + Host.DEFAULT_WOL_PORT);
			mWolWaitView.setText("" + Host.DEFAULT_WOL_WAIT);
		}
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			final Host host = new Host();
			host.name = mNameView.getText().toString();
			host.addr = mHostView.getText().toString();
			try {
				host.port = Integer.parseInt(mPortView.getText().toString());
			} catch (NumberFormatException e) {
				host.port = Host.DEFAULT_HTTP_PORT;
			}
			host.user = mUserView.getText().toString();
			host.pass = mPassView.getText().toString();

			try {
				host.esPort = Integer.parseInt(mEsPortView.getText().toString());
			} catch (NumberFormatException e) {
				host.esPort = Host.DEFAULT_EVENTSERVER_PORT;
			}
			try {
				host.timeout = Integer.parseInt(mTimeoutView.getText().toString());
			} catch (NumberFormatException e) {
				host.timeout = Host.DEFAULT_TIMEOUT;
			}
			host.access_point = mAccPointView.getText().toString();
			host.mac_addr = mMacAddrView.getText().toString();
			host.wifi_only = mWifiOnlyView.isChecked();
			try {
				host.wol_port = Integer.parseInt(mWolPortView.getText().toString());
			}catch (NumberFormatException e) {
				host.wol_port = Host.DEFAULT_WOL_PORT;
			}
			try {
				host.wol_wait = Integer.parseInt(mWolWaitView.getText().toString());
			}catch (NumberFormatException e) {
				host.wol_wait = Host.DEFAULT_WOL_WAIT;
			}
			
			
			if (mHost == null) {
				HostFactory.addHost(getContext(), host);
			} else {
				host.id = mHost.id;
				HostFactory.updateHost(getContext(), host);
			}
			if (callChangeListener(host)) {
				notifyChanged();
			}
			setHost(host);
			
			if (HostFactory.host == null) {
				HostFactory.saveHost(mContext, host);
			}
		}
	}
}