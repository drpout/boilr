package com.github.andrefbsantos.boilr.listeners;

import java.io.IOException;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;

import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.boilr.services.LocalBinder;
import com.github.andrefbsantos.boilr.services.StorageAndControlService;

public class OnSwipeTouchListener implements OnTouchListener {

	private final GestureDetector gestureDetector;

	public OnSwipeTouchListener(Context ctx) {
		gestureDetector = new GestureDetector(ctx, new GestureListener(ctx));
	}

	private final class GestureListener extends SimpleOnGestureListener {

		private static final int SWIPE_THRESHOLD = 100;
		private static final int SWIPE_VELOCITY_THRESHOLD = 100;
		private Context ctx;
		private int pointToPosition;

		private StorageAndControlService mService;
		private boolean mBound;

		private ServiceConnection deleteAlarmsServiceConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName className, IBinder binder) {
				mService = ((LocalBinder<StorageAndControlService>) binder).getService();
				mBound = true;

				// Callback action performed after the service has been bound
				if (mBound) {
					try {
						mService.deleteAlarm(id);

						ArrayAdapter<AlarmWrapper> listAdapter = ((ArrayAdapter<AlarmWrapper>) ((ListActivity) ctx).getListAdapter());
						List<AlarmWrapper> alarms = mService.getAlarms();
						listAdapter.clear();
						listAdapter.addAll(alarms);
						listAdapter.notifyDataSetChanged();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						ctx.unbindService(deleteAlarmsServiceConnection);
					}
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName className) {
				mBound = false;
			}
		};
		private int id;

		public GestureListener(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			boolean result = false;
			try {
				float diffY = e2.getY() - e1.getY();
				float diffX = e2.getX() - e1.getX();
				if (Math.abs(diffX) > Math.abs(diffY)) {
					if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
						int pointToPosition = ((ListActivity) ctx).getListView().pointToPosition((int) e1.getX(), (int) e1.getY());

						id = ((AlarmWrapper) ((ListActivity) ctx).getListAdapter().getItem(pointToPosition)).getAlarm().getId();

						System.out.println(" delete position " + pointToPosition + " " + id);

						Intent serviceIntent = new Intent(ctx, StorageAndControlService.class);
						ctx.startService(serviceIntent);
						ctx.bindService(serviceIntent, deleteAlarmsServiceConnection, Context.BIND_AUTO_CREATE);
						// if (diffX > 0) {
						// onSwipeRight();
						// } else {
						// onSwipeLeft();
						// }
					}
					result = true;
				}
				// else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) >
				// SWIPE_VELOCITY_THRESHOLD) {
				// if (diffY > 0) {
				// onSwipeBottom();
				// } else {
				// onSwipeTop();
				// }
				// }
				else {
					System.out.println("just a click");
				}
				result = true;
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return result;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}
}
