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

package org.xbmc.android.remote.lib.business;

import java.io.IOException;

import org.xbmc.android.remote.business.Command;
import org.xbmc.android.remote.lib.util.ClientFactory;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.api.data.IEventClient;
import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.httpapi.WifiStateException;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Super class of the wrappers, keeps common code.
 * 
 * @author Team XBMC
 */
public class EventClientManager implements INotifiableManager, IEventClientManager {
	protected IEventClient getEventClient(INotifiableManager manager, Context context) throws WifiStateException {
	    return ClientFactory.getInstance().getEventClient(manager, context);
	}
    
	protected static final String TAG = "EventClientManager";
	protected static final Boolean DEBUG = false;
	
	private INotifiableController mController = null;
	private Context mContext = null;
	protected static EventClientManager sInstance = null;

	public static EventClientManager getInstance(INotifiableController controller, Context context) {
	   if (sInstance == null) {
	       sInstance = new EventClientManager(controller, context);
	   }
	   return sInstance;
	}

	public EventClientManager(INotifiableController controller, Context context) {
	    this.setController(controller);
	    mContext = context;
	}

	public void onMessage(int code, String message) {
		onMessage(message);
	}

	public void sendAction(String actionmessage) throws IOException, WifiStateException {
		getEventClient(this, mContext).sendAction(actionmessage);
	}

	public void sendButton(short code, boolean repeat, boolean down, boolean queue, short amount, byte axis) throws IOException, WifiStateException {
		getEventClient(this, mContext).sendButton(code, repeat, down, queue, amount, axis);
	}

	public void sendButton(String mapName, String buttonName, boolean repeat, boolean down, boolean queue, short amount, byte axis) throws IOException, WifiStateException {
		getEventClient(this, mContext).sendButton(mapName, buttonName, repeat, down, queue, amount, axis);
	}

	public void sendLog(byte loglevel, String logmessage) throws IOException, WifiStateException {
		getEventClient(this, mContext).sendLog(loglevel, logmessage);
	}

	public void sendMouse(int x, int y) throws IOException, WifiStateException {
		getEventClient(this, mContext).sendMouse(x, y);
	}

	public void sendNotification(String title, String message) throws IOException, WifiStateException {
		getEventClient(this, mContext).sendNotification(title, message);
	}

	public void sendNotification(String title, String message, byte icontype, byte[] icondata) throws IOException, WifiStateException {
		getEventClient(this, mContext).sendNotification(title, message, icontype, icondata);
	}
	
	public void onError(Exception e) {
		if (mController != null) {
			mController.onError(e);
		}
	}

	public void onMessage(String message) {
		if (mController != null) {
			mController.onMessage(message);
		}
	}

	public void getCover(DataResponse<Bitmap> response, ICoverArt cover, int thumbSize, Bitmap defaultCover, final Context context, boolean b) {
		// only a stub;
	}

	public void onWrongConnectionState(int state) {
		if (mController != null) {
			mController.onWrongConnectionState(state, null, null);
		}
	}

	public void onFinish(DataResponse<?> response) {
		// TODO Auto-generated method stub
		
	}


	public void retryAll() {
		// TODO Auto-generated method stub
		
	}

    public void setController(INotifiableController controller) {
        mController = controller;
    }

    public void onWrongConnectionState(int state, Command<?> cmd) {
        // TODO Auto-generated method stub
    }
}