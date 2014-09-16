package mobi.boilr.boilr.utils;

import android.content.Context;
import android.os.PowerManager;

/**
 * Utility class to hold wake lock in app.
 * Taken from Android DeskClock.
 */
public class AlarmAlertWakeLock {

	private static PowerManager.WakeLock sCpuWakeLock;

	public static PowerManager.WakeLock createPartialWakeLock(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmAlertWakeLock");
	}

	public static void acquireCpuWakeLock(Context context) {
		if (sCpuWakeLock != null)
			return;
		sCpuWakeLock = createPartialWakeLock(context);
		sCpuWakeLock.acquire();
	}

	public static void releaseCpuLock() {
		if (sCpuWakeLock != null) {
			sCpuWakeLock.release();
			sCpuWakeLock = null;
		}
	}
}
