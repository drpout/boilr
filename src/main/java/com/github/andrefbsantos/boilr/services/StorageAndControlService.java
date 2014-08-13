package com.github.andrefbsantos.boilr.services;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import com.github.andrefbsantos.boilr.R;
import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.boilr.views.fragments.SettingsFragment;
import com.github.andrefbsantos.libdynticker.core.Exchange;

public class StorageAndControlService extends Service {

	private Map<Integer, AlarmWrapper> alarmsMap;
	private Map<String, Exchange> exchangesMap;
	private long prevAlarmID = 0;
	private final IBinder binder = new StorageAndControlServiceBinder();
	AlarmManager alarmManager;

	public class StorageAndControlServiceBinder extends Binder {
		StorageAndControlService getService() {
			// Return this instance of StorageService so clients can call public methods
			return StorageAndControlService.this;
		}
	}

	@Override
	public void onCreate() {
		alarmsMap = new HashMap<Integer, AlarmWrapper>();
		exchangesMap = new HashMap<String, Exchange>();
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		// TODO Retrieve alarms and prevAlarmID for DB
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public Exchange getExchange(String classname) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException, IllegalArgumentException,
	InvocationTargetException, SecurityException {
		if(exchangesMap.containsKey(classname))
			return exchangesMap.get(classname);
		else {
			@SuppressWarnings("unchecked")
			Class<? extends Exchange> c = (Class<? extends Exchange>) Class.forName(classname);
			SharedPreferences sharedPreferences = this.getSharedPreferences(getResources().getResourceEntryName(R.xml.app_settings), Context.MODE_PRIVATE);
			long pairInterval = Long.parseLong(sharedPreferences.getString(SettingsFragment.PREF_KEY_CHECK_PAIRS_INTERVAL, ""));
			Exchange exchange = (Exchange) c.getDeclaredConstructors()[0].newInstance(pairInterval);
			exchangesMap.put(classname, exchange);
			return exchange;
		}
	}

	public Collection<AlarmWrapper> getAlarms() {
		return alarmsMap.values();
	}

	public long generateAlarmID() {
		return ++prevAlarmID;
	}

	public void startAlarm(AlarmWrapper wrapper) {
		Intent intent = new Intent(this, UpdateLastValueService.class);
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
}
