package mobi.boilr.boilr.views.fragments;

import java.io.IOException;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.boilr.domain.AndroidNotifier;
import mobi.boilr.boilr.services.LocalBinder;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.TimeFrameSmallerOrEqualUpdateIntervalException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.RingtonePreference;
import android.widget.Toast;

public abstract class AlarmSettingsFragment extends AlarmPreferencesFragment {
	protected Alarm alarm;

	private class InitializePreferencesConnection implements ServiceConnection {
		private int alarmID;

		public InitializePreferencesConnection(int alarmID) {
			this.alarmID = alarmID;
		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mStorageAndControlService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;

			alarm = mStorageAndControlService.getAlarm(alarmID);

			String exchangeCode = alarm.getExchangeCode();
			String exchangeName = alarm.getExchange().getName();
			exchangeIndex = exchangeListPref.findIndexOfValue(exchangeCode);
			exchangeListPref.setSummary(exchangeName);

			if(recoverSavedInstance) {
				alarmAlertTypePref.setSummary(alarmAlertTypePref.getEntry());
				alertSoundPref.setSummary(Conversions.ringtoneUriToName(sharedPrefs.getString(PREF_KEY_ALARM_ALERT_SOUND, ""), enclosingActivity));
			} else {
				exchangeListPref.setValue(exchangeCode);

				AndroidNotifier notifier = (AndroidNotifier) alarm.getNotifier();
				Integer alertType = notifier.getAlertType();
				if(alertType == null) {
					alertType = Integer.parseInt(sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_TYPE, ""));
				}
				alarmAlertTypePref.setValue(alertType.toString());
				alarmAlertTypePref.setSummary(alarmAlertTypePref.getEntries()[alarmAlertTypePref.findIndexOfValue(alertType.toString())]);
				alertSoundPref.setRingtoneType(alertType);

				String alertSound = notifier.getAlertSound();
				if(alertSound == null) {
					alertSound = sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, "");
				}
				sharedPrefs.edit().putString(PREF_KEY_ALARM_ALERT_SOUND, alertSound).commit();
				alertSoundPref.setSummary(Conversions.ringtoneUriToName(alertSound, enclosingActivity));

				Boolean isVibrate = notifier.isVibrate();
				if(isVibrate == null) {
					isVibrate = sharedPrefs.getBoolean(SettingsFragment.PREF_KEY_VIBRATE_DEFAULT, true);
				}
				vibratePref.setChecked(isVibrate);
			}
			initializePreferences();
			updatePairsList(exchangeCode, exchangeName, alarm.getPair().toString());
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
			AndroidNotifier notifier = (AndroidNotifier) alarm.getNotifier();
			if(key.equals(PREF_KEY_EXCHANGE)) {
				ListPreference listPref = (ListPreference) preference;
				exchangeIndex = listPref.findIndexOfValue((String) newValue);
				String exchangeName = (String) listPref.getEntries()[exchangeIndex];
				listPref.setSummary(exchangeName);
				pairIndex = 0;
				updatePairsList((String) newValue, exchangeName, null);
				try {
					if(!mBound) {
						throw new IOException(enclosingActivity.getString(R.string.not_bound, "AlarmSettingsFragment"));
					}
					alarm.setExchange(mStorageAndControlService.getExchange((String) newValue));
				} catch(Exception e) {
					Log.e("Cannot change Exchange.", e);
				}
			} else if(key.equals(PREF_KEY_PAIR)) {
				pairIndex = Integer.parseInt((String) newValue);
				Pair pair = pairs.get(pairIndex);
				preference.setSummary(pair.toString());
				updateDependentOnPairAux();
				alarm.setPair(pair);
			} else if(key.equals(PREF_KEY_UPDATE_INTERVAL)) {
				try {
					alarm.setPeriod(1000 * Long.parseLong((String) newValue));
					if(mBound) {
						mStorageAndControlService.resetAlarmPeriod(alarm);
					} else {
						Log.e(enclosingActivity.getString(R.string.not_bound, "PriceHitAlarmSettingsFragment"));
					}
					preference.setSummary(enclosingActivity.getString(R.string.sec_abrv_input_as_string, newValue));
				} catch(TimeFrameSmallerOrEqualUpdateIntervalException e) {
					String msg = enclosingActivity.getString(R.string.failed_save_alarm) + " "
						+ enclosingActivity.getString(R.string.frame_must_longer_interval);
					Log.e(msg, e);
					Toast.makeText(enclosingActivity, msg, Toast.LENGTH_LONG).show();
				}
			} else if(key.equals(PREF_KEY_ALARM_ALERT_TYPE)) {
				ListPreference alertTypePref = (ListPreference) preference;
				alertTypePref.setSummary(alertTypePref.getEntries()[alertTypePref.findIndexOfValue((String) newValue)]);
				// Change selectable ringtones according to the alert type
				int ringtoneType = Integer.parseInt((String) newValue);
				alertSoundPref.setRingtoneType(ringtoneType);
				String defaultRingtone = RingtoneManager.getDefaultUri(ringtoneType).toString();
				alertSoundPref.setSummary(Conversions.ringtoneUriToName(defaultRingtone, enclosingActivity));
				notifier.setAlertType(ringtoneType);
				notifier.setAlertSound(defaultRingtone);
			} else if(key.equals(PREF_KEY_ALARM_ALERT_SOUND)) {
				RingtonePreference alertSoundPref = (RingtonePreference) preference;
				String alertSound = (String) newValue;
				alertSoundPref.setSummary(Conversions.ringtoneUriToName(alertSound, enclosingActivity));
				notifier.setAlertSound(alertSound);
			} else if(key.equals(PREF_KEY_ALARM_VIBRATE)) {
				notifier.setVibrate((Boolean) newValue);
			} else {
				Log.d("No behavior for " + key);
				return true;
			}

			if(mBound) {
				mStorageAndControlService.replaceAlarmDB(alarm);
			} else {
				Log.e(enclosingActivity.getString(R.string.not_bound, "AlarmSettingsFragment"));
			}
			return true;
		}
	}

	@Override
	protected void updateDependentOnPair() {
		alarm.setPair(pairs.get(pairIndex));
	}

	protected abstract void initializePreferences();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int alarmID = Integer.MIN_VALUE;
		if(savedInstanceState == null) {
			Bundle args = getArguments();
			if(args != null) {
				alarmID = args.getInt(AlarmSettingsActivity.alarmID);
			}
		} else {
			alarmID = savedInstanceState.getInt(AlarmSettingsActivity.alarmID);
		}
		alarmTypePref.setEnabled(false);

		mStorageAndControlServiceConnection = new InitializePreferencesConnection(alarmID);
		enclosingActivity.bindService(serviceIntent, mStorageAndControlServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt(AlarmSettingsActivity.alarmID, alarm.getId());
	}
}
