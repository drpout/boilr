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
	private String tablename;

	public DatabaseHelper(Context context, String name, CursorFactory factory, int version,
			String tableName) {
		super(context, name, factory, version);
		tablename = tableName;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createDB = "CREATE TABLE IF NOT EXISTS " + tablename + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, bytes BLOB) ;";
		db.execSQL(createDB);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
}