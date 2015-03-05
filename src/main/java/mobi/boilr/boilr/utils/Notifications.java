package mobi.boilr.boilr.utils;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.boilr.activities.NotificationActivity;
import mobi.boilr.boilr.activities.SettingsActivity;
import mobi.boilr.boilr.services.StorageAndControlService;
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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.RingtoneManager;

/* Based on Android DeskClock AlarmNotifications. */
public final class Notifications {

	private static final int sNoNetNotifID = 432191926;
	private static Notification.Builder sNoNetNotif = null;
	public static boolean sAllowNoNetNotif = true;
	private static Bitmap sSmallUpArrowBitmap = null;
	public static Bitmap sBigUpArrowBitmap = null;
	private static Bitmap sSmallDownArrowBitmap = null;
	public static Bitmap sBigDownArrowBitmap = null;
	private static final int sBigArrowSize = 250;
	private static final int sSmallArrowSize = 100;
	// Action used to turn off no internet notification.
	public static final String ACTION_DISABLE_NET_NOTIF = "ACTION_DISABLE_NET_NOTIF";

	private static void statusBarNotifAux(Context context, Alarm alarm, String firingReasonTitle, String firingReasonBody) {
		if(sSmallUpArrowBitmap == null) {
			int tickerGreen = context.getResources().getColor(R.color.tickergreen);
			int tickerRed = context.getResources().getColor(R.color.tickerred);
			sSmallUpArrowBitmap = textAsBitmap("▲", sSmallArrowSize, tickerGreen);
			sBigUpArrowBitmap = textAsBitmap("▲", sBigArrowSize, tickerGreen);
			sSmallDownArrowBitmap = textAsBitmap("▼", sSmallArrowSize, tickerRed);
			sBigDownArrowBitmap = textAsBitmap("▼", sBigArrowSize, tickerRed);
		}
		Notification.Builder notification = new Notification.Builder(context)
			.setContentTitle(firingReasonTitle)
			.setContentText(firingReasonBody)
			.setSmallIcon(R.drawable.ic_notification)
			.setLights(0xFFFF0000, 333, 333) // Blink in red ~3 times per second.
			.setOngoing(false)
			.setAutoCancel(true);
		if(isDirectionUp(alarm)) {
			notification.setLargeIcon(sSmallUpArrowBitmap);
		} else {
			notification.setLargeIcon(sSmallDownArrowBitmap);
		}
		Intent alarmSettingsIntent = new Intent(context, AlarmSettingsActivity.class);
		alarmSettingsIntent.putExtra(AlarmSettingsActivity.alarmID, alarm.getId());
		alarmSettingsIntent.putExtra(AlarmSettingsActivity.alarmType, alarm.getClass().getSimpleName());
		alarmSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notification.setContentIntent(PendingIntent.getActivity(context, alarm.getId(), alarmSettingsIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		//notification.setPriority(Notification.PRIORITY_DEFAULT); API 16 only
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); 
		nm.cancel(alarm.hashCode());
		nm.notify(alarm.hashCode(), notification.getNotification());
	}

	public static void showStatusBarNotification(Context context, Alarm alarm) {
		String firingReasonTitle = getFiringReasonTitle(alarm);
		String firingReasonBody = getFiringReasonBody(context, alarm);
		statusBarNotifAux(context, alarm, firingReasonTitle, firingReasonBody);
	}

	public static void showFullscreenNotification(Context context, Alarm alarm) {
		String firingReasonTitle = getFiringReasonTitle(alarm);
		String firingReasonBody = getFiringReasonBody(context, alarm);
		statusBarNotifAux(context, alarm, firingReasonTitle, firingReasonBody);

		// Close dialogs and window shade, so this will display
		context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		// Setup fullscreen intent
		int alarmID = alarm.getId();
		Intent fullScreenIntent = new Intent(context, NotificationActivity.class);
		fullScreenIntent.putExtra("alarmID", alarmID);
		fullScreenIntent.putExtra("firingReason", firingReasonTitle + "\n" + firingReasonBody);
		fullScreenIntent.putExtra("canKeepMonitoring", canKeepMonitoring(alarm));
		fullScreenIntent.putExtra("isDirectionUp", isDirectionUp(alarm));
		fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		context.startActivity(fullScreenIntent);
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
		if(sAllowNoNetNotif) {
			if(sNoNetNotif == null) {
				Intent changeSettingsIntent = new Intent(context, SettingsActivity.class);
				changeSettingsIntent.setAction(Notifications.ACTION_DISABLE_NET_NOTIF);
				changeSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Intent disableIntent = new Intent(context, StorageAndControlService.class);
				disableIntent.setAction(Notifications.ACTION_DISABLE_NET_NOTIF);
				Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_no_wifi_dark);
				sNoNetNotif = new Notification.Builder(context)
						.setContentTitle(context.getString(R.string.check_connection))
						.setContentText(context.getString(R.string.alarms_not_updating))
						.setSmallIcon(R.drawable.ic_notification)
						.setLargeIcon(largeIcon)
						.setOngoing(false)
						.setAutoCancel(true)
						//.setPriority(Notification.PRIORITY_DEFAULT) API 16 only
						.setWhen(0)
						.setContentIntent(PendingIntent.getActivity(context, sNoNetNotifID, changeSettingsIntent, PendingIntent.FLAG_UPDATE_CURRENT))
						.setDeleteIntent(PendingIntent.getService(context, sNoNetNotifID, disableIntent, 0))
						.setOnlyAlertOnce(true)
						.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
						.setVibrate(NotificationKlaxon.sVibratePattern);
			}
			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(sNoNetNotifID, sNoNetNotif.getNotification());
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
			String change = changeAlarm.getDirection() == Alarm.Direction.DOWN ? "-" : "+";
			double lastChange = changeAlarm.getLastChange();
			if(changeAlarm.isPercent())
				change += Conversions.format2DecimalPlaces(lastChange) + "%";
			else
				change += Conversions.format8SignificantDigits(lastChange) + " " + pair.getExchange();
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
		nm.cancel(sNoNetNotifID);
		sAllowNoNetNotif = true;
	}

	public static void rebuildNoInternetNotification() {
		sNoNetNotif = null;
	}
}
