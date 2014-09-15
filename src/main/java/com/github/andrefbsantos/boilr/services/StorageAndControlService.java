package com.github.andrefbsantos.boilr.services;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.github.andrefbsantos.boilr.database.DBManager;
import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.boilr.domain.AndroidNotify;
import com.github.andrefbsantos.boilr.utils.Log;
import com.github.andrefbsantos.boilr.views.fragments.SettingsFragment;
import com.github.andrefbsantos.libdynticker.bitstamp.BitstampExchange;
import com.github.andrefbsantos.libdynticker.btcchina.BTCChinaExchange;
import com.github.andrefbsantos.libdynticker.btce.BTCEExchange;
import com.github.andrefbsantos.libdynticker.core.Exchange;
import com.github.andrefbsantos.libdynticker.core.Pair;
import com.github.andrefbsantos.libpricealarm.Alarm;
import com.github.andrefbsantos.libpricealarm.PriceHitAlarm;

public class StorageAndControlService extends Service {

	private Map<Integer, AlarmWrapper> alarmsMap;
	private Map<String, Exchange> exchangesMap;
	private int prevAlarmID = 0;
	private AlarmManager alarmManager;
	private DBManager db;
	private AndroidNotify notify;

	@SuppressLint("UseSparseArrays")
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("Creating StorageAndControlService.");
		alarmsMap = new HashMap<Integer, AlarmWrapper>();
		exchangesMap = new HashMap<String, Exchange>();
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		notify = new AndroidNotify(this);
		try {
			db = new DBManager(this);
			prevAlarmID = db.getNextID();
			alarmsMap = db.getAlarms();
			if(prevAlarmID == 0) {
				populateDB();
			}
			// Set Exchange and start alarm
			for (AlarmWrapper wrapper : alarmsMap.values()) {
				wrapper.getAlarm().setExchange(getExchange(wrapper.getAlarm().getExchangeCode()));
				if(wrapper.getAlarm().isOn()) {
					this.startAlarm(wrapper);
				}
			}
		} catch (Exception e) {
			Log.e("Caught exception while recovering alarms from DB.", e);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder<StorageAndControlService>(this);
	}

	public Exchange getExchange(String classname) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException, IllegalArgumentException,
	InvocationTargetException, SecurityException {
		if(exchangesMap.containsKey(classname)) {
			return exchangesMap.get(classname);
		} else {
			@SuppressWarnings("unchecked")
			Class<? extends Exchange> c = (Class<? extends Exchange>) Class.forName(classname);
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			long pairInterval = Long.parseLong(sharedPreferences.getString(SettingsFragment.PREF_KEY_CHECK_PAIRS_INTERVAL, ""));
			Exchange exchange = (Exchange) c.getDeclaredConstructors()[0].newInstance(pairInterval);
			exchangesMap.put(classname, exchange);
			return exchange;
		}
	}

	public List<AlarmWrapper> getAlarms() {
		return new ArrayList<AlarmWrapper>(alarmsMap.values());
	}

	public AlarmWrapper getAlarm(int alarmID) {
		return alarmsMap.get(alarmID);
	}

	public int generateAlarmID() {
		return ++prevAlarmID;
	}

	public void startAlarm(AlarmWrapper wrapper) {
		Intent intent = new Intent(this, UpdateLastValueService.class);
		intent.putExtra("alarmID", wrapper.getAlarm().getId());
		PendingIntent pendingIntent = PendingIntent.getService(this, wrapper.getAlarm().getId(), intent, 0);
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, wrapper.getAlarm().getPeriod(), wrapper.getAlarm().getPeriod(), pendingIntent);
	}

	public void startAlarm(int alarmID) {
		startAlarm(alarmsMap.get(alarmID));
	}

	public void stopAlarm(AlarmWrapper wrapper) {
		stopAlarm(wrapper.getAlarm().getId());
	}

	public void stopAlarm(int alarmID) {
		Intent intent = new Intent(this, UpdateLastValueService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, alarmID, intent, 0);
		alarmManager.cancel(pendingIntent);
	}

	public void addAlarm(AlarmWrapper wrapper) throws IOException {
		alarmsMap.put(wrapper.getAlarm().getId(), wrapper);
		db.storeAlarm(wrapper);
	}

	public void replaceAlarm(AlarmWrapper wrapper) throws IOException {
		db.updateAlarm(wrapper);
	}

	public void deleteAlarm(AlarmWrapper wrapper) throws IOException {
		db.deleteAlarm(wrapper);
		alarmsMap.remove(wrapper.getAlarm().getId());
	}

	public void deleteAlarm(int id) throws IOException {
		deleteAlarm(alarmsMap.get(id));
	}

	public AndroidNotify getNotify() {
		return notify;
	}

	// Only used to place Alarms on DB
	private void populateDB() {
		Log.d("Populating DB.");
		try {
			Alarm alarm = new PriceHitAlarm(generateAlarmID(), new BitstampExchange(10000000), new Pair("BTC", "USD"), 60000, notify, 10000, 300);
			AlarmWrapper wrapper = new AlarmWrapper(alarm);
			addAlarm(wrapper);
			startAlarm(wrapper);

			alarm = new PriceHitAlarm(generateAlarmID(), new BTCEExchange(10000000), new Pair("BTC", "EUR"), 60000, notify, 800, 100);
			wrapper = new AlarmWrapper(alarm);
			addAlarm(wrapper);
			startAlarm(wrapper);

			alarm = new PriceHitAlarm(generateAlarmID(), new BTCChinaExchange(10000000), new Pair("BTC", "CNY"), 60000, notify, 10000, 500);
			wrapper = new AlarmWrapper(alarm);
			addAlarm(wrapper);
			startAlarm(wrapper);

			((PriceHitAlarm) wrapper.getAlarm()).setLowerBound(200);
			replaceAlarm(wrapper);
		} catch (Exception e) {
			Log.e("Caught exception while populating DB.", e);
		}
	}
}
