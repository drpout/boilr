package com.github.andrefbsantos.boilr.activities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.andrefbsantos.boilr.R;
import com.github.andrefbsantos.boilr.fragments.GenericAlarmSettingsFragment;
import com.github.andrefbsantos.boilr.views.fragments.SettingsFragment;
import com.github.andrefbsantos.libdynticker.allcoin.*;
import com.github.andrefbsantos.libdynticker.core.Exchange;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class AlarmCreationActivity extends PreferenceActivity {

	private static final String PRICE_HIT = "price_hit";
	private static final String PRICE_VAR = "price_var";
	private static final String tag = "AlarmCreationActivity";

	SettingsFragment settingsFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.alarm_settings);
//		settingsFragment = new SettingsFragment();
//		getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();

		addPreferencesFromResource(R.layout.generic_alarm_settings);
		
		ListPreference exchangesListPreference = (ListPreference) findPreference("exchange");

		Set<Class<? extends Exchange>> exchanges = Exchange.getExchanges();
		String []  values = new String[exchanges.size()];
		String [] entries = new String[exchanges.size()];
		
		Iterator<Class<? extends Exchange>> iterator = exchanges.iterator();
		int i = 0;
		System.out.println(iterator.hasNext());
		System.out.println("AAAAA " + exchanges.size());
		while(iterator.hasNext()){
			Class<? extends Exchange> exchange = iterator.next();
			values[i]=exchange.toString();
			entries[i]=exchange.toString().substring(0, exchange.toString().indexOf(".class"));
			System.out.println(values[i]+"_"+entries[i]);
			i++;
		}
		
		exchangesListPreference.setEntries(entries);		
		exchangesListPreference.setEntryValues(values);
		
		exchangesListPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Log.d(tag,(String) newValue);
				return true;
			}
		});

		

//		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//		fragmentTransaction.add(R.id.generic_alarm_settings, new GenericAlarmSettingsFragment());
//
//		//By default it will show a price hit alarm
//		//fragmentTransaction.add(R.id.specific_alarm_creation, new PriceHitAlarmSettingsFragment());		
//		//fragmentTransaction.add(R.id.alert_alarm_creation, new AlarmAlertSettingsFragment());
//		fragmentTransaction.commit();
	}

	//	public void changeAlarmType(String type){
	//		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
	//		if(type.equals(PRICE_HIT) ){
	//			fragmentTransaction.add(R.id.specific_alarm_creation, new PriceHitAlarmSettingsFragment());
	//		}else if(type.equals(PRICE_VAR)){
	//			fragmentTransaction.add(R.id.specific_alarm_creation, new PriceVarAlarmSettingsFragment());
	//		}
	//		fragmentTransaction.commit();
	//	}
}
