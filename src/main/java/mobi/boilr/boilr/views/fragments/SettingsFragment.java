package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.services.LocalBinder;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Exchange;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
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
	private StorageAndControlService mStorageAndControlService;
	private boolean mBound;
	private ServiceConnection mStorageAndControlServiceConnection = new ServiceConnection() {

		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mStorageAndControlService = ((LocalBinder<StorageAndControlService>) binder).getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent serviceIntent = new Intent(getActivity(), StorageAndControlService.class);
		getActivity().bindService(serviceIntent, mStorageAndControlServiceConnection, Context.BIND_AUTO_CREATE);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.app_settings);
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
		alertSoundPref.setSummary(Conversions.ringtoneUriToName(sharedPreferences.getString(PREF_KEY_DEFAULT_ALERT_SOUND, ""), getActivity()));

		Preference pref;
		pref = findPreference(PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT);
		pref.setSummary(sharedPreferences.getString(PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, "") + " s");
		pref = findPreference(PREF_KEY_DEFAULT_UPDATE_INTERVAL_CHANGE);
		pref.setSummary(Conversions.buildMinToDaysSummary(sharedPreferences.getString(PREF_KEY_DEFAULT_UPDATE_INTERVAL_CHANGE, "")));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unbindService(mStorageAndControlServiceConnection);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
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
			sharedPrefs.edit().putString(PREF_KEY_DEFAULT_ALERT_SOUND, defaultRingtone).apply();
			alertSoundPref.setSummary(Conversions.ringtoneUriToName(defaultRingtone, getActivity()));
		} else if(key.equals(PREF_KEY_THEME)) {
			ListPreference listPref = (ListPreference) pref;
			listPref.setSummary(listPref.getEntry());
		} else if(key.equals(PREF_KEY_CHECK_PAIRS_INTERVAL)) {
			ListPreference listPref = (ListPreference) pref;
			listPref.setSummary(listPref.getEntry());
			if(mBound) {
				long pairInterval = Long.parseLong(sharedPrefs.getString(PREF_KEY_CHECK_PAIRS_INTERVAL, ""));
				for (Exchange e : mStorageAndControlService.getLoadedExchanges()) {
					e.setExperiedPeriod(pairInterval);
				}
			} else {
				Log.d(getActivity().getString(R.string.not_bound, "PreferenceFragment"));
			}
		} else if(key.equals(PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT)) {
			pref.setSummary(sharedPrefs.getString(key, "") + " s");
		} else if(key.equals(PREF_KEY_DEFAULT_UPDATE_INTERVAL_CHANGE)) {
			pref.setSummary(Conversions.buildMinToDaysSummary(sharedPrefs.getString(key, "")));
		} else if(key.equals(PREF_KEY_MOBILE_DATA)) {
			StorageAndControlService.allowMobileData = sharedPrefs.getBoolean(key, false);
		}
	}

	/*
	 * Patch to overcome onSharedPreferenceChange not being called by RingtonePreference.
	 * By Arad on Stack Overflow http://stackoverflow.com/a/8105349
	 */
	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		pref.setSummary(Conversions.ringtoneUriToName((String) newValue, getActivity()));
		return true;
	}
}
