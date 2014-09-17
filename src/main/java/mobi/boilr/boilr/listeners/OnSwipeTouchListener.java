package mobi.boilr.boilr.listeners;

import java.util.List;

import mobi.boilr.boilr.services.LocalBinder;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.Alarm;
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

public class OnSwipeTouchListener implements OnTouchListener {

	private final GestureDetector gestureDetector;

	public OnSwipeTouchListener(Context ctx) {
		gestureDetector = new GestureDetector(ctx, new GestureListener(ctx));
	}

	private final class GestureListener extends SimpleOnGestureListener {

		private static final int SWIPE_THRESHOLD = 100;
		private static final int SWIPE_VELOCITY_THRESHOLD = 100;
		private Context ctx;

		private StorageAndControlService mService;
		private boolean mBound;

		private ServiceConnection deleteAlarmsServiceConnection = new ServiceConnection() {

			@SuppressWarnings("unchecked")
			@Override
			public void onServiceConnected(ComponentName className, IBinder binder) {
				mService = ((LocalBinder<StorageAndControlService>) binder).getService();
				mBound = true;

				// Callback action performed after the service has been bound
				if(mBound) {
					mService.deleteAlarm(id);
					ArrayAdapter<Alarm> listAdapter = ((ArrayAdapter<Alarm>) ((ListActivity) ctx).getListAdapter());
					List<Alarm> alarms = mService.getAlarms();
					listAdapter.clear();
					listAdapter.addAll(alarms);
					listAdapter.notifyDataSetChanged();
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
				if(Math.abs(diffX) > Math.abs(diffY)) {
					if(Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
						int pointToPosition = ((ListActivity) ctx).getListView().pointToPosition((int) e1.getX(), (int) e1.getY());

						id = ((Alarm) ((ListActivity) ctx).getListAdapter().getItem(pointToPosition)).getId();

						Log.d("Delete position " + pointToPosition + " " + id);

						Intent serviceIntent = new Intent(ctx, StorageAndControlService.class);
						ctx.startService(serviceIntent);
						ctx.bindService(serviceIntent, deleteAlarmsServiceConnection, Context.BIND_AUTO_CREATE);
					}
					result = true;
				} else {
					Log.d("Just a click.");
				}
				result = true;
			} catch(Exception exception) {
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
