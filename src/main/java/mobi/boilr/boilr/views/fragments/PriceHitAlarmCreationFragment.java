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

public class PriceHitAlarmCreationFragment extends AlarmCreationFragment {

	protected static final String PREF_KEY_ALARM_HIT_UPDATE_INTERVAL = "pref_key_alarm_update_interval_hit";

	private class OnPriceHitSettingsPreferenceChangeListener extends
	OnAlarmSettingsPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if(preference.getKey().equals(PREF_KEY_UPPER_VALUE)) {
				preference.setSummary((CharSequence) newValue);
			} else if(preference.getKey().equals(PREF_KEY_LOWER_VALUE)) {
				preference.setSummary((CharSequence) newValue);
			} else if(preference.getKey().equals(PREF_KEY_ALARM_HIT_UPDATE_INTERVAL)) {
				preference.setSummary(newValue + " s");
			} else {
				return super.onPreferenceChange(preference, newValue);
			}
			return true;
		}
	}

	private static final String PREF_KEY_UPPER_VALUE = "pref_key_upper_value";
	private static final String PREF_KEY_LOWER_VALUE = "pref_key_lower_value";

	private OnAlarmSettingsPreferenceChangeListener listener = new OnPriceHitSettingsPreferenceChangeListener();

	public PriceHitAlarmCreationFragment(int exchangeIndex, int pairIndex) {
		super(exchangeIndex, pairIndex);
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Preference preference = findPreference(PREF_KEY_TYPE);
		preference.setSummary("Price Hit");

		PreferenceCategory category = (PreferenceCategory) findPreference("specific");
		category.setTitle("Price Hit");

		EditTextPreference edit;
		edit = new EditTextPreference(this.getActivity());
		edit.setKey(PREF_KEY_UPPER_VALUE);
		edit.setTitle("Upper Bound");
		category.addPreference(edit);
		edit.setOnPreferenceChangeListener(listener);

		edit = new EditTextPreference(this.getActivity());
		edit.setKey(PREF_KEY_LOWER_VALUE);
		edit.setTitle("Down Bound");
		category.addPreference(edit);
		edit.setOnPreferenceChangeListener(listener);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

		edit = new EditTextPreference(this.getActivity());
		edit.setKey(PREF_KEY_ALARM_HIT_UPDATE_INTERVAL);

		edit.setTitle("Hit alarm update interval");
		edit.setDialogMessage("In seconds. Defines how often a hit alarm gets data from the exchange. You can set another interval for a particular alarm on its settings.");
		edit.setSummary(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, "") + " s");
		edit.setDefaultValue(null);
		edit.setOnPreferenceChangeListener(listener);

		category = (PreferenceCategory) findPreference("alert");
		category.addPreference(edit);
	}

	@Override
	public void makeAlarm() {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

		Log.d("exchange " + ((ListPreference) findPreference(PREF_KEY_EXCHANGE)).getValue());
		Log.d("pair " + pairs.get(Integer.parseInt(((ListPreference) findPreference(PREF_KEY_PAIR)).getValue())));
		Log.d("alertType " + ((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue());
		Log.d("alertSound " + sharedPreferences.getString(PREF_KEY_ALARM_ALERT_SOUND, null));
		Log.d("vibrate " + ((CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE)).isChecked());
		Log.d("Hit Refresh " + ((EditTextPreference) findPreference(PREF_KEY_ALARM_HIT_UPDATE_INTERVAL)).getText());

		Integer alertType = ((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue() != null ? Integer.parseInt(((ListPreference) findPreference(PREF_KEY_ALARM_ALERT_TYPE)).getValue()) : null;
		String alertSound;
		if(sharedPreferences.getString(PREF_KEY_ALARM_ALERT_SOUND, "").equals(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, ""))) {
			alertSound = null;
		} else {
			alertSound = sharedPreferences.getString(PREF_KEY_ALARM_ALERT_SOUND, "");
		}

		Boolean vibrate = defaultVibrateDefinition ? null : ((CheckBoxPreference) findPreference(PREF_KEY_ALARM_VIBRATE)).isChecked();

		Log.d(alertType + " " + alertSound + " " + vibrate);

		AndroidNotify notify;
		if(alertType == null && alertSound == null && vibrate == null) {
			notify = new AndroidNotify(getActivity());
		} else {
			notify = new AndroidNotify(this.getActivity().getApplicationContext(), alertType, alertSound, vibrate);
		}
		int id = mStorageAndControlService.generateAlarmID();

		try {
			Exchange exchange = mStorageAndControlService.getExchange(((ListPreference) findPreference(PREF_KEY_EXCHANGE)).getValue());
			Pair pair = pairs.get(pairIndex);

			// Time is in seconds, convert to milliseconds
			long period = 1000 * Long.parseLong(
					((EditTextPreference) findPreference(PREF_KEY_ALARM_HIT_UPDATE_INTERVAL)).getText() != null ?
							((EditTextPreference) findPreference(PREF_KEY_ALARM_HIT_UPDATE_INTERVAL)).getText() :
								sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_UPDATE_INTERVAL_HIT, ""));
			double upperBound = Double.parseDouble(((EditTextPreference) findPreference(PREF_KEY_UPPER_VALUE)).getText());
			double lowerBound = Double.parseDouble(((EditTextPreference) findPreference(PREF_KEY_LOWER_VALUE)).getText());
			mStorageAndControlService.addAlarm(id, exchange, pair, period, notify, upperBound, lowerBound);
		} catch (Exception e) {
			Log.e("Failed to create Alarm", e);
			Toast.makeText(this.getActivity(), "Failed to create alarm", Toast.LENGTH_SHORT).show();
		}
	}
}
