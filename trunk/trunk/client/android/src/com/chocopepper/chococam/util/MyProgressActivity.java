package com.chocopepper.chococam.util;

import com.chocopepper.chococam.R;

import android.app.Activity;
import android.os.Bundle;

public class MyProgressActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progress);
		MyProgressDialog.setProgressActivityInstance(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		MyProgressDialog.cancelAllTasks();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		return;
		//super.onBackPressed();
	}

}
