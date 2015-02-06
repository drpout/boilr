package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.views.fragments.SettingsFragment;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Manages the app theme.
 * Based on org.fdroid.fdroid.FDroidApp.java
 */
public class Themer {
	public static enum Theme {
		dark, light
	}

	private static Theme curTheme = null;

	public static void changeTheme(String newTheme) {
		curTheme = Theme.valueOf(newTheme);
	}

	public static void applyTheme(Activity activity) {
		if(curTheme == null) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
			curTheme = Theme.valueOf(sharedPreferences.getString(SettingsFragment.PREF_KEY_THEME, "light"));
		}
		switch(curTheme) {
			case dark:
				activity.setTheme(R.style.BoilrTheme_Dark);
				break;
			case light:
				activity.setTheme(R.style.BoilrTheme_Light);
				break;
		}
	}

	public static Theme getCurTheme() {
		return curTheme;
	}

}
