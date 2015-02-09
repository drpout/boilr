package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.boilr.activities.NotificationActivity;
import mobi.boilr.boilr.activities.SettingsActivity;
import mobi.boilr.libdynticker.core.Pair;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.Alarm.Direction;
import mobi.boilr.libpricealarm.PriceChangeAlarm;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/* Based on Android DeskClock AlarmNotifications. */
public final class Notifications {

	private static final int noInternetNotificationID = 432191926;
	private static Notification.Builder noInternetNotification = null;
	public static boolean allowNoInternetNotification = true;
	private static Bitmap smallUpArrowBitmap = null;
	public static Bitmap bigUpArrowBitmap = null;
	private static Bitmap smallDownArrowBitmap = null;
	public static Bitmap bigDownArrowBitmap = null;
	private static final int bigArrowSize = 250;
	private static final int smallArrowSize = 100;

	private static Notification.Builder setCommonNotificationProps(Context context, Alarm alarm,
			String firingReasonTitle, String firingReasonBody) {
		if(smallUpArrowBitmap == null) {
			int tickerGreen = context.getResources().getColor(R.color.tickergreen);
			int tickerRed = context.getResources().getColor(R.color.tickerred);
			smallUpArrowBitmap = textAsBitmap("▲", smallArrowSize, tickerGreen);
			bigUpArrowBitmap = textAsBitmap("▲", bigArrowSize, tickerGreen);
			smallDownArrowBitmap = textAsBitmap("▼", smallArrowSize, tickerRed);
			bigDownArrowBitmap = textAsBitmap("▼", bigArrowSize, tickerRed);
		}
		Notification.Builder notification = new Notification.Builder(context)
			.setContentTitle(firingReasonTitle)
			.setContentText(firingReasonBody)
			.setSmallIcon(R.drawable.ic_action_alarms)
			.setLights(0xFFFF0000, 333, 333) // Blink in red ~3 times per second.
			.setOngoing(false)
			.setAutoCancel(true);
		if(isDirectionUp(alarm)) {
			notification.setLargeIcon(smallUpArrowBitmap);
		} else {
			notification.setLargeIcon(smallDownArrowBitmap);
		}
		Intent alarmSettingsIntent = new Intent(context, AlarmSettingsActivity.class);
		alarmSettingsIntent.putExtra(AlarmSettingsActivity.alarmID, alarm.getId());
		alarmSettingsIntent.putExtra(AlarmSettingsActivity.alarmType, alarm.getClass().getSimpleName());
		alarmSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notification.setContentIntent(PendingIntent.getActivity(context, alarm.getId(), alarmSettingsIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		return notification;
	}

	public static void showLowPriorityNotification(Context context, Alarm alarm) {
		String firingReasonTitle = getFiringReasonTitle(alarm);
		String firingReasonBody = getFiringReasonBody(context, alarm);
		Notification.Builder notification = setCommonNotificationProps(context, alarm, firingReasonTitle, firingReasonBody);
		notification.setPriority(Notification.PRIORITY_DEFAULT);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); 
		nm.cancel(alarm.hashCode());
		nm.notify(alarm.hashCode(), notification.build());
	} 

	public static void showAlarmNotification(Context context, Alarm alarm) {
		int alarmID = alarm.getId();
		// Close dialogs and window shade, so this will display
		context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		String firingReasonTitle = getFiringReasonTitle(alarm);
		String firingReasonBody = getFiringReasonBody(context, alarm);
		Notification.Builder notification = setCommonNotificationProps(context, alarm, firingReasonTitle, firingReasonBody);
		notification.setPriority(Notification.PRIORITY_MAX);

		// Setup fullscreen intent
		Intent fullScreenIntent = new Intent(context, NotificationActivity.class);
		fullScreenIntent.putExtra("alarmID", alarmID);
		fullScreenIntent.putExtra("firingReason", firingReasonTitle + "\n" + firingReasonBody);
		fullScreenIntent.putExtra("canKeepMonitoring", canKeepMonitoring(alarm));
		fullScreenIntent.putExtra("isDirectionUp", isDirectionUp(alarm));
		fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		notification.setFullScreenIntent(PendingIntent.getActivity(context, alarmID, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT), true);

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(alarm.hashCode());
		nm.notify(alarm.hashCode(), notification.build());
	}

	private static boolean isDirectionUp(Alarm alarm) {
		boolean isDirectionUp;
		if(alarm instanceof PriceHitAlarm) {
			/*
			 * PriceHitAlarm has no valid direction if it triggers on the first
			 * time it fetches last price.
			 */
			PriceHitAlarm hitAlarm = (PriceHitAlarm) alarm;
			isDirectionUp = hitAlarm.wasUpperLimitHit();
		} else {
			isDirectionUp = alarm.getDirection() == Direction.UP;
		}
		return isDirectionUp;
	}

	/*
	 * By Ted Hopp https://stackoverflow.com/a/8799344
	 */
	private static Bitmap textAsBitmap(String text, float textSize, int textColor) {
		Paint paint = new Paint();
		paint.setTextSize(textSize);
		paint.setColor(textColor);
		paint.setTextAlign(Paint.Align.LEFT);
		int width = (int) (paint.measureText(text) + 0.5f); // round
		float baseline = (int) (-paint.ascent() + 0.5f); // ascent() is negative
		int height = (int) (baseline + paint.descent() + 0.5f);
		Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(image);
		canvas.drawText(text, 0, baseline, paint);
		return image;
	}

	private static boolean canKeepMonitoring(Alarm alarm) {
		if(alarm instanceof PriceChangeAlarm)
			return true;
		else
			return false;
	}

	public static void showNoInternetNotification(Context context) {
		if(allowNoInternetNotification) {
			if(noInternetNotification == null) {
				Intent changeSettingsIntent = new Intent(context, SettingsActivity.class);
				changeSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				noInternetNotification = new Notification.Builder(context)
						.setContentTitle(context.getString(R.string.no_internet))
						.setContentText(context.getString(R.string.no_updates))
						.setSmallIcon(R.drawable.ic_action_warning)
						.setOngoing(false)
						.setAutoCancel(true)
						.setPriority(Notification.PRIORITY_DEFAULT)
						.setWhen(0)
						.setContentIntent(PendingIntent.getActivity(context, noInternetNotificationID, changeSettingsIntent, PendingIntent.FLAG_UPDATE_CURRENT));
			}
			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(noInternetNotificationID, noInternetNotification.build());
		}
	}

	private static String getFiringReasonTitle(Alarm alarm) {
		return alarm.getPair().toString() + " @ " + alarm.getExchange().getName();
	}

	private static String getFiringReasonBody(Context context, Alarm alarm) {
		Pair pair = alarm.getPair();
		if(alarm instanceof PriceHitAlarm) {
			return Conversions.format8SignificantDigits(alarm.getLastValue()) + " " + pair.getExchange();
		} else if(alarm instanceof PriceChangeAlarm) {
			PriceChangeAlarm changeAlarm = (PriceChangeAlarm) alarm;
			String change;
			if(changeAlarm.isPercent())
				change = Conversions.format2DecimalPlaces(changeAlarm.getLastChange()) + "%";
			else
				change = Conversions.format8SignificantDigits(changeAlarm.getLastChange()) + " " + pair.getExchange();
			return context.getString(R.string.price_change_firing_reason, change,
					Conversions.formatMilis(changeAlarm.getElapsedMilis(), context));
		}
		return "Could not retrieve firing reason.";
	}

	public static void clearNotification(Context context, int alarmID) {
		Log.d("Clearing notifications for alarm instance: " + alarmID);
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(alarmID);
	}

	public static void clearNoInternetNotification(Context context) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(noInternetNotificationID);
	}

	public static void rebuildNoInternetNotification() {
		noInternetNotification = null;
	}
}
