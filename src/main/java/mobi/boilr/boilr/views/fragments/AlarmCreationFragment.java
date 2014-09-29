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
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.UpperBoundSmallerThanLowerBoundException;
import android.app.Activity;
import android.app.Fragment;
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
	protected Activity enclosingActivity;
	protected int exchangeIndex = 0;
	protected int pairIndex = 0;
	protected boolean defaultVibrateDef = true;
	protected List<Pair> pairs = new ArrayList<Pair>();

	protected OnAlarmSettingsPreferenceChangeListener listener = new OnAlarmSettingsPreferenceChangeListener();

	protected class OnAlarmSettingsPreferenceChangeListener implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			if(key.equals(PREF_KEY_EXCHANGE)) {
				ListPreference listPref = (ListPreference) preference;
				exchangeIndex = listPref.findIndexOfValue((String) newValue);
				String exchangeName = (String) listPref.getEntries()[exchangeIndex];
				listPref.setSummary(exchangeName);
				if(mBound) {
					ListPreference pairListPref = (ListPreference) findPreference(PREF_KEY_PAIR);
					pairIndex = 0;
					updatePairsList(pairListPref, (String) newValue, exchangeName, pairIndex);
				} else {
					Log.d("AlarmCreationFragment not bound to StorageAndControlService.");
				}
			} else if(key.equals(PREF_KEY_PAIR)) {
				pairIndex = Integer.parseInt((String) newValue);
				preference.setSummary(pairs.get(pairIndex).toString());
				updateDependentOnPair();
			} else if(key.equals(PREF_KEY_TYPE)) {
				Fragment creationFrag;
				Bundle args = new Bundle();
				args.putInt("exchangeIndex", exchangeIndex);
				args.putInt("pairIndex", pairIndex);
				ListPreference alarmAlertTypePref = (ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE);
				args.putString("alertType", alarmAlertTypePref.getValue());
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);
				args.putString("alertSound", sharedPrefs.getString(PREF_KEY_ALARM_ALERT_SOUND, ""));
				CheckBoxPreference vibratePref = (CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE);
				args.putBoolean("vibrate", vibratePref.isChecked());
				args.putBoolean("defaultVibrateDef", defaultVibrateDef);
				if(newValue.equals(PREF_VALUE_PRICE_CHANGE)) {
					creationFrag = new PriceChangeAlarmCreationFragment();
				} else { // newValue.equals(PREF_VALUE_PRICE_HIT))
					creationFrag = new PriceHitAlarmCreationFragment();
				}
				creationFrag.setArguments(args);
				enclosingActivity.getFragmentManager().beginTransaction().replace(android.R.id.content, creationFrag).commit();
			} else if(key.equals(PREF_KEY_ALARM_ALERT_TYPE)) {
				ListPreference alertTypePref = (ListPreference) preference;
				alertTypePref.setSummary(alertTypePref.getEntries()[alertTypePref.findIndexOfValue((String) newValue)]);
				// Change selectable ringtones according to the alert type
				RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_ALARM_ALERT_SOUND);
				int ringtoneType = Integer.parseInt((String) newValue);
				alertSoundPref.setRingtoneType(ringtoneType);
				String defaultRingtone = RingtoneManager.getDefaultUri(ringtoneType).toString();
				alertSoundPref.setSummary(Conversions.ringtoneUriToName(defaultRingtone, enclosingActivity));
			} else if(key.equals(PREF_KEY_ALARM_ALERT_SOUND)) {
				RingtonePreference alertSoundPref = (RingtonePreference) preference;
				alertSoundPref.setSummary(Conversions.ringtoneUriToName((String) newValue, enclosingActivity));
			} else if(key.equals(PREF_KEY_ALARM_VIBRATE)) {
				defaultVibrateDef = false;
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
		private CharSequence exchangeName;
		private ListPreference pairListPref;

		public StorageAndControlServiceConnection(CharSequence exchangeCode,
				CharSequence exchangeName, ListPreference pairListPref) {
			this.exchangeCode = exchangeCode;
			this.exchangeName = exchangeName;
			this.pairListPref = pairListPref;

		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mStorageAndControlService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;
			// Callback action performed after the service has been bound
			updatePairsList(pairListPref, exchangeCode.toString(), exchangeName.toString(), pairIndex);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		enclosingActivity = getActivity();
		addPreferencesFromResource(R.xml.alarm_settings);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);
		ListPreference exchangeListPref = (ListPreference) findPreference(PREF_KEY_EXCHANGE);
		ListPreference pairListPref = (ListPreference) findPreference(PREF_KEY_PAIR);
		ListPreference alarmAlertTypePref = (ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE);
		RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_ALARM_ALERT_SOUND);
		CheckBoxPreference vibratePref = (CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE);
		Preference[] prefs = { exchangeListPref,
				pairListPref,
				findPreference(PREF_KEY_TYPE),
				alarmAlertTypePref,
				alertSoundPref,
				vibratePref };
		for(Preference pref : prefs) {
			pref.setOnPreferenceChangeListener(listener);
		}
		if(savedInstanceState == null) {
			Bundle args = getArguments();
			String alertType = null, alertSound = null;
			Boolean vibrate = null, defaultDef = null;
			if(args != null) {
				exchangeIndex = args.getInt("exchangeIndex");
				pairIndex = args.getInt("pairIndex");
				alertType = args.getString("alertType");
				alertSound = args.getString("alertSound");
				vibrate = args.getBoolean("vibrate");
				defaultDef = args.getBoolean("defaultVibrateDef");
			}
			alarmAlertTypePref.setValue(alertType);
			if(alertType == null) {
				alertType = sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_TYPE, "");
			}
			alarmAlertTypePref.setSummary(alarmAlertTypePref.getEntries()[alarmAlertTypePref.findIndexOfValue(alertType)]);
			alertSoundPref.setRingtoneType(Integer.parseInt(alertType));

			alertSoundPref.setDefaultValue(alertSound);
			if(alertSound == null) {
				alertSound = sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, "");
			}
			alertSoundPref.setSummary(Conversions.ringtoneUriToName(alertSound, enclosingActivity));

			if(defaultDef == null) {
				vibrate = sharedPrefs.getBoolean(SettingsFragment.PREF_KEY_VIBRATE_DEFAULT, true);
			} else {
				defaultVibrateDef = defaultDef;
			}
			vibratePref.setChecked(vibrate);
		} else {
			exchangeIndex = savedInstanceState.getInt("exchangeIndex");
			pairIndex = savedInstanceState.getInt("pairIndex");
			defaultVibrateDef = savedInstanceState.getBoolean("defaultVibrateDef");

			if(alarmAlertTypePref.getValue() == null) {
				alarmAlertTypePref.setSummary(alarmAlertTypePref.getEntries()[alarmAlertTypePref.findIndexOfValue(sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_TYPE, ""))]);
			} else {
				alarmAlertTypePref.setSummary(alarmAlertTypePref.getEntry());
			}

			String ringtoneUri = sharedPrefs.getString(PREF_KEY_ALARM_ALERT_SOUND, null);
			if(ringtoneUri == null) {
				ringtoneUri = sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, "");
			}
			alertSoundPref.setSummary(Conversions.ringtoneUriToName(ringtoneUri, enclosingActivity));
		}
		CharSequence exchangeCode = exchangeListPref.getEntryValues()[exchangeIndex];
		CharSequence exchangeName = exchangeListPref.getEntries()[exchangeIndex];
		exchangeListPref.setSummary(exchangeName);
		exchangeListPref.setValueIndex(exchangeIndex);

		Intent serviceIntent = new Intent(enclosingActivity, StorageAndControlService.class);
		mStorageAndControlServiceConnection = new StorageAndControlServiceConnection(exchangeCode, exchangeName, pairListPref);
		enclosingActivity.bindService(serviceIntent, mStorageAndControlServiceConnection, Context.BIND_AUTO_CREATE);

		setHasOptionsMenu(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
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

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("exchangeIndex", exchangeIndex);
		savedInstanceState.putInt("pairIndex", pairIndex);
		savedInstanceState.putBoolean("defaultVibrateDef", defaultVibrateDef);
	}

	private void updatePairsList(ListPreference pairListPref, String exchangeCode,
			String exchangeName, int index) {
		try {
			pairs = mStorageAndControlService.getPairs(exchangeCode);
			if(pairs == null)
				throw new Exception("Pairs is null.");
			CharSequence[] sequence = new CharSequence[pairs.size()];
			CharSequence[] ids = new CharSequence[pairs.size()];
			for(int i = 0; i < pairs.size(); i++) {
				sequence[i] = pairs.get(i).getCoin() + "/" + pairs.get(i).getExchange();
				ids[i] = String.valueOf(i);
			}
			pairListPref.setEnabled(true);
			pairListPref.setEntries(sequence);
			pairListPref.setEntryValues(ids);
			pairListPref.setSummary(sequence[index]);
			pairListPref.setValueIndex(index);
			updateDependentOnPair();
		} catch(Exception e) {
			String message = enclosingActivity.getString(R.string.couldnt_retrieve_pairs, exchangeName);
			Toast.makeText(enclosingActivity, message, Toast.LENGTH_LONG).show();
			Log.e(message, e);
			pairListPref.setEntries(null);
			pairListPref.setEntryValues(null);
			pairListPref.setSummary(null);
			pairListPref.setEnabled(false);
			disableDependentOnPair();
		}
	}

	private void createAlarmAndReturn() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);

		Integer alertType = ((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue() != null ? Integer.parseInt(((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue()) : null;
		String alertSound;
		if(sharedPrefs.getString(PREF_KEY_ALARM_ALERT_SOUND, "").equals(sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, ""))) {
			alertSound = null;
		} else {
			alertSound = sharedPrefs.getString(PREF_KEY_ALARM_ALERT_SOUND, "");
		}

		Boolean vibrate = defaultVibrateDef ? null : ((CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE)).isChecked();

		Log.d(alertType + " " + alertSound + " " + vibrate);

		AndroidNotify notify = new AndroidNotify(enclosingActivity, alertType, alertSound, vibrate);
		int id = mStorageAndControlService.generateAlarmID();

		try {
			Exchange exchange = mStorageAndControlService.getExchange(((ListPreference) findPreference(PREF_KEY_EXCHANGE)).getValue());
			Pair pair = pairs.get(pairIndex);
			makeAlarm(id, exchange, pair, notify);
			Intent intent = new Intent(enclosingActivity, AlarmListActivity.class);
			startActivity(intent);
		} catch(Exception e) {
			String failedCreate = enclosingActivity.getString(R.string.failed_create_alarm);
			Log.e(failedCreate, e);
			Toast.makeText(enclosingActivity, failedCreate + " " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	protected abstract void makeAlarm(int id, Exchange exchange, Pair pair, AndroidNotify notify)
			throws UpperBoundSmallerThanLowerBoundException, IOException, InterruptedException,
			ExecutionException;

	protected abstract void updateDependentOnPair();

	protected abstract void disableDependentOnPair();
}
