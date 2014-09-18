package mobi.boilr.boilr.listeners;

import mobi.boilr.boilr.adapters.AlarmListAdapter;
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

public class OnSwipeTouchListener implements OnTouchListener {

	private final GestureDetector gestureDetector;

	public OnSwipeTouchListener(Context ctx) {
		gestureDetector = new GestureDetector(ctx, new GestureListener(ctx));
	}

	private final class GestureListener extends SimpleOnGestureListener {

		private static final int SWIPE_THRESHOLD = 100;
		private static final int SWIPE_VELOCITY_THRESHOLD = 100;
		private Context context;
		private StorageAndControlService mService;
		private ServiceConnection deleteAlarmsServiceConnection = new ServiceConnection() {

			@SuppressWarnings("unchecked")
			@Override
			// Callback action performed after the service has been bound
			public void onServiceConnected(ComponentName className, IBinder binder) {
				mService = ((LocalBinder<StorageAndControlService>) binder).getService();
				mService.deleteAlarm(alarm);
			}

			@Override
			public void onServiceDisconnected(ComponentName className) {
			}
		};
		private Alarm alarm;

		public GestureListener(Context ctx) {
			this.context = ctx;
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
						int pointToPosition = ((ListActivity) context).getListView().pointToPosition((int) e1.getX(), (int) e1.getY());
						AlarmListAdapter adapter = (AlarmListAdapter) ((ListActivity) context).getListAdapter();
						adapter.remove(pointToPosition);
						alarm = adapter.getItem(pointToPosition);
						Log.d("Delete position " + pointToPosition + " with alarm " + alarm.getId());
						Intent serviceIntent = new Intent(context, StorageAndControlService.class);
						context.bindService(serviceIntent, deleteAlarmsServiceConnection, Context.BIND_AUTO_CREATE);
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
