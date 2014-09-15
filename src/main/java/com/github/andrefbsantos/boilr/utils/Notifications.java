
package com.github.andrefbsantos.boilr.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.github.andrefbsantos.boilr.R;
import com.github.andrefbsantos.boilr.activities.AlarmListActivity;
import com.github.andrefbsantos.boilr.activities.NotificationActivity;
import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.libdynticker.core.Pair;
import com.github.andrefbsantos.libpricealarm.Alarm;
import com.github.andrefbsantos.libpricealarm.PriceHitAlarm;
import com.github.andrefbsantos.libpricealarm.PriceVarAlarm;

/* Based on Android DeskClock AlarmNotifications. */
public final class Notifications {

	private static Notification.Builder setCommonNotificationProps(Context context, int alarmID,
			String firingReason) {
		Notification.Builder notification = new Notification.Builder(context)
		.setContentTitle(context.getString(R.string.boilr_alarm))
		.setContentText(firingReason)
		.setSmallIcon(R.drawable.ic_action_alarms)
		.setAutoCancel(false);

		Intent viewAlarmsIntent = new Intent(context, AlarmListActivity.class);
		viewAlarmsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notification.setContentIntent(PendingIntent.getActivity(context, alarmID, viewAlarmsIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		return notification;
	}

	public static void showLowPriorityNotification(Context context, AlarmWrapper alarmWrapper) {
		Alarm alarm = alarmWrapper.getAlarm();
		int alarmID = alarm.getId();
		Log.v("Displaying low priority notification for alarm instance: " + alarmID);
		String firingReason = getFiringReason(alarm);
		Notification.Builder notification = setCommonNotificationProps(context, alarmID, firingReason);
		notification
		.setOngoing(false)
		.setPriority(Notification.PRIORITY_DEFAULT);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(alarmID, notification.build());
	}

	public static void showAlarmNotification(Context context, AlarmWrapper alarmWrapper) {
		Alarm alarm = alarmWrapper.getAlarm();
		int alarmID = alarm.getId();
		Log.v("Displaying alarm notification for alarm instance: " + alarmID);
		// Close dialogs and window shade, so this will display
		context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		String firingReason = getFiringReason(alarm);
		Notification.Builder notification = setCommonNotificationProps(context, alarmID, firingReason);
		notification
		.setOngoing(true)
		.setDefaults(Notification.DEFAULT_LIGHTS)
		.setWhen(0)
		.setPriority(Notification.PRIORITY_MAX);

		// Setup fullscreen intent
		Intent fullScreenIntent = new Intent(context, NotificationActivity.class);
		fullScreenIntent.putExtra("alarmID", alarmID);
		fullScreenIntent.putExtra("firingReason", firingReason);
		fullScreenIntent.putExtra("canKeepMonitoring", canKeepMonitoring(alarm));
		fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		notification.setFullScreenIntent(PendingIntent.getActivity(context, alarmID, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT), true);

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(alarmID, notification.build());
	}

	private static boolean canKeepMonitoring(Alarm alarm) {
		if(alarm instanceof PriceVarAlarm)
			return true;
		else
			return false;
	}

	private static String getFiringReason(Alarm alarm) {
		Pair pair = alarm.getPair();
		if(alarm instanceof PriceHitAlarm) {
			return pair.getCoin() + " @ " + alarm.getLastValue() + " " + pair.getExchange() +
					" in " + alarm.getExchange().getName();
		} else if(alarm instanceof PriceVarAlarm) {
			PriceVarAlarm varAlarm = (PriceVarAlarm) alarm;
			String reason = pair.getCoin() + "/" + pair.getExchange() + " had ";
			if(varAlarm.isPercent())
				reason += varAlarm.getPercent() + " %";
			else
				reason += varAlarm.getVariation() + " " + pair.getExchange();
			reason += " variation in " + alarm.getExchange().getName();
			return reason;
		}
		return "Could not retrieve firing reason.";
	}

	public static void clearNotification(Context context, int alarmID) {
		Log.v("Clearing notifications for alarm instance: " + alarmID);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(alarmID);
	}
}
