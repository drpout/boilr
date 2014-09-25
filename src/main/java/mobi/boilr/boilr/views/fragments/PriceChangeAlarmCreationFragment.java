package mobi.boilr.boilr.views.fragments;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.InputType;

public class PriceChangeAlarmCreationFragment extends AlarmCreationFragment {
	public static final String PREF_KEY_CHANGE_IN_PERCENTAGE = "pref_key_change_in_percentage";
	public static final String PREF_KEY_CHANGE_VALUE = "pref_key_change_value";

	private boolean isPercentage = false;

	private class OnPriceChangeSettingsPreferenceChangeListener extends
			OnAlarmSettingsPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			if(key.equals(PREF_KEY_CHANGE_IN_PERCENTAGE)) {
				isPercentage = (Boolean) newValue;
				EditTextPreference edit = (EditTextPreference) findPreference(PREF_KEY_CHANGE_VALUE);
				edit.setSummary(getChangeValueSummary(edit.getText()));
			} else if(key.equals(PREF_KEY_CHANGE_VALUE)) {
				preference.setSummary(getChangeValueSummary((String) newValue));
			} else if(key.equals(PREF_KEY_UPDATE_INTERVAL)) {
				preference.setSummary(SettingsFragment.buildMinToDaysSummary((String) newValue));
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			return true;
		}

		private String getChangeValueSummary(String value) {
			if(isPercentage)
				return value + "%";
			else
				return value + " " + pairs.get(pairIndex).getExchange();
		}

	}

	OnAlarmSettingsPreferenceChangeListener listener = new OnPriceChangeSettingsPreferenceChangeListener();

	public PriceChangeAlarmCreationFragment(int exchangeIndex, int pairIndex) {
		super(exchangeIndex, pairIndex);
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		ListPreference alarmTypePref = (ListPreference) findPreference(PREF_KEY_TYPE);
		alarmTypePref.setValueIndex(1);
		alarmTypePref.setSummary(alarmTypePref.getEntry());
		PreferenceCategory category = (PreferenceCategory) findPreference(PREF_KEY_SPECIFIC);
		category.setTitle(alarmTypePref.getEntry());

		CheckBoxPreference checkBoxPref = new CheckBoxPreference(enclosingActivity);
		checkBoxPref.setTitle(R.string.pref_title_change_in_percentage);
		checkBoxPref.setKey(PREF_KEY_CHANGE_IN_PERCENTAGE);
		checkBoxPref.setDefaultValue(isPercentage);
		checkBoxPref.setOnPreferenceChangeListener(listener);
		checkBoxPref.setOrder(0);
		category.addPreference(checkBoxPref);
		checkBoxPref.setChecked(isPercentage);

		EditTextPreference edit = new EditTextPreference(enclosingActivity);
		edit.setKey(PREF_KEY_CHANGE_VALUE);
		edit.setTitle(R.string.pref_title_change_value);
		edit.setDialogTitle(R.string.pref_title_change_value);
		edit.setDefaultValue(null);
		edit.setOnPreferenceChangeListener(listener);
		edit.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		edit.setOrder(1);
		category.addPreference(edit);
		edit.setText(null);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);

		edit = (EditTextPreference) findPreference(PREF_KEY_UPDATE_INTERVAL);
		edit.setTitle(R.string.pref_title_time_frame);
		edit.setDialogMessage(R.string.pref_summary_update_interval_change);
		edit.setSummary(SettingsFragment.buildMinToDaysSummary(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_CHANGE, "")));
		edit.setOnPreferenceChangeListener(listener);
		edit.setText(null);
	}

	@Override
	public void makeAlarm(int id, Exchange exchange, Pair pair, AndroidNotify notify)
			throws InterruptedException, ExecutionException, IOException {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);
		String updateInterval = ((EditTextPreference) findPreference(PREF_KEY_UPDATE_INTERVAL)).getText();
		// Time is in minutes, convert to milliseconds
		long period = 60000 * Long.parseLong(updateInterval != null ? updateInterval :
			sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_CHANGE, ""));
		String changeValueString = ((EditTextPreference) findPreference(PREF_KEY_CHANGE_VALUE)).getText();
		double change;
		if(changeValueString == null || changeValueString.equals(""))
			change = Double.POSITIVE_INFINITY;
		else
			change = Double.parseDouble(changeValueString);
		if(isPercentage) {
			float percent = (float) change;
			mStorageAndControlService.addAlarm(id, exchange, pair, period, notify, percent);
		} else {
			mStorageAndControlService.addAlarm(id, exchange, pair, period, notify, change);
		}
	}
}
