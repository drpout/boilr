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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.andrefbsantos.boilr.R;
import com.github.andrefbsantos.boilr.adapters.AlarmListAdapter;
import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.boilr.listeners.OnSwipeTouchListener;
import com.github.andrefbsantos.boilr.services.LocalBinder;
import com.github.andrefbsantos.boilr.services.StorageAndControlService;
import com.github.andrefbsantos.boilr.views.fragments.AboutDialogFragment;

public class AlarmListActivity extends ListActivity {

	private int id;
	private final static String tag = "AlarmListActivity";

	private ArrayAdapter<AlarmWrapper> adapter;
	private StorageAndControlService mService;
	private boolean mBound;

	private ServiceConnection getAllAlarmsServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;

			// Callback action performed after the service has been bound
			if(mBound) {
				Log.d(tag, "It's Bound");
				List<AlarmWrapper> list = mService.getAlarms();
				adapter = new AlarmListAdapter(AlarmListActivity.this, R.layout.price_hit_alarm_row, list);
				setListAdapter(adapter);
				unbindService(getAllAlarmsServiceConnection);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	private ServiceConnection toggleAlarmServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;

			if(mBound) {
				List<AlarmWrapper> list = mService.getAlarms();
				Log.d(tag, "ID=" + id);

				for (AlarmWrapper wrapper : list) {
					if(wrapper.getAlarm().getId() == id) {
						wrapper.getAlarm().toggle();
						break;
					}
				}
				// adapter.clear();
				// adapter.addAll(list);
				adapter.notifyDataSetChanged();
				unbindService(toggleAlarmServiceConnection);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_list);
		PreferenceManager.setDefaultValues(this, R.xml.app_settings, false);

		getListView().setOnTouchListener(new OnSwipeTouchListener(this));

		Intent serviceIntent = new Intent(this, StorageAndControlService.class);
		startService(serviceIntent);
		bindService(serviceIntent, getAllAlarmsServiceConnection, Context.BIND_AUTO_CREATE);
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
		Log.d(tag, "ListView click " + id);
		// Intent alarmSettingsIntent = new Intent(this, AlarmSettingsActivity.class);
		// alarmSettingsIntent.putExtra("id", id);
		// startActivity(alarmSettingsIntent);
	}

	public void onAddAlarmClicked(View v) {
		// Handle click on Add Button. Launch activity to create a new alarm.
		Intent alarmCreationIntent = new Intent(this, AlarmCreationActivity.class);
		startActivity(alarmCreationIntent);
	}

	public void onToggleClicked(View view) {
		id = (Integer) view.getTag();
		Intent serviceIntent = new Intent(this, StorageAndControlService.class);
		bindService(serviceIntent, toggleAlarmServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mService = null;
	}

	@Override
	public void onStart() {
		super.onStart();
		// if (mBound) {
		// System.out.println("Bounded");
		// List<AlarmWrapper> list = mService.getAlarms();
		// adapter = new AlarmListAdapter(AlarmListActivity.this, R.layout.price_hit_alarm_row,
		// list);
		// setListAdapter(adapter);
		// unbindService(mConnection);
		// }
	}
}