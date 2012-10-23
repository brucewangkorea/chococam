package com.chocopepper.chococam.dao.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chocopepper.chococam.model.AppInfo;

/**
 * Project G 의 데이터베이스 정의 객체
 * @author KHAN0405
 *
 */
public class G_DBHelper extends SQLiteOpenHelper {
	
	private static final String CREATE = " CREATE TABLE ";
	private static final String PK_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT ";
	private static final String TEXT = " TEXT ";
	private static final String INTEGER = " INTEGER ";
	private static final String BLOB = " BLOB ";
	private static final String UNIQUE = "UNIQUE";
	@SuppressWarnings("unused")
	private static final String REAL = " REAL ";
	
	public G_DBHelper(Context context) {
		super(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		StringBuilder sql;

		
		// AppInfo Table
		sql = new StringBuilder();
		sql.append(CREATE).append(AppInfo.TABLE_NAME).append(" (");
		sql.append(AppInfo.ID).append(PK_AUTOINCREMENT).append(", ");
		sql.append(AppInfo.NAME).append(TEXT).append(", ");
		sql.append(AppInfo.PACKAGENAME).append(TEXT).append(", ");
		sql.append(AppInfo.RUNCOUNT).append(TEXT).append(", ");
		sql.append(AppInfo.ICON).append(BLOB).append(", ");
		sql.append(AppInfo.LASTRUNNINGTIME).append(TEXT).append(");");
		
		db.execSQL(sql.toString());


	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion <  newVersion) {
			db.execSQL(" DROP TABLE IF EXISTS " + AppInfo.TABLE_NAME);
			onCreate(db);
		}	
	}
}
