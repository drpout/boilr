package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import mobi.boilr.libpricealarm.UpperLimitSmallerOrEqualLowerLimitException;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.widget.Toast;

public class PriceHitAlarmSettingsFragment extends AlarmSettingsFragment {
	private PriceHitAlarm priceHitAlarm;

	private class OnPriceHitSettingsPreferenceChangeListener extends
	OnAlarmSettingsPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			if(key.equals(PREF_KEY_UPPER_VALUE)) {
				try {
					priceHitAlarm.setUpperLimit(Double.parseDouble((String) newValue));
					preference.setSummary(newValue + " " + alarm.getPair().getExchange());
				} catch(UpperLimitSmallerOrEqualLowerLimitException e) {
					handleLimitsExceptions(e);
				}
			} else if(key.equals(PREF_KEY_LOWER_VALUE)) {
				try {
					priceHitAlarm.setLowerLimit(Double.parseDouble((String) newValue));
					preference.setSummary(newValue + " " + alarm.getPair().getExchange());
				} catch(UpperLimitSmallerOrEqualLowerLimitException e) {
					handleLimitsExceptions(e);
				}
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			if(mBound) {
				mStorageAndControlService.replaceAlarmDB(priceHitAlarm);
			} else {
				Log.e(enclosingActivity.getString(R.string.not_bound, "PriceHitAlarmSettingsFragment"));
			}
			return true;
		}

		private void handleLimitsExceptions(UpperLimitSmallerOrEqualLowerLimitException e) {
			String msg = enclosingActivity.getString(R.string.failed_save_alarm) + " "
				+ enclosingActivity.getString(R.string.upper_must_larger_lower);
			Log.e(msg, e);
			Toast.makeText(enclosingActivity, msg, Toast.LENGTH_LONG).show();
			/*
			 * The following does not work. We would have to use a
			 * SharedPreferences instead of Preference:
			 * http://stackoverflow.com/a/20598084
			 * ((EditTextPreference) preference)
			 * .getEditText().setText(Conversions.formatMaxDecimalPlaces
			 * (limitValue));
			 */
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		listener = new OnPriceHitSettingsPreferenceChangeListener();
		super.onCreate(savedInstanceState);

		removePrefs(hitAlarmPrefsToKeep);
		// Upper and lower limit prefs summary will be updated by updateDependentOnPair()
		alarmTypePref.setValueIndex(0);
		specificCat.setTitle(alarmTypePref.getEntry());
		alarmTypePref.setSummary(alarmTypePref.getEntry());
	}

	@Override
	protected void updateDependentOnPair() {
		super.updateDependentOnPair();
		EditTextPreference[] edits = { upperLimitPref, lowerLimitPref };
		String text;
		for (EditTextPreference edit : edits) {
			edit.setEnabled(true);
			text = edit.getText();
			if(text != null && !text.equals(""))
				edit.setSummary(text + " " + alarm.getPair().getExchange());
		}
	}

	@Override
	protected void disableDependentOnPair() {
		disableDependentOnPairHitAlarm();
	}

	@Override
	protected void initializePreferences() {
		priceHitAlarm = (PriceHitAlarm) alarm;
		long secondsPeriod = alarm.getPeriod() / 1000;
		updateIntervalPref.setSummary(enclosingActivity.getString(R.string.seconds_abbreviation, secondsPeriod));
		if(!recoverSavedInstance) {
			String formated = Conversions.formatMaxDecimalPlaces(priceHitAlarm.getUpperLimit());
			upperLimitPref.setText(formated);
			formated = Conversions.formatMaxDecimalPlaces(priceHitAlarm.getLowerLimit());
			lowerLimitPref.setText(formated);
			updateIntervalPref.setText(String.valueOf(secondsPeriod));
		}
	}
}
