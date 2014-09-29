package mobi.boilr.boilr.views.fragments;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.boilr.utils.Conversions;
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
				updateChangeValueSummary();
			} else if(key.equals(PREF_KEY_CHANGE_VALUE)) {
				preference.setSummary(getChangeValueSummary((String) newValue));
			} else if(key.equals(PREF_KEY_UPDATE_INTERVAL)) {
				preference.setSummary(Conversions.buildMinToDaysSummary((String) newValue));
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			return true;
		}
	}

	OnAlarmSettingsPreferenceChangeListener listener = new OnPriceChangeSettingsPreferenceChangeListener();

	@Override
	protected void updateDependentOnPair() {
		EditTextPreference edit = (EditTextPreference) findPreference(PREF_KEY_CHANGE_VALUE);
		edit.setEnabled(true);
		updateChangeValueSummary();
	}

	private void updateChangeValueSummary() {
		EditTextPreference edit = (EditTextPreference) findPreference(PREF_KEY_CHANGE_VALUE);
		String text = edit.getText();
		if(text != null && !text.equals("")) {
			edit.setSummary(getChangeValueSummary(text));
		}
	}

	private String getChangeValueSummary(String value) {
		if(isPercentage)
			return value + "%";
		else
			return value + " " + pairs.get(pairIndex).getExchange();
	}

	@Override
	protected void disableDependentOnPair() {
		EditTextPreference edit = (EditTextPreference) findPreference(PREF_KEY_CHANGE_VALUE);
		edit.setEnabled(false);
		edit.setSummary(null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ListPreference alarmTypePref = (ListPreference) findPreference(PREF_KEY_TYPE);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);
		PreferenceCategory specificCat = (PreferenceCategory) findPreference(PREF_KEY_SPECIFIC);
		Preference pref;
		String key;
		for (int i = 0; i < specificCat.getPreferenceCount(); i++) {
			pref = specificCat.getPreference(i);
			key = pref.getKey();
			if(!key.equals(PREF_KEY_CHANGE_IN_PERCENTAGE) && !key.equals(PREF_KEY_CHANGE_VALUE) && !key.equals(PREF_KEY_UPDATE_INTERVAL)) {
				specificCat.removePreference(pref);
				i--;
			}
		}
		EditTextPreference changePref = (EditTextPreference) findPreference(PREF_KEY_CHANGE_VALUE);
		EditTextPreference updateIntervalPref = (EditTextPreference) findPreference(PREF_KEY_UPDATE_INTERVAL);
		EditTextPreference[] prefs = { changePref,
				updateIntervalPref };
		for (Preference p : prefs) {
			p.setOnPreferenceChangeListener(listener);
		}
		CheckBoxPreference isPercentPref = (CheckBoxPreference) findPreference(PREF_KEY_CHANGE_IN_PERCENTAGE);
		isPercentPref.setOnPreferenceChangeListener(listener);
		isPercentage = isPercentPref.isChecked();
		if(savedInstanceState == null) {
			alarmTypePref.setValueIndex(1);

			for (EditTextPreference p : prefs) {
				p.setText(null);
			}

			updateIntervalPref.setDialogMessage(R.string.pref_summary_update_interval_change);
			updateIntervalPref.setSummary(Conversions.buildMinToDaysSummary(sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_CHANGE, "")));
		} else {
			// Change value pref summary will be updated by updateDependentOnPair()

			String updateInterval = updateIntervalPref.getText();
			if(updateInterval == null || updateInterval.equals("")) {
				updateIntervalPref.setSummary(Conversions.buildMinToDaysSummary(sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_CHANGE, "")));
			} else {
				updateIntervalPref.setSummary(Conversions.buildMinToDaysSummary(updateInterval));
			}
		}
		specificCat.setTitle(alarmTypePref.getEntry());
		alarmTypePref.setSummary(alarmTypePref.getEntry());
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
		if(mBound) {
			if(isPercentage) {
				float percent = (float) change;
				mStorageAndControlService.createAlarm(id, exchange, pair, period, notify, percent);
			} else {
				mStorageAndControlService.createAlarm(id, exchange, pair, period, notify, change);
			}
		} else {
			throw new IOException("PriceChangeAlarmCreationFragment not bound to StorageAndControlService.");
		}
	}
}
