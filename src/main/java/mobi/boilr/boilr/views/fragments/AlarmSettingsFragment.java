package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Log;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;

public class AlarmSettingsFragment extends PreferenceFragment{


	private static final String PREF_VALUE_PRICE_HIT = "price_hit";
	private static final String PREF_VALUE_PRICE_VAR = "price_var";
	private static final String PREF_KEY_EXCHANGE = "exchange";
	private static final String PREF_KEY_TYPE = "type";
	private static final String PREF_KEY_PAIR = "pair";
	//private static final String PREF_EXCHANGE_DEFAULT = "Bitstamp";
	private static final String PREF_TYPE_DEFAULT_VALUE = "Price Hit";

	class OnSettingsPreferenceChangeListener implements OnPreferenceChangeListener{

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Log.d(preference.getKey() +" " + newValue);

			CharSequence summary = (String) newValue;
			if(preference.getKey().equals(PREF_KEY_EXCHANGE)){
				summary = ((ListPreference) preference).getEntry();
				//TODO: refresh pair list
			}else if(preference.getKey().equals(PREF_KEY_PAIR)){	
				summary = ((ListPreference) preference).getEntry();
				//TODO
			}else if(preference.getKey().equals(PREF_KEY_TYPE)){
				if(newValue.equals(PREF_VALUE_PRICE_VAR)){
					getActivity().getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceVarAlarmSettingsFragment()).commit();
				}else if(newValue.equals(PREF_VALUE_PRICE_HIT)){
					getActivity().getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceHitAlarmSettingsFragment()).commit();
				}
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

		pref = findPreference(PREF_KEY_EXCHANGE);
		pref.setOnPreferenceChangeListener(new OnSettingsPreferenceChangeListener());

		//First Entry as default
		CharSequence defaultEntry = ((ListPreference)pref).getEntries()[0]; 
		pref.setSummary(defaultEntry);

		pref = findPreference(PREF_KEY_PAIR);
		pref.setOnPreferenceChangeListener(new OnSettingsPreferenceChangeListener());


		pref = findPreference(PREF_KEY_PAIR);
		pref.setOnPreferenceChangeListener(new OnSettingsPreferenceChangeListener());
		
		
		//SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
//		SharedPreferences sharedPreferences = 	PreferenceManager.getDefaultSharedPreferences(this.getActivity());
//		
//		RingtonePreference alertSoundPref = (RingtonePreference) findPreference(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND);
//		ListPreference alertTypePref = (ListPreference) findPreference(SettingsFragment.PREF_KEY_DEFAULT_ALERT_TYPE);
//		
//		alertSoundPref.setRingtoneType(Integer.parseInt(alertTypePref.getValue()));
//		alertSoundPref.setSummary(ringtoneUriToName(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, "")));
		
		
	}
}
