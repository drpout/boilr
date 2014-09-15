package com.github.andrefbsantos.boilr.fragments;

import org.xmlpull.v1.XmlPullParser;

import com.github.andrefbsantos.boilr.R;
import com.github.andrefbsantos.boilr.activities.AlarmSettingsActivity;
import com.github.andrefbsantos.boilr.domain.AlarmWrapper;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AlertAlarmSettingsFragment extends Fragment {

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		View view = inflater.inflate(R.layout.alert_alarm_settings,container,false);
		AlarmWrapper wrapper = ((AlarmSettingsActivity) this.getActivity()).getAlarmWrapper();
		//TODO Populate view 
		return view; 
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
}
