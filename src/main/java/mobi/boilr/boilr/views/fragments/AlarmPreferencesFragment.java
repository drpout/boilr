package mobi.boilr.boilr.views.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import android.app.Activity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.widget.Toast;

public abstract class AlarmPreferencesFragment extends PreferenceFragment {
	protected static final String PREF_KEY_GENERIC = "pref_key_generic";
	protected static final String PREF_KEY_SPECIFIC = "pref_key_specific";
	protected static final String PREF_KEY_ALERTS = "pref_key_alerts";
	protected static final String PREF_VALUE_PRICE_HIT = "price_hit";
	protected static final String PREF_VALUE_PRICE_CHANGE = "price_change";
	protected static final String PREF_KEY_EXCHANGE = "pref_key_exchange";
	protected static final String PREF_KEY_TYPE = "pref_key_type";
	protected static final String PREF_KEY_PAIR = "pref_key_pair";
	protected static final String PREF_KEY_ALARM_ALERT_SOUND = "pref_key_alarm_alert_sound";
	protected static final String PREF_KEY_ALARM_ALERT_TYPE = "pref_key_alarm_alert_type";
	protected static final String PREF_KEY_ALARM_VIBRATE = "pref_key_alarm_vibrate";
	protected static final String PREF_KEY_UPDATE_INTERVAL = "pref_key_update_interval";
	protected static final String PREF_KEY_LAST_VALUE = "pref_key_last_value";
	protected static final String PREF_KEY_UPPER_VALUE = "pref_key_upper_value";
	protected static final String PREF_KEY_LOWER_VALUE = "pref_key_lower_value";
	protected static final String PREF_KEY_CHANGE_IN_PERCENTAGE = "pref_key_change_in_percentage";
	protected static final String PREF_KEY_CHANGE_VALUE = "pref_key_change_value";
	protected static final List<String> hitAlarmPrefsToKeep = Arrays.asList(PREF_KEY_UPPER_VALUE, PREF_KEY_LOWER_VALUE, PREF_KEY_UPDATE_INTERVAL);
	protected static final List<String> changeAlarmPrefsToKeep = Arrays.asList(PREF_KEY_CHANGE_IN_PERCENTAGE, PREF_KEY_CHANGE_VALUE, PREF_KEY_UPDATE_INTERVAL);
	protected Activity enclosingActivity;
	protected int exchangeIndex = 0, pairIndex = 0;
	protected List<Pair> pairs = new ArrayList<Pair>();
	protected double lastValue = Double.POSITIVE_INFINITY;
	protected boolean isPercentage = false, recoverSavedInstance = false;
	protected SharedPreferences sharedPrefs;
	protected PreferenceCategory specificCat;
	protected ListPreference exchangeListPref, pairListPref, alarmTypePref, alarmAlertTypePref;
	protected RingtonePreference alertSoundPref;
	protected CheckBoxPreference isPercentPref, vibratePref;
	protected EditTextPreference lastValuePref, upperBoundPref, lowerBoundPref, updateIntervalPref,
			changePref;
	protected OnPreferenceChangeListener listener;
	protected StorageAndControlService mStorageAndControlService;
	protected boolean mBound = false;
	protected ServiceConnection mStorageAndControlServiceConnection;
	protected Intent serviceIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null)
			recoverSavedInstance = true;
		enclosingActivity = getActivity();
		serviceIntent = new Intent(enclosingActivity, StorageAndControlService.class);
		addPreferencesFromResource(R.xml.alarm_settings);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(enclosingActivity);
		exchangeListPref = (ListPreference) findPreference(PREF_KEY_EXCHANGE);
		specificCat = (PreferenceCategory) findPreference(PREF_KEY_SPECIFIC);
		pairListPref = (ListPreference) findPreference(PREF_KEY_PAIR);
		lastValuePref = (EditTextPreference) findPreference(PREF_KEY_LAST_VALUE);
		alarmTypePref = (ListPreference) findPreference(PREF_KEY_TYPE);
		alarmAlertTypePref = (ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE);
		alertSoundPref = (RingtonePreference) findPreference(PREF_KEY_ALARM_ALERT_SOUND);
		vibratePref = (CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE);
		upperBoundPref = (EditTextPreference) findPreference(PREF_KEY_UPPER_VALUE);
		lowerBoundPref = (EditTextPreference) findPreference(PREF_KEY_LOWER_VALUE);
		isPercentPref = (CheckBoxPreference) findPreference(PREF_KEY_CHANGE_IN_PERCENTAGE);
		changePref = (EditTextPreference) findPreference(PREF_KEY_CHANGE_VALUE);
		updateIntervalPref = (EditTextPreference) findPreference(PREF_KEY_UPDATE_INTERVAL);

		Preference[] prefs = { exchangeListPref, pairListPref, lastValuePref,
				alarmTypePref, upperBoundPref, lowerBoundPref,
				isPercentPref, changePref, updateIntervalPref,
				alarmAlertTypePref, alertSoundPref, vibratePref };
		for (Preference pref : prefs) {
			pref.setOnPreferenceChangeListener(listener);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		enclosingActivity.unbindService(mStorageAndControlServiceConnection);
	}

	protected void updatePairsList(String exchangeCode, String exchangeName, String pairString) {
		try {
			if(!mBound) {
				throw new IOException(enclosingActivity.getString(R.string.not_bound, "AlarmPreferencesFragment"));
			}
			mStorageAndControlService.getPairs(this, exchangeCode, exchangeName, pairString);
		} catch (Exception e) {
			warnFailedRetrievePairs(exchangeName, e);
		}

	}

	private void warnFailedRetrievePairs(String exchangeName, Exception e) {
		String message = enclosingActivity.getString(R.string.couldnt_retrieve_pairs, exchangeName);
		Toast.makeText(enclosingActivity, message, Toast.LENGTH_LONG).show();
		Log.e(message, e);
		pairListPref.setEntries(null);
		pairListPref.setEntryValues(null);
		pairListPref.setSummary(null);
		pairListPref.setEnabled(false);
		disableDependentOnPairAux();
		lastValue = Double.POSITIVE_INFINITY;
	}

	public void updatePairsListCallback(String exchangeName, String pairString, List<Pair> pairs) {
		this.pairs = pairs;
		try {
			if(pairs == null)
				throw new Exception("Pairs is null.");
			CharSequence[] sequence = new CharSequence[pairs.size()];
			CharSequence[] ids = new CharSequence[pairs.size()];
			for (int i = 0; i < pairs.size(); i++) {
				sequence[i] = pairs.get(i).toString();
				if(pairString != null && sequence[i].equals(pairString))
					pairIndex = i;
				ids[i] = String.valueOf(i);
			}
			pairListPref.setEnabled(true);
			pairListPref.setEntries(sequence);
			pairListPref.setEntryValues(ids);
			pairListPref.setSummary(sequence[pairIndex]);
			pairListPref.setValueIndex(pairIndex);
			updateDependentOnPairAux();
		} catch (Exception e) {
			warnFailedRetrievePairs(exchangeName, e);
		}
	}

	protected void updateDependentOnPairAux() {
		if(recoverSavedInstance) {
			lastValuePref.setSummary(lastValuePref.getText());
			updateDependentOnPair();
			recoverSavedInstance = false;
		} else {
			try {
				if(!mBound) {
					throw new IOException(enclosingActivity.getString(R.string.not_bound, "AlarmPreferencesFragment"));
				}
				Exchange exchange = mStorageAndControlService.getExchange(exchangeListPref.getEntryValues()[exchangeIndex].toString());
				mStorageAndControlService.getLastValue(this, exchange, pairs.get(pairIndex));
			} catch (Exception e) {
				warnFailedRetrieveLastValue(e);
			}
		}
	}

	private void warnFailedRetrieveLastValue(Exception e) {
		String message = enclosingActivity.getString(R.string.couldnt_retrieve_last_value, exchangeListPref.getEntry(), pairs.get(pairIndex).toString());
		Toast.makeText(enclosingActivity, message, Toast.LENGTH_LONG).show();
		Log.e(message, e);
		lastValue = Double.POSITIVE_INFINITY;
		lastValuePref.setEnabled(false);
		lastValuePref.setText(null);
		lastValuePref.setSummary(null);
		updateDependentOnPair();
	}

	public void getLastValueCallback(Double result) {
		try {
			if(result == null)
				throw new Exception("Last value is null.");
			lastValue = result;
			lastValuePref.setEnabled(true);
			String lastValueString = Conversions.formatMaxDecimalPlaces(lastValue) + " " + pairs.get(pairIndex).getExchange();
			lastValuePref.setText(lastValueString);
			lastValuePref.setSummary(lastValueString);
			updateDependentOnPair();
		} catch (Exception e) {
			warnFailedRetrieveLastValue(e);
		}
	}

	protected abstract void updateDependentOnPair();

	protected void updateDependentOnPairChangeAlarm() {
		changePref.setEnabled(true);
		updateChangeValueSummary();
	}

	protected void disableDependentOnPairAux() {
		lastValue = Double.POSITIVE_INFINITY;
		lastValuePref.setEnabled(false);
		lastValuePref.setText(null);
		lastValuePref.setSummary(null);
		disableDependentOnPair();
	}

	protected abstract void disableDependentOnPair();

	protected void disableDependentOnPairHitAlarm() {
		EditTextPreference[] edits = { upperBoundPref, lowerBoundPref };
		for (EditTextPreference edit : edits) {
			edit.setEnabled(false);
			edit.setText(null);
			edit.setSummary(null);
		}
	}

	protected void disableDependentOnPairChangeAlarm() {
		changePref.setEnabled(false);
		changePref.setSummary(null);
	}

	protected void removePrefs(List<String> prefsToKeep) {
		Preference pref;
		for (int i = 0; i < specificCat.getPreferenceCount(); i++) {
			pref = specificCat.getPreference(i);
			if(!prefsToKeep.contains(pref.getKey())) {
				specificCat.removePreference(pref);
				i--;
			}
		}
	}

	protected void updateChangeValueSummary() {
		String text = changePref.getText();
		if(text != null && !text.equals("")) {
			changePref.setSummary(getChangeValueSummary(text));
		}
	}

	protected String getChangeValueSummary(String value) {
		if(isPercentage)
			return value + "%";
		else
			return value + " " + pairs.get(pairIndex).getExchange();
	}
}
