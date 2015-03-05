package mobi.boilr.boilr.preference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.views.fragments.AlarmPreferencesFragment;
import mobi.boilr.boilr.views.fragments.SettingsFragment;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.AttributeSet;

/**
 * RingtonePreference which adjusts to the current theme and has an app default
 * ringtone (instead of system default).
 * 
 * Copyright (C) 2013 Trinitrotoluol (licensed under CC BY-SA 3.0)
 *           (C) 2014 Martin Pfeffer (licensed under CC BY-SA 3.0)
 *           https://stackoverflow.com/questions/16589467/ringtonepreference-theme/16702655
 *           (C) 2015 David Ludovino <david.ludovino@gmail.com>, Boilr (modifications licensed under GPLv3)
 */
public class ThemableRingtonePreference extends ListPreference {

	private MediaPlayer mMediaPlayer;
	private int mClickedDialogEntryIndex, mRingtoneType = 4;
	private boolean mCurrentShowDefault, mShowDefault;
	private String mAppRingtone;
	private SharedPreferences mSharedPrefs;
	/* Pattern to match something like:
	 * content://media/internal/audio/media/38
	 */
	private Pattern p = Pattern.compile("[a-z:/]+\\d+");

	/**
	 * After the constructor call setRingtoneType(int) to fill ringtone's list.
	 */
	public ThemableRingtonePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(attrs != null) {
			Theme t = context.getTheme();
			TypedArray a = t.obtainStyledAttributes(attrs, R.styleable.ThemableRingtonePreference, 0, 0);
			try {
				mCurrentShowDefault = mShowDefault = a.getBoolean(R.styleable.ThemableRingtonePreference_showDefault, false);
			} finally {
				a.recycle();
			}
		}
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
	}

	/**
	 * Sets the value of the key. This should be one of the entries in
	 * {@link #getEntryValues()}.
	 * 
	 * @param value The value to set for the key.
	 */
	@Override
	public void setValue(String value) {
		super.setValue(value);
		setSummary(getEntry());
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);
		mMediaPlayer = new MediaPlayer();
		CharSequence[] entries = getEntries();
		CharSequence[] entryValues = getEntryValues();
		if(entries == null || entryValues == null) {
			throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
		}
		mClickedDialogEntryIndex = findIndexOfValue(getValue());
		builder.setSingleChoiceItems(entries, mClickedDialogEntryIndex, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mClickedDialogEntryIndex = which;
				String value = getEntryValues()[which].toString();
				if(value.equals(AlarmPreferencesFragment.DEFAULT))
					value = mAppRingtone;
				try {
					playTone(value);
				} catch(Exception e) {
					Log.e("Could not play ringtone. " + e);
				}
			}
		});
		builder.setPositiveButton(getContext().getString(R.string.ok), this);
		builder.setNegativeButton(getContext().getString(R.string.cancel), this);
	}

	private void playTone(String path) throws IllegalArgumentException, IllegalStateException, IOException {
		mMediaPlayer.reset();
		mMediaPlayer.setDataSource(path);
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		// mMediaPlayer.setLooping(true);
		mMediaPlayer.prepare();
		mMediaPlayer.start();
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		CharSequence[] entryValues = getEntryValues();
		if(positiveResult && mClickedDialogEntryIndex >= 0 && entryValues != null) {
			String value = entryValues[mClickedDialogEntryIndex].toString();
			if(callChangeListener(value)) {
				setValue(value);
			}
		}
		mMediaPlayer.stop();
		mMediaPlayer.release();
	}

	private int getAlertInt(String alertType) {
		return Integer.parseInt(alertType.equals(AlarmPreferencesFragment.DEFAULT) ?
				AlarmPreferencesFragment.getDefaultAlertType(mSharedPrefs, getContext()) : alertType);
	}

	public void setRingtoneType(String alertType) {
		mRingtoneType = getAlertInt(alertType);
		RingtoneManager ringtoneManager = new RingtoneManager(getContext());
		mCurrentShowDefault = mShowDefault && alertType.equals(AlarmPreferencesFragment.DEFAULT) ? true : false;
		ringtoneManager.setType(mRingtoneType);
		final Cursor ringtones = ringtoneManager.getCursor();
		List<String> entries = new ArrayList<String>();
		List<String> entryValues = new ArrayList<String>();
		if(mCurrentShowDefault) {
			mAppRingtone = mSharedPrefs.getString(
					SettingsFragment.PREF_KEY_DEFAULT_ALERT_SOUND, Conversions.getSystemRingtone(mRingtoneType, getContext()));
			entries.add(getContext().getString(R.string.default_value, Conversions.ringtoneUriToName(mAppRingtone, getContext())));
			entryValues.add(AlarmPreferencesFragment.DEFAULT);
		}
		// Silent
		entries.add(getContext().getString(R.string.silent));
		entryValues.add("");
		String value;
		int id;
		Matcher m;
		for(ringtones.moveToFirst(); !ringtones.isAfterLast(); ringtones.moveToNext()) {
			entries.add(ringtones.getString(RingtoneManager.TITLE_COLUMN_INDEX));
			value = ringtones.getString(RingtoneManager.URI_COLUMN_INDEX);
			m = p.matcher(value);
			if(!m.matches()) {
				id = ringtones.getInt(ringtones.getColumnIndex(MediaStore.MediaColumns._ID));
				value += "/" + id;
			}
			entryValues.add(value);
		}
		setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
		setEntries(entries.toArray(new CharSequence[entries.size()]));
	}

	public void setDefaultValue() {
		if(mCurrentShowDefault) {
			setValue((String) getEntryValues()[0]);
		} else {
			setValue(Conversions.getSystemRingtone(mRingtoneType, getContext()));
		}
	}
}