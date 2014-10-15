package mobi.boilr.boilr.activities;

import mobi.boilr.boilr.utils.Languager;
import mobi.boilr.boilr.utils.Themer;
import mobi.boilr.boilr.views.fragments.SettingsFragment;
import android.app.Activity;
import android.os.Bundle;
import android.preference.RingtonePreference;

public class SettingsActivity extends Activity {
	private SettingsFragment settingsFragment;
	public static final int RESULT_RESTART = Activity.RESULT_FIRST_USER;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Themer.applyTheme(this);
		Languager.setLanguage(this);
		// Display the fragment as the main content.
		settingsFragment = new SettingsFragment();
		getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		settingsFragment.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(settingsFragment);
		/*
		 * Patch to overcome onSharedPreferenceChange not being called by RingtonePreference.
		 * By Arad on Stack Overflow http://stackoverflow.com/a/8105349
		 */
		RingtonePreference pref = (RingtonePreference) settingsFragment.findPreference(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND);
		pref.setOnPreferenceChangeListener(settingsFragment);
	}

	@Override
	protected void onPause() {
		super.onPause();
		settingsFragment.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(settingsFragment);
	}
}
