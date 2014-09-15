package com.github.andrefbsantos.boilr.activities;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

public class OnAlarmSettingsPreferenceChangeListener implements OnPreferenceChangeListener {

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		
		
		
		
		preference.setSummary((String)newValue);
		return true;
	}

}
