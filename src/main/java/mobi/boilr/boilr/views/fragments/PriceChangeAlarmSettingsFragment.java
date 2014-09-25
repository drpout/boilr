package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceChangeAlarm;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.InputType;

public class PriceChangeAlarmSettingsFragment extends AlarmSettingsFragment {

	private class OnPriceChangeSettingsPreferenceChangeListener extends
			OnAlarmSettingsPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			PriceChangeAlarm priceChangeAlarm = (PriceChangeAlarm) alarm;

			if(key.equals(PriceChangeAlarmCreationFragment.PREF_KEY_CHANGE_IN_PERCENTAGE)) {
				if((Boolean) newValue) {
					priceChangeAlarm.setPercent((float) priceChangeAlarm.getChange());
					findPreference(PriceChangeAlarmCreationFragment.PREF_KEY_CHANGE_VALUE).setSummary(priceChangeAlarm.getPercent() + "%");
				} else {
					priceChangeAlarm.setChange(priceChangeAlarm.getPercent());
					priceChangeAlarm.setPercent(0);
					findPreference(PriceChangeAlarmCreationFragment.PREF_KEY_CHANGE_VALUE)
							.setSummary(priceChangeAlarm.getChange() + " " + alarm.getPair()
									.getExchange());
				}
			} else if(key.equals(PriceChangeAlarmCreationFragment.PREF_KEY_CHANGE_VALUE)) {
				if(priceChangeAlarm.isPercent()) {
					priceChangeAlarm.setPercent(Float.parseFloat((String) newValue));
					preference.setSummary(newValue + "%");
				} else {
					priceChangeAlarm.setChange(Double.parseDouble((String) newValue));
					preference.setSummary(newValue + " " + alarm.getPair().getExchange());
				}
			} else if(key.equals(AlarmCreationFragment.PREF_KEY_UPDATE_INTERVAL)) {
				preference.setSummary(SettingsFragment.buildMinToDaysSummary((String) newValue));
				priceChangeAlarm.setPeriod(Long.parseLong((String) newValue) * 60000);
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			((AlarmSettingsActivity) enclosingActivity).getStorageAndControlService()
					.replaceAlarm(priceChangeAlarm);
			return true;
		}
	}

	OnAlarmSettingsPreferenceChangeListener listener = new OnPriceChangeSettingsPreferenceChangeListener();

	public PriceChangeAlarmSettingsFragment(Alarm alarm) {
		super(alarm);
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		PriceChangeAlarm priceChangeAlarm = (PriceChangeAlarm) alarm;

		ListPreference alarmTypePref = (ListPreference) findPreference(AlarmCreationFragment.PREF_KEY_TYPE);
		alarmTypePref.setValueIndex(1);
		alarmTypePref.setSummary(alarmTypePref.getEntry());

		PreferenceCategory category = (PreferenceCategory) findPreference(AlarmCreationFragment.PREF_KEY_SPECIFIC);
		category.setTitle(alarmTypePref.getEntry());

		CheckBoxPreference checkBoxPref = new CheckBoxPreference(enclosingActivity);
		checkBoxPref.setTitle(R.string.pref_title_change_in_percentage);
		checkBoxPref.setKey(PriceChangeAlarmCreationFragment.PREF_KEY_CHANGE_IN_PERCENTAGE);
		checkBoxPref.setDefaultValue(priceChangeAlarm.isPercent());
		checkBoxPref.setOnPreferenceChangeListener(listener);
		checkBoxPref.setOrder(0);
		category.addPreference(checkBoxPref);
		checkBoxPref.setChecked(priceChangeAlarm.isPercent());

		EditTextPreference edit = new EditTextPreference(enclosingActivity);
		edit.setKey(PriceChangeAlarmCreationFragment.PREF_KEY_CHANGE_VALUE);
		edit.setTitle(R.string.pref_title_change_value);
		edit.setDialogTitle(R.string.pref_title_change_value);
		edit.setOnPreferenceChangeListener(listener);
		edit.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		edit.setOrder(1);

		if(priceChangeAlarm.isPercent()) {
			edit.setDefaultValue(priceChangeAlarm.getPercent());
			edit.setText(SettingsFragment.cleanDoubleToString(priceChangeAlarm.getPercent()));
			edit.setSummary(SettingsFragment.cleanDoubleToString(priceChangeAlarm.getPercent()) + "%");
		} else {
			edit.setDefaultValue(priceChangeAlarm.getChange());
			edit.setText(SettingsFragment.cleanDoubleToString(priceChangeAlarm.getChange()));
			edit.setSummary(SettingsFragment.cleanDoubleToString(priceChangeAlarm.getChange()) + " " + alarm
					.getPair().getExchange());
		}

		category.addPreference(edit);

		edit = (EditTextPreference) findPreference(AlarmCreationFragment.PREF_KEY_UPDATE_INTERVAL);
		edit.setTitle(R.string.pref_title_time_frame);
		edit.setDialogMessage(R.string.pref_summary_update_interval_change);
		edit.setSummary(SettingsFragment.buildMinToDaysSummary(String.valueOf(priceChangeAlarm.getPeriod() / 60000)));
		edit.setOnPreferenceChangeListener(listener);
		edit.setText(String.valueOf(priceChangeAlarm.getPeriod() / 60000));
	}
}
