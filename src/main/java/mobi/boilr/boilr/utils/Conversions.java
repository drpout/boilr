package mobi.boilr.boilr.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import mobi.boilr.boilr.R;
import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;

public class Conversions {
	public static final long MILIS_IN_MINUTE = 60000; // 60 * 1000
	private static final long MILIS_IN_HOUR = 3600000; // 60 * 60 * 1000
	private static final long MILIS_IN_DAY = 86400000; // 24 * 60 * 60 * 1000

	private Conversions() {
	}

	public static String formatMilis(long milis, Context context) {
		String formated;
		if(milis < MILIS_IN_MINUTE) {
			formated = context.getString(R.string.seconds_abbreviation, String.valueOf(milis / 1000));
		} else if(milis < MILIS_IN_HOUR) {
			formated = context.getString(R.string.minutes_abbreviation, String.valueOf(milis / MILIS_IN_MINUTE));
		} else if(milis < MILIS_IN_DAY) {
			formated = context.getString(R.string.hours_abbreviation, String.valueOf(milis / MILIS_IN_HOUR));
		} else {
			formated = context.getString(R.string.days_abbreviation, String.valueOf(milis / MILIS_IN_DAY));
		}
		return formated;
	}
	
	private static final DecimalFormatSymbols symbols;
	static {
		symbols = new DecimalFormatSymbols(Locale.ENGLISH);
		symbols.setNaN("—");
	}

	private static final DecimalFormat twoPlacesFormatter;
	static {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		twoPlacesFormatter = (DecimalFormat) nf;
		twoPlacesFormatter.applyPattern("#.##");
		twoPlacesFormatter.setDecimalFormatSymbols(symbols);
	}

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

	private static final DecimalFormat maxPlacesFormater;
	static {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		maxPlacesFormater = (DecimalFormat) nf;
		maxPlacesFormater.setMaximumFractionDigits(340);
		maxPlacesFormater.setGroupingUsed(false);
		maxPlacesFormater.setDecimalFormatSymbols(symbols);
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

	private static final DecimalFormat eightSignificantFormatter;
	static {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		eightSignificantFormatter = (DecimalFormat) nf;
		eightSignificantFormatter.applyPattern("@#######");
		eightSignificantFormatter.setDecimalFormatSymbols(symbols);
	}

	private static final DecimalFormat eightDecimalPlacesFormatter;
	static {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		eightDecimalPlacesFormatter = (DecimalFormat) nf;
		eightDecimalPlacesFormatter.applyPattern("#.########");
		eightDecimalPlacesFormatter.setDecimalFormatSymbols(symbols);
	}
	
	/**
	 * Converts a double to a String with:
	 * - a minimum of 1 significant digit and a maximum of 8 for
	 * numbers bigger than 1;
	 * - a maximum of 8 decimal places for numbers smaller than 1
	 *
	 * @param d double to be converted
	 * @return String with up to 8 significant digits
	 */
	public static String format8SignificantDigits(double d) {
		return d < 1 ? eightDecimalPlacesFormatter.format(d) : eightSignificantFormatter.format(d);
	}

	private static final double MINUTES_IN_DAY = 1440; // 60*24

	public static String buildMinToDaysSummary(String minutesString, Context context) {
		int min = Integer.parseInt(minutesString);
		String days = format2DecimalPlaces((min / MINUTES_IN_DAY));
		return context.getString(R.string.minutes_abbreviation, String.valueOf(min)) +
				" (" + context.getString(R.string.days_abbreviation, days) + ")";
	}

	public static String buildMinToHoursSummary(String minutesString, Context context) {
		int min = Integer.parseInt(minutesString);
		String hours = format2DecimalPlaces((min / 60.0));
		return context.getString(R.string.minutes_abbreviation, String.valueOf(min)) +
				" (" + context.getString(R.string.hours_abbreviation, hours) + ")";
	}

	public static String ringtoneUriToName(String stringUri, Context context) {
		if(stringUri.equals(""))
			return context.getString(R.string.silent);
		Uri uri = Uri.parse(stringUri);
		// Context context = activity.getApplicationContext();
		Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
		return ringtone != null ? ringtone.getTitle(context) : context.getString(R.string.unknown_ringtone);
	}

	/**
	 * Based on Stack Overflow answer by Beatlej
	 * https://stackoverflow.com/a/28378096
	 */
	public static String getSystemRingtone(int ringtoneType, Context context) {
		Uri mediaUri = RingtoneManager.getDefaultUri(ringtoneType);
		if(mediaUri.getAuthority().equals(Settings.AUTHORITY)) {
			Cursor c = null;
			try {
				c = context.getContentResolver().query(mediaUri, new String[] { Settings.NameValueTable.VALUE }, null, null, null);
				if(c != null && c.moveToFirst()) {
					String val = c.getString(0);
					mediaUri = Uri.parse(val);
				}
			} catch(Exception e) {
				Log.e("Error getting system ringtone." + e);
			} finally {
				c.close();
			}
		}
		return mediaUri.toString();
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
			while (tval >= 1000.0) {
				tval /= 1000.0;
				order += 3;
			}
			while (tval < 1.0) {
				tval *= 1000.0;
				order -= 3;
			}
		}
		return format2DecimalPlaces(tval) + prefixes.get(order);
	}
}
