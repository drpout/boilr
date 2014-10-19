package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.PriceChangeAlarm;
import android.os.Bundle;
import android.preference.Preference;

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
			} else if(key.equals(PREF_KEY_UPDATE_INTERVAL)) {
				preference.setSummary(Conversions.buildMinToDaysSummary((String) newValue, enclosingActivity));
				priceChangeAlarm.setPeriod(Long.parseLong((String) newValue) * Conversions.MILIS_IN_MINUTE);
				if(mBound) {
					mStorageAndControlService.restartAlarm(priceChangeAlarm);
				} else {
					Log.d(enclosingActivity.getString(R.string.not_bound, "PriceChangeAlarmSettingsFragment"));
				}
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			if(mBound) {
				mStorageAndControlService.replaceAlarmDB(priceChangeAlarm);
			} else {
				Log.d(enclosingActivity.getString(R.string.not_bound, "PriceChangeAlarmSettingsFragment"));
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

		if(savedInstanceState == null) {
			alarmTypePref.setValueIndex(1);
			updateIntervalPref.setTitle(R.string.pref_title_time_frame);
			updateIntervalPref.setDialogMessage(R.string.pref_summary_update_interval_change);
		}
		// Change value pref summary will be updated by updateDependentOnPair()
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
		long minPeriod = alarm.getPeriod() / Conversions.MILIS_IN_MINUTE;
		updateIntervalPref.setSummary(Conversions.buildMinToDaysSummary(String.valueOf(minPeriod), enclosingActivity));
		if(!recoverSavedInstance) {
			isPercentage = priceChangeAlarm.isPercent();
			isPercentPref.setChecked(isPercentage);
			updateChangeValueText();
			updateIntervalPref.setText(String.valueOf(minPeriod));
		}
	}
}
