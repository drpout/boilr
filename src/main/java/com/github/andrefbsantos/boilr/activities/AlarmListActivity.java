package com.github.andrefbsantos.boilr.activities;

// <<<<<<< HEAD
import java.io.IOException;
import java.util.List;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.github.andrefbsantos.boilr.R;
import com.github.andrefbsantos.boilr.adaptar.AlarmListAdapter;
import com.github.andrefbsantos.boilr.database.DBManager;
import com.github.andrefbsantos.boilr.views.fragments.AboutDialogFragment;
import com.github.andrefbsantos.libpricealarm.Alarm;
import com.github.andrefbsantos.libpricealarm.UpperBoundSmallerThanLowerBoundException;

public class AlarmListActivity extends ListActivity {

	private ActionBar actionBar;
	private DBManager dbManager;
	private BaseAdapter adapter;

	private List<Alarm> alarms;
	private int alarmID = 1;

	// =======
	// import android.app.Activity;
	// import android.content.Intent;
	// import android.os.Bundle;
	// import android.preference.PreferenceManager;
	// import android.view.Menu;
	// import android.view.MenuItem;
	// import android.widget.SearchView;
	//
	// import com.github.andrefbsantos.boilr.R;
	// import com.github.andrefbsantos.boilr.views.fragments.AboutDialogFragment;
	//
	// public class AlarmListActivity extends Activity {
	// >>>>>>> 76f20563d20748b753fcebce645e8dd0b6c3a723

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// <<<<<<< HEAD

		setContentView(R.layout.alarm_list);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		try {
			dbManager = new DBManager(this);
			alarms = dbManager.getAlarms();
			adapter = new AlarmListAdapter(this, R.layout.price_hit_alarm_row, alarms);
			setListAdapter(adapter);
		} catch (UpperBoundSmallerThanLowerBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		// =======
		// setContentView(R.layout.alarm_list);
		// PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		// >>>>>>> 76f20563d20748b753fcebce645e8dd0b6c3a723
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alarm_list, menu);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// <<<<<<< HEAD
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// Handle list clicks. Pass corresponding alarm to populate the detailed view.
		Intent viewAlarmSettingsIntent = new Intent(this, ViewAlarmSettingsActivity.class);
		Alarm alarm = alarms.get(position);
		viewAlarmSettingsIntent.putExtra("alarm", alarm);
		startActivity(viewAlarmSettingsIntent);
	}

	public void onAddAlarmClicked(View v) {
		// Handle click on Add Button. Launch activity to create a new alarm.
		Intent alarmCreationIntent = new Intent(this, AlarmCreationActivity.class);
		startActivity(alarmCreationIntent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO
		// =======
		// // Handle presses on the action bar items
		// switch (item.getItemId()) {
		// case R.id.action_search:
		// // openSearch();
		// return true;
		// case R.id.action_settings:
		// Intent intent = new Intent(this, SettingsActivity.class);
		// startActivity(intent);
		// return true;
		// case R.id.action_about:
		// (new AboutDialogFragment()).show(getFragmentManager(), "about");
		// return true;
		// default:
		// return super.onOptionsItemSelected(item);
		// }
		//
		// >>>>>>> 76f20563d20748b753fcebce645e8dd0b6c3a723
	}
}
