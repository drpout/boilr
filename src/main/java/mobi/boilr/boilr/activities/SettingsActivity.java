package mobi.boilr.boilr.activities;

import mobi.boilr.boilr.views.fragments.SettingsFragment;
import android.app.Activity;
import android.os.Bundle;
import android.preference.RingtonePreference;

public class SettingsActivity extends Activity {
	SettingsFragment settingsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
