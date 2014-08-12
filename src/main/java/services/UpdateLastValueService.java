package services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UpdateLastValueService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
