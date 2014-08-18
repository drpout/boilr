package com.github.andrefbsantos.boilr.activities;

import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

class MyGestureDetector extends SimpleOnGestureListener {
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2,
			float velocityX, float velocityY) {
		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
			return false;
		}
		// right to left swipe
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			Log.d("ICS-Calendar", "Fling left");
			System.out.println("LEFT");
			return true;
		} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			Log.d("ICS-Calendar", "Fling right");
			System.out.println("RIGHT");
			return true;
		}

		return false;
	}
}