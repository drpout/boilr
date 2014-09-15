package com.github.andrefbsantos.boilr.utils;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.github.andrefbsantos.boilr.domain.AlarmWrapper;
import com.github.andrefbsantos.boilr.views.fragments.SettingsFragment;

/**
 * Manages playing ringtone and vibrating the device.
 * Based on Android DeskClock AlarmKlaxon.
 */
public class NotificationKlaxon {
	private static final long[] sVibratePattern = new long[] { 500, 500 };
	private static MediaPlayer sMediaPlayer = null;
	private static boolean sStarted = false;

	public static void stop(Context context) {
		Log.v("NotificationKlaxon stopped.");
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

	public static void start(final Context context, AlarmWrapper alarmWrapper,
			boolean inTelephoneCall) {
		Log.v("NotificationKlaxon started.");
		// Make sure we are stop before starting
		stop(context);

		Uri alertSound;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		if(inTelephoneCall) {
			alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		} else {
			alertSound = alarmWrapper.getAlertSound();
			if(alertSound == null)
				alertSound = Uri.parse(sharedPreferences.getString(SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, ""));
		}

		sMediaPlayer = new MediaPlayer();
		sMediaPlayer.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e("Error occurred while playing audio. Stopping NotificationKlaxon.");
				NotificationKlaxon.stop(context);
				return true;
			}
		});

		try {
			sMediaPlayer.setDataSource(context, alertSound);
			startAlarm(context, sMediaPlayer);
		} catch (Exception ex) {
			Log.v("NotificationKlaxon using the fallback ringtone.");
			// The alarmNoise may be on the sd card which could be busy right
			// now. Use the fallback ringtone.
			try {
				// Must reset the media player to clear the error state.
				sMediaPlayer.reset();
				alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
				sMediaPlayer.setDataSource(context, alertSound);
				startAlarm(context, sMediaPlayer);
			} catch (Exception ex2) {
				// At this point we just don't play anything.
				Log.e("NotificationKlaxon failed to play fallback ringtone.", ex2);
			}
		}

		Boolean vibrate = alarmWrapper.isVibrate();
		if(vibrate == null)
			vibrate = sharedPreferences.getBoolean(SettingsFragment.PREF_KEY_VIBRATE_DEFAULT, true);
		if(vibrate) {
			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(sVibratePattern, 0);
		}

		sStarted = true;
	}

	// Do the common stuff when starting the alarm.
	private static void startAlarm(Context context, MediaPlayer player) throws IOException {
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		// do not play alarms if stream volume is 0 (typically because ringer mode is silent).
		if(audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
			player.setAudioStreamType(AudioManager.STREAM_ALARM);
			player.setLooping(true);
			player.prepare();
			audioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			player.start();
		}
	}
}
