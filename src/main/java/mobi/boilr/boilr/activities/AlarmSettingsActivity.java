package mobi.boilr.boilr.activities;

import mobi.boilr.boilr.services.LocalBinder;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.views.fragments.PriceChangeAlarmSettingsFragment;
import mobi.boilr.boilr.views.fragments.PriceHitAlarmSettingsFragment;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceChangeAlarm;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;

public class AlarmSettingsActivity extends PreferenceActivity {

	public static final String ID = "id";
	private StorageAndControlService mStorageAndControlService;
	private boolean mBound;

	private GetAlarmServiceConnection mStorageAndControlServiceConnection;

	private class GetAlarmServiceConnection implements ServiceConnection {
		private int id;

		public GetAlarmServiceConnection(int id) {
			this.id = id;
		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mStorageAndControlService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;
			// Callback action performed after the service has been bound
			Alarm alarm = mStorageAndControlService.getAlarm(id);
			if(alarm instanceof PriceHitAlarm) {
				getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceHitAlarmSettingsFragment(alarm)).commit();
			} else if(alarm instanceof PriceChangeAlarm) {
				getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceChangeAlarmSettingsFragment(alarm)).commit();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);		
        Bundle bund = getIntent().getExtras();
        Integer id = bund.getInt(ID);
		Intent serviceIntent = new Intent(this, StorageAndControlService.class);
		mStorageAndControlServiceConnection = new GetAlarmServiceConnection(id);
		bindService(serviceIntent, mStorageAndControlServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mStorageAndControlServiceConnection);
	}

	public StorageAndControlService getStorageAndControlService() {
		return mStorageAndControlService;
	}

	public boolean isBound() {
		return mBound;
	}
}
