package mobi.boilr.boilr.activities;

import java.util.ArrayList;
import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.listeners.SwipeAndMoveTouchListener;
import mobi.boilr.boilr.services.LocalBinder;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Languager;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.utils.Notifications;
import mobi.boilr.boilr.utils.Themer;
import mobi.boilr.boilr.utils.VersionTracker;
import mobi.boilr.boilr.views.fragments.AboutDialogFragment;
import mobi.boilr.boilr.views.fragments.SettingsFragment;
import mobi.boilr.boilr.widget.AlarmGridView;
import mobi.boilr.boilr.widget.AlarmLayout;
import mobi.boilr.boilr.widget.AlarmListAdapter;
import mobi.boilr.libpricealarm.Alarm;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class AlarmListActivity extends Activity {
	private static final int[] attrs = new int[] { R.attr.ic_action_search /*index 0*/};
	private static int REQUEST_SETTINGS = 0, REQUEST_CREATE = 1;
	private AlarmGridView mView;
	private AlarmListAdapter mAdapter;
	private SearchView mSearchView;
	private StorageAndControlService mStorageAndControlService;
	private boolean mBound;
	private boolean unscheduleOffedAlarms = true;
	private SwipeAndMoveTouchListener mTouchListener;
	private ServiceConnection mStorageAndControlServiceConnection = new ServiceConnection() {

		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mStorageAndControlService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;
			// Callback action performed after the service has been bound
			List<Alarm> alarms = mStorageAndControlService.getAlarms();
			mAdapter.clear();
			mAdapter.addAll(alarms);
			mStorageAndControlService.scheduleOffedAlarms();
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	private OnQueryTextListener queryListener = new OnQueryTextListener() {
		@Override
		public boolean onQueryTextSubmit(String query) {
			return true;
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			mAdapter.getFilter().filter(newText);
			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Themer.applyTheme(this);
		Languager.setLanguage(this);
		VersionTracker.showChangeLog(this);
		PreferenceManager.setDefaultValues(this, R.xml.app_settings, false);
		if(VersionTracker.isFirstRun) {
			PreferenceManager.getDefaultSharedPreferences(this).edit()
					.putString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, Conversions.getSystemRingtone(RingtoneManager.TYPE_ALARM, this))
					.commit();
		}
		setContentView(R.layout.alarm_list);
		mView = ((AlarmGridView) findViewById(R.id.list));
		mAdapter = new AlarmListAdapter(AlarmListActivity.this, new ArrayList<Alarm>());
		mView.setAdapter(mAdapter);
		mView.start();
		mTouchListener = new SwipeAndMoveTouchListener(this);
		mView.setOnTouchListener(mTouchListener);
		OnItemClickListener listener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
				Alarm alarm = ((AlarmLayout) view).getAlarm();
				Intent alarmSettingsIntent = new Intent(AlarmListActivity.this, AlarmSettingsActivity.class);
				alarmSettingsIntent.putExtra(AlarmSettingsActivity.ALARM_ID, alarm.getId());
				alarmSettingsIntent.putExtra(AlarmSettingsActivity.ALARM_TYPE, alarm.getClass().getSimpleName());
				AlarmListActivity.this.startActivity(alarmSettingsIntent);
			}
		};
		mView.setOnItemClickListener(listener);
		Intent serviceIntent = new Intent(this, StorageAndControlService.class);
		startService(serviceIntent);
		bindService(serviceIntent, mStorageAndControlServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alarm_list, menu);
		mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		mSearchView.setOnQueryTextListener(queryListener);
		/*
		 * Hack to keep the search icon consistent between themes. Without this
		 * the icon for the LIGHT theme is smaller than the one on the DARK
		 * theme. By just_user on Stack Overflow http://stackoverflow.com/questions/10445760/how-to-change-the-default-icon-on-the-searchview-to-be-use-in-the-action-bar-on/18360563#18360563
		 */
		int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
		ImageView view = (ImageView) mSearchView.findViewById(searchImgId);
		TypedArray ta = obtainStyledAttributes(attrs);
		view.setImageResource(ta.getResourceId(0, R.drawable.ic_action_remove_light));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()) {
			case R.id.action_add_alarm:
				startActivityForResult(new Intent(this, AlarmCreationActivity.class), REQUEST_CREATE);
				return true;
			case R.id.action_settings:
				startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
				return true;
			case R.id.action_about:
				(new AboutDialogFragment()).show(getFragmentManager(), "about");
				return true;
			case R.id.action_refresh_alarms:
				if(mBound) {
					mStorageAndControlService.refreshAlarms();
				} else {
					Log.e(getString(R.string.not_bound, "AlarmListActivity"));
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void onToggleClicked(View view) {
		int id = (Integer) view.getTag();
		if(mBound) {
			mStorageAndControlService.toggleAlarm(id);
			mAdapter.notifyDataSetChanged();
		} else {
			Log.e(getString(R.string.not_bound, "AlarmListActivity"));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_SETTINGS && resultCode == SettingsActivity.RESULT_RESTART) {
			/*
			 * finish() runs always after startActivity() therefore we need the
			 * following boolean to avoid staying with unscheduled offed alarms
			 * while the newly created activity is running.
			 */
			unscheduleOffedAlarms = false;
			finish();
			startActivity(getIntent());
		} else if(requestCode == REQUEST_CREATE && resultCode == Activity.RESULT_OK) {
			int alarmID = data.getIntExtra("ALARM_ID", Integer.MIN_VALUE);
			if(alarmID != Integer.MIN_VALUE) {
				if(mBound) {
					mAdapter.add(mStorageAndControlService.getAlarm(alarmID));
				} else {
					Log.e(getString(R.string.not_bound, "AlarmListActivity"));
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		mTouchListener.clearUndoBar();
		if(mBound) {
			if(unscheduleOffedAlarms)
				mStorageAndControlService.unscheduleOffedAlarms();
			unbindService(mStorageAndControlServiceConnection);
		} else {
			Log.e(getString(R.string.not_bound, "AlarmListActivity"));
		}
		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();
		mView.start();
		Notifications.sClearedNoNetNotif = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onStop(){
		super.onStop();
		mView.stop();
	}

	public boolean ismBound() {
		return mBound;
	}

	public StorageAndControlService getStorageAndControlService() {
		return mStorageAndControlService;
	}

	public AlarmListAdapter getAdapter() {
		return mAdapter;
	}

	public GridView getGridView() {
		return mView;
	}
}
