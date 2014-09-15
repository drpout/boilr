package com.github.andrefbsantos.boilr.fragments;

import com.github.andrefbsantos.boilr.R;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GenericAlarmSettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
				
		addPreferencesFromResource(R.layout.generic_alarm_settings);
	}
	
}
