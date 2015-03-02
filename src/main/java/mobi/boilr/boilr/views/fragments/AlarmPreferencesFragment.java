package mobi.boilr.boilr.views.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.preference.ThemableRingtonePreference;
import mobi.boilr.boilr.services.StorageAndControlService;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.IconToast;
import mobi.boilr.boilr.utils.Languager;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.utils.Themer;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import android.app.Activity;
import android.content.Context;
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
	protected static final String PREF_KEY_TIME_FRAME = "pref_key_time_frame";
	protected static final String PREF_KEY_UPDATE_INTERVAL = "pref_key_update_interval";
	protected static final String PREF_KEY_LAST_VALUE = "pref_key_last_value";
	protected static final String PREF_KEY_UPPER_VALUE = "pref_key_upper_value";
	protected static final String PREF_KEY_LOWER_VALUE = "pref_key_lower_value";
	protected static final String PREF_KEY_CHANGE_IN_PERCENTAGE = "pref_key_change_in_percentage";
	protected static final String PREF_KEY_CHANGE_VALUE = "pref_key_change_value";
	protected static final List<String> hitAlarmPrefsToKeep = Arrays.asList(PREF_KEY_UPPER_VALUE, PREF_KEY_LOWER_VALUE,
			PREF_KEY_UPDATE_INTERVAL);
	protected static final List<String> changeAlarmPrefsToKeep = Arrays.asList(PREF_KEY_CHANGE_IN_PERCENTAGE,
			PREF_KEY_CHANGE_VALUE, PREF_KEY_TIME_FRAME, PREF_KEY_UPDATE_INTERVAL);
	protected Activity mEnclosingActivity;
	protected int mExchangeIndex = 0, mPairIndex = 0;
	protected List<Pair> mPairs = new ArrayList<Pair>();
	protected double mLastValue = Double.POSITIVE_INFINITY;
	protected boolean mIsPercentage = false, mRecoverSavedInstance = false;
	protected SharedPreferences mSharedPrefs;
	protected PreferenceCategory mSpecificCat;
	protected ListPreference mExchangeListPref, mPairListPref, mAlarmTypePref, mAlarmAlertTypePref, mVibratePref;
	protected ThemableRingtonePreference mAlertSoundPref;
	protected CheckBoxPreference mIsPercentPref;
	protected EditTextPreference mLastValuePref, mUpperLimitPref, mLowerLimitPref, mTimeFramePref, mUpdateIntervalPref,
			mChangePref;
	protected OnPreferenceChangeListener mListener;
	protected StorageAndControlService mStorageAndControlService;
	protected boolean mBound = false;
	protected ServiceConnection mStorageAndControlServiceConnection;
	protected Intent mServiceIntent;
	public static final String DEFAULT = "default";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null)
			mRecoverSavedInstance = true;
		mEnclosingActivity = getActivity();
		Themer.applyTheme(mEnclosingActivity);
		Languager.setLanguage(mEnclosingActivity);
		mServiceIntent = new Intent(mEnclosingActivity, StorageAndControlService.class);
		addPreferencesFromResource(R.xml.alarm_settings);
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mEnclosingActivity);
		mExchangeListPref = (ListPreference) findPreference(PREF_KEY_EXCHANGE);
		mSpecificCat = (PreferenceCategory) findPreference(PREF_KEY_SPECIFIC);
		mPairListPref = (ListPreference) findPreference(PREF_KEY_PAIR);
		mLastValuePref = (EditTextPreference) findPreference(PREF_KEY_LAST_VALUE);
		mAlarmTypePref = (ListPreference) findPreference(PREF_KEY_TYPE);
		mAlarmAlertTypePref = (ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE);
		mAlertSoundPref = (ThemableRingtonePreference) findPreference(PREF_KEY_ALARM_ALERT_SOUND);
		mVibratePref = (ListPreference) findPreference(PREF_KEY_ALARM_VIBRATE);
		mUpperLimitPref = (EditTextPreference) findPreference(PREF_KEY_UPPER_VALUE);
		mLowerLimitPref = (EditTextPreference) findPreference(PREF_KEY_LOWER_VALUE);
		mIsPercentPref = (CheckBoxPreference) findPreference(PREF_KEY_CHANGE_IN_PERCENTAGE);
		mChangePref = (EditTextPreference) findPreference(PREF_KEY_CHANGE_VALUE);
		mTimeFramePref = (EditTextPreference) findPreference(PREF_KEY_TIME_FRAME);
		mUpdateIntervalPref = (EditTextPreference) findPreference(PREF_KEY_UPDATE_INTERVAL);

		Preference[] prefs = { mExchangeListPref, mPairListPref, mLastValuePref,
				mAlarmTypePref, mUpperLimitPref, mLowerLimitPref, mIsPercentPref,
				mChangePref, mTimeFramePref, mUpdateIntervalPref,
				mAlarmAlertTypePref, mAlertSoundPref, mVibratePref };
		for (Preference pref : prefs) {
			pref.setOnPreferenceChangeListener(mListener);
		}
		
		CharSequence[] entries = mAlarmAlertTypePref.getEntries();
		entries[0] = mEnclosingActivity.getString(R.string.default_value, getDefaultAlertTypeName());
		mAlarmAlertTypePref.setEntries(entries);
		
		entries = mVibratePref.getEntries();
		entries[0] = mEnclosingActivity.getString(R.string.default_value, getDefaultVibrateName());
		mVibratePref.setEntries(entries);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		SharedPreferences.Editor editor = mSharedPrefs.edit();
		editor.putInt("exchangeIndex", mExchangeIndex);
		editor.putInt("pairIndex", mPairIndex);
		editor.commit();
		mEnclosingActivity.unbindService(mStorageAndControlServiceConnection);
	}

	protected void updatePairsList(String exchangeCode, String exchangeName, String pairString) {
		mPairListPref.setEntries(null);
		mPairListPref.setEntryValues(null);
		mPairListPref.setSummary(null);
		mPairListPref.setEnabled(false);
		disableDependentOnPairAux();
		try {
			if(!mBound) {
				throw new IOException(mEnclosingActivity.getString(R.string.not_bound, "AlarmPreferencesFragment"));
			}
			mStorageAndControlService.getPairs(this, exchangeCode, exchangeName, pairString);
		} catch (Exception e) {
			warnFailedRetrievePairs(exchangeName, e);
		}

	}

	private void warnFailedRetrievePairs(String exchangeName, Exception e) {
		String message = mEnclosingActivity.getString(R.string.couldnt_retrieve_pairs, exchangeName);
		IconToast.warning(mEnclosingActivity, message);
		Log.e(message, e);
	}

	public void updatePairsListCallback(String exchangeName, String pairString, List<Pair> pairs) {
		this.mPairs = pairs;
		try {
			if(pairs == null)
				throw new Exception("Pairs is null.");
			CharSequence[] sequence = new CharSequence[pairs.size()];
			CharSequence[] ids = new CharSequence[pairs.size()];
			for (int i = 0; i < pairs.size(); i++) {
				sequence[i] = pairs.get(i).toString();
				if(pairString != null && sequence[i].equals(pairString))
					mPairIndex = i;
				ids[i] = String.valueOf(i);
			}
			mPairListPref.setEnabled(true);
			mPairListPref.setEntries(sequence);
			mPairListPref.setEntryValues(ids);
			mPairListPref.setSummary(sequence[mPairIndex]);
			mPairListPref.setValueIndex(mPairIndex);
			updateDependentOnPairAux();
		} catch (Exception e) {
			warnFailedRetrievePairs(exchangeName, e);
		}
	}

	protected void updateDependentOnPairAux() {
		if(mRecoverSavedInstance) {
			mLastValuePref.setSummary(mLastValuePref.getText());
			updateDependentOnPair();
			mRecoverSavedInstance = false;
		} else {
			mLastValue = Double.POSITIVE_INFINITY;
			mLastValuePref.setEnabled(false);
			mLastValuePref.setText(null);
			mLastValuePref.setSummary(null);
			try {
				if(!mBound) {
					throw new IOException(mEnclosingActivity.getString(R.string.not_bound, "AlarmPreferencesFragment"));
				}
				Exchange exchange = mStorageAndControlService.getExchange(mExchangeListPref.getEntryValues()[mExchangeIndex].toString());
				mStorageAndControlService.getLastValue(this, exchange, mPairs.get(mPairIndex));
			} catch (Exception e) {
				warnFailedRetrieveLastValue(e);
			}
		}
	}

	private void warnFailedRetrieveLastValue(Exception e) {
		String message = mEnclosingActivity.getString(R.string.couldnt_retrieve_last_value, mExchangeListPref.getEntry(), mPairs.get(mPairIndex).toString());
		IconToast.warning(mEnclosingActivity, message);
		Log.e(message, e);
		updateDependentOnPair();
	}

	public void getLastValueCallback(Double result) {
		try {
			if(result == null)
				throw new Exception("Last value is null.");
			mLastValue = result;
			mLastValuePref.setEnabled(true);
			String lastValueString = Conversions.formatMaxDecimalPlaces(mLastValue) + " " + mPairs.get(mPairIndex).getExchange();
			mLastValuePref.setText(lastValueString);
			mLastValuePref.setSummary(lastValueString);
			updateDependentOnPair();
		} catch (Exception e) {
			warnFailedRetrieveLastValue(e);
		}
	}

	protected abstract void updateDependentOnPair();

	protected void updateDependentOnPairChangeAlarm() {
		mChangePref.setEnabled(true);
		updateChangeValueSummary();
	}

	protected void disableDependentOnPairAux() {
		mLastValue = Double.POSITIVE_INFINITY;
		mLastValuePref.setEnabled(false);
		mLastValuePref.setText(null);
		mLastValuePref.setSummary(null);
		disableDependentOnPair();
	}

	protected abstract void disableDependentOnPair();

	protected void disableDependentOnPairHitAlarm() {
		EditTextPreference[] edits = { mUpperLimitPref, mLowerLimitPref };
		for (EditTextPreference edit : edits) {
			edit.setEnabled(false);
			edit.setSummary(null);
		}
	}

	protected void disableDependentOnPairChangeAlarm() {
		mChangePref.setEnabled(false);
		mChangePref.setSummary(null);
	}

	protected void removePrefs(List<String> prefsToKeep) {
		Preference pref;
		for (int i = 0; i < mSpecificCat.getPreferenceCount(); i++) {
			pref = mSpecificCat.getPreference(i);
			if(!prefsToKeep.contains(pref.getKey())) {
				mSpecificCat.removePreference(pref);
				i--;
			}
		}
	}

	protected void updateChangeValueSummary() {
		String text = mChangePref.getText();
		if(text != null && !text.equals("")) {
			mChangePref.setSummary(getChangeValueSummary(text));
		}
	}

	protected String getChangeValueSummary(String value) {
		if(mIsPercentage)
			return value + "%";
		else
			return value + " " + mPairs.get(mPairIndex).getExchange();
	}
	
	public static String getDefaultAlertType(SharedPreferences sharedPrefs, Context context) {
		return sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_TYPE,
				context.getString(R.string.pref_default_alert_type));
	}

	protected CharSequence getDefaultAlertTypeName() {
		return mAlarmAlertTypePref.getEntries()[mAlarmAlertTypePref.
		                                        findIndexOfValue(getDefaultAlertType(mSharedPrefs, mEnclosingActivity))];
	}

	protected CharSequence getDefaultVibrateName() {
		return mSharedPrefs.getBoolean(SettingsFragment.PREF_KEY_VIBRATE_DEFAULT, true) ? 
				mEnclosingActivity.getString(R.string.yes) : mEnclosingActivity.getString(R.string.no);
	}
}
