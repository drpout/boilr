package mobi.boilr.boilr.services;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import mobi.boilr.boilr.database.DBManager;
import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.boilr.utils.AlarmAlertWakeLock;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.utils.Notifications;
import mobi.boilr.boilr.utils.PercentageAlarmParameter;
import mobi.boilr.boilr.utils.VariationAlarmParameter;
import mobi.boilr.boilr.views.fragments.SettingsFragment;
import mobi.boilr.libdynticker.bitstamp.BitstampExchange;
import mobi.boilr.libdynticker.btcchina.BTCChinaExchange;
import mobi.boilr.libdynticker.btce.BTCEExchange;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.Notify;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import mobi.boilr.libpricealarm.PriceVarAlarm;
import mobi.boilr.libpricealarm.UpperBoundSmallerThanLowerBoundException;
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

public class StorageAndControlService extends Service {

	private static boolean wifiConnected = false;
	private static boolean mobileConnected = false;
	public static boolean allowMobileData = false;
	private Map<Integer, Alarm> alarmsMap;
	private Map<String, Exchange> exchangesMap;
	private int prevAlarmID = 0;
	private AlarmManager alarmManager;
	private DBManager db;
	private SharedPreferences sharedPrefs;
	// Private action used to update last value from the Exchange.
	private static final String UPDATE_LAST_VALUE = "UPDATE_LAST_VALUE";

