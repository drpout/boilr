package com.github.andrefbsantos.boilr.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.boilr.utils.AlarmAlertWakeLock;

public class NotifyService extends Service {

	private final static String tag = "NotifyService";
	// Private action used to start a notification with this service.
	public static final String START_NOTIFY_ACTION = "START_NOTIFY";
	// Private action used to stop a notification with this service.
	public static final String STOP_NOTIFY_ACTION = "STOP_NOTIFY";

	private AlarmWrapper mCurrentAlarm = null;
	private StorageAndControlService mService;
	private boolean mBound;
	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	/**
	 * Utility method to help start a notification properly.
	 * Based on code from Android DeskClock.
	 */
	public static void startNotify(Context context, int alarmID) {
		Intent serviceIntent = new Intent(context, NotifyService.class);
		serviceIntent.setAction(START_NOTIFY_ACTION);
		serviceIntent.putExtra("alarmID", alarmID);
		// Maintain a cpu wake lock until the service can get it
		AlarmAlertWakeLock.acquireCpuWakeLock(context);
		context.startService(serviceIntent);
	}

	/**
	 * Utility method to help stop an alarm properly.
	 * Nothing will happen, if alarm is not firing or using a different instance.
	 * Based on code from Android DeskClock.
	 */
	public static void stopNotify(Context context, int alarmID) {
		Intent serviceIntent = new Intent(context, NotifyService.class);
		serviceIntent.setAction(STOP_NOTIFY_ACTION);
		serviceIntent.putExtra("alarmID", alarmID);
		// We don't need a wake lock here, since we are trying to kill an alarm
		context.startService(serviceIntent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (START_NOTIFY_ACTION.equals(intent.getAction())) {
			int alarmID = intent.getIntExtra("alarmID", Integer.MIN_VALUE);
			if (alarmID != Integer.MIN_VALUE) {
				Intent serviceIntent = new Intent(this, StorageAndControlService.class);
				bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
				if (mBound) {
					Log.d(tag, "Bound to StorageAndControlService.");
					AlarmWrapper alarm = mService.getAlarm(alarmID);
					if (mCurrentAlarm != null) {
						mCurrentAlarm = alarm;
						startFullNotify(mCurrentAlarm);
					} else
						addNotification(alarm);
				}
			}
		} else if (STOP_NOTIFY_ACTION.equals(intent.getAction()))
			stopSelf();
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopCurrentNotify();
		unbindService(mConnection);
	}

	private void addNotification(AlarmWrapper alarm) {
		// TODO Auto-generated method stub
	}

	private void startFullNotify(AlarmWrapper mCurrentAlarm2) {
		// TODO Auto-generated method stub
	}

	private void stopCurrentNotify() {
		// TODO Auto-generated method stub
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
