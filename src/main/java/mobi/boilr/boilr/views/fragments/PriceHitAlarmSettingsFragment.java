package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmSettingsActivity;
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
			Log.d(preference.getKey() + " " + newValue);
			PriceHitAlarm priceHitAlarm = (PriceHitAlarm) alarm;
			if (preference.getKey().equals(PriceHitAlarmCreationFragment.PREF_KEY_UPPER_VALUE)) {
				preference
				.setSummary((CharSequence) newValue + " " + alarm.getPair().getExchange());
				priceHitAlarm.setUpperBound(Double.parseDouble((String) newValue));
			} else if (preference.getKey()
					.equals(PriceHitAlarmCreationFragment.PREF_KEY_LOWER_VALUE)) {
				preference
				.setSummary((CharSequence) newValue + " " + alarm.getPair().getExchange());
				priceHitAlarm.setLowerBound(Double.parseDouble((String) newValue));
			} else if (preference.getKey()
					.equals(PriceHitAlarmCreationFragment.PREF_KEY_UPDATE_INTERVAL)) {
				preference.setSummary(newValue + " s");
				alarm.setPeriod(1000 * Long.parseLong((String) newValue));
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			((AlarmSettingsActivity) enclosingActivity).getStorageAndControlService()
			.replaceAlarm(priceHitAlarm);
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
		PreferenceCategory category = (PreferenceCategory) findPreference(PriceHitAlarmCreationFragment.PREF_KEY_SPECIFIC);
		category.setTitle(alarmTypePref.getEntry());

		EditTextPreference edit = new EditTextPreference(enclosingActivity);
		edit.setKey(PriceHitAlarmCreationFragment.PREF_KEY_UPPER_VALUE);
		edit.setTitle(R.string.pref_title_upper_bound);
		edit.setDialogTitle(R.string.pref_title_upper_bound);
		edit.setDefaultValue(priceHitAlarm.getUpperBound());
		edit.setOnPreferenceChangeListener(listener);
		edit.getEditText()
		.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		edit.setOrder(0);
		edit.setSummary(SettingsFragment.cleanDoubleToString(priceHitAlarm.getUpperBound()) + " " + alarm
				.getPair().getExchange());
		category.addPreference(edit);
		// setText only works after adding the preference.
		edit.setText(SettingsFragment.cleanDoubleToString(priceHitAlarm.getUpperBound()));

		edit = new EditTextPreference(enclosingActivity);
		edit.setKey(PriceHitAlarmCreationFragment.PREF_KEY_LOWER_VALUE);
		edit.setTitle(R.string.pref_title_lower_bound);
		edit.setDialogTitle(R.string.pref_title_lower_bound);
		edit.setDefaultValue(priceHitAlarm.getLowerBound());
		edit.setOnPreferenceChangeListener(listener);
		edit.getEditText()
		.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		edit.setOrder(1);
		edit.setSummary(SettingsFragment.cleanDoubleToString(priceHitAlarm.getLowerBound()) + " " + alarm
				.getPair().getExchange());
		category.addPreference(edit);
		edit.setText(SettingsFragment.cleanDoubleToString(priceHitAlarm.getLowerBound()));

		edit = (EditTextPreference) findPreference(PriceHitAlarmCreationFragment.PREF_KEY_UPDATE_INTERVAL);
		edit.setDialogMessage(R.string.pref_summary_update_interval_hit);
		edit.setSummary((priceHitAlarm.getPeriod() / 1000) + " s");
		edit.setOnPreferenceChangeListener(listener);
	}
}
