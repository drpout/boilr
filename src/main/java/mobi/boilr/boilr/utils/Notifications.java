
package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmListActivity;
import mobi.boilr.boilr.activities.NotificationActivity;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import mobi.boilr.libpricealarm.PriceVarAlarm;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/* Based on Android DeskClock AlarmNotifications. */
public final class Notifications {

	private static Notification.Builder setCommonNotificationProps(Context context, int alarmID,
			String firingReason) {
		Notification.Builder notification = new Notification.Builder(context)
				.setContentTitle(context.getString(R.string.boilr_alarm))
				.setContentText(firingReason)
				.setSmallIcon(R.drawable.ic_action_alarms)
				.setLights(0xFFFF0000, 333, 333) // Blink in red ~3 times per second.
				.setOngoing(false)
				.setAutoCancel(false);

		Intent viewAlarmsIntent = new Intent(context, AlarmListActivity.class);
		viewAlarmsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notification.setContentIntent(PendingIntent.getActivity(context, alarmID, viewAlarmsIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		return notification;
	}

	public static void showLowPriorityNotification(Context context, Alarm alarm) {
		int alarmID = alarm.getId();
		Log.v("Displaying low priority notification for alarm instance: " + alarmID);
		String firingReason = getFiringReason(alarm);
		Notification.Builder notification = setCommonNotificationProps(context, alarmID, firingReason);
		notification.setPriority(Notification.PRIORITY_DEFAULT);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(alarmID, notification.build());
	}

	public static void showAlarmNotification(Context context, Alarm alarm) {
		int alarmID = alarm.getId();
		Log.v("Displaying alarm notification for alarm instance: " + alarmID);
		// Close dialogs and window shade, so this will display
		context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		String firingReason = getFiringReason(alarm);
		Notification.Builder notification = setCommonNotificationProps(context, alarmID, firingReason);
		notification.setPriority(Notification.PRIORITY_MAX);

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
