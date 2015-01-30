package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.PriceChangeAlarm;
import mobi.boilr.libpricealarm.PriceSpikeAlarm;
import mobi.boilr.libpricealarm.TimeFrameSmallerOrEqualUpdateIntervalException;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;

public class PriceChangeAlarmSettingsFragment extends AlarmSettingsFragment {
	private PriceChangeAlarm priceChangeAlarm;

	private class OnPriceChangeSettingsPreferenceChangeListener extends
			OnAlarmSettingsPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			if(key.equals(PREF_KEY_CHANGE_IN_PERCENTAGE)) {
				isPercentage = (Boolean) newValue;
				if(isPercentage) {
					priceChangeAlarm.setPercent((float) priceChangeAlarm.getChange());
				} else {
					priceChangeAlarm.setChange(priceChangeAlarm.getPercent());
				}
				updateChangeValueText();
				updateChangeValueSummary();
			} else if(key.equals(PREF_KEY_CHANGE_VALUE)) {
				if(isPercentage) {
					priceChangeAlarm.setPercent(Float.parseFloat((String) newValue));
				} else {
					priceChangeAlarm.setChange(Double.parseDouble((String) newValue));
				}
				preference.setSummary(getChangeValueSummary((String) newValue));
			} else if(key.equals(PREF_KEY_TIME_FRAME)) {
				long timeFrame = Long.parseLong((String) newValue) * Conversions.MILIS_IN_MINUTE;
				try {
					if(isSpikeAlert) {
						((PriceSpikeAlarm) priceChangeAlarm).setTimeFrame(timeFrame);
					} else {
						priceChangeAlarm.setPeriod(timeFrame);
						if(mBound) {
							mStorageAndControlService.resetAlarmPeriod(priceChangeAlarm);
						} else {
							Log.e(enclosingActivity.getString(R.string.not_bound, "PriceChangeAlarmSettingsFragment"));
						}
					}
					preference.setSummary(Conversions.buildMinToDaysSummary((String) newValue, enclosingActivity));
				} catch(TimeFrameSmallerOrEqualUpdateIntervalException e) {
					String msg = enclosingActivity.getString(R.string.failed_save_alarm) + " "
						+ enclosingActivity.getString(R.string.frame_must_longer_interval);
					Log.e(msg, e);
					Toast.makeText(enclosingActivity, msg, Toast.LENGTH_LONG).show();
				}
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			if(mBound) {
				mStorageAndControlService.replaceAlarmDB(priceChangeAlarm);
			} else {
				Log.e(enclosingActivity.getString(R.string.not_bound, "PriceChangeAlarmSettingsFragment"));
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
		listener = new OnPriceChangeSettingsPreferenceChangeListener();
		super.onCreate(savedInstanceState);

		removePrefs(changeAlarmPrefsToKeep);
		// Change value pref summary will be updated by updateDependentOnPair()
		alarmTypePref.setValueIndex(1);
		specificCat.setTitle(alarmTypePref.getEntry());
		alarmTypePref.setSummary(alarmTypePref.getEntry());
	}

	private void updateChangeValueText() {
		String formated;
		if(isPercentage) {
			formated = Conversions.formatMaxDecimalPlaces(priceChangeAlarm.getPercent());
		} else {
			formated = Conversions.formatMaxDecimalPlaces(priceChangeAlarm.getChange());
		}
		changePref.setText(formated);
	}

	@Override
	protected void initializePreferences() {
		priceChangeAlarm = (PriceChangeAlarm) alarm;
		long minPeriod, secondsPeriod = 30;
		isSpikeAlert = priceChangeAlarm instanceof PriceSpikeAlarm;
		if(isSpikeAlert) {
			PriceSpikeAlarm priceSpike = (PriceSpikeAlarm) priceChangeAlarm;
			minPeriod = priceSpike.getTimeFrame() / Conversions.MILIS_IN_MINUTE;
			secondsPeriod = priceSpike.getPeriod() / 1000;
			updateIntervalPref.setSummary(secondsPeriod + " s");
		} else {
			minPeriod = alarm.getPeriod() / Conversions.MILIS_IN_MINUTE;
			specificCat.removePreference(updateIntervalPref);
		}
		timeFramePref.setSummary(Conversions.buildMinToDaysSummary(String.valueOf(minPeriod), enclosingActivity));
		spikeAlertPref.setEnabled(false);
		if(!recoverSavedInstance) {
			spikeAlertPref.setChecked(isSpikeAlert);
			isPercentage = priceChangeAlarm.isPercent();
			isPercentPref.setChecked(isPercentage);
			updateChangeValueText();
			timeFramePref.setText(String.valueOf(minPeriod));
			if(isSpikeAlert) {
				updateIntervalPref.setText(String.valueOf(secondsPeriod));
			}
		}
	}
}
