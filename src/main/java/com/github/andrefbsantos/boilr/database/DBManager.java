package com.github.andrefbsantos.boilr.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.andrefbsantos.boilr.domain.AlarmWrapper;

public class DBManager {

	public static final String DATABASE_NAME = "boilr";
	public static final int VERSION = '1';
	public static final String TABLE_NAME = "alarms";
	public static final String BYTES = "bytes";
	public static final String _ID = "_id";
	private static final String MAX = "max";

	private DatabaseHelper databaseHelper;

	// public DBManager(Context context, String name, CursorFactory factory, int version)
	public DBManager(Context context) {
		databaseHelper = new DatabaseHelper(context, DBManager.DATABASE_NAME, null, DBManager.VERSION, DBManager.TABLE_NAME);
	}

	@SuppressLint("UseSparseArrays")
	public Map<Integer, AlarmWrapper> getAlarms() throws ClassNotFoundException, IOException {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		String sql = "SELECT " + _ID + "," + BYTES + " FROM " + DBManager.TABLE_NAME + ";";
		Cursor cursor = db.rawQuery(sql, null);

		Map<Integer, AlarmWrapper> alarmsMap = new HashMap<Integer, AlarmWrapper>();

		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndex(_ID));
				AlarmWrapper wrapper = (AlarmWrapper) Serializer.deserializeObject(cursor.getBlob(cursor
						.getColumnIndex(BYTES)));
				alarmsMap.put(id, wrapper);
			} while (cursor.moveToNext());
		}

		db.close();

		return alarmsMap;
	}

	public void clean() {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + DBManager.TABLE_NAME + " ;");
		db.close();
	}

	public void dropTable() {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		db.execSQL("DROP TABLE " + DBManager.TABLE_NAME + " ;");
		db.close();
	}

	public void storeAlarm(AlarmWrapper wrapper) throws IOException {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		byte[] bytes = Serializer.serializeObject(wrapper);
		ContentValues contentValues = new ContentValues();
		contentValues.put(_ID, wrapper.getAlarm().getId());
		contentValues.put(BYTES, bytes);
		// Alarm's id is primary key, therefore it shouldn't be necessary to check for duplicates.
		db.insert(DBManager.TABLE_NAME, null, contentValues);
		db.close();
	}

	public void updateAlarm(AlarmWrapper wrapper) throws IOException {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		byte[] bytes = Serializer.serializeObject(wrapper);

		ContentValues values = new ContentValues();
		values.put(_ID, wrapper.getAlarm().getId());
		values.put(BYTES, bytes);

		// Update where works like a template, (( age=? && name=? ), { 25, Andre })
		// becomes age=25 && name=Andre
		String whereClause = _ID + " = ? "; // template
		String[] whereArgs = new String[] { String.valueOf(wrapper.getAlarm().getId()) }; // values
		db.update(TABLE_NAME, values, whereClause, whereArgs);
		db.close();
	}

	public int getNextID() {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		String sql = "SELECT MAX( " + _ID + " ) as " + MAX + " FROM " + DBManager.TABLE_NAME + ";";
		Cursor cursor = db.rawQuery(sql, null);
		int id = 0;
		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			int columnIndex = cursor.getColumnIndex(MAX);
			id = cursor.getInt(columnIndex);
		}
		db.close();
		return id;
	}

	public void deleteAlarm(AlarmWrapper wrapper) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		// String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + _ID + "=" +
		// wrapper.getAlarm().getId();
		// db.rawQuery(sql, null);
		ContentValues values = new ContentValues();
		values.put(_ID, wrapper.getAlarm().getId());
		String whereClause = _ID + " = ? "; // template
		String[] whereArgs = new String[] { String.valueOf(wrapper.getAlarm().getId()) }; // values
		db.update(TABLE_NAME, values, whereClause, whereArgs);
		db.delete(TABLE_NAME, whereClause, whereArgs);
		db.close();
	}
}