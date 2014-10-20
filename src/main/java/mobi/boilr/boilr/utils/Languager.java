package mobi.boilr.boilr.utils;

import java.util.Locale;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.views.fragments.SettingsFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

public class Languager {
	public static void setLanguage(Context context){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String language = sharedPreferences.getString(SettingsFragment.PREF_KEY_LANGUAGE, "");
		Locale locale;
		if( !language.equals("") ){
			locale = new Locale(language);
		}else{
			language = Locale.getDefault().getLanguage();
			String [] knownLanguages = context.getResources().getStringArray(R.array.pref_values_language);
			int size = knownLanguages.length;
			for(int i = 0 ; i<size;i++ ){
				if(knownLanguages[i].equals(language)){
					return;
				}
			}
			locale = new Locale(context.getResources().getString(R.string.pref_default_languange_code));		
		}
		Locale.setDefault(locale); 
		Configuration config = new Configuration();
		config.locale = locale;
		context.getApplicationContext().getResources().updateConfiguration(config, null);
	}
}
