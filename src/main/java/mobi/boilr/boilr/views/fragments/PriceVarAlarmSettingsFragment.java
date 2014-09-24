package mobi.boilr.boilr.views.fragments;


import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceVarAlarm;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.InputType;

public class PriceVarAlarmSettingsFragment extends AlarmSettingsFragment {

	private class OnPriceVarSettingsPreferenceChangeListener extends OnAlarmSettingsPreferenceChangeListener{

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			PriceVarAlarm priceVarAlarm = (PriceVarAlarm) alarm;

			if(key.equals(PriceVarAlarmCreationFragment.PREF_KEY_VAR_IN_PERCENTAGE)){
				if((Boolean)newValue){
					priceVarAlarm.setPercent((float) priceVarAlarm.getVariation());
					findPreference(PriceVarAlarmCreationFragment.PREF_KEY_VAR_VALUE).setSummary(priceVarAlarm.getPercent() + "%");
				}else{
					priceVarAlarm.setVariation(priceVarAlarm.getPercent());
					priceVarAlarm.setPercent(0);
					findPreference(PriceVarAlarmCreationFragment.PREF_KEY_VAR_VALUE)
					.setSummary(priceVarAlarm.getVariation() + " " + alarm.getPair()
							.getExchange());
				}
			}else if(key.equals(PriceVarAlarmCreationFragment.PREF_KEY_VAR_VALUE)){
				if(priceVarAlarm.isPercent()){
					priceVarAlarm.setPercent(Float.parseFloat((String) newValue));
					preference.setSummary(newValue + "%");
				}else{
					priceVarAlarm.setVariation(Double.parseDouble((String) newValue));
					preference.setSummary(newValue + " " + alarm.getPair().getExchange());
				}
			}else if(key.equals(AlarmCreationFragment.PREF_KEY_UPDATE_INTERVAL)) {
				preference.setSummary(SettingsFragment.buildMinToDaysSummary((String)newValue));
				priceVarAlarm.setPeriod(Long.parseLong((String) newValue) * 60000);
			}else{
				return super.onPreferenceChange(preference, newValue);
			}
			((AlarmSettingsActivity) enclosingActivity).getStorageAndControlService()
			.replaceAlarm(priceVarAlarm);
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

		PriceVarAlarm priceVarAlarm = (PriceVarAlarm) alarm;

		ListPreference alarmTypePref = (ListPreference) findPreference(AlarmCreationFragment.PREF_KEY_TYPE);
		alarmTypePref.setValueIndex(1);
		alarmTypePref.setSummary(alarmTypePref.getEntry());

		PreferenceCategory category = (PreferenceCategory) findPreference(AlarmCreationFragment.PREF_KEY_SPECIFIC);
		category.setTitle(alarmTypePref.getEntry());

		CheckBoxPreference checkBoxPref = new CheckBoxPreference(enclosingActivity);
		checkBoxPref.setTitle(R.string.pref_title_var_in_percentage);
		checkBoxPref.setKey(PriceVarAlarmCreationFragment.PREF_KEY_VAR_IN_PERCENTAGE);
		checkBoxPref.setDefaultValue(priceVarAlarm.isPercent());
		checkBoxPref.setOnPreferenceChangeListener(listener);
		checkBoxPref.setOrder(0);
		category.addPreference(checkBoxPref);
		checkBoxPref.setChecked(priceVarAlarm.isPercent());

		EditTextPreference edit = new EditTextPreference(enclosingActivity);
		edit.setKey(PriceVarAlarmCreationFragment.PREF_KEY_VAR_VALUE);
		edit.setTitle(R.string.pref_title_var_value);
		edit.setDialogTitle(R.string.pref_title_var_value);
		edit.setOnPreferenceChangeListener(listener);
		edit.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		edit.setOrder(1);

		if(priceVarAlarm.isPercent()){
			edit.setDefaultValue(priceVarAlarm.getPercent());
			edit.setText(SettingsFragment.cleanDoubleToString(priceVarAlarm.getPercent()));
			edit.setSummary(SettingsFragment.cleanDoubleToString(priceVarAlarm.getPercent()) + "%");
		}else{
			edit.setDefaultValue(priceVarAlarm.getVariation());
			edit.setText(SettingsFragment.cleanDoubleToString(priceVarAlarm.getVariation()));
			edit.setSummary(SettingsFragment.cleanDoubleToString(priceVarAlarm.getVariation()) + " " + alarm
					.getPair().getExchange());
		}

		category.addPreference(edit);

		edit = (EditTextPreference) findPreference(AlarmCreationFragment.PREF_KEY_UPDATE_INTERVAL);
		edit.setDialogMessage(R.string.pref_summary_update_interval_var);
		edit.setSummary(SettingsFragment.buildMinToDaysSummary(String.valueOf(priceVarAlarm.getPeriod()/60000)));
		edit.setOnPreferenceChangeListener(listener);
		edit.setText(String.valueOf(priceVarAlarm.getPeriod()/60000));
	}
}
