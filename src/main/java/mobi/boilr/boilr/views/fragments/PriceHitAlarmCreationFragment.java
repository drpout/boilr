package mobi.boilr.boilr.views.fragments;

import java.io.IOException;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.UpperBoundSmallerThanLowerBoundException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

public class PriceHitAlarmCreationFragment extends AlarmCreationFragment {
	public static final String PREF_KEY_UPPER_VALUE = "pref_key_upper_value";
	public static final String PREF_KEY_LOWER_VALUE = "pref_key_lower_value";
	private boolean overwriteBounds = true;

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

	private OnAlarmSettingsPreferenceChangeListener listener = new OnPriceHitSettingsPreferenceChangeListener();

	@Override
	protected void updateDependentOnPair() {
		double lastValue = Double.POSITIVE_INFINITY;
		if(mBound) {
			ListPreference exchangePref = (ListPreference) findPreference(PREF_KEY_EXCHANGE);
			Exchange e;
			try {
				e = mStorageAndControlService.getExchange(exchangePref.getEntryValues()[exchangeIndex].toString());
				lastValue = mStorageAndControlService.getLastValue(e, pairs.get(pairIndex));
			} catch (Exception e1) {
				Log.e("Cannot get last value for " + exchangePref.getEntry() + " with pair " + pairs.get(pairIndex), e1);
			}
		} else {
			Log.d("PriceHitAlarmCreationFragment not bound to StorageAndControlService.");
		}
		EditTextPreference[] edits = { (EditTextPreference) findPreference(PREF_KEY_UPPER_VALUE),
				(EditTextPreference) findPreference(PREF_KEY_LOWER_VALUE) };
		if(overwriteBounds && lastValue != Double.POSITIVE_INFINITY) {
			for (EditTextPreference edit : edits)
				edit.setText(Conversions.formatMaxDecimalPlaces(lastValue));
		}
		if(!overwriteBounds)
			overwriteBounds = true;
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
		EditTextPreference[] edits = { (EditTextPreference) findPreference(PREF_KEY_UPPER_VALUE),
				(EditTextPreference) findPreference(PREF_KEY_LOWER_VALUE) };
		for (EditTextPreference edit : edits) {
			edit.setEnabled(false);
			edit.setText(null);
			edit.setSummary(null);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ListPreference alarmTypePref = (ListPreference) findPreference(PREF_KEY_TYPE);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);
		PreferenceCategory specificCat = (PreferenceCategory) findPreference(PREF_KEY_SPECIFIC);
		Preference pref;
		String key;
		for (int i = 0; i < specificCat.getPreferenceCount(); i++) {
			pref = specificCat.getPreference(i);
			key = pref.getKey();
			if(!key.equals(PREF_KEY_UPPER_VALUE) && !key.equals(PREF_KEY_LOWER_VALUE) && !key.equals(PREF_KEY_UPDATE_INTERVAL)) {
				specificCat.removePreference(pref);
				i--;
			}
		}
		EditTextPreference upperBoundPref = (EditTextPreference) findPreference(PREF_KEY_UPPER_VALUE);
		EditTextPreference lowerBoundPref = (EditTextPreference) findPreference(PREF_KEY_LOWER_VALUE);
		EditTextPreference updateIntervalPref = (EditTextPreference) findPreference(PREF_KEY_UPDATE_INTERVAL);
		EditTextPreference[] prefs = { upperBoundPref,
				lowerBoundPref,
				updateIntervalPref };
		for (Preference p : prefs) {
			p.setOnPreferenceChangeListener(listener);
		}
		if(savedInstanceState == null) {
			alarmTypePref.setValueIndex(0);

			for (EditTextPreference p : prefs) {
				p.setText(null);
			}

			updateIntervalPref.setDialogMessage(R.string.pref_summary_update_interval_hit);
			updateIntervalPref.setSummary(sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, "") + " s");
		} else {
			// Upper and lower bound prefs summary will be updated by updateDependentOnPair()
			overwriteBounds = false;

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
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);
		String updateInterval = ((EditTextPreference) findPreference(PREF_KEY_UPDATE_INTERVAL)).getText();
		// Time is in seconds, convert to milliseconds
		long period = 1000 * Long.parseLong(updateInterval != null ? updateInterval :
				sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, ""));
		String upperBoundString = ((EditTextPreference) findPreference(PREF_KEY_UPPER_VALUE)).getText();
		double upperBound;
		if(upperBoundString == null || upperBoundString.equals(""))
			upperBound = Double.POSITIVE_INFINITY;
		else
			upperBound = Double.parseDouble(upperBoundString);
		String lowerBoundString = ((EditTextPreference) findPreference(PREF_KEY_LOWER_VALUE)).getText();
		double lowerBound;
		if(lowerBoundString == null || lowerBoundString.equals(""))
			lowerBound = Double.NEGATIVE_INFINITY;
		else
			lowerBound = Double.parseDouble(lowerBoundString);
		if(mBound) {
			mStorageAndControlService.createAlarm(id, exchange, pair, period, notify, upperBound, lowerBound);
		} else {
			throw new IOException("PriceHitAlarmCreationFragment not bound to StorageAndControlService.");
		}
	}
}
