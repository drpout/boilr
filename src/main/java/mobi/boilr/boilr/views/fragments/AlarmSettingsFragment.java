package mobi.boilr.boilr.views.fragments;

import java.io.IOException;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.boilr.domain.AndroidNotifier;
import mobi.boilr.boilr.services.LocalBinder;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.utils.IconToast;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.TimeFrameSmallerOrEqualUpdateIntervalException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

public abstract class AlarmSettingsFragment extends AlarmPreferencesFragment {
	protected Alarm alarm;

	private class InitializePreferencesConnection implements ServiceConnection {
		private int alarmID;

		public InitializePreferencesConnection(int alarmID) {
			this.alarmID = alarmID;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mStorageAndControlService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;

			alarm = mStorageAndControlService.getAlarm(alarmID);

			String exchangeCode = alarm.getExchangeCode();
			String exchangeName = alarm.getExchange().getName();
			mExchangeIndex = mExchangeListPref.findIndexOfValue(exchangeCode);
			mExchangeListPref.setSummary(exchangeName);

			if(mRecoverSavedInstance) {
				mAlarmAlertTypePref.setSummary(mAlarmAlertTypePref.getEntry());
				mAlertSoundPref.setRingtoneType(mAlarmAlertTypePref.getValue());
				mAlertSoundPref.setSummary(mAlertSoundPref.getEntry());
				mVibratePref.setSummary(mVibratePref.getEntry());
			} else {
				mExchangeListPref.setValue(exchangeCode);

				AndroidNotifier notifier = (AndroidNotifier) alarm.getNotifier();
				Integer alertType = notifier.getAlertType();
				mAlarmAlertTypePref.setValue(alertType == null ? DEFAULT : alertType.toString());
				mAlarmAlertTypePref.setSummary(mAlarmAlertTypePref.getEntry());

				mAlertSoundPref.setRingtoneType(mAlarmAlertTypePref.getValue());
				String alertSound = notifier.getAlertSound();
				if(alertSound != null) {
					mAlertSoundPref.setValue(alertSound);
				} else {
					mAlertSoundPref.setDefaultValue();
				}

				Boolean vibrate = notifier.isVibrate();
				mVibratePref.setValue(vibrate == null ? DEFAULT : vibrate.toString());
				mVibratePref.setSummary(mVibratePref.getEntry());
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
				mExchangeIndex = listPref.findIndexOfValue((String) newValue);
				String exchangeName = (String) listPref.getEntries()[mExchangeIndex];
				listPref.setSummary(exchangeName);
				mPairIndex = 0;
				updatePairsList((String) newValue, exchangeName, null);
				try {
					if(!mBound) {
						throw new IOException(mEnclosingActivity.getString(R.string.not_bound, "AlarmSettingsFragment"));
					}
					alarm.setExchange(mStorageAndControlService.getExchange((String) newValue));
				} catch(Exception e) {
					Log.e("Cannot change Exchange.", e);
				}
			} else if(key.equals(PREF_KEY_PAIR)) {
				mPairIndex = Integer.parseInt((String) newValue);
				Pair pair = mPairs.get(mPairIndex);
				preference.setSummary(pair.toString());
				updateDependentOnPairAux();
				alarm.setPair(pair);
			} else if(key.equals(PREF_KEY_UPDATE_INTERVAL)) {
				try {
					alarm.setPeriod(1000 * Long.parseLong((String) newValue));
					if(mBound) {
						mStorageAndControlService.resetAlarmPeriod(alarm);
					} else {
						Log.e(mEnclosingActivity.getString(R.string.not_bound, "PriceHitAlarmSettingsFragment"));
					}
					preference.setSummary(mEnclosingActivity.getString(R.string.seconds_abbreviation, newValue));
				} catch(TimeFrameSmallerOrEqualUpdateIntervalException e) {
					String msg = mEnclosingActivity.getString(R.string.failed_save_alarm) + " "
						+ mEnclosingActivity.getString(R.string.frame_must_longer_interval);
					Log.e(msg, e);
					IconToast.warning(mEnclosingActivity, msg);
				}
			} else if(key.equals(PREF_KEY_ALARM_ALERT_TYPE)) {
				ListPreference alertTypePref = (ListPreference) preference;
				String alertType = (String) newValue;
				alertTypePref.setSummary(alertTypePref.getEntries()[alertTypePref.findIndexOfValue(alertType)]);
				// Change selectable ringtones according to the alert type
				mAlertSoundPref.setRingtoneType(alertType);
				mAlertSoundPref.setDefaultValue();
				if(alertType.equals(DEFAULT))
					notifier.setAlertType(null);
				else
					notifier.setAlertType(Integer.parseInt(alertType));
				notifier.setAlertSound(null);
			} else if(key.equals(PREF_KEY_ALARM_ALERT_SOUND)) {
				String alertSound = (String) newValue;
				if(alertSound.equals(DEFAULT))
					alertSound = null;
				notifier.setAlertSound(alertSound);
			} else if(key.equals(PREF_KEY_ALARM_VIBRATE)) {
				ListPreference vibratePref = (ListPreference) preference;
				String vibrate = (String) newValue;
				vibratePref.setSummary(vibratePref.getEntries()[vibratePref.findIndexOfValue(vibrate)]);
				if(vibrate.equals(DEFAULT))
					notifier.setVibrate(null);
				else
					notifier.setVibrate(Boolean.parseBoolean(vibrate));
			} else {
				Log.d("No behavior for " + key);
				return true;
			}

			if(mBound) {
				mStorageAndControlService.replaceAlarmDB(alarm);
			} else {
				Log.e(mEnclosingActivity.getString(R.string.not_bound, "AlarmSettingsFragment"));
			}
			return true;
		}
	}

	@Override
	protected void updateDependentOnPair() {
		alarm.setPair(mPairs.get(mPairIndex));
	}

	protected abstract void initializePreferences();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int alarmID = Integer.MIN_VALUE;
		if(savedInstanceState == null) {
			Bundle args = getArguments();
			if(args != null) {
				alarmID = args.getInt(AlarmSettingsActivity.ALARM_ID);
			}
		} else {
			alarmID = savedInstanceState.getInt(AlarmSettingsActivity.ALARM_ID);
		}
		mAlarmTypePref.setEnabled(false);

		mStorageAndControlServiceConnection = new InitializePreferencesConnection(alarmID);
		mEnclosingActivity.bindService(mServiceIntent, mStorageAndControlServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt(AlarmSettingsActivity.ALARM_ID, alarm.getId());
	}
}
