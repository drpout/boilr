package mobi.boilr.boilr.views.fragments;

import java.util.ArrayList;
import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.Alarm;
import android.app.Activity;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.widget.Toast;

public abstract class AlarmSettingsFragment extends PreferenceFragment {

	protected List<Pair> pairs = new ArrayList<Pair>();
	protected Alarm alarm;
	protected Activity enclosingActivity;

	protected OnAlarmSettingsPreferenceChangeListener listener = new OnAlarmSettingsPreferenceChangeListener();

	protected class OnAlarmSettingsPreferenceChangeListener implements OnPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Log.d(preference.getKey() + " " + newValue);
			if(preference.getKey().equals(AlarmCreationFragment.PREF_KEY_EXCHANGE)) {
				ListPreference listPreference = (ListPreference) preference;
				listPreference.setSummary(listPreference.getEntries()[listPreference.findIndexOfValue((String) newValue)]);
				if(((AlarmSettingsActivity) enclosingActivity).isBound()) {
					try {
						pairs = ((AlarmSettingsActivity) enclosingActivity).getStorageAndControlService().getPairs((String) newValue);
						if(pairs == null)
							throw new Exception("Pairs is null.");
						CharSequence[] sequence = new CharSequence[pairs.size()];
						CharSequence[] ids = new CharSequence[pairs.size()];

						for(int i = 0; i < pairs.size(); i++) {
							sequence[i] = pairs.get(i).getCoin() + "/" + pairs.get(i).getExchange();
							ids[i] = String.valueOf(i);
						}
						ListPreference pairListPreference = (ListPreference) findPreference(AlarmCreationFragment.PREF_KEY_PAIR);
						pairListPreference.setEntries(sequence);
						pairListPreference.setEntryValues(ids);
						pairListPreference.setDefaultValue(ids[0]);
						pairListPreference.setSummary(sequence[0]);
						pairListPreference.setValueIndex(0);
						alarm.setPair(pairs.get(0));
					} catch(Exception e) {
						String message = "Could not retrieve pairs for " + listPreference.getEntries()[listPreference.findIndexOfValue((String) newValue)] + ".";
						Toast.makeText(enclosingActivity, message, Toast.LENGTH_LONG).show();
						Log.e(message, e);
					}
				} else {
					Log.d("Not Bound");
				}
				try {
					alarm.setExchange(((AlarmSettingsActivity) enclosingActivity)
							.getStorageAndControlService().getExchange((String) newValue));
				} catch(Exception e) {
					Log.e("Cannot change Exchange", e);
				}
			} else if(preference.getKey().equals(AlarmCreationFragment.PREF_KEY_PAIR)) {
				Pair pair = pairs.get(Integer.parseInt((String) newValue));
				preference.setSummary(pair.getCoin() + "/" + pair.getExchange());
				alarm.setPair(pair);
			} else if(preference.getKey().equals(AlarmCreationFragment.PREF_KEY_ALARM_ALERT_TYPE)) {
				ListPreference alertTypePref = (ListPreference) preference;
				alertTypePref.setSummary(alertTypePref.getEntries()[alertTypePref.findIndexOfValue((String) newValue)]);

				// Change selectable ringtones according to the alert type
				RingtonePreference alertSoundPref = (RingtonePreference) findPreference(AlarmCreationFragment.PREF_KEY_ALARM_ALERT_SOUND);
				int ringtoneType = Integer.parseInt((String) newValue);
				alertSoundPref.setRingtoneType(ringtoneType);
				String defaultRingtone = RingtoneManager.getDefaultUri(ringtoneType).toString();
				alertSoundPref.setSummary(Conversions
						.ringtoneUriToName(defaultRingtone, enclosingActivity));
				((AndroidNotify) alarm.getNotify()).setAlertType((Integer.parseInt((String) newValue)));
				((AndroidNotify) alarm.getNotify()).setAlertSound(defaultRingtone);
			} else if(preference.getKey().equals(AlarmCreationFragment.PREF_KEY_ALARM_ALERT_SOUND)) {
				RingtonePreference alertSoundPref = (RingtonePreference) preference;
				alertSoundPref.setSummary(Conversions
						.ringtoneUriToName((String) newValue, enclosingActivity));
				((AndroidNotify) alarm.getNotify()).setAlertSound((String) newValue);
			} else if(preference.getKey().equals(AlarmCreationFragment.PREF_KEY_ALARM_VIBRATE)) {
				((AndroidNotify) alarm.getNotify()).setVibrate((Boolean) newValue);
			} else {
				Log.d("No behavior for " + preference.getKey());
			}

