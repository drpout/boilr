package mobi.boilr.boilr.views.fragments;

import java.io.IOException;

import mobi.boilr.boilr.domain.AndroidNotifier;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.RollingPriceChangeAlarm;
import mobi.boilr.libpricealarm.TimeFrameSmallerOrEqualUpdateIntervalException;
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
			} else if(key.equals(PREF_KEY_TIME_FRAME)) {
				preference.setSummary(Conversions.buildMinToHoursSummary((String) newValue, enclosingActivity));
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
			changePref.setText(null);
			setTimeFramePref();
			setUpdateIntervalPref();
		} else {
			// Change value pref summary will be updated by updateDependentOnPair()
			checkAndSetTimeFramePref();
			checkAndSetUpdateIntervalPref();
		}
		alarmTypePref.setValueIndex(1);
		specificCat.setTitle(alarmTypePref.getEntry());
		alarmTypePref.setSummary(alarmTypePref.getEntry());
	}

	private void setTimeFramePref() {
		String timeFrame = sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_TIME_FRAME, "");
		timeFramePref.setText(timeFrame);
		timeFramePref.setSummary(Conversions.buildMinToHoursSummary(timeFrame, enclosingActivity));
	}

	private void checkAndSetTimeFramePref() {
		String timeFrame = timeFramePref.getText();
		if(timeFrame == null || timeFrame.equals("")) {
			setTimeFramePref();
		} else {
			timeFramePref.setSummary(Conversions.buildMinToHoursSummary(timeFrame, enclosingActivity));
		}
	}

	@Override
	public Alarm makeAlarm(int id, Exchange exchange, Pair pair, AndroidNotifier notifier)
			throws TimeFrameSmallerOrEqualUpdateIntervalException, IOException {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);
		String timeFrameString = ((EditTextPreference) findPreference(PREF_KEY_TIME_FRAME)).getText();
		// Time is in minutes, convert to milliseconds
		long timeFrame = Conversions.MILIS_IN_MINUTE
			* Long.parseLong(timeFrameString != null ? timeFrameString :
				sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_TIME_FRAME, ""));
		String changeValueString = ((EditTextPreference) findPreference(PREF_KEY_CHANGE_VALUE)).getText();
		double change;
		if(changeValueString == null || changeValueString.equals(""))
			change = Double.POSITIVE_INFINITY;
		else
			change = Double.parseDouble(changeValueString);
		long updateInterval = 3000;
		String updateIntervalString = updateIntervalPref.getText();
		// Time is in seconds, convert to milliseconds
		updateInterval = 1000 * Long.parseLong(updateIntervalString != null ? updateIntervalString : sharedPrefs.getString(
				SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL, ""));
		Alarm ret;
		if(isPercentage) {
			float percent = (float) change;
			ret = new RollingPriceChangeAlarm(id, exchange, pair, updateInterval, notifier, percent, timeFrame);
		} else {
			ret = new RollingPriceChangeAlarm(id, exchange, pair, updateInterval, notifier, change, timeFrame);
		}
		return ret;
	}
}
