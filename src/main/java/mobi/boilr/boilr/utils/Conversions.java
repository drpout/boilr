package mobi.boilr.boilr.utils;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

public class Conversions {
	private static long MILIS_IN_MINUTE = 60000; // 60 * 1000
	private static long MILIS_IN_HOUR = 3600000; // 60 * 60 * 1000
	private static long MILIS_IN_DAY = 86400000; // 24 * 60 * 60 * 1000

	public static String formatMilis(long milis) {
		String formated;
		if(milis < MILIS_IN_MINUTE) {
			// seconds
			formated = milis / 1000 + "s";
		} else if(milis < MILIS_IN_HOUR) {
			// minutes
			formated = (milis / MILIS_IN_MINUTE) + "m";
		} else if(milis < MILIS_IN_DAY) {
			// hours
			formated = (milis / MILIS_IN_HOUR) + "h";
		} else {
			// days
			formated = (milis / MILIS_IN_DAY) + "d";
		}
		return formated;
	}

	private static DecimalFormat twoPlacesFormatter = new DecimalFormat("#.##");

	/**
	 * Converts a double to a String using up to 2 decimal places
	 * but only when they are needed.
	 *
	 * @param d double to be converted
	 * @return String with up to 2 decimal places
	 */
	public static String format2DecimalPlaces(double d) {
		return twoPlacesFormatter.format(d);
	}

	private static DecimalFormat maxPlacesFormater = new DecimalFormat();
	static {
		maxPlacesFormater.setMaximumFractionDigits(340);
		maxPlacesFormater.setGroupingUsed(false);
	}

	/**
	 * Converts a double to a String using up to 340 (the maximum)
	 * decimal places but only when they are needed.
	 *
	 * Based on answer http://stackoverflow.com/a/25308216 by JBE
	 * on Stack Overflow
	 * 
	 * @param d double to be converted
	 * @return String with up to 340 decimal places
	 */
	public static String formatMaxDecimalPlaces(double d) {
		return maxPlacesFormater.format(d);
	}

	private static final double MINUTES_IN_DAY = 1440; // 60*24

	public static String buildMinToDaysSummary(String minutesString) {
		int min = Integer.parseInt(minutesString);
		double days = min / MINUTES_IN_DAY;
		String result = minutesString + " min (" + format2DecimalPlaces(days);
		if(days == 1.0) {
			result += " day)";
		} else {
			result += " days)";
		}
		return result;
	}

	public static String ringtoneUriToName(String stringUri, Activity activity) {
		Uri uri = Uri.parse(stringUri);
		Context context = activity.getApplicationContext();
		Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
		return ringtone.getTitle(context);
	}

	private static final Map<Integer, String> prefixes;
	static {
		Map<Integer, String> tempPrefixes = new HashMap<Integer, String>();
		tempPrefixes.put(12, "T");
		tempPrefixes.put(9, "G");
		tempPrefixes.put(6, "M");
		tempPrefixes.put(3, "k");
		tempPrefixes.put(0, "");
		tempPrefixes.put(-3, "m");
		tempPrefixes.put(-6, "µ");
		tempPrefixes.put(-9, "n");
		tempPrefixes.put(-12, "p");
		prefixes = Collections.unmodifiableMap(tempPrefixes);
	}

	/**
	 * Converts a double to a String in Engineering Notation
	 * Based on Stack Overflow answer by corsiKa at http://stackoverflow.com/a/5036540
	 *
	 * @param value double to be converted
	 * @return String with double formated in Engineering Notation
	 */
	public static String formatEngNotation(double value) {
		double tval = value;
		int order = 0;
		if(tval != 0.0 && tval != Double.POSITIVE_INFINITY && tval != Double.NEGATIVE_INFINITY && tval != Double.NaN) {
			while(tval > 1000.0) {
				tval /= 1000.0;
				order += 3;
			}
			while(tval < 1.0) {
				tval *= 1000.0;
				order -= 3;
			}
		}
		return format2DecimalPlaces(tval) + prefixes.get(order);
	}
}
