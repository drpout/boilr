package com.github.andrefbsantos.boilr.views.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;

public class PriceVarAlarmSettingsFragment extends AlarmSettingsFragment {

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		
		Preference preference = findPreference("type");
		preference.setSummary("Price Var");
		
		PreferenceCategory category = (PreferenceCategory) findPreference("specific");
		category.setTitle("Price Var");
		
		
		
	}
}
