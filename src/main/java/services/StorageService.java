package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.github.andrefbsantos.libdynticker.core.Exchange;

import domain.AlarmWrapper;

public class StorageService extends Service {

	private List<AlarmWrapper> alarmsList;
	private Map<String, Exchange> exchangesMap;

	@Override
	public void onCreate() {
		alarmsList = new ArrayList<AlarmWrapper>();
		exchangesMap = new HashMap<String, Exchange>();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
