package com.github.andrefbsantos.boilr.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.boilr.utils.Log;

public class UpdateLastValueService extends Service {

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

	@Override
	public void onCreate() {
		Intent serviceIntent = new Intent(this, StorageAndControlService.class);
		bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
		Log.d("Creating UpdateLastValueService.");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int alarmID = intent.getIntExtra("alarmID", Integer.MIN_VALUE);
		if(alarmID != Integer.MIN_VALUE) {
			Log.d("UpdateLastValueService running for alarm " + alarmID);
			if(mBound) {
				Log.d("UpdateLastValueService bound to StorageAndControlService.");
				AlarmWrapper wrapper = mService.getAlarm(alarmID);
				wrapper.getAlarm().run();
			} else
				Log.d("UpdateLastValueService NOT bound to StorageAndControlService.");
		}
		stopSelf();
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(mConnection);
	}

}
