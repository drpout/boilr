package mobi.boilr.boilr.activities;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Languager;
import mobi.boilr.boilr.utils.Themer;
import mobi.boilr.boilr.views.fragments.PriceHitAlarmCreationFragment;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class AlarmCreationActivity extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Themer.applyTheme(this);
		Languager.setLanguage(this);
		setTitle(getResources().getString(R.string.add_alarm));
		if(savedInstanceState == null) {
			getFragmentManager().beginTransaction().replace(android.R.id.content, new PriceHitAlarmCreationFragment()).commit();
		}
		setResult(RESULT_CANCELED);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alarm_creation, menu);
		menu.findItem(R.id.action_send_now).getActionView();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}
}
