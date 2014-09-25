package mobi.boilr.boilr.views.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmListActivity;
import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.boilr.services.LocalBinder;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.UpperBoundSmallerThanLowerBoundException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class AlarmCreationFragment extends PreferenceFragment {

	protected static final String PREF_KEY_GENERIC = "pref_key_generic";
	protected static final String PREF_KEY_SPECIFIC = "pref_key_specific";
	protected static final String PREF_KEY_ALERTS = "pref_key_alerts";
	protected static final String PREF_VALUE_PRICE_HIT = "price_hit";
	protected static final String PREF_VALUE_PRICE_CHANGE = "price_change";
	protected static final String PREF_KEY_EXCHANGE = "pref_key_exchange";
	protected static final String PREF_KEY_TYPE = "pref_key_type";
	protected static final String PREF_KEY_PAIR = "pref_key_pair";
	protected static final String PREF_KEY_ALARM_ALERT_SOUND = "pref_key_alarm_alert_sound";
	protected static final String PREF_KEY_ALARM_ALERT_TYPE = "pref_key_alarm_alert_type";
	protected static final String PREF_KEY_ALARM_VIBRATE = "pref_key_alarm_vibrate";
	protected static final String PREF_KEY_UPDATE_INTERVAL = "pref_key_update_interval";
	private int exchangeIndex = 0;
	protected int pairIndex = 0;
	protected Activity enclosingActivity;

	protected OnAlarmSettingsPreferenceChangeListener listener = new OnAlarmSettingsPreferenceChangeListener();

	protected class OnAlarmSettingsPreferenceChangeListener implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			if(key.equals(PREF_KEY_EXCHANGE)) {
				ListPreference listPreference = (ListPreference) preference;
				exchangeIndex = (listPreference).findIndexOfValue((String) newValue);
				listPreference.setSummary((listPreference).getEntries()[exchangeIndex]);
				if(mBound) {
					pairs = mStorageAndControlService.getPairs((String) newValue);
					CharSequence[] sequence = new CharSequence[pairs.size()];
					CharSequence[] ids = new CharSequence[pairs.size()];
					for (int i = 0; i < pairs.size(); i++) {
						sequence[i] = pairs.get(i).getCoin() + "/" + pairs.get(i).getExchange();
						ids[i] = String.valueOf(i);
					}
					ListPreference pairListPreference = (ListPreference) findPreference(PREF_KEY_PAIR);
					pairListPreference.setEntries(sequence);
					pairListPreference.setEntryValues(ids);
					pairListPreference.setDefaultValue(ids[0]);
					pairListPreference.setSummary(sequence[0]);
					pairListPreference.setValueIndex(0);
				} else {
					Log.d("Not Bound");
				}
			} else if(key.equals(PREF_KEY_PAIR)) {
				pairIndex = Integer.parseInt((String) newValue);
				preference.setSummary(pairs.get(pairIndex).toString());
			} else if(key.equals(PREF_KEY_TYPE)) {
				if(newValue.equals(PREF_VALUE_PRICE_CHANGE)) {
					enclosingActivity.getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceChangeAlarmCreationFragment(exchangeIndex, pairIndex)).commit();
				} else if(newValue.equals(PREF_VALUE_PRICE_HIT)) {
					enclosingActivity.getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceHitAlarmCreationFragment(exchangeIndex, pairIndex)).commit();
				}
			} else if(key.equals(PREF_KEY_ALARM_ALERT_TYPE)) {
				ListPreference alertTypePref = (ListPreference) preference;
				alertTypePref.setSummary(alertTypePref.getEntries()[alertTypePref.findIndexOfValue((String) newValue)]);
				// Change selectable ringtones according to the alert type
				RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_ALARM_ALERT_SOUND);
				int ringtoneType = Integer.parseInt((String) newValue);
				alertSoundPref.setRingtoneType(ringtoneType);
				String defaultRingtone = RingtoneManager.getDefaultUri(ringtoneType).toString();
				alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName(defaultRingtone, enclosingActivity));
			} else if(key.equals(PREF_KEY_ALARM_ALERT_SOUND)) {
				RingtonePreference alertSoundPref = (RingtonePreference) preference;
				alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName((String) newValue, enclosingActivity));
			} else if(key.equals(PREF_KEY_ALARM_VIBRATE)) {
				defaultVibrateDefinition = false;
			} else {
				Log.d("No behavior for " + key);
			}
			return true;
		}
	}

	protected StorageAndControlService mStorageAndControlService;
	protected boolean mBound;
	private ServiceConnection mStorageAndControlServiceConnection;

	private class StorageAndControlServiceConnection implements ServiceConnection {

		private CharSequence exchangeCode;
		private ListPreference pairListPreference;

		public StorageAndControlServiceConnection(CharSequence exchangeCode,
				ListPreference pairListPreference) {
			this.exchangeCode = exchangeCode;
			this.pairListPreference = pairListPreference;

		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mStorageAndControlService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;
			// Callback action performed after the service has been bound
			if(mBound) {
				pairs = mStorageAndControlService.getPairs((String) exchangeCode);
				CharSequence[] sequence = new CharSequence[pairs.size()];
				CharSequence[] ids = new CharSequence[pairs.size()];
				for (int i = 0; i < pairs.size(); i++) {
					sequence[i] = pairs.get(i).getCoin() + "/" + pairs.get(i).getExchange();
					ids[i] = String.valueOf(i);
				}
				pairListPreference.setEntries(sequence);
				pairListPreference.setEntryValues(ids);
				pairListPreference.setSummary(sequence[pairIndex]);
				pairListPreference.setValueIndex(pairIndex);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	protected boolean defaultVibrateDefinition = true;
	protected List<Pair> pairs = new ArrayList<Pair>();

	public AlarmCreationFragment(int exchangeIndex, int pairIndex) {
		this.exchangeIndex = exchangeIndex;
		this.pairIndex = pairIndex;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		enclosingActivity = getActivity();

		addPreferencesFromResource(R.xml.alarm_settings);

		ListPreference exchangeListPref = (ListPreference) findPreference(PREF_KEY_EXCHANGE);
		CharSequence exchangeCode = exchangeListPref.getEntryValues()[exchangeIndex];
		exchangeListPref.setSummary(exchangeListPref.getEntries()[exchangeIndex]);
		exchangeListPref.setValueIndex(exchangeIndex);
		exchangeListPref.setOnPreferenceChangeListener(listener);

		ListPreference pairListPref = (ListPreference) findPreference(PREF_KEY_PAIR);
		pairListPref.setOnPreferenceChangeListener(listener);
		Intent serviceIntent = new Intent(enclosingActivity, StorageAndControlService.class);
		mStorageAndControlServiceConnection = new StorageAndControlServiceConnection(exchangeCode, pairListPref);
		enclosingActivity.bindService(serviceIntent, mStorageAndControlServiceConnection, Context.BIND_AUTO_CREATE);
		pairListPref.setOnPreferenceChangeListener(listener);

		ListPreference alarmTypePref = (ListPreference) findPreference(PREF_KEY_TYPE);
		alarmTypePref.setOnPreferenceChangeListener(listener);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);
		ListPreference alarmAlertTypePref = (ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE);
		alarmAlertTypePref.setSummary(alarmAlertTypePref.getEntries()[alarmAlertTypePref.findIndexOfValue(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_TYPE, ""))]);
		alarmAlertTypePref.setValue(null);
		alarmAlertTypePref.setOnPreferenceChangeListener(listener);

		RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_ALARM_ALERT_SOUND);
		alertSoundPref.setDefaultValue(null);
		alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, ""), enclosingActivity));
		alertSoundPref.setOnPreferenceChangeListener(listener);

		CheckBoxPreference vibratePref = (CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE);
		boolean defaultValue = sharedPreferences.getBoolean(SettingsFragment.PREF_KEY_VIBRATE_DEFAULT, true);
		vibratePref.setDefaultValue(defaultValue);
		vibratePref.setChecked(defaultValue);
		vibratePref.setOnPreferenceChangeListener(listener);

		setHasOptionsMenu(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_send_now:
				createAlarmAndReturn();
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		enclosingActivity.unbindService(mStorageAndControlServiceConnection);
	}

	private void createAlarmAndReturn() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);

		Integer alertType = ((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue() != null ? Integer.parseInt(((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue()) : null;
		String alertSound;
		if(sharedPreferences.getString(PREF_KEY_ALARM_ALERT_SOUND, "").equals(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, ""))) {
			alertSound = null;
		} else {
			alertSound = sharedPreferences.getString(PREF_KEY_ALARM_ALERT_SOUND, "");
		}

		Boolean vibrate = defaultVibrateDefinition ? null : ((CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE)).isChecked();

		Log.d(alertType + " " + alertSound + " " + vibrate);

		AndroidNotify notify = new AndroidNotify(enclosingActivity, alertType, alertSound, vibrate);
		int id = mStorageAndControlService.generateAlarmID();

		try {
			Exchange exchange = mStorageAndControlService.getExchange(((ListPreference) findPreference(PREF_KEY_EXCHANGE)).getValue());
			Pair pair = pairs.get(pairIndex);
			makeAlarm(id, exchange, pair, notify);
			Intent intent = new Intent(enclosingActivity, AlarmListActivity.class);
			startActivity(intent);
		} catch (Exception e) {
			Log.e("Failed to create alarm.", e);
			Toast.makeText(enclosingActivity, "Failed to create alarm. " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	protected abstract void makeAlarm(int id, Exchange exchange, Pair pair, AndroidNotify notify)
			throws UpperBoundSmallerThanLowerBoundException, IOException, InterruptedException,
			ExecutionException;
}
