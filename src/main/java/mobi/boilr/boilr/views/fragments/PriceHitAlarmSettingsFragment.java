package mobi.boilr.boilr.views.fragments;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.PriceHitAlarm;
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
				} catch (Exception e) {
					String msg = enclosingActivity.getString(R.string.cannot_set_limit);
					Log.e(msg, e);
					Toast.makeText(enclosingActivity, msg + " " + e.getMessage(), Toast.LENGTH_LONG).show();
					EditTextPreference edit = (EditTextPreference) preference;
					edit.setText(Conversions.formatMaxDecimalPlaces(priceHitAlarm.getUpperLimit()));
				}
			} else if(key.equals(PREF_KEY_LOWER_VALUE)) {
				try {
					priceHitAlarm.setLowerLimit(Double.parseDouble((String) newValue));
					preference.setSummary(newValue + " " + alarm.getPair().getExchange());
				} catch (Exception e) {
					String msg = enclosingActivity.getString(R.string.cannot_set_limit);
					Log.e(msg, e);
					Toast.makeText(enclosingActivity, msg + " " + e.getMessage(), Toast.LENGTH_LONG).show();
					EditTextPreference edit = (EditTextPreference) preference;
					edit.setText(Conversions.formatMaxDecimalPlaces(priceHitAlarm.getLowerLimit()));
				}
			} else if(key.equals(PREF_KEY_UPDATE_INTERVAL)) {
				preference.setSummary(newValue + " s");
				alarm.setPeriod(1000 * Long.parseLong((String) newValue));
				if(mBound) {
					mStorageAndControlService.restartAlarm(priceHitAlarm);
				} else {
					Log.d(enclosingActivity.getString(R.string.not_bound, "PriceHitAlarmSettingsFragment"));
				}
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			if(mBound) {
				mStorageAndControlService.replaceAlarmDB(priceHitAlarm);
			} else {
				Log.d(enclosingActivity.getString(R.string.not_bound, "PriceHitAlarmSettingsFragment"));
			}
			return true;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		listener = new OnPriceHitSettingsPreferenceChangeListener();
		super.onCreate(savedInstanceState);

		removePrefs(hitAlarmPrefsToKeep);

		if(savedInstanceState == null) {
			alarmTypePref.setValueIndex(0);
			updateIntervalPref.setDialogMessage(R.string.pref_summary_update_interval_hit);
		}
		// Upper and lower limit prefs summary will be updated by updateDependentOnPair()
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
		updateIntervalPref.setSummary(secondsPeriod + " s");
		if(!recoverSavedInstance) {
			String formated = Conversions.formatMaxDecimalPlaces(priceHitAlarm.getUpperLimit());
			upperLimitPref.setText(formated);
			formated = Conversions.formatMaxDecimalPlaces(priceHitAlarm.getLowerLimit());
			lowerLimitPref.setText(formated);
			updateIntervalPref.setText(String.valueOf(secondsPeriod));
		}
	}
}
