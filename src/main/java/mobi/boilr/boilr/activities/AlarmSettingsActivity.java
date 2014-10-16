package mobi.boilr.boilr.activities;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Languager;
import mobi.boilr.boilr.utils.Themer;
import mobi.boilr.boilr.views.fragments.PriceChangeAlarmSettingsFragment;
import mobi.boilr.boilr.views.fragments.PriceHitAlarmSettingsFragment;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

public class AlarmSettingsActivity extends Activity {
	public static final String alarmID = "alarmID";
	public static final String alarmType = "alarmType";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Themer.applyTheme(this);
		Languager.setLanguage(this);
		setTitle(getResources().getString(R.string.alarm_settings));
		if(savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			Bundle args = new Bundle();
			args.putInt(alarmID, extras.getInt(alarmID));
			String type = extras.getString(alarmType);
			Fragment settingsFrag;
			if(type.equals("PriceHitAlarm")) {
				settingsFrag = new PriceHitAlarmSettingsFragment();
			} else { // type.equals("PriceChangeAlarm")
				settingsFrag = new PriceChangeAlarmSettingsFragment();
			}
			settingsFrag.setArguments(args);
			getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFrag).commit();
		}
	}
}
