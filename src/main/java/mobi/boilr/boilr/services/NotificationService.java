package mobi.boilr.boilr.services;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.NotificationActivity;
import mobi.boilr.boilr.utils.AlarmAlertWakeLock;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Languager;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.utils.NotificationKlaxon;
import mobi.boilr.boilr.utils.Notifications;
import mobi.boilr.boilr.views.fragments.SettingsFragment;
import mobi.boilr.libpricealarm.Alarm;

public class NotificationService extends Service {

	// Private action used to start a notification with this service.
	private static final String START_NOTIFY_ACTION = "START_NOTIFY";
	// Private action used to stop a notification with this service.
	private static final String STOP_NOTIFY_ACTION = "STOP_NOTIFY";

	private static int currentAlarmID = Integer.MIN_VALUE;
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
			for (int alarmID : mPendingAlarms)
				startNotify(alarmID);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};
	private boolean mInFullScreen = false;
	private AlarmManager mAlarmManager;
	private SharedPreferences mSharedPrefs;

	/**
	 * Utility method to help start a notification properly.
	 * Based on code from Android DeskClock.
	 */
	public static void startNotify(Context context, int alarmID) {
		if(alarmID != currentAlarmID) {
			currentAlarmID = alarmID;
			Intent serviceIntent = new Intent(context, NotificationService.class);
			serviceIntent.setAction(START_NOTIFY_ACTION);
			serviceIntent.putExtra("alarmID", alarmID);
			// Maintain a cpu wake lock until the service can get it
			AlarmAlertWakeLock.acquireCpuWakeLock(context);
			context.startService(serviceIntent);
		}
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
		super.onCreate();
		Languager.setLanguage(this);
		Intent serviceIntent = new Intent(this, StorageAndControlService.class);
		bindService(serviceIntent, mConnection, StorageAndControlService.BIND_FLAGS);
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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
				mInFullScreen = false; //It might be redundant
				boolean keepMonitoring = intent.getBooleanExtra("keepMonitoring", false);
				if(keepMonitoring) {
					if(mBound)
						mService.startAlarm(alarmID);
					else
						Log.e(getString(R.string.not_bound, "NotificationService"));
				}
				stopSelf();
			}
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		if(currentAlarmID == Integer.MIN_VALUE) {
			Log.d("NotificationService: there is no current alarm to stop.");
		} else {
			NotificationKlaxon.stop(this);
			currentAlarmID = Integer.MIN_VALUE;
		}
		unbindService(mConnection);
		super.onDestroy();
		AlarmAlertWakeLock.releaseCpuLock();
	}

	private void startNotify(int alarmID) {
		Alarm alarm = mService.getAlarm(alarmID);
		int callState = mTelephonyManager.getCallState();
		boolean inCall = callState != TelephonyManager.CALL_STATE_IDLE;
		if(inCall) {
			/*
			 * Place a notification for this alarm in the drawer and
			 * keep the alarm on so it can fire again later.
			 */
			//Log.d("Showing in call notification for alarm " + alarmID + ".");
			Notifications.showStatusBarNotification(this, alarm);
			NotificationKlaxon.ringSingleNotification(this);
			stopSelf();
		} else if(!mInFullScreen){
			mInFullScreen = true;
			//Log.d("Showing fullscreen notification for alarm " + alarmID + ".");
			mService.stopAlarm(alarmID);
			Notifications.showFullscreenNotification(this, alarm);
			NotificationKlaxon.start(this, alarm);
			Intent intent = new Intent(NotificationActivity.FINISH_ACTION);
			intent.putExtra("alarmID", alarmID);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmID, intent, 0);
			// Time is in minutes, convert to milliseconds
			long snoozeTime = Conversions.MILIS_IN_MINUTE
					* Long.parseLong(mSharedPrefs.getString(SettingsFragment.PREF_KEY_AUTO_SNOOZE_TIME, "2"));
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + snoozeTime, pendingIntent);
		} else {
			/*
			 * A full screen notification is already being displayed.
			 * Place a notification for this alarm in the drawer and
			 * keep the alarm on so it can fire again later.
			 */
			//Log.d("Showing low priority notification for alarm " + alarmID + ".");
			Notifications.showStatusBarNotification(this, alarm);
			AlarmAlertWakeLock.releaseCpuLock();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
