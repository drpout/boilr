package mobi.boilr.boilr.domain;

import mobi.boilr.boilr.services.NotificationService;
import mobi.boilr.libpricealarm.Notify;
import android.content.Context;

public class AndroidNotify implements Notify {

	private static final long serialVersionUID = 228178154489839207L;
	private transient Context context;
	/*
	 * A null on the following fields means the default
	 * app settings should be used.
	 */
	private Integer alertType;
	private String alertSound;
	private Boolean vibrate;

	public AndroidNotify(Context context) {
		this.context = context;
	}

	public AndroidNotify(Context context, Integer alertType, String alertSound, Boolean vibrate) {
		this.context = context;
		this.alertType = alertType;
		this.alertSound = alertSound;
		this.vibrate = vibrate;
	}

	@Override
	public boolean trigger(int alarmID) {
		NotificationService.startNotify(context, alarmID);
		return false;
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
