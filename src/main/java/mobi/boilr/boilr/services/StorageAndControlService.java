package mobi.boilr.boilr.services;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import mobi.boilr.boilr.database.DBManager;
import mobi.boilr.boilr.domain.AndroidNotifier;
import mobi.boilr.boilr.utils.AlarmAlertWakeLock;
import mobi.boilr.boilr.utils.Languager;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.utils.Notifications;
import mobi.boilr.boilr.views.fragments.AlarmPreferencesFragment;
import mobi.boilr.boilr.views.fragments.SettingsFragment;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libdynticker.exchanges.BitstampExchange;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.AlarmPositionComparator;
import mobi.boilr.libpricealarm.PriceHitAlarm;

public class StorageAndControlService extends Service {

	private static boolean wifiConnected = false;
	private static boolean mobileConnected = false;
	public static boolean allowMobileData = true;
	private Map<Integer, Alarm> alarmsMap;
	private Map<String, Exchange> exchangesMap;
	private List<Alarm> scheduledOffedAlarms = new ArrayList<Alarm>();
	private int nextAlarmID = 0;
	private AlarmManager alarmManager;
	private DBManager db;
	private SharedPreferences sharedPrefs;
	// Private action used to update last value from the Exchange.
	private static final String ACTION_RUN_ALARM = "ACTION_RUN_ALARM";
	private boolean offedAlarmsScheduled = false;
	// Preferred bind flags to use with this service.
	public static final int BIND_FLAGS = Context.BIND_ABOVE_CLIENT | Context.BIND_AUTO_CREATE;

