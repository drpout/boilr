package com.github.andrefbsantos.boilr.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.andrefbsantos.boilr.notification.DummyNotify;
import com.github.andrefbsantos.libdynticker.bitstamp.BitstampExchange;
import com.github.andrefbsantos.libdynticker.core.Pair;
import com.github.andrefbsantos.libpricealarm.Alarm;
import com.github.andrefbsantos.libpricealarm.PriceHitAlarm;
import com.github.andrefbsantos.libpricealarm.UpperBoundSmallerThanLowerBoundException;

public class DBManager {

	// public SQLiteDatabase DB;
	// public static Context currentContext;

	public String path;
	private SQLiteDatabase db;
	public static final String name = "boilr";
	public static final int version = '1';
	public static final String tableName = "alarms";

	// public DBManager(Context context, String name, CursorFactory factory, int version)
	public DBManager(Context context) throws UpperBoundSmallerThanLowerBoundException, IOException {
		db = (new DatabaseHelper(context, name, null, version, tableName)).getWritableDatabase();
		populateDB();
	}

	// Only for development proposes
	private void populateDB() {
		List<Alarm> alarms = new ArrayList<Alarm>();
		try {
			Alarm alarm = new PriceHitAlarm(1, new BitstampExchange(10000000), new Pair("BTC", "USD"), 100000, new DummyNotify(), 500, 400);
			alarms.add(alarm);
			alarm = new PriceHitAlarm(2, new BitstampExchange(10000000), new Pair("BTC", "USD"), 100000, new DummyNotify(), 500, 400);
			alarms.add(alarm);

			// alarms.add(new PriceHitAlarm(2, new BTCChinaExchange(10000), new Pair("BTC", "USD"),
			// new Timer(), 1000000, new DummyNotify(), 600, 580));
			// alarms.add(new PriceHitAlarm(3, new HuobiExchange(10000), new Pair("BTC", "CNY"), new
			// Timer(), 1000000, new DummyNotify(), 600, 580));
		} catch (UpperBoundSmallerThanLowerBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Alarm alarm : alarms) {
			byte[] bytes;
			try {
				bytes = Serializer.serializeObject(alarm);
				ContentValues contentValues = new ContentValues();
				contentValues.put("bytes", bytes);
				db.insert(DBManager.tableName, null, contentValues);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public List<Alarm> getAlarms() throws ClassNotFoundException, IOException {
		// Retrieve alarms from DB
		Cursor cursor = db.rawQuery("SELECT _id, bytes FROM " + DBManager.tableName + ";", null);
		List<Alarm> alarms = new ArrayList<Alarm>();
		if (cursor.moveToFirst()) {
			do {
				Alarm alarm = (Alarm) Serializer.deserializeObject(cursor.getBlob(cursor
						.getColumnIndex("bytes")));
				// TODO get Exchange from map
				// alarm.setExchange(exchangeMap.get(alarm.getExchangeCode());)
				alarms.add(alarm);
			} while (cursor.moveToNext());
		}
		return alarms;
	}

	// public void storeObject(Object object) throws UpperBoundSmallerThanLowerBoundException,
	// IOException {
	// // TODO
	// }

	public void clean() {
		db.execSQL("DELETE FROM " + DBManager.tableName + " ;");
	}
}