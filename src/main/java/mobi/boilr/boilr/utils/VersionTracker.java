/*
 * Copyright (C) 2015 David Ludovino <david.ludovino@gmail.com> (relicensed under GPLv3+)
 * Extracted the functions that handle version tracking.
 * 
 * Based on ckChangeLog (licensed under APLv2)
 * Copyright (C) 2012-2015 cketti and contributors
 * https://github.com/cketti/ckChangeLog/graphs/contributors
 * Portions Copyright (C) 2012 Martin van Zuilekom http://martin.cubeactive.com
 *
 * ckChangeLog is based on android-change-log
 * Copyright (C) 2011, Karsten Priegnitz, with the license:
 * Permission to use, copy, modify, and distribute this piece of software
 * for any purpose with or without fee is hereby granted, provided that
 * the above copyright notice and this permission notice appear in the
 * source code of all copies.
 *
 * It would be appreciated if you mention the author in your change log,
 * contributors list or the like.
 *
 * http://code.google.com/p/android-change-log/
 */
package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.views.fragments.ChangelogDialogFragment;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

public class VersionTracker {
    /**
	 * Key used to store version code in SharedPreferences.
	 */
	private static final String PREF_KEY_VERSION = "pref_key_last_version_code";

    /**
	 * Constant used when no version code is available.
	 */
	private static final int NO_VERSION = -1;
	private static int currentVersionCode;
	public static boolean isFirstRun;
	private static Context context;

	private VersionTracker() {
	}

	public static void showChangeLog(Activity activity) {
		context = activity;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		int lastVersionCode = sharedPreferences.getInt(PREF_KEY_VERSION, NO_VERSION);
        try {
			PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            currentVersionCode = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            currentVersionCode = NO_VERSION;
			Log.e("Could not get version information from manifest.", e);
        }
		isFirstRun = lastVersionCode < currentVersionCode;
		if(isFirstRun)
			(new ChangelogDialogFragment()).show(activity.getFragmentManager(), "changelog");
    }

	public static void updateVersionInPreferences() {
		if(isFirstRun) {
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putInt(PREF_KEY_VERSION, currentVersionCode);
			// TODO: Update preferences from a background thread
			editor.commit();
		}
    }
}