	private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateConnectedFlags();
		}
	};

	private class UpdateLastValueTask extends AsyncTask<Alarm, Void, Void> {
		@Override
		protected Void doInBackground(Alarm... alarms) {
			if(hasNetworkConnection()) {
				if(alarms.length == 1) {
					try {
						alarms[0].run();
						Log.d("Last value for alarm " + alarms[0].getId() + " " + alarms[0].getLastValue());
					} catch (IOException e) {
						Log.e("Could not retrieve last value for alarm " + alarms[0].getId(), e);
					}
				}
				Notifications.clearNoInternetNotification(StorageAndControlService.this);
			} else {
				Notifications.showNoInternetNotification(StorageAndControlService.this);
			}
			AlarmAlertWakeLock.releaseCpuLock();
			return null;
		}
	}

	private class PopupalteDBTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg) {
			Log.d("Populating DB.");
			try {
				Alarm alarm = new PriceHitAlarm(generateAlarmID(), new BitstampExchange(10000000), new Pair("BTC", "USD"), 60000, new AndroidNotify(StorageAndControlService.this), 476, 475);
				addAlarm(alarm);
				startAlarm(alarm);

				alarm = new PriceHitAlarm(generateAlarmID(), new BTCEExchange(10000000), new Pair("BTC", "EUR"), 60000, new AndroidNotify(StorageAndControlService.this), 374, 373);
				addAlarm(alarm);
				startAlarm(alarm);

				if(hasNetworkConnection()) {
					alarm = new PriceVarAlarm(generateAlarmID(), new BTCChinaExchange(10000000), new Pair("BTC", "CNY"), 60000, new AndroidNotify(StorageAndControlService.this), 0.01f);
					addAlarm(alarm);
					startAlarm(alarm);
				}
			} catch (Exception e) {
				Log.e("Caught exception while populating DB.", e);
			}
			return null;
		}

	}

	private class GetPairsTask extends AsyncTask<String, Void, List<Pair>> {
		@Override
		protected List<Pair> doInBackground(String... exchangeCode) {
			try {
				return getExchange(exchangeCode[0]).getPairs();
			} catch (Exception e) {
				Log.e("Can't get pairs for " + exchangeCode[0], e);
			}
			return null;
		}
	}

	private class addPercentageAlarm extends
			AsyncTask<PercentageAlarmParameter, Void, PriceVarAlarm> {

		@Override
		protected PriceVarAlarm doInBackground(PercentageAlarmParameter... arg0) {
			if(arg0.length == 1) {
				int id = arg0[0].getId();
				Exchange exchange = arg0[0].getExchange();
				Pair pair = arg0[0].getPair();
				long period = arg0[0].getPeriod();
				Notify notify = arg0[0].getNotify();
				float percent = arg0[0].getPercent();
				try {
					return new PriceVarAlarm(id, exchange, pair, period, notify, percent);
				} catch (Exception e) {
					Log.e("AssyncTask AddPercentageAlarm failed", e);
				}
			}
			return null;
		}
	}

	private class addVariationAlarm extends AsyncTask<VariationAlarmParameter, Void, PriceVarAlarm> {

		@Override
		protected PriceVarAlarm doInBackground(VariationAlarmParameter... arg0) {
			if(arg0.length == 1) {
				int id = arg0[0].getId();
				Exchange exchange = arg0[0].getExchange();
				Pair pair = arg0[0].getPair();
				long period = arg0[0].getPeriod();
				Notify notify = arg0[0].getNotify();
				double variation = arg0[0].getVariation();
				try {
					return new PriceVarAlarm(id, exchange, pair, period, notify, variation);
				} catch (Exception e) {
					Log.e("AssyncTask AddVariationAlarm failed", e);
				}
			}
			return null;
		}

	}

	@SuppressLint("UseSparseArrays")
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("Creating StorageAndControlService.");
		// Register BroadcastReceiver to track connection changes.
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkReceiver, filter);
		updateConnectedFlags();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		allowMobileData = sharedPrefs.getBoolean(SettingsFragment.PREF_KEY_MOBILE_DATA, false);
		alarmsMap = new HashMap<Integer, Alarm>();
		exchangesMap = new HashMap<String, Exchange>();
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		try {
			db = new DBManager(this);
			prevAlarmID = db.getNextID();
			alarmsMap = db.getAlarms();
			if(prevAlarmID == 0) {
				// new PopupalteDBTask().execute();
			} else {
				// Set Exchange and start alarm
				for (Alarm alarm : alarmsMap.values()) {
					alarm.setExchange(getExchange(alarm.getExchangeCode()));
					if(alarm.isOn()) {
						this.startAlarm(alarm);
					}
				}
			}
		} catch (Exception e) {
			Log.e("Caught exception while recovering alarms from DB.", e);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if(action != null && UPDATE_LAST_VALUE.equals(action)) {
			int alarmID = intent.getIntExtra("alarmID", Integer.MIN_VALUE);
			if(alarmID != Integer.MIN_VALUE) {
				Alarm alarm = getAlarm(alarmID);
				AlarmAlertWakeLock.acquireCpuWakeLock(this);
				new UpdateLastValueTask().execute(alarm);
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
		Log.d("StorageAndControlService destroyed.");
		super.onDestroy();
		unregisterReceiver(networkReceiver);
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

	public List<Alarm> getAlarms() {
		return new ArrayList<Alarm>(alarmsMap.values());
	}

	public Alarm getAlarm(int alarmID) {
		return alarmsMap.get(alarmID);
	}

	public int generateAlarmID() {
		return ++prevAlarmID;
	}

	public void startAlarm(Alarm alarm) {
		Intent intent = new Intent(this, StorageAndControlService.class);
		intent.setAction(UPDATE_LAST_VALUE);
		intent.putExtra("alarmID", alarm.getId());
		PendingIntent pendingIntent = PendingIntent.getService(this, alarm.getId(), intent, 0);
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarm.getPeriod(), alarm.getPeriod(), pendingIntent);
		alarm.turnOn();
		replaceAlarm(alarm);
	}

	public void startAlarm(int alarmID) {
		startAlarm(alarmsMap.get(alarmID));
	}

	public void stopAlarm(Alarm alarm) {
		stopAlarm(alarm.getId());
	}

	public void stopAlarm(int alarmID) {
		Intent intent = new Intent(this, StorageAndControlService.class);
		intent.setAction(UPDATE_LAST_VALUE);
		PendingIntent pendingIntent = PendingIntent.getService(this, alarmID, intent, 0);
		alarmManager.cancel(pendingIntent);
		Alarm alarm = alarmsMap.get(alarmID);
		alarm.turnOff();
		replaceAlarm(alarm);
		if(!anyActiveAlarm())
			stopSelf();
	}

	public void addAlarm(Alarm alarm) throws IOException {
		alarmsMap.put(alarm.getId(), alarm);
		db.storeAlarm(alarm);
	}

	public void replaceAlarm(Alarm alarm) {
		try {
			db.updateAlarm(alarm);
		} catch (IOException e) {
			Log.e("Could not update alarm " + alarm.getId() + " in the DB.", e);
		}
	}

	public void deleteAlarm(Alarm alarm) {
		db.deleteAlarm(alarm);
		alarmsMap.remove(alarm.getId());
	}

	public void deleteAlarm(int id) {
		deleteAlarm(alarmsMap.get(id));
	}

	private boolean anyActiveAlarm() {
		for (Alarm alarm : alarmsMap.values())
			if(alarm.isOn())
				return true;
		return false;
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

	public List<Pair> getPairs(String exchangeCode) {
		try {
			return new GetPairsTask().execute(exchangeCode).get();
		} catch (Exception e) {
			Log.e("Get Pairs", e);
		}
		return null;
	}

	public void addAlarm(int id, Exchange exchange, Pair pair, long period, AndroidNotify notify,
			float percent) throws InterruptedException, ExecutionException, IOException {
		// Var alarms always check last value to build variation
		PriceVarAlarm priceVarAlarm = ((new addPercentageAlarm()).execute(new PercentageAlarmParameter(id, exchange, pair, period, notify, percent))).get();
		addAlarm(priceVarAlarm);
		this.startAlarm(priceVarAlarm);
	}

	public void addAlarm(int id, Exchange exchange, Pair pair, long period, AndroidNotify notify,
			double variation) throws InterruptedException, ExecutionException, IOException {
		// Var alarms always check last value to build variation
		PriceVarAlarm priceVarAlarm = ((new addVariationAlarm()).execute(new VariationAlarmParameter(id, exchange, pair, period, notify, variation))).get();
		addAlarm(priceVarAlarm);
		this.startAlarm(priceVarAlarm);
	}

	public void addAlarm(int id, Exchange exchange, Pair pair, long period, AndroidNotify notify,
			double upperBound, double lowerBound) throws UpperBoundSmallerThanLowerBoundException,
			IOException {
		PriceHitAlarm priceHitAlarm = new PriceHitAlarm(id, exchange, pair, period, notify, upperBound, lowerBound);
		addAlarm(priceHitAlarm);
		this.startAlarm(priceHitAlarm);
	}
}
