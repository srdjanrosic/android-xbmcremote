/*
 *      Copyright (C) 2005-2011 Team XBMC
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

import java.io.IOException;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.business.EventClientManager;
import org.xbmc.android.remote.presentation.activity.GestureRemoteActivity;
import org.xbmc.android.remote.presentation.activity.HostSettingsActivity;
import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;
import org.xbmc.android.remote.presentation.activity.SettingsActivity;
import org.xbmc.android.util.ClientFactory;
import org.xbmc.android.util.HostFactory;
import org.xbmc.android.widget.gestureremote.IGestureListener;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.info.GuiSettings;
import org.xbmc.api.object.Host;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.httpapi.WifiStateException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class RemoteController extends org.xbmc.android.remote.lib.presentation.controller.RemoteController {
    
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
    
    protected IEventClientManager getEventClientManagerInstance(INotifiableController controller, Context context) {
        Log.d(TAG, "Getting an EventClientManager");
        return EventClientManager.getInstance(controller, context);
    }
    
    public static final int LAST_REMOTE_GESTURE = 1;
	
	protected static final int MENU_NOW_PLAYING = 401;
	protected static final int MENU_XBMC_EXIT = 402;
	
//	protected static final int MENU_SWITCH_MOUSE = 404;
	protected static final int MENU_SWITCH_GESTURE = 405;

	IInfoManager mInfoManager;
	IControlManager mControl;
	GestureThread mGestureThread;
	
	private int mEventServerInitialDelay = 750;
	
	public RemoteController(Activity activity) {
	    super(activity);
		
		mControl = ManagerFactory.getControlManager(this);
		mInfoManager = ManagerFactory.getInfoManager(this);
		mInfoManager.getGuiSettingInt(new DataResponse<Integer>() {
//			@Override
//			public void run() {
//				mHandler.post(new Runnable() {
//					public void run() {
//						mEventServerInitialDelay = value;
//						Log.i("RemoteController", "Saving previous value " + GuiSettings.getName(GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY) + " = " + value);
//					}
//				});
//			}
		}, GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY, activity.getApplicationContext());
	}

	public IGestureListener startGestureThread(final Context context) {
		mGestureThread = new GestureThread(mEventClientManager);
		IGestureListener listener = new IGestureListener() {
			private boolean mScrolling = false;
			public double[] getZones() {
				double[] ret = { 0.13, 0.25, 0.5, 0.75 };
				return ret;
			}
			public void onHorizontalMove(int value) {
				Log.d(TAG, "onHorizontalMove(" + value + ")");
				if (value == 0) {
					if (mGestureThread != null) {
						mGestureThread.quit();
						mGestureThread = null;
					}
				} else {
					if (mGestureThread == null) {
						mGestureThread = new GestureThread(mEventClientManager);
					}
					mGestureThread.setLevel(value, value > 0 ? GestureThread.ACTION_RIGHT : GestureThread.ACTION_LEFT);
				}
			}
			public void onVerticalMove(int value) {
				Log.d(TAG, "onVerticalMove(" + value + ")");
				if (value == 0) {
					if (mGestureThread != null) {
						mGestureThread.quit();
						mGestureThread = null;
					}
				} else {
					if (mGestureThread == null) {
						mGestureThread = new GestureThread(mEventClientManager);
					}
					mGestureThread.setLevel(value, value > 0 ? GestureThread.ACTION_DOWN : GestureThread.ACTION_UP);
				}
			}
			private void scroll(String button, double amount) {
				try {
					if (amount != 0) {
						if (!mScrolling) {
							Log.i(TAG, "Setting " + GuiSettings.getName(GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY) + " = " + 25);
							mInfoManager.setGuiSettingInt(new DataResponse<Boolean>(), GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY, 25, context);
						}
						mEventClientManager.sendButton("XG", button, true, true, false, (short)(amount * 65535), (byte)0);
						mScrolling = true;
					} else {
						mEventClientManager.sendButton("XG", button, false, false, false, (short)0, (byte)0);
						Log.i(TAG, "Restoring " + GuiSettings.getName(GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY) + " = " + mEventServerInitialDelay);
						mInfoManager.setGuiSettingInt(new DataResponse<Boolean>(), GuiSettings.Services.EVENT_SERVER_INITIAL_DELAY, mEventServerInitialDelay, context);
						mScrolling = false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (WifiStateException e) {
                    e.printStackTrace();
                }
			}
			public void onScrollDown(double amount) {
				Log.d(TAG, "onScrollDown(" + amount + ")");
				scroll(ButtonCodes.GAMEPAD_RIGHT_ANALOG_TRIGGER, amount);
			}
			public void onScrollUp(double amount) {
				Log.d(TAG, "onScrollUp(" + amount + ")");
				scroll(ButtonCodes.GAMEPAD_LEFT_ANALOG_TRIGGER, amount);
			}
			public void onSelect() {
				try {
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_SELECT, false, true, false, (short)0, (byte)0);
				} catch (IOException e) {
				} catch (WifiStateException e) {
                    e.printStackTrace();
                }
			}
			public void onScrollDown() {
				Log.d(TAG, "onScrollDown()");
				try {
					mEventClientManager.sendButton("KB", ButtonCodes.KEYBOARD_PAGEDOWN, false, true, true, (short)0, (byte)0);
				} catch (IOException e) {
				} catch (WifiStateException e) {
                    e.printStackTrace();
                }
			}
			public void onScrollUp() {
				Log.d(TAG, "onScrollUp()");
				try {
					mEventClientManager.sendButton("KB", ButtonCodes.KEYBOARD_PAGEUP, false, true, true, (short)0, (byte)0);
				} catch (IOException e) {
				} catch (WifiStateException e) {
                    e.printStackTrace();
                }
			}
			public void onBack() {
				try {
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_BACK, false, true, true, (short)0, (byte)0);
				} catch (IOException e) {
				} catch (WifiStateException e) {
                    e.printStackTrace();
                }
			}
			public void onInfo() {
				try {
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_INFO, false, true, true, (short)0, (byte)0);
				} catch (IOException e) {
				} catch (WifiStateException e) {
                    e.printStackTrace();
                }
			}
			public void onMenu() {
				try {
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_MENU, false, true, true, (short)0, (byte)0);
				} catch (IOException e) {
				} catch (WifiStateException e) {
                    e.printStackTrace();
                }
			}
			public void onTitle() {
				try {
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_TITLE, false, true, true, (short)0, (byte)0);
				} catch (IOException e) {
				} catch (WifiStateException e) {
                    e.printStackTrace();
                }
			}
		};
		return listener;
	}
	
	private static class GestureThread extends Thread {
		public static final int ACTION_UP = 1;
		public static final int ACTION_RIGHT = 2;
		public static final int ACTION_DOWN = 3;
		public static final int ACTION_LEFT = 4;
		private final IEventClientManager mEventClient;
		private boolean mQuit = false;
		private int mLevel = 0;
		private int[] mSpeed = { 0, 800, 400, 200, 100, 50, 0 };
		private int mAction = 0;
		public GestureThread(IEventClientManager eventClient) {
			super("RemoteController.GestureThread");
			mEventClient = eventClient;
		}
		@Override
		public void run() {
			Log.i("GestureThread", "STARTING...");
			while (!mQuit) {
				try {
					switch (mAction) {
						case ACTION_UP:
							mEventClient.sendButton("R1", ButtonCodes.REMOTE_UP, false, true, true, (short)0, (byte)0);
							break;
						case ACTION_RIGHT:
							mEventClient.sendButton("R1", ButtonCodes.REMOTE_RIGHT, false, true, true, (short)0, (byte)0);
							break;
						case ACTION_DOWN:
							mEventClient.sendButton("R1", ButtonCodes.REMOTE_DOWN, false, true, true, (short)0, (byte)0);
							break;
						case ACTION_LEFT:
							mEventClient.sendButton("R1", ButtonCodes.REMOTE_LEFT, false, true, true, (short)0, (byte)0);
							break;
					}
//					Log.i("GestureThread", "action: " + mAction);
					Thread.sleep(mSpeed[Math.abs(mLevel)]);
				} catch (InterruptedException e) {
					mQuit = true;
				} catch (IOException e1) {
					mQuit = true;
				} catch (WifiStateException e) {
                    e.printStackTrace();
                    mQuit = true;
                }

			}
		}
		public synchronized void setLevel(int level, int action) {
			mLevel = level;
			mAction = action;
			if (!isAlive()) {
				start();
			}
		}
		public synchronized void quit() {
			Log.i("GestureThread", "QUITTING.");
			mQuit = true;
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_NOW_PLAYING, 0, "Now playing").setIcon(R.drawable.menu_nowplaying);
		menu.add(0, MENU_SWITCH_GESTURE, 0, "Gesture mode").setIcon(R.drawable.menu_gesture_mode);
		menu.add(0, MENU_XBMC_EXIT, 0, "Exit XBMC").setIcon(R.drawable.menu_xbmc_exit);
		menu.add(0, MENU_XBMC_S, 0, "Press \"S\"").setIcon(R.drawable.menu_xbmc_s);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			Intent intent = null;
			switch (item.getItemId()) {
				case MENU_NOW_PLAYING:
					intent = new Intent(mActivity, NowPlayingActivity.class);
					break;
				case MENU_SWITCH_GESTURE:
					intent = new Intent(mActivity, GestureRemoteActivity.class);
					intent.addFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
					break;
				case MENU_XBMC_EXIT:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_POWER, false, true, true, (short)0, (byte)0);
					break;
				case MENU_XBMC_S:
					mEventClientManager.sendButton("KB", "S", false, true, true, (short)0, (byte)0);
					break;
			}
			if (intent != null) {
				intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mActivity.startActivity(intent);
				return true;
			}
		} catch (IOException e) {
			return false;
		} catch (WifiStateException e) {
            e.printStackTrace();
            return false;
        }
		return true;
	}
	
	public void onActivityPause() {
		mInfoManager.setController(null);
		if (mGestureThread != null) {
			mGestureThread.quit();
		}
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		mInfoManager.setController(this);
	}
}