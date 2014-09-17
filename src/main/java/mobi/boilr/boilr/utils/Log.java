package mobi.boilr.boilr.utils;

/**
 * package-level logging flag
 * Based on Android DeskClock Log.
 */
public class Log {
	public final static String LOGTAG = "Boilr";

	/**
	 * This must be false for production. If true, turns on logging,
	 * test code, etc.
	 */
	public static final boolean LOGV = false;

	public static void d(String logMe) {
		android.util.Log.d(LOGTAG, logMe);
	}

	public static void v(String logMe) {
		android.util.Log.v(LOGTAG, logMe);
	}

	public static void i(String logMe) {
		android.util.Log.i(LOGTAG, logMe);
	}

	public static void e(String logMe) {
		android.util.Log.e(LOGTAG, logMe);
	}

	public static void e(String logMe, Exception e) {
		android.util.Log.e(LOGTAG, logMe, e);
	}

	public static void w(String logMe) {
		android.util.Log.w(LOGTAG, logMe);
	}

	public static void wtf(String logMe) {
		android.util.Log.wtf(LOGTAG, logMe);
	}
}
