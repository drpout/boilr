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
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

public class PriceChangeAlarmCreationFragment extends AlarmCreationFragment {
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

	@Override
	protected void updateDependentOnPair() {
		updateDependentOnPairChangeAlarm();
	}

	@Override
	protected void disableDependentOnPair() {
		disableDependentOnPairChangeAlarm();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		listener = new OnPriceChangeSettingsPreferenceChangeListener();
		super.onCreate(savedInstanceState);

		removePrefs(changeAlarmPrefsToKeep);
		isPercentage = isPercentPref.isChecked();
		if(savedInstanceState == null) {
			alarmTypePref.setValueIndex(1);
			EditTextPreference[] prefs = { changePref, updateIntervalPref };
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
		long period = Conversions.MILIS_IN_MINUTE * Long.parseLong(updateInterval != null ? updateInterval :
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
			throw new IOException(enclosingActivity.getString(R.string.not_bound, "PriceChangeAlarmCreationFragment"));
		}
	}
}
