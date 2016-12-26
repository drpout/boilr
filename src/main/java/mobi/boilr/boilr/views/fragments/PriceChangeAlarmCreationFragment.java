package mobi.boilr.boilr.views.fragments;

import java.io.IOException;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import mobi.boilr.boilr.domain.AndroidNotifier;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.RollingPriceChangeAlarm;
import mobi.boilr.libpricealarm.TimeFrameSmallerOrEqualUpdateIntervalException;

public class PriceChangeAlarmCreationFragment extends AlarmCreationFragment {
	private class OnPriceChangeSettingsPreferenceChangeListener extends
	OnAlarmSettingsPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			if(key.equals(PREF_KEY_CHANGE_IN_PERCENTAGE)) {
				updateChangeValueSummary((Boolean) newValue);
			} else if(key.equals(PREF_KEY_CHANGE_VALUE)) {
				preference.setSummary(getChangeValueSummary((String) newValue, mIsPercentPref.isChecked()));
			} else if(key.equals(PREF_KEY_TIME_FRAME)) {
				preference.setSummary(Conversions.buildMinToHoursSummary((String) newValue, mEnclosingActivity));
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
		mListener = new OnPriceChangeSettingsPreferenceChangeListener();
		super.onCreate(savedInstanceState);

		removePrefs(changeAlarmPrefsToKeep);
		if(savedInstanceState == null) {
			mChangePref.setText(null);
			setTimeFramePref();
			setUpdateIntervalPref();
		} else {
			// Change value pref summary will be updated by updateDependentOnPair()
			checkAndSetTimeFramePref();
			checkAndSetUpdateIntervalPref();
		}
		mAlarmTypePref.setValueIndex(1);
		mSpecificCat.setTitle(mAlarmTypePref.getEntry());
		mAlarmTypePref.setSummary(mAlarmTypePref.getEntry());
	}

	private void setTimeFramePref() {
		String timeFrame = mSharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_TIME_FRAME, "");
		mTimeFramePref.setText(timeFrame);
		mTimeFramePref.setSummary(Conversions.buildMinToHoursSummary(timeFrame, mEnclosingActivity));
	}

	private void checkAndSetTimeFramePref() {
		String timeFrame = mTimeFramePref.getText();
		if(timeFrame == null || timeFrame.equals("")) {
			setTimeFramePref();
		} else {
			mTimeFramePref.setSummary(Conversions.buildMinToHoursSummary(timeFrame, mEnclosingActivity));
		}
	}

	@Override
	public Alarm makeAlarm(int id, Exchange exchange, Pair pair, AndroidNotifier notifier)
			throws TimeFrameSmallerOrEqualUpdateIntervalException, IOException {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mEnclosingActivity);
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
		String updateIntervalString = mUpdateIntervalPref.getText();
		// Time is in seconds, convert to milliseconds
		updateInterval = 1000 * Long.parseLong(updateIntervalString != null ? updateIntervalString : mSharedPrefs.getString(
				SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL, ""));
		Alarm ret;
		if(mIsPercentPref.isChecked()) {
			float percent = (float) change;
			ret = new RollingPriceChangeAlarm(id, exchange, pair, updateInterval, notifier,
					mSnoozeOnRetracePref.isChecked(), percent, timeFrame);
		} else {
			ret = new RollingPriceChangeAlarm(id, exchange, pair, updateInterval, notifier,
					mSnoozeOnRetracePref.isChecked(), change, timeFrame);
		}
		return ret;
	}
}
