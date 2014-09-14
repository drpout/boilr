package com.github.andrefbsantos.boilr.domain;

import android.content.Context;

import com.github.andrefbsantos.boilr.services.NotificationService;
import com.github.andrefbsantos.libpricealarm.Notify;

public class AndroidNotify implements Notify {

	private static final long serialVersionUID = 228178154489839207L;
	private Context context;

	public AndroidNotify(Context context) {
		this.context = context;
	}

	@Override
	public boolean trigger(int alarmID) {
		NotificationService.startNotify(context, alarmID);
		return false;
	}
}
