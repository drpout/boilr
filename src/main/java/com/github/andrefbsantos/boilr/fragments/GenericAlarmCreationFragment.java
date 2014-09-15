package com.github.andrefbsantos.boilr.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.andrefbsantos.boilr.R;

public class GenericAlarmCreationFragment extends GenericAlarmSettingsFragment {

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		View view = inflater.inflate(R.layout.alert_alarm_settings,container,false);
		//TODO populate view with defaults
		return view; 
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
}
