package mobi.boilr.boilr.views.fragments;


import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceVarAlarm;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PriceVarAlarmSettingsFragment extends AlarmSettingsFragment {

	private static final String PREF_KEY_ALARM_VAR_UPDATE_INTERVAL = "pref_key_alarm_var_update_interval";
	private static final String PREF_KEY_ALARM_VAR_TYPE = "pref_key_alarm_var_type";
	private static final String PREF_KEY_ALARM_VAR_TYPE_PERCENTAGE = "pref_key_alarm_type_percentage";
	private static final String PREF_KEY_ALARM_VAR_TYPE_VARIATION = "pref_key_alarm_var_type_variation";
	private static final String PREF_KEY_ALARM_VAR_VALUE = "pref_key_alarm_var_value";

	private class OnPriceVarSettingsPreferenceChangeListener extends OnAlarmSettingsPreferenceChangeListener{

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			PriceVarAlarm priceVarAlarm = (PriceVarAlarm) alarm;
			if(key.equals(PREF_KEY_ALARM_VAR_TYPE)){
				ListPreference listPreference = (ListPreference) preference;
				listPreference.setSummary(listPreference.getEntries()[listPreference.findIndexOfValue((String) newValue)]);
				listPreference.setValue((String) newValue);
			}else if(key.equals(PREF_KEY_ALARM_VAR_VALUE)){
				preference.setSummary((CharSequence) newValue);
				Log.d(((ListPreference) findPreference(PREF_KEY_ALARM_VAR_TYPE)).getValue());
				if(((ListPreference) findPreference(PREF_KEY_ALARM_VAR_TYPE)).getValue().equals(PREF_KEY_ALARM_VAR_TYPE_VARIATION)){
					priceVarAlarm.setVariation(Double.parseDouble((String) newValue));
					priceVarAlarm.setPercent(0);
				}else{
					priceVarAlarm.setPercent(Float.parseFloat((String) newValue));
				}
			}else if(preference.getKey().equals(PREF_KEY_ALARM_VAR_UPDATE_INTERVAL)) {
				preference.setSummary(SettingsFragment.buildMinToDaysSummary((String)newValue));
				priceVarAlarm.setPeriod(Long.parseLong((String) newValue));
			}else{
				return super.onPreferenceChange(preference, newValue);
			}
			((AlarmSettingsActivity) getActivity()).getStorageAndControlService().replaceAlarm(priceVarAlarm);
			return true;
		}
	}

	OnAlarmSettingsPreferenceChangeListener listener = new OnPriceVarSettingsPreferenceChangeListener();


	public PriceVarAlarmSettingsFragment(Alarm alarm) {
		super(alarm);
	}

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

		PreferenceCategory category = (PreferenceCategory) findPreference("specific");
		category.setTitle("Price Var");

		String [] entries = {"Variation","Percentage"};
		String [] values = {PREF_KEY_ALARM_VAR_TYPE_VARIATION,PREF_KEY_ALARM_VAR_TYPE_PERCENTAGE};
		ListPreference listPreference = new ListPreference(getActivity());
		listPreference.setTitle("Variation type");
		listPreference.setEntries(entries);
		listPreference.setEntryValues(values);

		listPreference.setKey(PREF_KEY_ALARM_VAR_TYPE);
		listPreference.setOnPreferenceChangeListener(listener);
		category.addPreference(listPreference);

		EditTextPreference editTextPreference = new EditTextPreference(getActivity());
		editTextPreference.setKey(PREF_KEY_ALARM_VAR_VALUE);
		editTextPreference.setTitle("Variation");
		editTextPreference.setDialogMessage("Insert a variation");

		editTextPreference.setOnPreferenceChangeListener(listener);
		category.addPreference(editTextPreference);

		PriceVarAlarm priceVaralarm = (PriceVarAlarm) alarm;
		if(priceVaralarm.isPercent()){
			listPreference.setSummary("Percentage");
			listPreference.setValueIndex(1);
			editTextPreference.setSummary(String.valueOf(priceVaralarm.getPercent()));
		}else{
			listPreference.setSummary("Variation");
			listPreference.setValueIndex(0);
			editTextPreference.setSummary(String.valueOf(priceVaralarm.getVariation()));
		}

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		editTextPreference = new EditTextPreference(getActivity());
		editTextPreference.setKey(PREF_KEY_ALARM_VAR_UPDATE_INTERVAL);
		editTextPreference.setTitle("Variation Update Interval");
		editTextPreference.setDialogMessage("Insert an Interval");
		editTextPreference.setDefaultValue(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_VAR, ""));
		editTextPreference.setSummary(SettingsFragment.buildMinToDaysSummary(String.valueOf(priceVaralarm.getPeriod())));
		editTextPreference.setOnPreferenceChangeListener(listener);
		category = (PreferenceCategory) findPreference("alert");
		category.addPreference(editTextPreference);
	}
}
