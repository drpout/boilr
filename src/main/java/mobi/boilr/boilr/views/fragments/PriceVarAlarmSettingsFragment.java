package mobi.boilr.boilr.views.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

public class PriceVarAlarmSettingsFragment extends AlarmSettingsFragment {

	private class OnPriceVarSettingsPreferenceChangeListener extends OnAlarmSettingsPreferenceChangeListener{

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if(true){
				
			}else if(preference.getKey().equals(PREF_KEY_ALARM_UPDATE_INTERVAL_VAR)) {
				preference.setSummary(SettingsFragment.buildMinToDaysSummary((String)newValue));
			}else{
				return super.onPreferenceChange(preference, newValue);
			}
			return true;
		}


	}

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

		Preference preference = findPreference("type");
		preference.setSummary("Price Var");

		PreferenceCategory category = (PreferenceCategory) findPreference("specific");
		category.setTitle("Price Var");

		SharedPreferences sharedPreferences = 	PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		ListPreference pref;
		pref = (ListPreference) findPreference(PREF_KEY_ALARM_UPDATE_INTERVAL_VAR);
		pref.setSummary(SettingsFragment.buildMinToDaysSummary(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_VAR, "")));
		pref.setDefaultValue(null);
		pref.setOnPreferenceChangeListener(listener);

	}

	@Override
	protected void makeAlarm() {
		// TODO Auto-generated method stub

	}
}
