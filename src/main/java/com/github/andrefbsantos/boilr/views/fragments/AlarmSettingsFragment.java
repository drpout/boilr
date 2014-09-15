package com.github.andrefbsantos.boilr.views.fragments;

import com.github.andrefbsantos.boilr.R;
import com.github.andrefbsantos.boilr.utils.Log;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;

public class AlarmSettingsFragment extends PreferenceFragment{


	private static final String PRICE_HIT = "price_hit";
	private static final String PRICE_VAR = "price_var";
	private static final String EXCHANGE = "exchange";
	private static final String TYPE = "type";
	private static final String PAIR = "pair";
	private static final String PREF_EXCHANGE_DEFAULT = "Bitstamp";
	private static final String PREF_TYPE_DEFAULT = "Price Hit";

	class OnSettingsPreferenceChangeListener implements OnPreferenceChangeListener{

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Log.d(preference.getKey() +" " + newValue);

			String summary = (String) newValue;
			if(preference.getKey().equals(EXCHANGE)){
				ListPreference list = (ListPreference) preference;
				CharSequence[] values = list.getEntryValues();
				for(int i = 0;i<values.length; i++){
					if(values[i].equals((CharSequence) newValue)){
						summary = (String) list.getEntries()[i];
						break;
					}
				}			
			}else if(preference.getKey().equals(PAIR)){	

			}else if(preference.getKey().equals(TYPE)){
				ListPreference list = (ListPreference) preference;
				CharSequence[] values = list.getEntryValues();
				for(int i = 0;i<values.length; i++){
					if(values[i].equals((CharSequence) newValue)){
						summary = (String) list.getEntries()[i];
						break;
					}
				}
				if(newValue.equals(PRICE_VAR)){
					getActivity().getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceVarAlarmSettingsFragment()).commit();
				}else if(newValue.equals(PRICE_HIT)){
					getActivity().getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceHitAlarmSettingsFragment()).commit();
				}
			}else if(1==1){

			}

			preference.setSummary(summary);
			return false;	
		}
	}

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		addPreferencesFromResource(R.xml.alarm_settings);
		Preference pref;

		pref = findPreference("exchange");
		pref.setOnPreferenceChangeListener(new OnSettingsPreferenceChangeListener());
		pref.setSummary(PREF_EXCHANGE_DEFAULT);

		pref = findPreference("pair");
		pref.setOnPreferenceChangeListener(new OnSettingsPreferenceChangeListener());

		pref = findPreference("type");
		pref.setOnPreferenceChangeListener(new OnSettingsPreferenceChangeListener());
	}
}
