package mobi.boilr.boilr.views.fragments;


import mobi.boilr.boilr.domain.AndroidNotify;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PriceVarAlarmCreationFragment extends AlarmCreationFragment {
	private static final String PREF_KEY_ALARM_VAR_UPDATE_INTERVAL = "pref_key_alarm_var_update_interval";
	private static final String PREF_KEY_ALARM_VAR_TYPE = "pref_key_alarm_var_type";
	private static final String PREF_KEY_ALARM_VAR_TYPE_PERCENTAGE = "pref_key_alarm_type_percentage";
	private static final String PREF_KEY_ALARM_VAR_TYPE_VARIATION = "pref_key_alarm_var_type_variation";
	private static final String PREF_KEY_ALARM_VAR_VALUE = "pref_key_alarm_var_value";


	private class OnPriceVarSettingsPreferenceChangeListener extends OnAlarmSettingsPreferenceChangeListener{



		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();

			if(key.equals(PREF_KEY_ALARM_VAR_TYPE)){
				ListPreference listPreference = (ListPreference) preference;
				listPreference.setSummary(listPreference.getEntries()[listPreference.findIndexOfValue((String) newValue)]);
			}else if(key.equals(PREF_KEY_ALARM_VAR_VALUE)){
				preference.setSummary((CharSequence) newValue);
			}else if(preference.getKey().equals(PREF_KEY_ALARM_VAR_UPDATE_INTERVAL)) {
				preference.setSummary(SettingsFragment.buildMinToDaysSummary((String)newValue));
			}else{
				return super.onPreferenceChange(preference, newValue);
			}
			return true;
		}


	}

	OnAlarmSettingsPreferenceChangeListener listener = new OnPriceVarSettingsPreferenceChangeListener();


	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

		Preference preference = findPreference(PREF_KEY_TYPE);
		preference.setSummary("Price Var");

		PreferenceCategory category = (PreferenceCategory) findPreference("specific");
		category.setTitle("Price Var");

		String [] entries = {"Variation","Percentage"};
		String [] values = {PREF_KEY_ALARM_VAR_TYPE_VARIATION,PREF_KEY_ALARM_VAR_TYPE_PERCENTAGE};
		ListPreference listPreference = new ListPreference(getActivity());
		listPreference.setTitle("Variation type");
		listPreference.setEntries(entries);
		listPreference.setEntryValues(values);
		listPreference.setValueIndex(0);
		listPreference.setSummary(entries[0]);
		listPreference.setKey(PREF_KEY_ALARM_VAR_TYPE);
		listPreference.setOnPreferenceChangeListener(listener);
		category.addPreference(listPreference);


		EditTextPreference editTextPreference = new EditTextPreference(getActivity());
		editTextPreference.setKey(PREF_KEY_ALARM_VAR_VALUE);
		editTextPreference.setTitle("Variation");
		editTextPreference.setDialogMessage("Insert a variation");
		editTextPreference.setDefaultValue("0");
		editTextPreference.setSummary("0");
		editTextPreference.setOnPreferenceChangeListener(listener);
		category.addPreference(editTextPreference);


		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

		editTextPreference = new EditTextPreference(getActivity());
		editTextPreference.setKey(PREF_KEY_ALARM_VAR_UPDATE_INTERVAL);
		editTextPreference.setTitle("Variation Update Interval");
		editTextPreference.setDialogMessage("Insert an Interval");
		editTextPreference.setDefaultValue(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_VAR, ""));
		editTextPreference.setSummary(SettingsFragment.buildMinToDaysSummary(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_VAR, "")));
		editTextPreference.setOnPreferenceChangeListener(listener);
		category = (PreferenceCategory) findPreference("alert");
		category.addPreference(editTextPreference);

	}

	@Override
	protected void makeAlarm() {
		
		SharedPreferences sharedPreferences = 	PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		Log.d("exchange " + ((ListPreference) findPreference(PREF_KEY_EXCHANGE)).getValue());
		Log.d("pair " + pairs.get(Integer.parseInt(((ListPreference) findPreference(PREF_KEY_PAIR)).getValue())));
		Log.d("alertType " + ((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue());
		Log.d("alertSound " + sharedPreferences.getString(PREF_KEY_ALARM_ALERT_SOUND, null));
		Log.d("vibrate " + ((CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE)).isChecked());
		
		Integer alertType = ((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue() != null ?  Integer.parseInt((String) ((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue()) : null; 
		String alertSound ;
		if(sharedPreferences.getString(PREF_KEY_ALARM_ALERT_SOUND,"").equals(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, ""))){
			alertSound = null;
		}else{
			alertSound = sharedPreferences.getString(PREF_KEY_ALARM_ALERT_SOUND, "");
		}

		Boolean vibrate = defaultVibrateDefinition ? null : ((CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE)).isChecked(); 

		Log.d(alertType + " " + alertSound + " "+ vibrate );

		AndroidNotify notify;
		if(alertType == null && alertSound==null && vibrate == null){
			notify = new AndroidNotify(getActivity());
		}else{
			notify =  new AndroidNotify(this.getActivity().getApplicationContext(), alertType, alertSound, vibrate);
		}

		int id = mStorageAndControlService.generateAlarmID();

		try {
			Exchange exchange = mStorageAndControlService.getExchange((String) ((ListPreference) findPreference(PREF_KEY_EXCHANGE)).getValue());
			int pairPosition = Integer.parseInt((String) ((ListPreference) findPreference(PREF_KEY_PAIR)).getValue());
			Pair pair = pairs.get(pairPosition);

			long period = Long.parseLong(
					((EditTextPreference) findPreference(PREF_KEY_ALARM_VAR_UPDATE_INTERVAL)).getText() != null ?
							((EditTextPreference) findPreference(PREF_KEY_ALARM_VAR_UPDATE_INTERVAL)).getText() :
								sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_VAR, ""));
			
			if(((ListPreference) findPreference(PREF_KEY_ALARM_VAR_TYPE)).getValue().equals(PREF_KEY_ALARM_VAR_TYPE_PERCENTAGE)){
				float percent = Float.parseFloat(((EditTextPreference) findPreference(PREF_KEY_ALARM_VAR_VALUE)).getText());
				Log.d("Percent " +percent);
				mStorageAndControlService.addAlarm(id, exchange, pair, period, notify, percent);
			}else if(((ListPreference) findPreference(PREF_KEY_ALARM_VAR_TYPE)).getValue().equals(PREF_KEY_ALARM_VAR_TYPE_VARIATION)){
				double variation = Double.parseDouble(((EditTextPreference) findPreference(PREF_KEY_ALARM_VAR_VALUE)).getText());
				Log.d("Variation " + variation);
				mStorageAndControlService.addAlarm(id, exchange, pair, period, notify, variation);
			}else{
				Log.d("not found " + ((ListPreference) findPreference(PREF_KEY_ALARM_VAR_TYPE)).getEntry() + " " + ((ListPreference) findPreference(PREF_KEY_ALARM_VAR_TYPE)).getValue());
			}
		} catch (Exception e) {
			Log.e("Failed to create Alarm", e);
			Toast.makeText(this.getActivity(), "Failed to create alarm", Toast.LENGTH_SHORT).show();
		}

		Toast.makeText(this.getActivity(), "Alarm created", Toast.LENGTH_SHORT).show();
	}
}
