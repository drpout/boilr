package mobi.boilr.boilr.views.fragments;

import java.awt.Checkbox;

import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.Notify;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

public class PriceHitAlarmSettingsFragment extends AlarmSettingsFragment {

	private class OnPriceHitSettingsPreferenceChangeListener extends OnAlarmSettingsPreferenceChangeListener{

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if(preference.getKey().equals(PREF_KEY_UPPER_VALUE)){
				preference.setSummary((CharSequence) newValue);
			}else if(preference.getKey().equals(PREF_KEY_LOWER_VALUE)){
				preference.setSummary((CharSequence) newValue);
			}else if(preference.getKey().equals(PREF_KEY_ALARM_UPDATE_INTERVAL_HIT)) {
				preference.setSummary(SettingsFragment.buildMinToDaysSummary((String)newValue));
			}else{
				return super.onPreferenceChange(preference, newValue);
			}
			return true;
		}
	}

	private static final String PREF_KEY_UPPER_VALUE = "pref_key_upper_value";
	private static final String PREF_KEY_LOWER_VALUE = "pref_key_lower_value";

	private OnAlarmSettingsPreferenceChangeListener listener = new OnPriceHitSettingsPreferenceChangeListener();


	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

		Preference preference = findPreference("type");
		preference.setSummary("Price Hit");

		PreferenceCategory category = (PreferenceCategory) findPreference("specific");
		category.setTitle("Price Hit");

		EditTextPreference edit;
		edit = new EditTextPreference(this.getActivity());
		edit.setKey(PREF_KEY_UPPER_VALUE);
		edit.setTitle("Upper");
		category.addPreference(edit);
		edit.setOnPreferenceChangeListener(listener);

		edit = new EditTextPreference(this.getActivity());
		edit.setKey(PREF_KEY_LOWER_VALUE);
		edit.setTitle("Down");
		category.addPreference(edit);
		edit.setOnPreferenceChangeListener(listener);


		SharedPreferences sharedPreferences = 	PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		Preference pref;
		pref = findPreference(PREF_KEY_ALARM_UPDATE_INTERVAL_HIT);
		pref.setSummary(sharedPreferences.getString(PREF_KEY_ALARM_UPDATE_INTERVAL_HIT, "") + " s");
		pref.setDefaultValue(null);
		pref.setOnPreferenceChangeListener(listener);
	}

	@Override
	public void makeAlarm(){


		Log.d("exchange " + ((ListPreference) findPreference(PREF_KEY_EXCHANGE)).getEntry());
		Log.d("pair " + ((ListPreference) findPreference(PREF_KEY_PAIR)).getEntry());
		Log.d("alertType " + ((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getEntry());
		Log.d("alertSound " + ((RingtonePreference)findPreference(PREF_KEY_ALARM_ALERT_SOUND)).getRingtoneType());
		Log.d("vibrate " + ((CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE)).getTitle());
		Log.d("Hit Refresh " + ((EditTextPreference) findPreference(PREF_KEY_ALARM_UPDATE_INTERVAL_HIT)).getText());

		//CharSequence alertType2 = ((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getEntry();
		//CharSequence alertType =  alertType2.equals(-1) ? null : alertType2;
		//				Exchange exchange = ((ListPreference) findPreference(PREF_KEY_EXCHANGE)).getEntry();
		//				Pair pair;
		//				long period;
//		Notify notify = new AndroidNotify(
//				this.getActivity().getApplicationContext(), 
//				((ListPreference)findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getEntry(), 
//				((RingtonePreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).ge,
//				defaultVibrateDefinition ? null : ((CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE)).isChecked());
//
//		Boolean vibrate;
//		Context context;
//		Integer alertType;
//		String alertSound;
//		new AndroidNotify(context, alertType, alertSound, vibrate)
		//				double upperBound;
		//				double lowerBound;
		//
		//				if(mBound){
		//					int id;
		//					new PriceHitAlarm(id, exchange, pair, period, notify, upperBound, lowerBound);
		//				}


	}
}

