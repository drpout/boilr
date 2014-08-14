package com.github.andrefbsantos.boilr.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.boilr.services.StorageAndControlService.StorageAndControlServiceBinder;

public class UpdateLastValueService extends Service {

	private StorageAndControlService mService;
	private boolean mBound;
	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = ((StorageAndControlServiceBinder) binder).getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int alarmID = intent.getIntExtra("alarmID", Integer.MIN_VALUE);
		if (alarmID != Integer.MIN_VALUE) {
			Intent serviceIntent = new Intent(this, StorageAndControlServiceBinder.class);
			bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
			if (mBound) {
				AlarmWrapper wrapper = mService.getAlarm(alarmID);
				if (!wrapper.getAlarm().run()) {
					mService.stopAlarm(alarmID);
				}
				unbindService(mConnection);
			}
		}
		stopSelf();
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
