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
		DARK, LIGHT
	}

	private static Theme curTheme = null;

	public static void changeTheme(String newTheme) {
		curTheme = Theme.valueOf(newTheme);
	}

	public static void applyTheme(Activity activity) {
		if(curTheme == null) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
			curTheme = Theme.valueOf(sharedPreferences.getString(SettingsFragment.PREF_KEY_THEME, "DARK"));
		}
		switch(curTheme) {
			case DARK:
				activity.setTheme(R.style.Theme_Boilr_Dark);
				break;
			case LIGHT:
				activity.setTheme(R.style.Theme_Boilr_Light);
				break;
		}
	}

	public static Theme getCurTheme() {
		return curTheme;
	}

}
