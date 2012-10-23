package com.chocopepper.chococam.dao;

import android.content.Context;

import com.chocopepper.chococam.util.Logger;

public class ApplicationDAO extends DefaultDAO {
	
	private static final String TAG = Logger.makeLogTag(ApplicationDAO.class);
	private static final String COUNT = "count(*)";
	private Context ctx;
	public ApplicationDAO(Context context) {
		super(context);
		ctx = context;
	}
	
}
