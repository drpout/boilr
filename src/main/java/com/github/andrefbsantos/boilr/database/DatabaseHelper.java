/**
 *
 */
package com.github.andrefbsantos.boilr.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author andre
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	private String tableName;

	public DatabaseHelper(Context context, String name, CursorFactory factory, int version,
			String tableName) {
		super(context, name, factory, version);
		this.tableName = tableName;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createDB = "CREATE TABLE IF NOT EXISTS " + tableName + " ( " + DBManager._ID + " INTEGER PRIMARY KEY, " + DBManager.BYTES + " BLOB) ;";
		db.execSQL(createDB);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
}