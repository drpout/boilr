package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

public class PriceHitAlarmSettingsFragment extends AlarmSettingsFragment {

	protected static final String PREF_KEY_ALARM_HIT_UPDATE_INTERVAL = "pref_key_alarm_update_interval_hit";
	private static final String PREF_KEY_UPPER_VALUE = "pref_key_upper_value";
	private static final String PREF_KEY_LOWER_VALUE = "pref_key_lower_value";

	private class OnPriceHitSettingsPreferenceChangeListener extends OnAlarmSettingsPreferenceChangeListener{

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if(preference.getKey().equals(PREF_KEY_UPPER_VALUE)){
				preference.setSummary((CharSequence) newValue);
				((PriceHitAlarm)alarm).setUpperBound(Double.parseDouble((String) newValue));
			}else if(preference.getKey().equals(PREF_KEY_LOWER_VALUE)){
				preference.setSummary((CharSequence) newValue);
				((PriceHitAlarm)alarm).setLowerBound(Double.parseDouble((String) newValue));
			}else if(preference.getKey().equals(PREF_KEY_ALARM_HIT_UPDATE_INTERVAL)) {
				preference.setSummary(newValue + " s");
				alarm.setPeriod( 1000*Long.parseLong((String) newValue));
			}else{
				return super.onPreferenceChange(preference, newValue);
			}
			((AlarmSettingsActivity) getActivity()).getStorageAndControlService().replaceAlarm(alarm);;
			return true;
		}
	}

	private OnAlarmSettingsPreferenceChangeListener listener = new OnPriceHitSettingsPreferenceChangeListener();

	public PriceHitAlarmSettingsFragment(Alarm alarm) {
		super(alarm);
	}

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

		PriceHitAlarm priceHitAlarm = (PriceHitAlarm) alarm;
		PreferenceCategory category = (PreferenceCategory) findPreference("specific");
		category.setTitle("Price Hit");

		EditTextPreference edit;
		edit = new EditTextPreference(this.getActivity());
		edit.setKey(PREF_KEY_UPPER_VALUE);
		edit.setTitle("Upper Bound");
		edit.setSummary(String.valueOf(priceHitAlarm.getUpperBound()));
		edit.setOnPreferenceChangeListener(listener);
		category.addPreference(edit);

		edit = new EditTextPreference(this.getActivity());
		edit.setKey(PREF_KEY_LOWER_VALUE);
		edit.setTitle("Lower Bound");
		edit.setSummary(String.valueOf(priceHitAlarm.getLowerBound()));
		edit.setOnPreferenceChangeListener(listener);
		category.addPreference(edit);

		SharedPreferences sharedPreferences = 	PreferenceManager.getDefaultSharedPreferences(this.getActivity());

		edit = new EditTextPreference(this.getActivity());
		edit.setKey(PREF_KEY_ALARM_HIT_UPDATE_INTERVAL);
		edit.setTitle("Hit alarm update interval");
		edit.setDialogMessage("In seconds. Defines how often a hit alarm gets data from the exchange. You can set another interval for a particular alarm on its settings.");
		//edit.setSummary(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, "") + " s");
		edit.setSummary(priceHitAlarm.getPeriod()/1000+" s");
		edit.setDefaultValue(null);
		edit.setOnPreferenceChangeListener(listener);

		category = (PreferenceCategory) findPreference("alert");
		category.addPreference(edit);
	}
}
