package mobi.boilr.boilr.activities;

import mobi.boilr.boilr.views.fragments.PriceHitAlarmSettingsFragment;
import mobi.boilr.boilr.views.fragments.PriceVarAlarmSettingsFragment;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Log;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class AlarmCreationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceHitAlarmSettingsFragment()).commit();
	}

	//	@Override
	//	public boolean onPreferenceChange(Preference preference, Object newValue) {
	//		Log.d("aaaaa");
	//		if(preference.getKey().equals(R.string.pref_exchange_title)){
	//			Log.d("title");
	//		}else if(1==2){
	//			
	//		}
	//		preference.setSummary((String) newValue);
	//		return false;
	//	}

	//	public void changeAlarmType(String type){
	//		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
	//		if(type.equals(PRICE_HIT) ){
	//			fragmentTransaction.add(R.id.specific_alarm_creation, new PriceHitAlarmSettingsFragment());
	//		}else if(type.equals(PRICE_VAR)){
	//			fragmentTransaction.add(R.id.specific_alarm_creation, new PriceVarAlarmSettingsFragment());
	//		}
	//		fragmentTransaction.commit();
	//	}
}
