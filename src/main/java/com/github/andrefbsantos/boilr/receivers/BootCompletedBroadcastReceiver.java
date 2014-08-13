package com.github.andrefbsantos.boilr.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.andrefbsantos.boilr.services.StorageAndControlService;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, StorageAndControlService.class);
		context.startService(serviceIntent);
	}

}