			((AlarmSettingsActivity) enclosingActivity).getStorageAndControlService()
			.replaceAlarm(alarm);

			return true;
		}
	}

	public AlarmSettingsFragment(Alarm alarm) {
		this.alarm = alarm;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		enclosingActivity = getActivity();

		addPreferencesFromResource(R.xml.alarm_settings);

		// First Entry as default
		ListPreference listPref = (ListPreference) findPreference(AlarmCreationFragment.PREF_KEY_EXCHANGE);
		listPref.setSummary(alarm.getExchange().getName());
		listPref.setValue(alarm.getExchangeCode());
		listPref.setOnPreferenceChangeListener(listener);

		ListPreference pairListPreference = (ListPreference) findPreference(AlarmCreationFragment.PREF_KEY_PAIR);
		try {
			pairs = ((AlarmSettingsActivity) enclosingActivity).getStorageAndControlService()
					.getPairs(alarm.getExchangeCode());
			if(pairs == null)
				throw new Exception("Pairs is null.");
			CharSequence[] sequence = new CharSequence[pairs.size()];
			CharSequence[] ids = new CharSequence[pairs.size()];
			for(int i = 0; i < pairs.size(); i++) {
				sequence[i] = pairs.get(i).getCoin() + "/" + pairs.get(i).getExchange();
				ids[i] = String.valueOf(i);
			}
			pairListPreference.setEntries(sequence);
			pairListPreference.setEntryValues(ids);
			pairListPreference.setSummary(alarm.getPair().getCoin() + "/" + alarm.getPair().getExchange());
		} catch(Exception e) {
			String message = "Could not retrieve pairs for " + alarm.getExchange().getName() + ".";
			Toast.makeText(enclosingActivity, message, Toast.LENGTH_LONG).show();
			Log.e(message, e);
		}
		pairListPreference.setOnPreferenceChangeListener(listener);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);
		listPref = (ListPreference) findPreference(AlarmCreationFragment.PREF_KEY_ALARM_ALERT_TYPE);
		Integer alertType = ((AndroidNotify) alarm.getNotify()).getAlertType();
		Log.d("alertType " + alertType);
		if(alertType == null) {
			listPref.setSummary(listPref.getEntries()[listPref.findIndexOfValue(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_TYPE, ""))]);
			listPref.setValue(null);
		} else {
			listPref.setSummary(listPref.getEntries()[listPref.findIndexOfValue(String.valueOf(alertType))]);
			listPref.setValue(String.valueOf(alertType));
		}
		listPref.setOnPreferenceChangeListener(listener);

		String alertSound = ((AndroidNotify) alarm.getNotify()).getAlertSound();
		RingtonePreference alertSoundPref = (RingtonePreference) findPreference(AlarmCreationFragment.PREF_KEY_ALARM_ALERT_SOUND);
		if(alertSound == null) {
			alertSoundPref
			.setSummary(Conversions.ringtoneUriToName(sharedPreferences
					.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, ""), enclosingActivity));
		} else {
			alertSoundPref.setSummary(Conversions
					.ringtoneUriToName(alertSound, enclosingActivity));
		}
		alertSoundPref.setOnPreferenceChangeListener(listener);

		CheckBoxPreference vibratePreference = (CheckBoxPreference) findPreference(AlarmCreationFragment.PREF_KEY_ALARM_VIBRATE);
		Boolean isVibrate = ((AndroidNotify) alarm.getNotify()).isVibrate();
		if(isVibrate == null) {
			vibratePreference.setChecked(sharedPreferences.getBoolean(SettingsFragment.PREF_KEY_VIBRATE_DEFAULT, false));
		} else {
			vibratePreference.setChecked(isVibrate);
		}
		vibratePreference.setOnPreferenceChangeListener(listener);
	}
}
