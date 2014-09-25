package mobi.boilr.boilr.services;

import java.util.ArrayList;
import java.util.List;

import mobi.boilr.boilr.utils.AlarmAlertWakeLock;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.utils.NotificationKlaxon;
import mobi.boilr.boilr.utils.Notifications;
import mobi.boilr.libpricealarm.Alarm;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class NotificationService extends Service {

	// Private action used to start a notification with this service.
	private static final String START_NOTIFY_ACTION = "START_NOTIFY";
	// Private action used to stop a notification with this service.
	private static final String STOP_NOTIFY_ACTION = "STOP_NOTIFY";

	private Alarm mCurrentAlarm = null;
	private TelephonyManager mTelephonyManager;
	private StorageAndControlService mService;
	private boolean mBound;
	private List<Integer> mPendingAlarms = new ArrayList<Integer>();
	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;
			Log.d("NotificationService bound to StorageAndControlService.");
			for (int alarmID : mPendingAlarms)
				startNotify(alarmID);
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
	public void onCreate() {
		Intent serviceIntent = new Intent(this, StorageAndControlService.class);
		bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		int alarmID = intent.getIntExtra("alarmID", Integer.MIN_VALUE);
		if(alarmID == Integer.MIN_VALUE)
			stopSelf();
		else {
			if(START_NOTIFY_ACTION.equals(action)) {
				if(mBound)
					startNotify(alarmID);
				else
					mPendingAlarms.add(alarmID);
			} else if(STOP_NOTIFY_ACTION.equals(action)) {
				boolean keepMonitoring = intent.getBooleanExtra("keepMonitoring", false);
				if(!keepMonitoring)
					if(mBound)
						mService.stopAlarm(alarmID);
				stopSelf();
			}
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

	private void startNotify(int alarmID) {
		Alarm alarm = mService.getAlarm(alarmID);
		if(mCurrentAlarm == null) {
			mCurrentAlarm = alarm;
			int callState = mTelephonyManager.getCallState();
			boolean inCall = callState != TelephonyManager.CALL_STATE_IDLE;
			if(inCall) {
				/*
				 * Place a notification for this alarm in the drawer and
				 * keep the alarm on so it can fire again later.
				 */
				Log.d("Showing in call notification for alarm " + alarmID + ".");
				Notifications.showLowPriorityNotification(this, alarm);
				NotificationKlaxon.ringSingleNotification(this);
				stopSelf();
			} else {
				Log.d("Showing fullscreen notification for alarm " + alarmID + ".");
				Notifications.showAlarmNotification(this, mCurrentAlarm);
				NotificationKlaxon.start(this, mCurrentAlarm);
			}
		} else {
			/*
			 * A full screen notification is already being displayed.
			 * Place a notification for this alarm in the drawer and
			 * keep the alarm on so it can fire again later.
			 */
			Log.d("Showing low priority notification for alarm " + alarmID + ".");
			Notifications.showLowPriorityNotification(this, alarm);
			AlarmAlertWakeLock.releaseCpuLock();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
