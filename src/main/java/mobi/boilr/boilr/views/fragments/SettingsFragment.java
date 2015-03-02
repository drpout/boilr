package mobi.boilr.boilr.views.fragments;

import java.util.Locale;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.SettingsActivity;
import mobi.boilr.boilr.preference.ThemableRingtonePreference;
import mobi.boilr.boilr.services.LocalBinder;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Languager;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.utils.Notifications;
import mobi.boilr.boilr.utils.Themer;
import mobi.boilr.libdynticker.core.Exchange;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener, OnPreferenceChangeListener {
	public static final String PREF_KEY_DEFAULT_ALERT_TYPE = "pref_key_default_alert_type";
	public static final String PREF_KEY_DEFAULT_ALERT_SOUND = "pref_key_default_alert_sound";
	public static final String PREF_KEY_THEME = "pref_key_theme";
	public static final String PREF_KEY_LANGUAGE = "pref_key_language";
	public static final String PREF_KEY_DEFAULT_UPDATE_INTERVAL = "pref_key_default_update_interval";
	public static final String PREF_KEY_DEFAULT_TIME_FRAME = "pref_key_default_time_frame";
	public static final String PREF_KEY_CHECK_PAIRS_INTERVAL = "pref_key_check_pairs_interval";
	private static final String PREF_KEY_SHOW_INTERNET_WARNING = "pref_key_show_internet_warning";
	public static final String PREF_KEY_VIBRATE_DEFAULT = "pref_key_vibrate_default";
	public static final String PREF_KEY_MOBILE_DATA = "pref_key_mobile_data";
	private static final String[] listPrefs = { PREF_KEY_DEFAULT_ALERT_TYPE, PREF_KEY_THEME,
			PREF_KEY_CHECK_PAIRS_INTERVAL, PREF_KEY_LANGUAGE };
	private Activity enclosingActivity;
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
		enclosingActivity = getActivity();
		Intent serviceIntent = new Intent(enclosingActivity, StorageAndControlService.class);
		enclosingActivity.bindService(serviceIntent, mStorageAndControlServiceConnection, Context.BIND_AUTO_CREATE);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.app_settings);
		// Set summaries to be the current value for the selected preference
		ListPreference listPref;
		for (String key : listPrefs) {
			listPref = (ListPreference) findPreference(key);
			listPref.setSummary(listPref.getEntry());
		}
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

		ThemableRingtonePreference alertSoundPref = (ThemableRingtonePreference) findPreference(PREF_KEY_DEFAULT_ALERT_SOUND);
		ListPreference alertTypePref = (ListPreference) findPreference(PREF_KEY_DEFAULT_ALERT_TYPE);
		alertSoundPref.setRingtoneType(Integer.parseInt(alertTypePref.getValue()));
		if(alertSoundPref.getValue() == null) {
			alertSoundPref.setDefaultValue();
		} else {
			alertSoundPref.setSummary(alertSoundPref.getEntry());
		}

		Preference pref;
		pref = findPreference(PREF_KEY_DEFAULT_UPDATE_INTERVAL);
		pref.setSummary(enclosingActivity.getString(R.string.seconds_abbreviation,
				sharedPreferences.getString(PREF_KEY_DEFAULT_UPDATE_INTERVAL, "")));
		pref = findPreference(PREF_KEY_DEFAULT_TIME_FRAME);
		pref.setSummary(Conversions.buildMinToHoursSummary(
				sharedPreferences.getString(PREF_KEY_DEFAULT_TIME_FRAME, ""), enclosingActivity));

		String language = sharedPreferences.getString(SettingsFragment.PREF_KEY_LANGUAGE, "");

		listPref = (ListPreference) findPreference(PREF_KEY_LANGUAGE);
		int index = listPref.findIndexOfValue(language);
		if(index >= 0) {
			listPref.setSummary(listPref.getEntries()[index]);
		} else {
			// Get SO language
			language = Locale.getDefault().getLanguage();
			index = listPref.findIndexOfValue(language);
			if(index >= 0) {
				listPref.setSummary(listPref.getEntries()[index]);
			} else {
				listPref.setSummary(enclosingActivity.getString(R.string.pref_default_language));
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		enclosingActivity.unbindService(mStorageAndControlServiceConnection);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		// Set summaries to be the current value for the selected preference
		Preference pref = findPreference(key);
		if(key.equals(PREF_KEY_DEFAULT_ALERT_TYPE)) {
			ListPreference alertTypePref = (ListPreference) pref;
			alertTypePref.setSummary(alertTypePref.getEntry());
			// Change selectable ringtones according to the alert type
			ThemableRingtonePreference alertSoundPref = (ThemableRingtonePreference) findPreference(PREF_KEY_DEFAULT_ALERT_SOUND);
			int ringtoneType = Integer.parseInt(alertTypePref.getValue());
			alertSoundPref.setRingtoneType(ringtoneType);
		} else if(key.equals(PREF_KEY_THEME)) {
			ListPreference listPref = (ListPreference) pref;
			listPref.setSummary(listPref.getEntry());
			Themer.changeTheme(listPref.getValue());
			restartActivity();
		} else if(key.equals(PREF_KEY_LANGUAGE)) {
			ListPreference listPref = (ListPreference) pref;
			listPref.setSummary(listPref.getEntry());
			Languager.setLanguage(enclosingActivity.getBaseContext());
			restartActivity();
			Notifications.rebuildNoInternetNotification();
		} else if(key.equals(PREF_KEY_CHECK_PAIRS_INTERVAL)) {
			ListPreference listPref = (ListPreference) pref;
			listPref.setSummary(listPref.getEntry());
			if(mBound) {
				long pairInterval = Long.parseLong(sharedPrefs.getString(PREF_KEY_CHECK_PAIRS_INTERVAL, ""));
				for (Exchange e : mStorageAndControlService.getLoadedExchanges()) {
					e.setExpiredPeriod(pairInterval);
				}
			} else {
				Log.e(enclosingActivity.getString(R.string.not_bound, "PreferenceFragment"));
			}
		} else if(key.equals(PREF_KEY_DEFAULT_UPDATE_INTERVAL)) {
			pref.setSummary(enclosingActivity.getString(R.string.seconds_abbreviation, sharedPrefs.getString(key, "")));
		} else if(key.equals(PREF_KEY_DEFAULT_TIME_FRAME)) {
			pref.setSummary(Conversions.buildMinToHoursSummary(sharedPrefs.getString(key, ""), enclosingActivity));
		} else if(key.equals(PREF_KEY_MOBILE_DATA)) {
			StorageAndControlService.allowMobileData = sharedPrefs.getBoolean(key, false);
		} else if(key.equals(PREF_KEY_SHOW_INTERNET_WARNING)) {
			boolean show = sharedPrefs.getBoolean(key, true);
			Notifications.allowNoInternetNotification = show;
			if(!show)
				Notifications.clearNoInternetNotification(enclosingActivity);
		}
	}

	private void restartActivity() {
		enclosingActivity.setResult(SettingsActivity.RESULT_RESTART);
		Intent intent = enclosingActivity.getIntent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		enclosingActivity.finish();
		enclosingActivity.startActivity(intent);
	}

	/*
	 * Patch to overcome onSharedPreferenceChange not being called by RingtonePreference.
	 * By Arad on Stack Overflow http://stackoverflow.com/a/8105349
	 */
	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		pref.setSummary(Conversions.ringtoneUriToName((String) newValue, enclosingActivity));
		return true;
	}
}