	private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateConnectedFlags();
		}
	};

	private HashSet<Integer> runTaskAlarmList = new HashSet<Integer>();

	private class RunAlarmTask extends AsyncTask<Alarm, Void, Void> {

		@Override
		protected Void doInBackground(Alarm... alarms) {
			if(alarms.length == 1) {
				Alarm alarm = alarms[0];
				if(hasNetworkConnection()) {
					try {
						alarm.run();
						// Log.d("Last value for alarm " + alarm.getId() + " " +
						// Conversions.formatMaxDecimalPlaces(alarm.getLastValue()));
						Notifications.clearNoInternetNotification(StorageAndControlService.this);
						replaceAlarmDB(alarm);
					} catch(NumberFormatException e) {
						Log.e("Could format last value for alarm " + alarm.getId(), e);
					} catch(IOException e) {
						Notifications.showNoInternetNotification(StorageAndControlService.this);
						Log.e("Couldn't retrieve last value for alarm " + alarm.getId(), e);
					}
				} else {
					Notifications.showNoInternetNotification(StorageAndControlService.this);
				}
				runTaskAlarmList.remove(alarm.getId());
			}
			AlarmAlertWakeLock.releaseCpuLock();
			return null;
		}
	}

	private class GetLastValueTask extends
	AsyncTask<android.util.Pair<Exchange, Pair>, Void, Double> {
		private AlarmPreferencesFragment frag;

		public GetLastValueTask(AlarmPreferencesFragment frag) {
			super();
			this.frag = frag;
		}

		@Override
		protected Double doInBackground(android.util.Pair<Exchange, Pair>... pairs) {
			if(hasNetworkConnection() && pairs.length == 1) {
				try {
					return pairs[0].first.getLastValue(pairs[0].second);
				} catch (IOException e) {
					Log.e("Cannot get last value for " + pairs[0].first.getName() + " with pair " + pairs[0].second.toString(), e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Double result) {
			frag.getLastValueCallback(result);
		}
	}

	private class GetPairsTask extends AsyncTask<String, Void, List<Pair>> {
		private AlarmPreferencesFragment frag;
		String exchangeName, pairString;

		public GetPairsTask(AlarmPreferencesFragment frag, String exchangeName, String pairString) {
			super();
			this.frag = frag;
			this.exchangeName = exchangeName;
			this.pairString = pairString;
		}

		@Override
		protected List<Pair> doInBackground(String... exchangeCode) {
			try {
				return getExchange(exchangeCode[0]).getPairs();
			} catch (Exception e) {
				Log.e("Can't get pairs for " + exchangeCode[0], e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Pair> result) {
			frag.updatePairsListCallback(exchangeName, pairString, result);
		}
	}

	private class PopupalteDBTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg) {
			Log.d("Populating DB.");
			try {
				for(int i = 1; i < 10; i++) {
					Alarm alarm = new PriceHitAlarm(generateAlarmID(), new BitstampExchange(10900000),
							new Pair("BTC", "USD"), i * 10000, new AndroidNotifier(StorageAndControlService.this),
							false, 1 * 1000, i);
					addAlarm(alarm);
					startAlarm(alarm.getId());
				}

			} catch (Exception e) {
				Log.e("Caught exception while populating DB.", e);
			}
			return null;
		}

	}

	@SuppressLint("UseSparseArrays")
	@Override
	public void onCreate() {
		super.onCreate();
		Languager.setLanguage(this);
		// Register BroadcastReceiver to track connection changes.
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkReceiver, filter);
		updateConnectedFlags();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		allowMobileData = sharedPrefs.getBoolean(SettingsFragment.PREF_KEY_MOBILE_DATA, true);
		alarmsMap = new HashMap<Integer, Alarm>();
		exchangesMap = new HashMap<String, Exchange>();
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		try {
			db = new DBManager(this);
			nextAlarmID = db.getMaxID() + 1;
			alarmsMap = db.getAlarms();
			if(alarmsMap.isEmpty()) {
				// new PopupalteDBTask().execute();
			} else {
				// Set Exchange and start alarm
				for (Alarm alarm : alarmsMap.values()) {
					alarm.setExchange(getExchange(alarm.getExchangeCode()));
					if(alarm.isOn()) {
						addToAlarmManager(alarm, 0);
					}
				}
			}
		} catch (Exception e) {
			Log.e("Caught exception while recovering alarms from DB.", e);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null) {
			String action = intent.getAction();
			if(ACTION_RUN_ALARM.equals(action)) {
				int alarmID = intent.getIntExtra("alarmID", Integer.MIN_VALUE);
				if(alarmID != Integer.MIN_VALUE) {
					if(!runTaskAlarmList.contains(alarmID)) {
						runTaskAlarmList.add(alarmID);
						Alarm alarm = getAlarm(alarmID);
						AlarmAlertWakeLock.acquireCpuWakeLock(this);
						new RunAlarmTask().execute(alarm);
					}
				}
			} else if(Notifications.ACTION_CLEAR_NET_NOTIF.equals(action)) {
				Notifications.sClearedNoNetNotif = true;
			}
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder<StorageAndControlService>(this);
	}

	@Override
	public void onDestroy() {
		// Log.d("StorageAndControlService destroyed.");
		unregisterReceiver(networkReceiver);
		db.close();
		super.onDestroy();
	}

	public Exchange getExchange(String classname) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException, IllegalArgumentException,
	InvocationTargetException, SecurityException {
		if(exchangesMap.containsKey(classname)) {
			return exchangesMap.get(classname);
		} else {
			@SuppressWarnings("unchecked")
			Class<? extends Exchange> c = (Class<? extends Exchange>) Class.forName(classname);
			long pairInterval = Long.parseLong(sharedPrefs.getString(SettingsFragment.PREF_KEY_CHECK_PAIRS_INTERVAL, ""));
			Exchange exchange = (Exchange) c.getDeclaredConstructors()[0].newInstance(pairInterval);
			exchangesMap.put(classname, exchange);
			return exchange;
		}
	}

	public List<Exchange> getLoadedExchanges() {
		return new ArrayList<Exchange>(exchangesMap.values());
	}

	public List<Alarm> getAlarms() {
		List<Alarm> alarms = new ArrayList<Alarm>(alarmsMap.values());
		Collections.sort(alarms, new AlarmPositionComparator());
		return alarms;
	}

	public Alarm getAlarm(int alarmID) {
		return alarmsMap.get(alarmID);
	}

	public int generateAlarmID() {
		return nextAlarmID++;
	}

	public void scheduleOffedAlarms() {
		for(Alarm alarm : alarmsMap.values()) {
			if(!alarm.isOn())
				scheduledOffedAlarms.add(alarm);
		}
		for(Alarm alarm : scheduledOffedAlarms) {
			addToAlarmManager(alarm, 0);
		}
		offedAlarmsScheduled = true;
	}

	public void unscheduleOffedAlarms() {
		for(Alarm alarm : scheduledOffedAlarms) {
			removeFromAlarmManager(alarm.getId());
		}
		boolean onlyOffedAlarms = scheduledOffedAlarms.containsAll(alarmsMap.values());
		scheduledOffedAlarms.clear();
		if(onlyOffedAlarms)
			stopSelf();
		offedAlarmsScheduled = false;
	}

	public void refreshAlarms() {
		for(Alarm alarm : alarmsMap.values()) {
			resetAlarmPeriod(alarm);
		}
	}

	public void toggleAlarm(int alarmID) {
		Alarm alarm = alarmsMap.get(alarmID);
		alarm.toggle();
		if(alarm.isOn())
			scheduledOffedAlarms.remove(alarm);
		else
			scheduledOffedAlarms.add(alarm);
		replaceAlarmDB(alarm);
	}

	public void startAlarm(int alarmID) {
		Alarm alarm = alarmsMap.get(alarmID);
		alarm.turnOn();
		scheduledOffedAlarms.remove(alarm);
		addToAlarmManager(alarm, alarm.getPeriod());
		replaceAlarmDB(alarm);
	}

	public void stopAlarm(int alarmID) {
		Alarm alarm = alarmsMap.get(alarmID);
		alarm.turnOff();
		replaceAlarmDB(alarm);
		if(offedAlarmsScheduled) {
			/*
			 * Keep this alarm running if offed alarms are scheduled (i.e.
			 * AlarmListActivity is active).
			 */
			scheduledOffedAlarms.add(alarm);
		} else {
			removeFromAlarmManager(alarmID);
		}
	}

	public void addAlarm(Alarm alarm) throws IOException {
		alarmsMap.put(alarm.getId(), alarm);
		db.storeAlarm(alarm);
		addToAlarmManager(alarm, 0);
	}

	public void updateAlarmPosition(Alarm a1, Alarm a2) {
		int pos2 = a2.getPosition();
		a2.setPosition(a1.getPosition());
		a1.setPosition(pos2);
		replaceAlarmDB(a1);
		replaceAlarmDB(a2);
	}

	public void deleteAlarm(int alarmID) {
		deleteAlarm(alarmsMap.get(alarmID));
	}

	public void deleteAlarm(Alarm alarm) {
		Notifications.clearNotification(this, alarm);
		removeFromAlarmManager(alarm.getId());
		db.deleteAlarm(alarm);
		alarmsMap.remove(alarm.getId());
		scheduledOffedAlarms.remove(alarm);
		if(scheduledOffedAlarms.isEmpty() && alarmsMap.isEmpty()) {
			stopSelf();
		}
	}

	private void addToAlarmManager(Alarm alarm, long firstDelay) {
		Intent intent = new Intent(this, StorageAndControlService.class);
		intent.setAction(ACTION_RUN_ALARM);
		intent.putExtra("alarmID", alarm.getId());
		PendingIntent pendingIntent = PendingIntent.getService(this, alarm.getId(), intent, 0);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + firstDelay, alarm.getPeriod(), pendingIntent);
	}

	public void resetAlarmPeriod(Alarm alarm) {
		addToAlarmManager(alarm, 0);
	}

	private void removeFromAlarmManager(int alarmID) {
		Intent intent = new Intent(this, StorageAndControlService.class);
		intent.setAction(ACTION_RUN_ALARM);
		PendingIntent pendingIntent = PendingIntent.getService(this, alarmID, intent, 0);
		alarmManager.cancel(pendingIntent);
	}

	public void replaceAlarmDB(Alarm alarm) {
		try {
			db.updateAlarm(alarm);
		} catch (IOException e) {
			Log.e("Could not update alarm " + alarm.getId() + " in the DB.", e);
		}
	}

	/**
	 * Checks the network connection and sets the wifiConnected
	 * and mobileConnected variables accordingly.
	 */
	private void updateConnectedFlags() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		if(activeInfo != null && activeInfo.isConnected()) {
			wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
			mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
		} else {
			wifiConnected = false;
			mobileConnected = false;
		}
	}

	private static boolean hasNetworkConnection() {
		return wifiConnected || (mobileConnected && allowMobileData);
	}

	public void getPairs(AlarmPreferencesFragment frag, String exchangeCode,
			String exchangeName, String pairString) {
		new GetPairsTask(frag, exchangeName, pairString).execute(exchangeCode);
	}

	@SuppressWarnings("unchecked")
	public void getLastValue(AlarmPreferencesFragment frag, Exchange e, Pair p) {
		new GetLastValueTask(frag).execute(new android.util.Pair<Exchange, Pair>(e, p));
	}
}
