package com.github.andrefbsantos.boilr.views.fragments;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;

public class PriceHitAlarmSettingsFragment extends AlarmSettingsFragment {

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

		Preference preference = findPreference("type");
		preference.setSummary("Price Hit");

		PreferenceCategory category = (PreferenceCategory) findPreference("specific");
		category.setTitle("Price Hit");

		EditTextPreference edit;
		edit = new EditTextPreference(this.getActivity());
		edit.setTitle("Upper");
		category.addPreference(edit);

		edit = new EditTextPreference(this.getActivity());
		edit.setTitle("down");
		category.addPreference(edit);
	}
}
