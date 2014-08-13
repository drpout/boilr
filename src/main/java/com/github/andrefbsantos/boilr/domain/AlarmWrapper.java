package com.github.andrefbsantos.boilr.domain;

import java.io.Serializable;

import android.net.Uri;

import com.github.andrefbsantos.libpricealarm.Alarm;

public class AlarmWrapper implements Serializable {

	private static final long serialVersionUID = 687991492884005033L;
	private Alarm alarm;
	/*
	 * A null on the following fields means the default
	 * app settings should be used.
	 */
	private Integer alertType;
	private Uri alertSound;
	private Boolean vibrate;

	public AlarmWrapper(Alarm alarm) {
		this.alarm = alarm;
	}

	public AlarmWrapper(Alarm alarm, Integer alertType, Uri alertSound, Boolean vibrate) {
		super();
		this.alarm = alarm;
		this.alertType = alertType;
		this.alertSound = alertSound;
		this.vibrate = vibrate;
	}

	public Alarm getAlarm() {
		return alarm;
	}

	public Integer getAlertType() {
		return alertType;
	}

	public void setAlertType(Integer alertType) {
		this.alertType = alertType;
	}

	public Uri getAlertSound() {
		return alertSound;
	}

	public void setAlertSound(Uri alertSound) {
		this.alertSound = alertSound;
	}

	public Boolean getVibrate() {
		return vibrate;
	}

	public void setVibrate(Boolean vibrate) {
		this.vibrate = vibrate;
	}

}
