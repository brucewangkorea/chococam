package com.chocopepper.chococam;

import android.app.Application;

import com.chocopepper.chococam.dao.db.G_DBHelper;
 
public class ProjectGApplication extends Application {
	
//	private static final String TAG = Logger.makeLogTag(ProjectGApplication.class);
	
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA        
		//ACRA.init(this);		
		super.onCreate();
		G_DBHelper helper = new G_DBHelper(getApplicationContext());
		helper.getWritableDatabase().close();
	}
}
