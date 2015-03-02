package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.IconToast;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import mobi.boilr.libpricealarm.UpperLimitSmallerOrEqualLowerLimitException;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;

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
					handleLimitsExceptions(e, mEnclosingActivity);
				}
			} else if(key.equals(PREF_KEY_LOWER_VALUE)) {
				try {
					priceHitAlarm.setLowerLimit(Double.parseDouble((String) newValue));
					preference.setSummary(newValue + " " + alarm.getPair().getExchange());
				} catch(UpperLimitSmallerOrEqualLowerLimitException e) {
					handleLimitsExceptions(e, mEnclosingActivity);
				}
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			if(mBound) {
				mStorageAndControlService.replaceAlarmDB(priceHitAlarm);
			} else {
				Log.e(mEnclosingActivity.getString(R.string.not_bound, "PriceHitAlarmSettingsFragment"));
			}
			return true;
		}
	}

	public static void handleLimitsExceptions(UpperLimitSmallerOrEqualLowerLimitException e, Context context) {
		String msg = context.getString(R.string.failed_save_alarm) + " " + context.getString(R.string.upper_must_larger_lower);
		Log.e(msg, e);
		IconToast.warning(context, msg);
		/*
		 * The following does not work. We would have to use a SharedPreferences
		 * instead of Preference: http://stackoverflow.com/a/20598084
		 * ((EditTextPreference) preference)
		 * .getEditText().setText(Conversions.formatMaxDecimalPlaces
		 * (limitValue));
		 */
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mListener = new OnPriceHitSettingsPreferenceChangeListener();
		super.onCreate(savedInstanceState);

		removePrefs(hitAlarmPrefsToKeep);
		// Upper and lower limit prefs summary will be updated by updateDependentOnPair()
		mAlarmTypePref.setValueIndex(0);
		mSpecificCat.setTitle(mAlarmTypePref.getEntry());
		mAlarmTypePref.setSummary(mAlarmTypePref.getEntry());
	}

	@Override
	protected void updateDependentOnPair() {
		super.updateDependentOnPair();
		EditTextPreference[] edits = { mUpperLimitPref, mLowerLimitPref };
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
		mUpdateIntervalPref.setSummary(mEnclosingActivity.getString(R.string.seconds_abbreviation, String.valueOf(secondsPeriod)));
		if(!mRecoverSavedInstance) {
			String formated = Conversions.formatMaxDecimalPlaces(priceHitAlarm.getUpperLimit());
			mUpperLimitPref.setText(formated);
			formated = Conversions.formatMaxDecimalPlaces(priceHitAlarm.getLowerLimit());
			mLowerLimitPref.setText(formated);
			mUpdateIntervalPref.setText(String.valueOf(secondsPeriod));
		}
	}
}
