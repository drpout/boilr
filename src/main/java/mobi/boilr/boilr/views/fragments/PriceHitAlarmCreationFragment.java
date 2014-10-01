package mobi.boilr.boilr.views.fragments;

import java.io.IOException;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.UpperBoundSmallerThanLowerBoundException;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;

public class PriceHitAlarmCreationFragment extends AlarmCreationFragment {

	private class OnPriceHitSettingsPreferenceChangeListener extends
	OnAlarmSettingsPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			if(key.equals(PREF_KEY_UPPER_VALUE) || key.equals(PREF_KEY_LOWER_VALUE)) {
				preference.setSummary(newValue + " " + pairs.get(pairIndex).getExchange());
			} else if(key.equals(PREF_KEY_UPDATE_INTERVAL)) {
				preference.setSummary(newValue + " s");
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			return true;
		}
	}

	@Override
	protected void updateDependentOnPair() {
		EditTextPreference[] edits = { upperBoundPref, lowerBoundPref };
		if(!recoverSavedInstance && lastValue != Double.POSITIVE_INFINITY) {
			for (EditTextPreference edit : edits)
				edit.setText(Conversions.formatMaxDecimalPlaces(lastValue));
		}
		String text;
		for (EditTextPreference edit : edits) {
			edit.setEnabled(true);
			text = edit.getText();
			if(text != null && !text.equals(""))
				edit.setSummary(text + " " + pairs.get(pairIndex).getExchange());
		}
	}

	@Override
	protected void disableDependentOnPair() {
		disableDependentOnPairHitAlarm();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		listener = new OnPriceHitSettingsPreferenceChangeListener();
		super.onCreate(savedInstanceState);

		removePrefs(hitAlarmPrefsToKeep);
		if(savedInstanceState == null) {
			alarmTypePref.setValueIndex(0);
			EditTextPreference[] prefs = { upperBoundPref, lowerBoundPref, updateIntervalPref };
			for (EditTextPreference p : prefs) {
				p.setText(null);
			}
			updateIntervalPref.setDialogMessage(R.string.pref_summary_update_interval_hit);
			updateIntervalPref.setSummary(sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, "") + " s");
		} else {
			// Upper and lower bound prefs summary will be updated by updateDependentOnPair()
			String updateInterval = updateIntervalPref.getText();
			if(updateInterval == null || updateInterval.equals("")) {
				updateIntervalPref.setSummary(sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, "") + " s");
			} else {
				updateIntervalPref.setSummary(updateInterval + " s");
			}
		}
		specificCat.setTitle(alarmTypePref.getEntry());
		alarmTypePref.setSummary(alarmTypePref.getEntry());
	}

	@Override
	public void makeAlarm(int id, Exchange exchange, Pair pair, AndroidNotify notify)
			throws UpperBoundSmallerThanLowerBoundException, IOException {
		String updateInterval = updateIntervalPref.getText();
		// Time is in seconds, convert to milliseconds
		long period = 1000 * Long.parseLong(updateInterval != null ? updateInterval :
			sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, ""));
		String upperBoundString = upperBoundPref.getText();
		double upperBound;
		if(upperBoundString == null || upperBoundString.equals(""))
			upperBound = Double.POSITIVE_INFINITY;
		else
			upperBound = Double.parseDouble(upperBoundString);
		String lowerBoundString = lowerBoundPref.getText();
		double lowerBound;
		if(lowerBoundString == null || lowerBoundString.equals(""))
			lowerBound = Double.NEGATIVE_INFINITY;
		else
			lowerBound = Double.parseDouble(lowerBoundString);
		if(mBound) {
			mStorageAndControlService.createAlarm(id, exchange, pair, period, notify, upperBound, lowerBound);
		} else {
			throw new IOException(enclosingActivity.getString(R.string.not_bound, "PriceHitAlarmCreationFragment"));
		}
	}
}
