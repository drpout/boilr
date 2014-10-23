package mobi.boilr.boilr.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class VolumePreference extends DialogPreference {
	private Context context;

	public VolumePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(which == DialogInterface.BUTTON_POSITIVE) {
			Intent intent = new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS);
			context.startActivity(intent);
		}
	}

}
