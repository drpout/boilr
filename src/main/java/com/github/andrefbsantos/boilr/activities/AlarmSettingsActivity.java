package com.github.andrefbsantos.boilr.activities;

import android.app.Activity;
import android.os.Bundle;

import com.github.andrefbsantos.boilr.R;
import com.github.andrefbsantos.libpricealarm.Alarm;
import com.github.andrefbsantos.libpricealarm.PriceHitAlarm;
import com.github.andrefbsantos.libpricealarm.PriceVarAlarm;

public class AlarmSettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Alarm alarm = (Alarm) bundle.get("alarm");

		if (alarm instanceof PriceHitAlarm) {
			setContentView(R.layout.price_hit_alarm_settings);
			// Populate view
		} else if (alarm instanceof PriceVarAlarm) {
			setContentView(R.layout.price_var_alarm_settings);
			// Populate view
		} else {

		}
	}

	// TODO Create Listener to handle changes on view.
}
