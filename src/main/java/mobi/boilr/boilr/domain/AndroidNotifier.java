package mobi.boilr.boilr.domain;

import android.content.Context;
import android.content.Intent;
import mobi.boilr.boilr.activities.NotificationActivity;
import mobi.boilr.boilr.services.NotificationService;
import mobi.boilr.libpricealarm.Notifier;

public class AndroidNotifier extends Notifier {

	private static final long serialVersionUID = 228178154489839207L;
	private transient Context context;
	/*
	 * A null on the following fields means the default
	 * app settings should be used.
	 */
	private Integer alertType;
	private String alertSound;
	private Boolean vibrate;

	public AndroidNotifier(Context context) {
		this.context = context;
	}

	public AndroidNotifier(Context context, Integer alertType, String alertSound, Boolean vibrate) {
		this.context = context;
		this.alertType = alertType;
		this.alertSound = alertSound;
		this.vibrate = vibrate;
	}

	@Override
	protected boolean notify(int alarmID) {
		NotificationService.startNotify(context, alarmID);
		return false;
	}

	@Override
	protected void suppress(int alarmID) {
		// Log.d("Suppressing alarm " + alarmID);
		Intent intent = new Intent(NotificationActivity.FINISH_ACTION);
		intent.putExtra("alarmID", alarmID);
		intent.putExtra("keepMonitoring", true);
		context.sendBroadcast(intent);
	}

	public Integer getAlertType() {
		return alertType;
	}

	public void setAlertType(Integer alertType) {
		this.alertType = alertType;
	}

	public String getAlertSound() {
		return alertSound;
	}

	public void setAlertSound(String alertSound) {
		this.alertSound = alertSound;
	}

	public Boolean isVibrate() {
		return vibrate;
	}

	public void setVibrate(Boolean vibrate) {
		this.vibrate = vibrate;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
}
