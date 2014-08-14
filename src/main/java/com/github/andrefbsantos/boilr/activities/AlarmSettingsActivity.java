package com.github.andrefbsantos.boilr.activities;

import android.app.Activity;
import android.os.Bundle;

public class AlarmSettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		int id = (Integer) bundle.get("id");

		// retrieve Alarm from DB, do something with it

		// if (wrapper.getAlarm() instanceof PriceHitAlarm) {
		// setContentView(R.layout.price_hit_alarm_settings);
		// // Populate view
		// } else if (wrapper.getAlarm() instanceof PriceVarAlarm) {
		// setContentView(R.layout.price_var_alarm_settings);
		// // Populate view
		// } else {
		//
		// }
	}

	// TODO Create Listener to handle changes on view.
}
