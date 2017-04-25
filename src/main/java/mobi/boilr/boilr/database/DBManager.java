package mobi.boilr.boilr.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import mobi.boilr.boilr.domain.AndroidNotifier;
import mobi.boilr.libpricealarm.Alarm;

public class DBManager {

	public static final String DATABASE_NAME = "boilrDB";
	public static final int VERSION = 1;
	public static final String TABLE_NAME = "alarms";
	public static final String BYTES = "bytes";
	public static final String _ID = "_id";
	private static final String MAX = "max";
	private final DatabaseHelper mDatabaseHelper;
	private final SQLiteDatabase db;
	private final Context mContext;

	public DBManager(Context context) {
		mContext = context;
		mDatabaseHelper = new DatabaseHelper(context, DBManager.DATABASE_NAME, null, DBManager.VERSION, DBManager.TABLE_NAME);
		db = mDatabaseHelper.getWritableDatabase();
	}

	@SuppressLint("UseSparseArrays")
	public Map<Integer, Alarm> getAlarms() throws ClassNotFoundException, IOException {
		String sql = "SELECT " + _ID + "," + BYTES + " FROM " + DBManager.TABLE_NAME + ";";
		Cursor cursor = db.rawQuery(sql, null);
		Map<Integer, Alarm> alarmsMap = new HashMap<>();
		if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndex(_ID));
				Alarm alarm = (Alarm) Serializer.deserializeObject(cursor.getBlob(cursor
						.getColumnIndex(BYTES)));
				((AndroidNotifier) alarm.getNotifier()).setContext(mContext);
				alarmsMap.put(id, alarm);
			} while(cursor.moveToNext());
		}
		return alarmsMap;
	}

	public void close() {
		mDatabaseHelper.close();
	}

	public void clean() {
		db.execSQL("DELETE FROM " + DBManager.TABLE_NAME + " ;");
	}

	public void dropTable() {
		db.execSQL("DROP TABLE " + DBManager.TABLE_NAME + " ;");
	}

	public void storeAlarm(Alarm alarm) throws IOException {
		byte[] bytes = Serializer.serializeObject(alarm);
		ContentValues contentValues = new ContentValues();
		contentValues.put(_ID, alarm.getId());
		contentValues.put(BYTES, bytes);
		// Alarm's id is primary key, therefore it shouldn't be necessary to check for duplicates.
		db.insert(DBManager.TABLE_NAME, null, contentValues);
	}

	public void updateAlarm(Alarm alarm) throws IOException {
		byte[] bytes = Serializer.serializeObject(alarm);
		ContentValues values = new ContentValues();
		values.put(_ID, alarm.getId());
		values.put(BYTES, bytes);
		// Update here works like a template, (( age=? && name=? ), { 25, Andre })
		// becomes age=25 && name=Andre
		String whereClause = _ID + " = ? "; // template
		String[] whereArgs = new String[] { String.valueOf(alarm.getId()) }; // values
		db.update(TABLE_NAME, values, whereClause, whereArgs);
	}

	public int getMaxID() {
		String sql = "SELECT MAX( " + _ID + " ) as " + MAX + " FROM " + DBManager.TABLE_NAME + ";";
		Cursor cursor = db.rawQuery(sql, null);
		int id = 0;
		if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			int columnIndex = cursor.getColumnIndex(MAX);
			id = cursor.getInt(columnIndex);
		}
		return id;
	}

	public void deleteAlarm(Alarm alarm) {
		ContentValues values = new ContentValues();
		values.put(_ID, alarm.getId());
		String whereClause = _ID + " = ? "; // template
		String[] whereArgs = new String[] { String.valueOf(alarm.getId()) }; // values
		db.delete(TABLE_NAME, whereClause, whereArgs);
	}
}