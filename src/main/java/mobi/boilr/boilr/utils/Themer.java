package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.R;
import android.app.Activity;

/**
 * Manages the app theme.
 * Based on org.fdroid.fdroid.FDroidApp.java
 */
public class Themer {
	public static enum Theme {
		dark, light
	}

	private static Theme curTheme = Theme.dark;

	public static void changeTheme(String newTheme) {
		curTheme = Theme.valueOf(newTheme);
	}

	public static void applyTheme(Activity activity) {
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
