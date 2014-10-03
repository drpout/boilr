package mobi.boilr.boilr.views.fragments;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import mobi.boilr.boilr.R;
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
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.RingtonePreference;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class AlarmCreationFragment extends AlarmPreferencesFragment {
	protected boolean defaultVibrateDef = true;

	private class UpdatePairsConnection implements ServiceConnection {
		private String exchangeCode;
		private String exchangeName;
		private String pairString;

		public UpdatePairsConnection(String exchangeCode, String exchangeName, String pairString) {
			super();
			this.exchangeCode = exchangeCode;
			this.exchangeName = exchangeName;
			this.pairString = pairString;
		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mStorageAndControlService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;
			updatePairsList(exchangeCode, exchangeName, pairString);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	protected abstract class OnAlarmSettingsPreferenceChangeListener implements
	OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			if(key.equals(PREF_KEY_EXCHANGE)) {
				ListPreference listPref = (ListPreference) preference;
				exchangeIndex = listPref.findIndexOfValue((String) newValue);
				String exchangeName = (String) listPref.getEntries()[exchangeIndex];
				listPref.setSummary(exchangeName);
				ListPreference pairListPref = (ListPreference) findPreference(PREF_KEY_PAIR);
				pairIndex = 0;
				updatePairsList((String) newValue, exchangeName, null);
			} else if(key.equals(PREF_KEY_PAIR)) {
				pairIndex = Integer.parseInt((String) newValue);
				preference.setSummary(pairs.get(pairIndex).toString());
				updateDependentOnPairAux();
			} else if(key.equals(PREF_KEY_TYPE)) {
				Fragment creationFrag;
				Bundle args = new Bundle();
				args.putInt("exchangeIndex", exchangeIndex);
				args.putInt("pairIndex", pairIndex);
				ListPreference alarmAlertTypePref = (ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE);
				args.putString("alertType", alarmAlertTypePref.getValue());
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		mStorageAndControlServiceConnection = new UpdatePairsConnection(exchangeCode.toString(), exchangeName.toString(), null);
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
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("exchangeIndex", exchangeIndex);
		savedInstanceState.putInt("pairIndex", pairIndex);
		savedInstanceState.putBoolean("defaultVibrateDef", defaultVibrateDef);
	}

	private void createAlarmAndReturn() {
		Integer alertType = ((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue() != null ? Integer.parseInt(((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue()) : null;
		String alertSound;
		if(sharedPrefs.getString(PREF_KEY_ALARM_ALERT_SOUND, "").equals(sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, ""))) {
			alertSound = null;
		} else {
			alertSound = sharedPrefs.getString(PREF_KEY_ALARM_ALERT_SOUND, "");
		}

		Boolean vibrate = defaultVibrateDef ? null : ((CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE)).isChecked();

		Log.d("alertType: " + alertType + ", alertSound: " + alertSound + ", vibrate: " + vibrate);

		AndroidNotify notify = new AndroidNotify(enclosingActivity, alertType, alertSound, vibrate);

		try {
			if(!mBound) {
				throw new IOException(enclosingActivity.getString(R.string.not_bound, "AlarmCreationFragment"));
			}
			int id = mStorageAndControlService.generateAlarmID();
			Exchange exchange = mStorageAndControlService.getExchange(((ListPreference) findPreference(PREF_KEY_EXCHANGE)).getValue());
			Pair pair = pairs.get(pairIndex);
			makeAlarm(id, exchange, pair, notify);
			Intent alarmIdIntent = new Intent();
			alarmIdIntent.putExtra("alarmID", id);
			enclosingActivity.setResult(Activity.RESULT_OK, alarmIdIntent);
			enclosingActivity.finish();
		} catch(Exception e) {
			String failedCreate = enclosingActivity.getString(R.string.failed_create_alarm);
			Log.e(failedCreate, e);
			Toast.makeText(enclosingActivity, failedCreate + " " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	protected abstract void makeAlarm(int id, Exchange exchange, Pair pair, AndroidNotify notify)
			throws UpperBoundSmallerThanLowerBoundException, IOException, InterruptedException,
			ExecutionException;
}
