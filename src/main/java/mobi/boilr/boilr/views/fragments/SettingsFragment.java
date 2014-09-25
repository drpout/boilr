package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.services.StorageAndControlService;
import android.app.Activity;
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

public class SettingsFragment extends PreferenceFragment implements
OnSharedPreferenceChangeListener, OnPreferenceChangeListener {
	public static final String PREF_KEY_DEFAULT_ALERT_TYPE = "pref_key_default_alert_type";
	public static final String PREF_KEY_DEFAULT_ALERT_SOUND = "pref_key_default_alert_sound";
	public static final String PREF_KEY_THEME = "pref_key_theme";
	static final String PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT = "pref_key_default_update_interval_hit";
	static final String PREF_KEY_DEFAULT_UPDATE_INTERVAL_CHANGE = "pref_key_default_update_interval_change";
	public static final String PREF_KEY_CHECK_PAIRS_INTERVAL = "pref_key_check_pairs_interval";
	public static final String PREF_KEY_VIBRATE_DEFAULT = "pref_key_vibrate_default";
	public static final String PREF_KEY_MOBILE_DATA = "pref_key_mobile_data";
	private static final String[] listPrefs = { PREF_KEY_DEFAULT_ALERT_TYPE, PREF_KEY_THEME,
		PREF_KEY_CHECK_PAIRS_INTERVAL };
	public static final double MINUTES_IN_DAY = 1440; // 60*24

	/**
	 * Converts a double to a String removing places after the decimal point
	 * when their value is zero or rounding them to 2 places when there is a value.
	 * Based on Stack Overflow answer by JasonD at http://stackoverflow.com/a/14126736
	 *
	 * @param d double to be converted
	 * @return String with nicely formatted double
	 */
	public static String cleanDoubleToString(double d) {
		if(d == (int) d) {
			return String.format("%d", (int) d);
		} else {
			return String.format("%.2f", d);
		}
	}

	public static String buildMinToDaysSummary(String minutesString) {
		int min = Integer.parseInt(minutesString);
		double days = min / MINUTES_IN_DAY;
		String result = minutesString + " min (" + cleanDoubleToString(days);
		if(days == 1.0) {
			result += " day)";
		} else {
			result += " days)";
		}
		return result;
	}

	public static String ringtoneUriToName(String stringUri, Activity activity) {
		Uri uri = Uri.parse(stringUri);
		Context context = activity.getApplicationContext();
		Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
		return ringtone.getTitle(context);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.app_settings);
		// Set summaries to be the current value for the selected preference
		ListPreference listPref;
		for(String key : listPrefs) {
			listPref = (ListPreference) findPreference(key);
			listPref.setSummary(listPref.getEntry());
		}
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

		RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_DEFAULT_ALERT_SOUND);
		ListPreference alertTypePref = (ListPreference) findPreference(PREF_KEY_DEFAULT_ALERT_TYPE);
		alertSoundPref.setRingtoneType(Integer.parseInt(alertTypePref.getValue()));
		alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName(sharedPreferences.getString(PREF_KEY_DEFAULT_ALERT_SOUND, ""), getActivity()));

		Preference pref;
		pref = findPreference(PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT);
		pref.setSummary(sharedPreferences.getString(PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, "") + " s");
		pref = findPreference(PREF_KEY_DEFAULT_UPDATE_INTERVAL_CHANGE);
		pref.setSummary(buildMinToDaysSummary(sharedPreferences.getString(PREF_KEY_DEFAULT_UPDATE_INTERVAL_CHANGE, "")));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Set summaries to be the current value for the selected preference
		Preference pref = findPreference(key);
		if(key.equals(PREF_KEY_DEFAULT_ALERT_TYPE)) {
			ListPreference alertTypePref = (ListPreference) pref;
			alertTypePref.setSummary(alertTypePref.getEntry());
			// Change selectable ringtones according to the alert type
			RingtonePreference alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_DEFAULT_ALERT_SOUND);
			int ringtoneType = Integer.parseInt(alertTypePref.getValue());
			alertSoundPref.setRingtoneType(ringtoneType);
			String defaultRingtone = RingtoneManager.getDefaultUri(ringtoneType).toString();
			sharedPreferences.edit().putString(PREF_KEY_DEFAULT_ALERT_SOUND, defaultRingtone).apply();
			alertSoundPref.setSummary(SettingsFragment.ringtoneUriToName(defaultRingtone,getActivity()));
		} else if(key.equals(PREF_KEY_THEME) || key.equals(PREF_KEY_CHECK_PAIRS_INTERVAL)) {
			ListPreference listPref = (ListPreference) pref;
			listPref.setSummary(listPref.getEntry());
		} else if(key.equals(PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT)) {
			pref.setSummary(sharedPreferences.getString(key, "") + " s");
		} else if(key.equals(PREF_KEY_DEFAULT_UPDATE_INTERVAL_CHANGE)) {
			pref.setSummary(buildMinToDaysSummary(sharedPreferences.getString(key, "")));
		} else if(key.equals(PREF_KEY_MOBILE_DATA)) {
			StorageAndControlService.allowMobileData = sharedPreferences.getBoolean(key, false);
		}
	}

	/*
	 * Patch to overcome onSharedPreferenceChange not being called by RingtonePreference.
	 * By Arad on Stack Overflow http://stackoverflow.com/a/8105349
	 */
	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		pref.setSummary(SettingsFragment.ringtoneUriToName((String) newValue, getActivity()));
		return true;
	}
}
