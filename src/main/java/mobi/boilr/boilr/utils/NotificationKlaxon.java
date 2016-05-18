package mobi.boilr.boilr.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mobi.boilr.boilr.domain.AndroidNotifier;
import mobi.boilr.boilr.views.fragments.SettingsFragment;
import mobi.boilr.libpricealarm.Alarm;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;

/**
 * Manages playing ringtone and vibrating the device.
 * Based on Android DeskClock AlarmKlaxon.
 */
public class NotificationKlaxon {
	public static final long[] sVibratePattern = new long[] { 500, 500 };
	private static MediaPlayer sMediaPlayer = null;
	private static boolean sStarted = false;
	private static final Map<Integer, Integer> sAlertToStreamType;
	static {
		Map<Integer, Integer> aux = new HashMap<>();
		aux.put(RingtoneManager.TYPE_RINGTONE, AudioManager.STREAM_RING);
		aux.put(RingtoneManager.TYPE_NOTIFICATION, AudioManager.STREAM_NOTIFICATION);
		aux.put(RingtoneManager.TYPE_ALARM, AudioManager.STREAM_ALARM);
		sAlertToStreamType = Collections.unmodifiableMap(aux);
	}

	public static void stop(final Context context) {
		if(sStarted) {
			// Stop audio playing
			if(sMediaPlayer != null) {
				sMediaPlayer.stop();
				AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				audioManager.abandonAudioFocus(null);
				sMediaPlayer.release();
				sMediaPlayer = null;
			}
			((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).cancel();
			sStarted = false;
		}
	}

	public static void start(final Context context, Alarm alarm) {
		AndroidNotifier notifier = (AndroidNotifier) alarm.getNotifier();
		// Make sure we are stop before starting
		stop(context);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String alertSound = notifier.getAlertSound();
		Uri alertSoundUri = null;
				
		if(alertSound != null)
			alertSoundUri = Uri.parse(alertSound);
		else
			alertSoundUri = Uri.parse(sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, ""));		
		
		
		if(!(Uri.EMPTY).equals(alertSoundUri)) { // Silent or None was selected
			sMediaPlayer = new MediaPlayer();
			sMediaPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.e("Error occurred while playing audio. Stopping NotificationKlaxon.");
					NotificationKlaxon.stop(context);
					return true;
				}
			});

			Integer alertType = notifier.getAlertType();
			if(alertType == null) {
				alertType = Integer.parseInt(sharedPrefs.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_TYPE, ""));
			}
			try {
				sMediaPlayer.setDataSource(context, alertSoundUri);
				startAlarm(context, sMediaPlayer, alertType);
			} catch(Exception ex) {
				Log.d("NotificationKlaxon using the fallback ringtone.");
				// The alarmNoise may be on the sd card which could be busy right
				// now. Use the fallback ringtone.
				try {
					// Must reset the media player to clear the error state.
					sMediaPlayer.reset();
					alertSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
					sMediaPlayer.setDataSource(context, alertSoundUri);
					startAlarm(context, sMediaPlayer, alertType);
				} catch(Exception ex2) {
					// At this point we just don't play anything.
					Log.e("NotificationKlaxon failed to play fallback ringtone.", ex2);
				}
			}
		}

		Boolean vibrate = notifier.isVibrate();
		if(vibrate == null)
			vibrate = sharedPrefs.getBoolean(SettingsFragment.PREF_KEY_VIBRATE_DEFAULT, true);
		if(vibrate) {
			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(sVibratePattern, 0);
		}

		sStarted = true;
	}

	public static void ringSingleNotification(final Context context) {
		Uri alertSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		RingtoneManager.getRingtone(context, alertSoundUri).play();
	}

	// Do the common stuff when starting the alarm.
	private static void startAlarm(final Context context, MediaPlayer player, int alertType)
			throws IOException {
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int streamType = sAlertToStreamType.get(alertType);
		// do not play alarms if stream volume is 0 (typically because ringer mode is silent).
		if(audioManager.getStreamVolume(streamType) != 0) {
			player.setAudioStreamType(streamType);
			player.setLooping(true);
			player.prepare();
			audioManager.requestAudioFocus(null, streamType, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			player.start();
		}
	}
}
