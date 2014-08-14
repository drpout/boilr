package com.github.andrefbsantos.boilr.activities;

import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ToggleButton;
import aunused.ToggleAlarmServiceConnection;

import com.github.andrefbsantos.boilr.R;
import com.github.andrefbsantos.boilr.adapters.AlarmListAdapter;
import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.boilr.services.StorageAndControlService;
import com.github.andrefbsantos.boilr.services.StorageAndControlService.StorageAndControlServiceBinder;
import com.github.andrefbsantos.boilr.views.fragments.AboutDialogFragment;

public class AlarmListActivity extends ListActivity {

	private ArrayAdapter<AlarmWrapper> adapter;

	private StorageAndControlService mService;
	private boolean mBound;

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection getAllAlarmsConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = ((StorageAndControlServiceBinder) binder).getService();
			mBound = true;

			List<AlarmWrapper> list = null;
			if (mBound) {
				list = mService.getAlarms();
				// unbindService(conn)
				unbindService(getAllAlarmsConnection);
			}

			adapter = new AlarmListAdapter(AlarmListActivity.this, R.layout.price_hit_alarm_row, list);
			setListAdapter(adapter);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	private ServiceConnection getAlarmConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = ((StorageAndControlServiceBinder) binder).getService();
			mBound = true;

			List<AlarmWrapper> list = null;
			System.out.println("asdasd");
			if (mBound) {
				list = mService.getAlarms();
				unbindService(getAllAlarmsConnection);
			}

			adapter = new AlarmListAdapter(AlarmListActivity.this, R.layout.price_hit_alarm_row, list);
			setListAdapter(adapter);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	private GestureDetector gestureDetector;
	private View.OnTouchListener gestureListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_list);
		PreferenceManager.setDefaultValues(this, R.xml.app_settings, false);

		Intent serviceIntent = new Intent(this, StorageAndControlService.class);
		bindService(serviceIntent, getAllAlarmsConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alarm_list, menu);
		menu.findItem(R.id.action_search).getActionView();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case R.id.action_search:
				// openSearch();
				return true;
			case R.id.action_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_about:
				(new AboutDialogFragment()).show(getFragmentManager(), "about");
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long layout) {
		// Handle list clicks. Pass corresponding alarm to populate the detailed view.
		int id = (Integer) v.findViewById(R.id.toggle_button).getTag();

		Intent alarmSettingsIntent = new Intent(this, AlarmSettingsActivity.class);
		alarmSettingsIntent.putExtra("id", id);
		startActivity(alarmSettingsIntent);
		System.out.println("DONE");

	}

	public void onAddAlarmClicked(View v) {
		// Handle click on Add Button. Launch activity to create a new alarm.
		Intent alarmCreationIntent = new Intent(this, AlarmCreationActivity.class);
		startActivity(alarmCreationIntent);
	}

	public void onToggleClicked(View view) {
		boolean on = ((ToggleButton) view).isChecked();

		int id = (Integer) view.getTag();

		// Find Alarm, and change its state

		ToggleAlarmServiceConnection toggleAlarmServiceConnection = new ToggleAlarmServiceConnection(adapter, id);

		// ServiceConnection changeAlarmStateConnection = new ServiceConnection() {
		//
		// @Override
		// public void onServiceConnected(ComponentName className, IBinder binder) {
		// mService = ((StorageAndControlServiceBinder) binder).getService();
		// mBound = true;
		//
		// List<AlarmWrapper> list = null;
		// System.out.println("asdasd");
		// if (mBound) {
		// list = mService.getAlarms();
		// unbindService(changeAlarmStateConnection);
		// }
		//
		// adapter = new AlarmListAdapter(AlarmListActivity.this, R.layout.price_hit_alarm_row,
		// list);
		// setListAdapter(adapter);
		// }
		//
		// @Override
		// public void onServiceDisconnected(ComponentName className) {
		// mBound = false;
		// }
		// };

		Intent serviceIntent = new Intent(this, StorageAndControlService.class);
		bindService(serviceIntent, toggleAlarmServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}