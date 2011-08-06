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

package org.xbmc.android.remote.lib.presentation.controller;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.xbmc.android.remote.lib.business.EventClientManager;
import org.xbmc.android.remote.lib.R;
import org.xbmc.android.remote.lib.presentation.activity.SettingsActivity;
import org.xbmc.api.business.IEventClientManager;
import org.xbmc.api.presentation.INotifiableController;
import org.xbmc.eventclient.ButtonCodes;
import org.xbmc.httpapi.WifiStateException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class RemoteController extends AbstractController implements INotifiableController, IController {
    protected IEventClientManager getEventClientManagerInstance(INotifiableController controller, Context context) {
        return EventClientManager.getInstance(controller, context);
    }
    
    
	public static final int LAST_REMOTE_BUTTON = 0;
	public static final String LAST_REMOTE_PREFNAME = "last_remote_type";
	
	protected static final int MENU_SETTINGS = 2;
    protected static final int MENU_SWITCH_XBMC = 5;
	protected static final int MENU_XBMC_S = 403;

	protected static final int DPAD_DOWN_MIN_DELTA_TIME = 100;
	protected static final int MOTION_EVENT_MIN_DELTA_TIME = 250;
	protected static final float MOTION_EVENT_MIN_DELTA_POSITION = 0.15f;
	
	protected static final long VIBRATION_LENGTH = 45;
	
	protected IEventClientManager mEventClientManager;
	
	/**
	 * timestamp since last trackball use.
	 */
	private long mTimestamp = 0;
	private final Vibrator mVibrator;
	private final boolean mDoVibrate;
	
	private Timer tmrKeyPress;
	
	protected final SharedPreferences prefs;
	
	public RemoteController(Activity activity) {
	    mActivity = activity;
		prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());

		mEventClientManager = getEventClientManagerInstance(this, activity.getApplicationContext());
		mVibrator = (Vibrator) activity.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
		mDoVibrate = prefs.getBoolean("setting_vibrate_on_touch", true);
	}
	
	public void showVolume() { }

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		char key = (char)event.getUnicodeChar();
		if (key > 'A' && key < 'z')
			return keyboardAction("" + key);
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					return onDirectionalPadDown(keyCode);
				case KeyEvent.KEYCODE_DPAD_UP:
					return onDirectionalPadDown(keyCode);
				case KeyEvent.KEYCODE_DPAD_LEFT:
					return onDirectionalPadDown(keyCode);
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					return onDirectionalPadDown(keyCode);
				case KeyEvent.KEYCODE_DPAD_CENTER:
					return onDirectionalPadDown(keyCode);
				default: 
					return false;
			}
		} catch (IOException e) {
			return false;
		} catch (WifiStateException e) {
            onWrongConnectionState(e.getState(), mEventClientManager, null);
            return false;
        }
	}
	
	private boolean onDirectionalPadDown(int keyCode){
			long newstamp = System.currentTimeMillis();
			if (newstamp - mTimestamp > DPAD_DOWN_MIN_DELTA_TIME){
				mTimestamp = newstamp;
				try{
					switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_DOWN:
							mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_DOWN, false, true, true, (short)0, (byte)0);
							return true;
						case KeyEvent.KEYCODE_DPAD_UP:
							mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_UP, false, true, true, (short)0, (byte)0);
							return true;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_LEFT, false, true, true, (short)0, (byte)0);
							return true;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_RIGHT, false, true, true, (short)0, (byte)0);
							return true;
						case KeyEvent.KEYCODE_DPAD_CENTER:
							mEventClientManager.sendButton("R1", ButtonCodes.REMOTE_ENTER, false, true, true, (short)0, (byte)0);
							return true;							
						default:
							return false;
					}
				} catch (IOException e) {
					return false;
                } catch (WifiStateException e) {
                    e.printStackTrace();
                }
			}
			return true;
	} 
	
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			return keyboardAction(ButtonCodes.KEYBOARD_ENTER);
		else{
			// check when the last trackball move happened to avoid too speedy selections
			long newstamp = System.currentTimeMillis();
			if (newstamp - mTimestamp > MOTION_EVENT_MIN_DELTA_TIME){
				mTimestamp = newstamp;
				if (Math.abs(event.getX()) > MOTION_EVENT_MIN_DELTA_POSITION) {
					return keyboardAction(event.getX() < 0 ? ButtonCodes.KEYBOARD_LEFT : ButtonCodes.KEYBOARD_RIGHT);
				} else if (Math.abs(event.getY()) > MOTION_EVENT_MIN_DELTA_POSITION){
					return keyboardAction(event.getY() < 0 ? ButtonCodes.KEYBOARD_UP : ButtonCodes.KEYBOARD_DOWN);
				}
			}
		}
		return true;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, MENU_SWITCH_XBMC, 0, "Switch XBMC").setIcon(R.drawable.menu_switch);
	    menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(R.drawable.menu_settings);
		menu.add(0, MENU_XBMC_S, 0, "Press \"S\"").setIcon(R.drawable.menu_xbmc_s);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			    case MENU_SWITCH_XBMC:
			        new HostChangerHelper(mActivity);
			        break;
			    case MENU_SETTINGS:
			        this.mActivity.startActivity(new Intent(mActivity.getApplicationContext(), SettingsActivity.class));
			        break;
			    case MENU_XBMC_S:
					mEventClientManager.sendButton("KB", "S", false, true, true, (short)0, (byte)0);
					break;
			}
		} catch (IOException e) {
			return false;
		} catch (WifiStateException e) {
            e.printStackTrace();
            return false;
        }
		return true;
	}
	
	/**
	 * Sends a keyboard event
	 * @param button
	 * @return
	 */
	private boolean keyboardAction(String button) {
		try {
			mEventClientManager.sendButton("KB", button, false, true, true, (short)0, (byte)0);
			return true;
		} catch (IOException e) {
			return false;
		} catch (WifiStateException e) {
            e.printStackTrace();
            return false;
        }
	}

	/**
	 * Shortcut for adding the listener class to the button
	 * @param resourceButton       Resource ID of the button
	 * @param action               Action string
	 * @param resourceButtonUp     Resource ID of the button up image
	 * @param resourceButtonDown   Resource ID of the button down image
	 */
	public void setupButton(View btn, String action) {
		if (btn != null) {
			btn.setOnTouchListener(new OnRemoteAction(action));
			((Button)btn).setSoundEffectsEnabled(true);
			((Button)btn).setClickable(true);
		}
	}
	
	/**
	 * Handles the push- release button code. Switches image of the pressed
	 * button, vibrates and executes command.
	 */
	private class OnRemoteAction implements OnTouchListener {
		private final String mAction;
		public OnRemoteAction(String action) {
			mAction = action;
		}
		public boolean onTouch(View v, MotionEvent event) {
			try {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Log.d(TAG, "onTouch - ACTION_DOWN");
					if (mDoVibrate) {
						mVibrator.vibrate(VIBRATION_LENGTH);
					}
					mEventClientManager.sendButton("R1", mAction, !prefs.getBoolean("setting_send_repeats", false), true, true, (short)0, (byte)0);									
					
					if (prefs.getBoolean("setting_send_repeats", false) && !prefs.getBoolean("setting_send_single_click", false)) {
															
						if (tmrKeyPress != null) {
							tmrKeyPress.cancel();						
						}
						
						int RepeatDelay = Integer.parseInt(prefs.getString("setting_repeat_rate", "250"));
						
						tmrKeyPress = new Timer();
						tmrKeyPress.schedule(new KeyPressTask(mAction), RepeatDelay, RepeatDelay);					
					}
					
					
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					Log.d(TAG, "onTouch - ACTION_UP");
					v.playSoundEffect(AudioManager.FX_KEY_CLICK);
					mEventClientManager.sendButton("R1", mAction, false, false, true, (short)0, (byte)0);
					
					if (tmrKeyPress != null) {
						tmrKeyPress.cancel();						
					}					
				}
			} catch (IOException e) {
				return false;
			} catch (WifiStateException e) {
                onWrongConnectionState(e.getState(), mEventClientManager, null);
                return false;
            }
			return false;
		}			
	}
	
	private class KeyPressTask extends TimerTask {
		
		private String mKeyPressAction = "";
		
		public KeyPressTask(String mAction) {
			mKeyPressAction = mAction;
		}

		public void run() {
			try {
				if (mKeyPressAction.length() > 0){
					mEventClientManager.sendButton("R1", mKeyPressAction, false, true, true, (short)0, (byte)0);
				}				
			} catch (IOException e) {
				return;
			} catch (WifiStateException e) {
                e.printStackTrace();
                return;
            }
		}
	}
	
	public void onActivityPause() {
		mEventClientManager.setController(null);
		super.onActivityPause();
	}

	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		mHandler = new Handler();
		mEventClientManager.setController(this);
	}
}