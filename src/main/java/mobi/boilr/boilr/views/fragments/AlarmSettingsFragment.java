package mobi.boilr.boilr.views.fragments;

import java.util.ArrayList;
import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.Alarm;
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

public abstract class AlarmSettingsFragment extends PreferenceFragment {

	public static final String PREF_VALUE_PRICE_HIT = "price_hit";
	public static final String PREF_VALUE_PRICE_VAR = "price_var";
	public static final String PREF_KEY_EXCHANGE = "exchange";
	public static final String PREF_KEY_TYPE = "type";
	public static final String PREF_KEY_PAIR = "pair";
	public static final String PREF_TYPE_DEFAULT_VALUE = "Price Hit";
	protected static final String PREF_KEY_ALARM_ALERT_SOUND = "pref_key_alarm_alert_sound";
	protected static final String PREF_KEY_ALARM_ALERT_TYPE = "pref_key_alarm_alert_type";
	public static final String PREF_KEY_ALARM_VIBRATE = "pref_key_alarm_vibrate";

	protected List<Pair> pairs = new ArrayList<Pair>();
	protected Alarm alarm;

	protected OnAlarmSettingsPreferenceChangeListener listener = new OnAlarmSettingsPreferenceChangeListener();

	protected class OnAlarmSettingsPreferenceChangeListener implements OnPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Log.d(preference.getKey() + " " + newValue);
			if(preference.getKey().equals(PREF_KEY_EXCHANGE)) {
				ListPreference listPreference = (ListPreference) preference;
				listPreference.setSummary((listPreference).getEntries()[(listPreference).findIndexOfValue((String) newValue)]);
				if(((AlarmSettingsActivity) getActivity()).isBound()) {
					pairs = ((AlarmSettingsActivity) getActivity()).getStorageAndControlService().getPairs((String) newValue);
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
					alarm.setPair(pairs.get(0));
				} else {
					Log.d("Not Bound");
				}
				try {
					alarm.setExchange(((AlarmSettingsActivity) getActivity()).getStorageAndControlService().getExchange((String) newValue));
				} catch (Exception e) {
					Log.e("Cannot change Exchange", e);
				}
			} else if(preference.getKey().equals(PREF_KEY_PAIR)) {
				Pair pair = pairs.get(Integer.parseInt((String) newValue));
				preference.setSummary(pair.getCoin() + "/" + pair.getExchange());
				alarm.setPair(pair);
			} else if(preference.getKey().equals(PREF_KEY_ALARM_ALERT_TYPE)) {
				ListPreference alertTypePref = (ListPreference) preference;
				alertTypePref.setSummary(alertTypePref.getEntries()[alertTypePref.findIndexOfValue((String) newValue)]);

				// Change selectable ringtones according to the alert type
				RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_ALARM_ALERT_SOUND);
				int ringtoneType = Integer.parseInt((String) newValue);
				alertSoundPref.setRingtoneType(ringtoneType);
				String defaultRingtone = RingtoneManager.getDefaultUri(ringtoneType).toString();
				alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName(defaultRingtone, getActivity()));
				((AndroidNotify) alarm.getNotify()).setAlertType((Integer.parseInt((String) newValue)));
				((AndroidNotify) alarm.getNotify()).setAlertSound(defaultRingtone);
			} else if(preference.getKey().equals(PREF_KEY_ALARM_ALERT_SOUND)) {
				RingtonePreference alertSoundPref = (RingtonePreference) preference;
				alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName((String) newValue, getActivity()));
				((AndroidNotify) alarm.getNotify()).setAlertSound((String) newValue);
			} else if(preference.getKey().equals(PREF_KEY_ALARM_VIBRATE)) {
				((AndroidNotify) alarm.getNotify()).setVibrate((Boolean) newValue);
			} else {
				Log.d("No behavior for " + preference.getKey());
			}

			((AlarmSettingsActivity) getActivity()).getStorageAndControlService().replaceAlarm(alarm);

			return true;
		}
	}

	public AlarmSettingsFragment(Alarm alarm) {
		this.alarm = alarm;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		addPreferencesFromResource(R.xml.alarm_settings);

		// First Entry as default
		ListPreference listPref = (ListPreference) findPreference(PREF_KEY_EXCHANGE);
		listPref.setSummary(alarm.getExchange().getName());
		listPref.setValue(alarm.getExchangeCode());
		listPref.setOnPreferenceChangeListener(listener);

		ListPreference pairListPreference = (ListPreference) findPreference(PREF_KEY_PAIR);
		pairs = ((AlarmSettingsActivity) getActivity()).getStorageAndControlService().getPairs(alarm.getExchangeCode());
		CharSequence[] sequence = new CharSequence[pairs.size()];
		CharSequence[] ids = new CharSequence[pairs.size()];

		for (int i = 0; i < pairs.size(); i++) {
			sequence[i] = pairs.get(i).getCoin() + "/" + pairs.get(i).getExchange();
			ids[i] = String.valueOf(i);
		}

		pairListPreference.setEntries(sequence);
		pairListPreference.setEntryValues(ids);
		pairListPreference.setSummary(alarm.getPair().getCoin() + "/" + alarm.getPair().getExchange());
		pairListPreference.setOnPreferenceChangeListener(listener);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		listPref = (ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE);
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
		RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_ALARM_ALERT_SOUND);
		if(alertSound == null) {
			alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, ""), getActivity()));
		} else {
			alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName(alertSound, getActivity()));
		}
		alertSoundPref.setOnPreferenceChangeListener(listener);

		CheckBoxPreference vibratePreference = (CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE);
		Boolean isVibrate = ((AndroidNotify) alarm.getNotify()).isVibrate();
		if(isVibrate == null) {
			vibratePreference.setChecked(sharedPreferences.getBoolean(SettingsFragment.PREF_KEY_VIBRATE_DEFAULT, false));
		} else {
			vibratePreference.setChecked(isVibrate);
		}
		vibratePreference.setOnPreferenceChangeListener(listener);
	}
}
