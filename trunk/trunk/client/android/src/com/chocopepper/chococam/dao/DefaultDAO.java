package com.chocopepper.chococam.dao;

import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.chocopepper.chococam.dao.db.G_DBHelper;

public class DefaultDAO {
	
	private G_DBHelper gDBHelper;

	private SQLiteDatabase readableDB;
	
	private SQLiteDatabase writableDB;

	private static ReentrantLock lockDB = new ReentrantLock();
	
	public DefaultDAO(Context context) {
		gDBHelper = new G_DBHelper(context);
	}
	
	protected SQLiteDatabase getWritableDatabase() {
		lockDB.lock();
		if (writableDB == null)
			writableDB = gDBHelper.getWritableDatabase(); 
		return writableDB; 
	}
	
	protected SQLiteDatabase getReadableDatabase() {
		lockDB.lock();
		if (readableDB == null)
			readableDB = gDBHelper.getReadableDatabase();
		return readableDB;
	}
	
	public void unlock() {
	    lockDB.unlock();
	}
	
	public void close() {
		
		if (readableDB != null)
			readableDB.close();
		if (writableDB != null)
			writableDB.close();
		readableDB = null;
		writableDB = null;
		gDBHelper.close();
	}
}
