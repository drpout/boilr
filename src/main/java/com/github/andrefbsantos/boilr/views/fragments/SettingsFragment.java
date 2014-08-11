package com.github.andrefbsantos.boilr.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;

import com.github.andrefbsantos.boilr.R;

public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener, OnPreferenceChangeListener {
	private static final String PREF_KEY_DEFAULT_ALERT_TYPE = "pref_key_default_alert_type";
	public static final String PREF_KEY_DEFAULT_ALERT_SOUND = "pref_key_default_alert_sound";
	private static final String PREF_KEY_THEME = "pref_key_theme";
	private static final String PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT = "pref_key_default_update_interval_hit";
	private static final String PREF_KEY_DEFAULT_UPDATE_INTERVAL_VAR = "pref_key_default_update_interval_var";
	private static final String PREF_KEY_CHECK_PAIRS_INTERVAL = "pref_key_check_pairs_interval";
	private static final String[] listPrefs = { PREF_KEY_DEFAULT_ALERT_TYPE, PREF_KEY_THEME,
			PREF_KEY_CHECK_PAIRS_INTERVAL };
	private static final double MINUTES_IN_DAY = 1440; // 60*24

	/**
	 * Converts a double to a String removing places after the decimal point
	 * when their value is zero or rounding them to 2 places when there is a value.
	 * Based on Stack Overflow answer by JasonD at http://stackoverflow.com/a/14126736
	 *
	 * @param d double to be converted
	 * @return String with nicely formatted double
	 */
	public static String cleanDoubleToString(double d) {
		if (d == (int) d) {
			return String.format("%d", (int) d);
		} else {
			return String.format("%.2f", d);
		}
	}

	private static String buildMinToDaysSummary(String minutesString) {
		int min = Integer.parseInt(minutesString);
		double days = min / MINUTES_IN_DAY;
		String result = minutesString + " min (" + cleanDoubleToString(days);
		if (days == 1.0) {
			result += " day)";
		} else {
			result += " days)";
		}
		return result;
	}

	private String ringtoneUriToName(String stringUri) {
		Uri uri = Uri.parse(stringUri);
		Context context = getActivity().getApplicationContext();
		Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
		return ringtone.getTitle(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		// Set summaries to be the current value for the selected preference
		ListPreference listPref;
		for (String key : listPrefs) {
			listPref = (ListPreference) findPreference(key);
			listPref.setSummary(listPref.getEntry());
		}
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

		RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_DEFAULT_ALERT_SOUND);
		ListPreference alertTypePref = (ListPreference) findPreference(PREF_KEY_DEFAULT_ALERT_TYPE);
		alertSoundPref.setRingtoneType(Integer.parseInt(alertTypePref.getValue()));
		alertSoundPref.setSummary(ringtoneUriToName(sharedPreferences.getString(PREF_KEY_DEFAULT_ALERT_SOUND, "")));

		Preference pref;
		pref = findPreference(PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT);
		pref.setSummary(sharedPreferences.getString(PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, "") + " s");
		pref = findPreference(PREF_KEY_DEFAULT_UPDATE_INTERVAL_VAR);
		pref.setSummary(buildMinToDaysSummary(sharedPreferences.getString(PREF_KEY_DEFAULT_UPDATE_INTERVAL_VAR, "")));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Set summaries to be the current value for the selected preference
		Preference pref = findPreference(key);
		if (key.equals(PREF_KEY_DEFAULT_ALERT_TYPE)) {
			ListPreference alertTypePref = (ListPreference) pref;
			alertTypePref.setSummary(alertTypePref.getEntry());
			// Change selectable ringtones according to the alert type
			RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_DEFAULT_ALERT_SOUND);
			int ringtoneType = Integer.parseInt(alertTypePref.getValue());
			alertSoundPref.setRingtoneType(ringtoneType);
			String defaultRingtone = RingtoneManager.getDefaultUri(ringtoneType).toString();
			sharedPreferences.edit().putString(PREF_KEY_DEFAULT_ALERT_SOUND, defaultRingtone).apply();
			alertSoundPref.setSummary(ringtoneUriToName(defaultRingtone));
		} else if (key.equals(PREF_KEY_THEME) || key.equals(PREF_KEY_CHECK_PAIRS_INTERVAL)) {
			ListPreference listPref = (ListPreference) pref;
			listPref.setSummary(listPref.getEntry());
		} else if (key.equals(PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT)) {
			pref.setSummary(sharedPreferences.getString(key, "") + " s");
		} else if (key.equals(PREF_KEY_DEFAULT_UPDATE_INTERVAL_VAR)) {
			pref.setSummary(buildMinToDaysSummary(sharedPreferences.getString(key, "")));
		}
	}

	/*
	 * Patch to overcome onSharedPreferenceChange not being called by RingtonePreference.
	 * By Arad on Stack Overflow http://stackoverflow.com/a/8105349
	 */
	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		pref.setSummary(ringtoneUriToName((String) newValue));
		return true;
	}
}
