package mobi.boilr.boilr.activities;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Languager;
import mobi.boilr.boilr.utils.Notifications;
import mobi.boilr.boilr.utils.Themer;
import mobi.boilr.boilr.views.fragments.SettingsFragment;
import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
	private SettingsFragment settingsFragment;
	public static final int RESULT_RESTART = Activity.RESULT_FIRST_USER;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Themer.applyTheme(this);
		Languager.setLanguage(this);
		setTitle(getResources().getString(R.string.boilr_settings));
		// Display the fragment as the main content.
		settingsFragment = new SettingsFragment();
		getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
		if(Notifications.ACTION_DISABLE_NET_NOTIF.equals(getIntent().getAction())) {
			Notifications.sAllowNoNetNotif = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		settingsFragment.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(settingsFragment);
	}

	@Override
	protected void onPause() {
		super.onPause();
		settingsFragment.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(settingsFragment);
	}
}
