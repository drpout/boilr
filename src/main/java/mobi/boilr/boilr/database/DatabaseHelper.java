/**
 *
 */
package mobi.boilr.boilr.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	private final String tableName;

	public DatabaseHelper(Context context, String name, CursorFactory factory, int version, String tableName) {
		super(context, name, factory, version);
		this.tableName = tableName;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createDB = "CREATE TABLE IF NOT EXISTS " + tableName + " ( " + DBManager._ID + " INTEGER PRIMARY KEY, " + DBManager.BYTES + " BLOB) ;";
		db.execSQL(createDB);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int olderVersion, int newVersion) {
		while(olderVersion < newVersion) {
			switch(olderVersion) {
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
			case 4:
				break;
			case 5:
				break;
			}
			olderVersion++;
		}
	}
}