package com.github.andrefbsantos.boilr.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.boilr.utils.AlarmAlertWakeLock;
import com.github.andrefbsantos.boilr.utils.Log;
import com.github.andrefbsantos.boilr.utils.NotificationKlaxon;
import com.github.andrefbsantos.boilr.utils.Notifications;

public class NotificationService extends Service {

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

	private TelephonyManager mTelephonyManager;

	/**
	 * Utility method to help start a notification properly.
	 * Based on code from Android DeskClock.
	 */
	public static void startNotify(Context context, int alarmID) {
		Intent serviceIntent = new Intent(context, NotificationService.class);
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
	public static void stopNotify(Context context, int alarmID, boolean keepMonitoring) {
		Intent serviceIntent = new Intent(context, NotificationService.class);
		serviceIntent.setAction(STOP_NOTIFY_ACTION);
		serviceIntent.putExtra("alarmID", alarmID);
		serviceIntent.putExtra("keepMonitoring", keepMonitoring);
		// We don't need a wake lock here, since we are trying to kill an alarm
		context.startService(serviceIntent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int alarmID;
		if(START_NOTIFY_ACTION.equals(intent.getAction())) {
			alarmID = intent.getIntExtra("alarmID", Integer.MIN_VALUE);
			if(alarmID != Integer.MIN_VALUE) {
				Intent serviceIntent = new Intent(this, StorageAndControlService.class);
				bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
				if(mBound) {
					Log.d("NotificationService bound to StorageAndControlService.");
					AlarmWrapper alarm = mService.getAlarm(alarmID);
					if(mCurrentAlarm == null) {
						mCurrentAlarm = alarm;
						startFullscreenNotify(mCurrentAlarm);
					} else
						Notifications.showLowPriorityNotification(this, alarm);
				}
			}
		} else if(STOP_NOTIFY_ACTION.equals(intent.getAction())) {
			alarmID = intent.getIntExtra("alarmID", Integer.MIN_VALUE);
			if(alarmID != Integer.MIN_VALUE) {
				boolean keepMonitoring = intent.getBooleanExtra("keepMonitoring", false);
				if(!keepMonitoring)
					if(mBound)
						mService.stopAlarm(alarmID);
			}
			stopSelf();
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mCurrentAlarm == null) {
			Log.v("NotificationService - There is no current alarm to stop.");
			return;
		}
		NotificationKlaxon.stop(this);
		mCurrentAlarm = null;
		AlarmAlertWakeLock.releaseCpuLock();
		unbindService(mConnection);
	}

	private void startFullscreenNotify(AlarmWrapper currentAlarm) {
		AlarmAlertWakeLock.acquireCpuWakeLock(this);
		Notifications.showAlarmNotification(this, currentAlarm);
		int callState = mTelephonyManager.getCallState();
		boolean inCall = callState != TelephonyManager.CALL_STATE_IDLE;
		NotificationKlaxon.start(this, currentAlarm, inCall);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
