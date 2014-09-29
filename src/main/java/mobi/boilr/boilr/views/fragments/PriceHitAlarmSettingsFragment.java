package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.InputType;

public class PriceHitAlarmSettingsFragment extends AlarmSettingsFragment {

	private class OnPriceHitSettingsPreferenceChangeListener extends
			OnAlarmSettingsPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			PriceHitAlarm priceHitAlarm = (PriceHitAlarm) alarm;
			String key = preference.getKey();
			if(key.equals(PriceHitAlarmCreationFragment.PREF_KEY_UPPER_VALUE)) {
				preference.setSummary(newValue + " " + alarm.getPair().getExchange());
				priceHitAlarm.setUpperBound(Double.parseDouble((String) newValue));
			} else if(key.equals(PriceHitAlarmCreationFragment.PREF_KEY_LOWER_VALUE)) {
				preference.setSummary(newValue + " " + alarm.getPair().getExchange());
				priceHitAlarm.setLowerBound(Double.parseDouble((String) newValue));
			} else if(key.equals(PriceHitAlarmCreationFragment.PREF_KEY_UPDATE_INTERVAL)) {
				preference.setSummary(newValue + " s");
				alarm.setPeriod(1000 * Long.parseLong((String) newValue));
				if(enclosingActivity.isBound()) {
					enclosingActivity.getStorageAndControlService().restartAlarm(priceHitAlarm);
				} else {
					Log.d("AlarmSettingsActivity not bound to StorageAndControlService.");
				}
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			if(enclosingActivity.isBound()) {
				enclosingActivity.getStorageAndControlService().replaceAlarmDB(priceHitAlarm);
			} else {
				Log.d("AlarmSettingsActivity not bound to StorageAndControlService.");
			}
			return true;
		}
	}

	private OnAlarmSettingsPreferenceChangeListener listener = new OnPriceHitSettingsPreferenceChangeListener();

	public PriceHitAlarmSettingsFragment(Alarm alarm) {
		super(alarm);
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		PriceHitAlarm priceHitAlarm = (PriceHitAlarm) alarm;

		ListPreference alarmTypePref = (ListPreference) findPreference(PriceHitAlarmCreationFragment.PREF_KEY_TYPE);
		alarmTypePref.setValueIndex(0);
		alarmTypePref.setSummary(alarmTypePref.getEntry());
		alarmTypePref.setEnabled(false);
		PreferenceCategory category = (PreferenceCategory) findPreference(PriceHitAlarmCreationFragment.PREF_KEY_SPECIFIC);
		category.setTitle(alarmTypePref.getEntry());

		EditTextPreference edit = new EditTextPreference(enclosingActivity);
		edit.setKey(PriceHitAlarmCreationFragment.PREF_KEY_UPPER_VALUE);
		edit.setTitle(R.string.pref_title_upper_bound);
		edit.setDialogTitle(R.string.pref_title_upper_bound);
		edit.setOnPreferenceChangeListener(listener);
		edit.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		edit.setOrder(0);
		String formated = Conversions.formatMaxDecimalPlaces(priceHitAlarm.getUpperBound());
		edit.setDefaultValue(formated);
		edit.setSummary(formated + " " + alarm.getPair().getExchange());
		category.addPreference(edit);
		// setText only works after adding the preference.
		edit.setText(formated);

		edit = new EditTextPreference(enclosingActivity);
		edit.setKey(PriceHitAlarmCreationFragment.PREF_KEY_LOWER_VALUE);
		edit.setTitle(R.string.pref_title_lower_bound);
		edit.setDialogTitle(R.string.pref_title_lower_bound);
		edit.setOnPreferenceChangeListener(listener);
		edit.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		edit.setOrder(1);
		formated = Conversions.formatMaxDecimalPlaces(priceHitAlarm.getLowerBound());
		edit.setDefaultValue(formated);
		edit.setSummary(formated + " " + alarm.getPair().getExchange());
		category.addPreference(edit);
		edit.setText(formated);
		edit = (EditTextPreference) findPreference(PriceHitAlarmCreationFragment.PREF_KEY_UPDATE_INTERVAL);
		edit.setDialogMessage(R.string.pref_summary_update_interval_hit);
		edit.setSummary((priceHitAlarm.getPeriod() / 1000) + " s");
		edit.setOnPreferenceChangeListener(listener);
	}
}
