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

package org.xbmc.android.remote.presentation.activity;

import org.xbmc.android.remote.presentation.activity.HomeActivity;
import org.xbmc.android.remote.presentation.controller.RemoteController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ViewFlipper;

/**
 * Activity for remote control. At the moment that's the good ol' Xbox remote
 * control, more to come...
 * 
 * @author Team XBMC
 */
public class RemoteActivity extends org.xbmc.android.remote.lib.presentation.activity.RemoteActivity {

    protected RemoteController getRemoteControllerInstance(Activity activity) {
        return new RemoteController(activity);
    }
    
    
	private ViewFlipper mViewFlipper;
	private View mRemoteView, mGestureView, mMousePadView;
	private float mOldTouchValue;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		if (mViewFlipper != null) {
			mRemoteView = mViewFlipper.getChildAt(0);
			mMousePadView = mViewFlipper.getChildAt(1);
			mGestureView = mViewFlipper.getChildAt(2);
			mViewFlipper.setDisplayedChild(0); // mRemoteView
		}
	}
	
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
	    Intent intent = new Intent(RemoteActivity.this, HomeActivity.class);
	    intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    startActivity(intent);
	    return true;
	}
	   
	@Override
	public boolean onTouchEvent(MotionEvent touchEvent) {
		// ignore all that on hdpi displays
		if (mViewFlipper == null) {
			return false;
		}
		
		// determine the current view and
		// who is to the right and to the left.
		final View currentView = mViewFlipper.getCurrentView();
		final View leftView, rightView;

		if (currentView == mRemoteView) {
			Log.d("current layout: ", "remote");
			leftView = mGestureView;
			rightView = mMousePadView;
		} else if (currentView == mMousePadView) {
			Log.d("current layout: ", "mousepad");
			leftView = mRemoteView;
			rightView = mGestureView;
		} else if (currentView == mGestureView) {
			Log.d("current layout: ", "gesture");
			leftView = mMousePadView;
			rightView = mRemoteView;
		}
		// This shouldn't happen unless someone adds another view
		// inside the ViewFlipper
		else {
			leftView = null;
			rightView = null;
		}

		switch (touchEvent.getAction()) {
			case MotionEvent.ACTION_DOWN: 
				// freezy: the mousepad seems to always flicker
				// at the start of the move action (i.e. action_down)
				// so i tried this but it doesn't seem to work.
				// thats the only thing i can think of that keeps this
				// feature from being 100%
				/*
				 * if(currentView != mMousePadView) {
				 * mMousePadView.setVisibility(View.INVISIBLE); }
				 */
				mOldTouchValue = touchEvent.getX();
			break;
		
			case MotionEvent.ACTION_UP: 
				float currentX = touchEvent.getX();
	
				if (mOldTouchValue < currentX) {
					mViewFlipper.setInAnimation(AnimationHelper.inFromLeftAnimation());
					mViewFlipper.setOutAnimation(AnimationHelper.outToRightAnimation());
					mViewFlipper.showPrevious();
				}
				if (mOldTouchValue > currentX) {
					mViewFlipper.setInAnimation(AnimationHelper.inFromRightAnimation());
					mViewFlipper.setOutAnimation(AnimationHelper.outToLeftAnimation());
					mViewFlipper.showNext();
				}
	
			break;
		
			case MotionEvent.ACTION_MOVE: 
				leftView.setVisibility(View.VISIBLE);
				rightView.setVisibility(View.VISIBLE);
	
				Log.d("current layout:", "left: "
						+ Integer.toString(currentView.getLeft()) + " right: "
						+ Integer.toString(currentView.getRight()));
				Log.d("previous layout:", "left: "
						+ Integer.toString(leftView.getLeft()) + " right: "
						+ Integer.toString(leftView.getRight()));
				Log.d("next layout:", "left: "
						+ Integer.toString(rightView.getLeft()) + " right: "
						+ Integer.toString(rightView.getRight()));
	
				// move the current view to the left or right.
				currentView.layout((int) (touchEvent.getX() - mOldTouchValue),
						currentView.getTop(),
						(int) (touchEvent.getX() - mOldTouchValue) + 320,
						currentView.getBottom());
	
				// place this view just left of the currentView
				leftView.layout(currentView.getLeft() - 320, leftView.getTop(),
						currentView.getLeft(), leftView.getBottom());
	
				// place this view just right of the currentView
				rightView.layout(currentView.getRight(), rightView.getTop(),
						currentView.getRight() + 320, rightView.getBottom());
			break;
		
		}
		return false;
	}

	public static class AnimationHelper {
		public static Animation inFromRightAnimation() {
			Animation inFromRight = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, +1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			inFromRight.setDuration(350);
			inFromRight.setInterpolator(new AccelerateInterpolator());
			return inFromRight;
		}

		public static Animation outToLeftAnimation() {
			Animation outtoLeft = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, -1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			outtoLeft.setDuration(350);
			outtoLeft.setInterpolator(new AccelerateInterpolator());
			return outtoLeft;
		}

		// for the next movement
		public static Animation inFromLeftAnimation() {
			Animation inFromLeft = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, -1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			inFromLeft.setDuration(350);
			inFromLeft.setInterpolator(new AccelerateInterpolator());
			return inFromLeft;
		}

		public static Animation outToRightAnimation() {
			Animation outtoRight = new TranslateAnimation(
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, +1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			outtoRight.setDuration(350);
			outtoRight.setInterpolator(new AccelerateInterpolator());
			return outtoRight;
		}
	}
}
