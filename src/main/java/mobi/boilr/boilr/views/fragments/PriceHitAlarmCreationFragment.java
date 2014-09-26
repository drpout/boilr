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
import android.text.InputType;

public class PriceHitAlarmCreationFragment extends AlarmCreationFragment {
	public static final String PREF_KEY_UPPER_VALUE = "pref_key_upper_value";
	public static final String PREF_KEY_LOWER_VALUE = "pref_key_lower_value";

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

	public PriceHitAlarmCreationFragment(int exchangeIndex, int pairIndex) {
		super(exchangeIndex, pairIndex);
	}

	@Override
	protected void updateDependentOnPair() {
		double lastValue = Double.POSITIVE_INFINITY;
		if(mBound) {
			ListPreference exchangePref = (ListPreference) findPreference(PREF_KEY_EXCHANGE);
			Exchange e;
			try {
				e = mStorageAndControlService.getExchange(exchangePref.getEntryValues()[exchangeIndex].toString());
				lastValue = mStorageAndControlService.getLastValue(e, pairs.get(pairIndex));
			} catch(Exception e1) {
				Log.e("Cannot get last value for " + exchangePref.getEntry() + " with pair " + pairs.get(pairIndex), e1);
			}
		} else {
			Log.d("PriceHitAlarmCreationFragment not bound to StorageAndControlService.");
		}
		EditTextPreference[] edits = { (EditTextPreference) findPreference(PREF_KEY_UPPER_VALUE),
				(EditTextPreference) findPreference(PREF_KEY_LOWER_VALUE) };
		if(lastValue != Double.POSITIVE_INFINITY) {
			for(EditTextPreference edit : edits)
				edit.setText(Conversions.formatMaxDecimalPlaces(lastValue));
		}
		String text;
		for(EditTextPreference edit : edits) {
			text = edit.getText();
			if(text != null && !text.equals(""))
				edit.setSummary(text + " " + pairs.get(pairIndex).getExchange());
		}
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		ListPreference alarmTypePref = (ListPreference) findPreference(PREF_KEY_TYPE);
		alarmTypePref.setValueIndex(0);
		alarmTypePref.setSummary(alarmTypePref.getEntry());
		PreferenceCategory category = (PreferenceCategory) findPreference(PREF_KEY_SPECIFIC);
		category.setTitle(alarmTypePref.getEntry());

		EditTextPreference edit = new EditTextPreference(enclosingActivity);
		edit.setKey(PREF_KEY_UPPER_VALUE);
		edit.setTitle(R.string.pref_title_upper_bound);
		edit.setDialogTitle(R.string.pref_title_upper_bound);
		edit.setDefaultValue(null);
		edit.setOnPreferenceChangeListener(listener);
		edit.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		edit.setOrder(0);
		category.addPreference(edit);
		// setText only works after adding the preference.
		edit.setText(null);

		edit = new EditTextPreference(enclosingActivity);
		edit.setKey(PREF_KEY_LOWER_VALUE);
		edit.setTitle(R.string.pref_title_lower_bound);
		edit.setDialogTitle(R.string.pref_title_lower_bound);
		edit.setDefaultValue(null);
		edit.setOnPreferenceChangeListener(listener);
		edit.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		edit.setOrder(1);
		category.addPreference(edit);
		edit.setText(null);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);

		edit = (EditTextPreference) findPreference(PREF_KEY_UPDATE_INTERVAL);
		edit.setDialogMessage(R.string.pref_summary_update_interval_hit);
		edit.setSummary(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, "") + " s");
		edit.setOnPreferenceChangeListener(listener);
		edit.setText(null);
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
			mStorageAndControlService.addAlarm(id, exchange, pair, period, notify, upperBound, lowerBound);
		} else {
			throw new IOException("PriceHitAlarmCreationFragment not bound to StorageAndControlService.");
		}
	}
}
