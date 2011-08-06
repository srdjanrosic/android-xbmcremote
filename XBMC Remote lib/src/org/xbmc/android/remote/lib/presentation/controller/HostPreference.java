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

package org.xbmc.android.remote.lib.presentation.controller;

import org.xbmc.android.remote.lib.R;
import org.xbmc.android.remote.lib.api.object.Host;
import org.xbmc.android.remote.lib.util.HostFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * One of those contains name, host, port, user and pass of an XBMC instance.
 * 
 * @author Team XBMC
 */
public class HostPreference extends DialogPreference {
	
	protected EditText mNameView, mHostView, mEsPortView, mAccPointView;
	
	protected CheckBox mWifiOnlyView;
	
	protected Host mHost;
	protected Context mContext;
	
	public static final String ID_PREFIX = "settings_host_";

	public HostPreference(Context context) {
		this(context, null);
	}
	
	public HostPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;
		
		setDialogLayoutResource(R.layout.preference_host);
		setDialogTitle("Add new host");
		setDialogIcon(R.drawable.bubble_add);
	}
	
	public void create(PreferenceManager preferenceManager) {
		onAttachedToHierarchy(preferenceManager);
		showDialog(null);
	}
	
	public void setHost(Host host) {
		mHost = host;
		setTitle(host.name);
		setSummary(host.getSummary());
		setDialogTitle(host.name);
		setDialogIcon(null);
	}
	
	public Host getHost() {
		return mHost;
	}
	
	protected void deleteHostFactoryHost(Context context, Host host) {
	    HostFactory.deleteHost(context, host);
	}
	
	@Override
	protected View onCreateView(final ViewGroup parent) {
		final ViewGroup view = (ViewGroup)super.onCreateView(parent);
		if (mHost != null) {
			ImageView btn = new ImageView(getContext());
			btn.setImageResource(R.drawable.bubble_del_up);
			btn.setClickable(true);
			btn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setMessage("Are you sure you want to delete the XBMC host \"" + mHost.name + "\"?");
					builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							deleteHostFactoryHost(getContext(), mHost);
							((PreferenceActivity)view.getContext()).getPreferenceScreen().removePreference(HostPreference.this);
						}
					});
					builder.setNegativeButton("Nah.", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					builder.create().show();
				}
			});
			view.addView(btn);
		}
		return view;
	}
	
	@Override
	protected View onCreateDialogView() {
		final ViewGroup parent = (ViewGroup)super.onCreateDialogView();
		mNameView = (EditText)parent.findViewById(R.id.pref_name);
		mHostView = (EditText)parent.findViewById(R.id.pref_host);
		mEsPortView = (EditText)parent.findViewById(R.id.pref_eventserver_port);
		mAccPointView = (EditText)parent.findViewById(R.id.pref_access_point);
		mWifiOnlyView = (CheckBox)parent.findViewById(R.id.pref_wifi_only);
		return parent;
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		if (mHost != null) {
			mNameView.setText(mHost.name);
			mHostView.setText(mHost.addr);
			mEsPortView.setText(String.valueOf(mHost.esPort));
			mAccPointView.setText(mHost.access_point);
			mWifiOnlyView.setChecked(mHost.wifi_only);
		} else {
			//set defaults:
			mEsPortView.setText("" + Host.DEFAULT_EVENTSERVER_PORT);
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
				host.esPort = Integer.parseInt(mEsPortView.getText().toString());
			} catch (NumberFormatException e) {
				host.esPort = Host.DEFAULT_EVENTSERVER_PORT;
			}
			
			host.access_point = mAccPointView.getText().toString();
			host.wifi_only = mWifiOnlyView.isChecked();
			
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