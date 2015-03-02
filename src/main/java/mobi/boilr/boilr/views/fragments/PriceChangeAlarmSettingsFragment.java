package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.IconToast;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.RollingPriceChangeAlarm;
import mobi.boilr.libpricealarm.TimeFrameSmallerOrEqualUpdateIntervalException;
import android.os.Bundle;
import android.preference.Preference;

public class PriceChangeAlarmSettingsFragment extends AlarmSettingsFragment {
	private RollingPriceChangeAlarm priceChangeAlarm;

	private class OnPriceChangeSettingsPreferenceChangeListener extends
			OnAlarmSettingsPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			if(key.equals(PREF_KEY_CHANGE_IN_PERCENTAGE)) {
				mIsPercentage = (Boolean) newValue;
				if(mIsPercentage) {
					priceChangeAlarm.setPercent((float) priceChangeAlarm.getChange());
				} else {
					priceChangeAlarm.setChange(priceChangeAlarm.getPercent());
				}
				updateChangeValueText();
				updateChangeValueSummary();
			} else if(key.equals(PREF_KEY_CHANGE_VALUE)) {
				if(mIsPercentage) {
					priceChangeAlarm.setPercent(Float.parseFloat((String) newValue));
				} else {
					priceChangeAlarm.setChange(Double.parseDouble((String) newValue));
				}
				preference.setSummary(getChangeValueSummary((String) newValue));
			} else if(key.equals(PREF_KEY_TIME_FRAME)) {
				long timeFrame = Long.parseLong((String) newValue) * Conversions.MILIS_IN_MINUTE;
				try {
					priceChangeAlarm.setTimeFrame(timeFrame);
					preference.setSummary(Conversions.buildMinToHoursSummary((String) newValue, mEnclosingActivity));
				} catch(TimeFrameSmallerOrEqualUpdateIntervalException e) {
					String msg = mEnclosingActivity.getString(R.string.failed_save_alarm) + " "
						+ mEnclosingActivity.getString(R.string.frame_must_longer_interval);
					Log.e(msg, e);
					IconToast.warning(mEnclosingActivity, msg);
				}
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			if(mBound) {
				mStorageAndControlService.replaceAlarmDB(priceChangeAlarm);
			} else {
				Log.e(mEnclosingActivity.getString(R.string.not_bound, "PriceChangeAlarmSettingsFragment"));
			}
			return true;
		}
	}

	@Override
	protected void updateDependentOnPair() {
		super.updateDependentOnPair();
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
		// Change value pref summary will be updated by updateDependentOnPair()
		mAlarmTypePref.setValueIndex(1);
		mSpecificCat.setTitle(mAlarmTypePref.getEntry());
		mAlarmTypePref.setSummary(mAlarmTypePref.getEntry());
	}

	private void updateChangeValueText() {
		String formated;
		if(mIsPercentage) {
			formated = Conversions.formatMaxDecimalPlaces(priceChangeAlarm.getPercent());
		} else {
			formated = Conversions.formatMaxDecimalPlaces(priceChangeAlarm.getChange());
		}
		mChangePref.setText(formated);
	}

	@Override
	protected void initializePreferences() {
		priceChangeAlarm = (RollingPriceChangeAlarm) alarm;
		long minPeriod = priceChangeAlarm.getTimeFrame() / Conversions.MILIS_IN_MINUTE;
		long secondsPeriod = priceChangeAlarm.getPeriod() / 1000;
		mUpdateIntervalPref.setSummary(mEnclosingActivity.getString(R.string.seconds_abbreviation, String.valueOf(secondsPeriod)));
		mTimeFramePref.setSummary(Conversions.buildMinToHoursSummary(String.valueOf(minPeriod), mEnclosingActivity));
		if(!mRecoverSavedInstance) {
			mIsPercentage = priceChangeAlarm.isPercent();
			mIsPercentPref.setChecked(mIsPercentage);
			updateChangeValueText();
			mTimeFramePref.setText(String.valueOf(minPeriod));
			mUpdateIntervalPref.setText(String.valueOf(secondsPeriod));
		}
	}
}
