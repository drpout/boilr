package mobi.boilr.boilr.activities;

import java.util.ArrayList;
import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.adapters.AlarmListAdapter;
import mobi.boilr.boilr.listeners.OnSwipeTouchListener;
import mobi.boilr.boilr.services.LocalBinder;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.views.fragments.AboutDialogFragment;
import mobi.boilr.libpricealarm.Alarm;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class AlarmListActivity extends ListActivity {

	private AlarmListAdapter adapter;
	private StorageAndControlService mStorageAndControlService;
	private boolean mBound;
	private SearchView searchView;
	private ServiceConnection mStorageAndControlServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mStorageAndControlService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;

			// Callback action performed after the service has been bound
			if(mBound) {
				Log.d("AlarmListActivity bound to StorageAndControlService.");
				List<Alarm> list = mStorageAndControlService.getAlarms();
				adapter.addAll(list);
			}
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
			adapter.getFilter().filter(newText);
			return true;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_list);
		PreferenceManager.setDefaultValues(this, R.xml.app_settings, false);

		getListView().setOnTouchListener(new OnSwipeTouchListener(this));
		adapter = new AlarmListAdapter(AlarmListActivity.this, new ArrayList<Alarm>());
		setListAdapter(adapter);

		Intent serviceIntent = new Intent(this, StorageAndControlService.class);
		startService(serviceIntent);
		bindService(serviceIntent, mStorageAndControlServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alarm_list, menu);
		searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setOnQueryTextListener(queryListener);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()) {
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
		Integer id = (Integer) v.findViewById(R.id.toggle_button).getTag();
		Log.d("ListView click " + id);
		Intent alarmSettingsIntent = new Intent(this, AlarmSettingsActivity.class);
		//Bundle bundle = new Bundle();
		//bundle.putInt("id", id);
		//alarmSettingsIntent.putExtras(bundle);
		alarmSettingsIntent.putExtra("id", id);
		startActivity(alarmSettingsIntent);
	}

	public void onAddAlarmClicked(View v) {
		// Handle click on Add Button. Launch activity to create a new alarm.
		Intent alarmCreationIntent = new Intent(this, AlarmCreationActivity.class);
		startActivity(alarmCreationIntent);
		adapter.notifyDataSetChanged();
	}

	public void onToggleClicked(View view) {
		int id = (Integer) view.getTag();
		if(mBound) {
			if(mStorageAndControlService.getAlarm(id).isOn()){
				Log.d("Turning off alarm " + id);
				mStorageAndControlService.stopAlarm(id);
			}else{
				mStorageAndControlService.startAlarm(id);
				Log.d("Turning on alarm " + id);
			}
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mStorageAndControlServiceConnection);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume(){
		super.onResume();
		adapter.clear();
		if(mBound){
			adapter.addAll(mStorageAndControlService.getAlarms());
			}
		adapter.notifyDataSetChanged();
	}
}